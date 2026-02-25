(function () {
    'use strict';

    function createClient(editor, exerciseId, workspaceUri, wsUrlOverride) {
        const monaco = window.monaco;
        if (!monaco || !editor) {
            return null;
        }
        const perf = window.performance;
        function perfNow() {
            return perf && typeof perf.now === 'function' ? perf.now() : Date.now();
        }
        function perfMark(name) {
            if (perf && typeof perf.mark === 'function') {
                perf.mark(name);
            }
        }
        function perfMeasure(name, startMark, endMark) {
            if (perf && typeof perf.measure === 'function') {
                try {
                    perf.measure(name, startMark, endMark);
                } catch (ignored) {
                }
            }
        }
        function logPerf(eventName, details) {
            if (!Array.isArray(window.__lspPerfEvents)) {
                window.__lspPerfEvents = [];
            }
            window.__lspPerfEvents.push({
                event: eventName,
                ts: Date.now(),
                details: details || {}
            });
            if (window.console && typeof window.console.debug === 'function') {
                window.console.debug('[LSP Perf]', eventName, details || {});
            }
        }

        const lspSessionId = String(Date.now()) + '-' + Math.floor(Math.random() * 1000000);
        const normalizedWorkspaceUri = workspaceUri && workspaceUri.startsWith('file://')
            ? workspaceUri.replace(/\/+$/, '')
            : 'file:///tmp/workspaces/session-' + lspSessionId;
        const uri = monaco.Uri.parse(normalizedWorkspaceUri + '/src/Exercise' + exerciseId + '.java');
        const oldModel = editor.getModel();
        const model = monaco.editor.createModel(oldModel ? oldModel.getValue() : '', 'java', uri);
        if (oldModel) {
            oldModel.dispose();
        }
        editor.setModel(model);

        const state = {
            socket: null,
            isReady: false,
            lspState: 'idle',
            requestId: 1,
            pending: new Map(),
            model: model,
            version: 1,
            changeTimer: null,
            hasUnsyncedChanges: false,
            readyResolve: null,
            readyPromise: null,
            completionRequestInFlight: null,
            completionRequestsSent: 0,
            providerDisposables: [],
            metrics: {
                wsConnectStartMs: 0,
                wsOpenMs: 0,
                initializeStartMs: 0,
                initializeEndMs: 0,
                didOpenSentMs: 0,
                firstDiagnosticsMs: 0,
                firstCompletionLogged: false
            }
        };
        state.readyPromise = new Promise(function (resolve) {
            state.readyResolve = resolve;
        });

        function getIndicatorElement() {
            return document.getElementById('lsp-status-indicator');
        }

        function updateIndicatorElement(nextState) {
            const indicator = getIndicatorElement();
            if (!indicator) {
                return;
            }
            const textElement = indicator.querySelector('.lsp-status__text');
            const textByState = {
                connecting: 'LSP verbindet...',
                ready: 'LSP bereit',
                degraded: 'LSP eingeschränkt',
                failed: 'LSP nicht verfügbar'
            };
            indicator.className = 'lsp-status lsp-status--' + nextState;
            indicator.setAttribute('data-state', nextState);
            if (textElement) {
                textElement.textContent = textByState[nextState] || 'LSP';
            }
        }

        function setLspState(nextState, reason) {
            if (state.lspState === nextState) {
                return;
            }
            state.lspState = nextState;
            updateIndicatorElement(nextState);
            logPerf('state-change', {
                sessionId: lspSessionId,
                state: nextState,
                reason: reason || null
            });
            if (!Array.isArray(window.__lspStateEvents)) {
                window.__lspStateEvents = [];
            }
            window.__lspStateEvents.push({
                ts: Date.now(),
                state: nextState,
                reason: reason || null
            });
        }

        function send(payload) {
            if (!state.socket || state.socket.readyState !== WebSocket.OPEN) {
                return;
            }
            state.socket.send(JSON.stringify(payload));
        }

        function sendRequest(method, params, timeoutMs) {
            if (!state.socket || state.socket.readyState !== WebSocket.OPEN) {
                return Promise.resolve(null);
            }

            const id = state.requestId++;
            const payload = {
                jsonrpc: '2.0',
                id: id,
                method: method,
                params: params
            };

            return new Promise(function (resolve, reject) {
                state.pending.set(id, { resolve: resolve, reject: reject });
                send(payload);
                const effectiveTimeoutMs = typeof timeoutMs === 'number' ? timeoutMs : 4000;
                setTimeout(function () {
                    if (state.pending.has(id)) {
                        state.pending.delete(id);
                        resolve(null);
                    }
                }, effectiveTimeoutMs);
            });
        }

        function delay(ms) {
            return new Promise(function (resolve) {
                setTimeout(resolve, ms);
            });
        }

        function isSocketOpen() {
            return !!state.socket && state.socket.readyState === WebSocket.OPEN;
        }

        function sendRequestWithRetry(method, params, maxRetries, initialBackoffMs, timeoutMs) {
            const retries = typeof maxRetries === 'number' ? maxRetries : 1;
            const backoffStartMs = typeof initialBackoffMs === 'number' ? initialBackoffMs : 120;
            let attempt = 0;

            function tryOnce() {
                return sendRequest(method, params, timeoutMs).then(function (result) {
                    if (result !== null) {
                        if (state.lspState === 'degraded' && state.isReady) {
                            setLspState('ready', method + '-recovered');
                        }
                        return result;
                    }
                    if (attempt >= retries || !isSocketOpen()) {
                        if (state.isReady) {
                            setLspState('degraded', method + '-timeout');
                        } else {
                            setLspState('failed', method + '-timeout');
                        }
                        throw new Error(method + '-timeout');
                    }
                    if (state.isReady) {
                        setLspState('degraded', method + '-retry-' + (attempt + 1));
                    }
                    const waitMs = backoffStartMs * Math.pow(2, attempt);
                    attempt += 1;
                    return delay(waitMs).then(tryOnce);
                }).catch(function (error) {
                    if (attempt >= retries || !isSocketOpen()) {
                        if (state.isReady) {
                            setLspState('degraded', method + '-error');
                        } else {
                            setLspState('failed', method + '-error');
                        }
                        throw error;
                    }
                    if (state.isReady) {
                        setLspState('degraded', method + '-retry-' + (attempt + 1));
                    }
                    const waitMs = backoffStartMs * Math.pow(2, attempt);
                    attempt += 1;
                    return delay(waitMs).then(tryOnce);
                });
            }

            return tryOnce();
        }

        function sendNotification(method, params) {
            send({
                jsonrpc: '2.0',
                method: method,
                params: params
            });
        }

        function flushDidChange() {
            state.changeTimer = null;
            if (!state.isReady || !state.hasUnsyncedChanges) {
                return;
            }

            state.version += 1;
            state.hasUnsyncedChanges = false;
            sendNotification('textDocument/didChange', {
                textDocument: {
                    uri: state.model.uri.toString(),
                    version: state.version
                },
                contentChanges: [{ text: state.model.getValue() }]
            });
        }

        function ensureLatestDocumentSent() {
            if (!state.isReady) {
                return;
            }

            if (state.changeTimer) {
                clearTimeout(state.changeTimer);
                state.changeTimer = null;
            }
            if (state.hasUnsyncedChanges) {
                flushDidChange();
            }
        }

        function scheduleDidChange(delayMs) {
            clearTimeout(state.changeTimer);
            state.changeTimer = setTimeout(flushDidChange, delayMs);
        }

        function waitUntilReady(timeoutMs) {
            if (state.isReady) {
                return Promise.resolve(true);
            }
            return Promise.race([
                state.readyPromise.then(function () { return true; }),
                new Promise(function (resolve) {
                    setTimeout(function () {
                        resolve(state.isReady);
                    }, timeoutMs);
                })
            ]);
        }

        function connect() {
            const protocol = window.location.protocol === 'https:' ? 'wss' : 'ws';
            const wsUrl = wsUrlOverride || (protocol + '://' + window.location.host + '/api/lsp/ws');
            setLspState('connecting', 'socket-connect-start');
            state.metrics.wsConnectStartMs = perfNow();
            perfMark('lsp-' + lspSessionId + '-ws-connect-start');
            state.socket = new WebSocket(wsUrl);

            state.socket.addEventListener('open', function () {
                state.metrics.wsOpenMs = perfNow();
                perfMark('lsp-' + lspSessionId + '-ws-open');
                perfMeasure('lsp-' + lspSessionId + '-ws-open-latency', 'lsp-' + lspSessionId + '-ws-connect-start', 'lsp-' + lspSessionId + '-ws-open');
                logPerf('ws-open', {
                    sessionId: lspSessionId,
                    latencyMs: Math.round(state.metrics.wsOpenMs - state.metrics.wsConnectStartMs)
                });

                state.metrics.initializeStartMs = perfNow();
                perfMark('lsp-' + lspSessionId + '-initialize-start');
                sendRequestWithRetry('initialize', {
                    processId: null,
                    rootUri: normalizedWorkspaceUri,
                    capabilities: {
                        textDocument: {
                            completion: { completionItem: { snippetSupport: true } },
                            publishDiagnostics: {}
                        }
                    },
                    workspaceFolders: [
                        {
                            uri: normalizedWorkspaceUri,
                            name: 'proginator-workspace'
                        }
                    ],
                    clientInfo: {
                        name: 'proginator-monaco',
                        version: '1.0.0'
                    }
                }, 1, 250, 30000).then(function (initResult) {
                    if (!initResult) {
                        setLspState('failed', 'initialize-empty');
                        return;
                    }
                    state.metrics.initializeEndMs = perfNow();
                    perfMark('lsp-' + lspSessionId + '-initialize-end');
                    perfMeasure('lsp-' + lspSessionId + '-initialize-latency', 'lsp-' + lspSessionId + '-initialize-start', 'lsp-' + lspSessionId + '-initialize-end');
                    logPerf('initialize-done', {
                        sessionId: lspSessionId,
                        latencyMs: Math.round(state.metrics.initializeEndMs - state.metrics.initializeStartMs)
                    });

                    sendNotification('initialized', {});
                    state.metrics.didOpenSentMs = perfNow();
                    perfMark('lsp-' + lspSessionId + '-did-open-sent');
                    logPerf('did-open-sent', {
                        sessionId: lspSessionId,
                        sinceWsConnectMs: Math.round(state.metrics.didOpenSentMs - state.metrics.wsConnectStartMs)
                    });
                    sendNotification('textDocument/didOpen', {
                        textDocument: {
                            uri: state.model.uri.toString(),
                            languageId: 'java',
                            version: state.version,
                            text: state.model.getValue()
                        }
                    });
                    state.isReady = true;
                    setLspState('ready', 'initialize-complete');
                    if (state.readyResolve) {
                        state.readyResolve(true);
                        state.readyResolve = null;
                    }
                    if (state.hasUnsyncedChanges) {
                        scheduleDidChange(0);
                    }
                }).catch(function () {
                    state.isReady = false;
                    setLspState('failed', 'initialize-error');
                });
            });

            state.socket.addEventListener('message', function (event) {
                let payload;
                try {
                    payload = JSON.parse(event.data);
                } catch (err) {
                    return;
                }

                if (payload.id !== undefined && state.pending.has(payload.id)) {
                    const resolver = state.pending.get(payload.id);
                    state.pending.delete(payload.id);
                    if (payload.error) {
                        resolver.reject(payload.error);
                    } else {
                        resolver.resolve(payload.result);
                    }
                    return;
                }

                if (payload.method === 'textDocument/publishDiagnostics' && payload.params) {
                    applyDiagnostics(payload.params);
                }
            });

            state.socket.addEventListener('close', function () {
                state.isReady = false;
                if (state.lspState === 'ready' || state.lspState === 'degraded') {
                    setLspState('degraded', 'socket-closed');
                } else {
                    setLspState('failed', 'socket-closed-before-ready');
                }
            });

            state.socket.addEventListener('error', function () {
                state.isReady = false;
                if (state.lspState === 'ready' || state.lspState === 'degraded') {
                    setLspState('degraded', 'socket-error');
                } else {
                    setLspState('failed', 'socket-error-before-ready');
                }
            });
        }

        function applyDiagnostics(params) {
            if (!params || params.uri !== state.model.uri.toString() || !Array.isArray(params.diagnostics)) {
                return;
            }

            if (state.metrics.firstDiagnosticsMs === 0) {
                state.metrics.firstDiagnosticsMs = perfNow();
                perfMark('lsp-' + lspSessionId + '-first-diagnostics');
                perfMeasure(
                    'lsp-' + lspSessionId + '-first-diagnostics-latency',
                    'lsp-' + lspSessionId + '-ws-connect-start',
                    'lsp-' + lspSessionId + '-first-diagnostics'
                );
                logPerf('first-diagnostics', {
                    sessionId: lspSessionId,
                    sinceWsConnectMs: Math.round(state.metrics.firstDiagnosticsMs - state.metrics.wsConnectStartMs)
                });
            }

            const markers = params.diagnostics.map(function (diag) {
                const start = diag.range && diag.range.start ? diag.range.start : { line: 0, character: 0 };
                const end = diag.range && diag.range.end ? diag.range.end : start;
                return {
                    startLineNumber: start.line + 1,
                    startColumn: start.character + 1,
                    endLineNumber: end.line + 1,
                    endColumn: end.character + 1,
                    message: diag.message || 'Diagnostic',
                    severity: mapSeverity(monaco, diag.severity)
                };
            });

            monaco.editor.setModelMarkers(state.model, 'jdtls', markers);
        }

        function mapSeverity(monaco, severity) {
            switch (severity) {
                case 1:
                    return monaco.MarkerSeverity.Error;
                case 2:
                    return monaco.MarkerSeverity.Warning;
                case 3:
                    return monaco.MarkerSeverity.Info;
                default:
                    return monaco.MarkerSeverity.Hint;
            }
        }

        function registerProviders() {
            const completionDisposable = monaco.languages.registerCompletionItemProvider('java', {
                triggerCharacters: ['.', '(', ','],
                provideCompletionItems: function (currentModel, position, context) {
                    if (currentModel.uri.toString() !== state.model.uri.toString()) {
                        return Promise.resolve({ suggestions: [] });
                    }

                    return waitUntilReady(1500).then(function (ready) {
                        if (!ready) {
                            return { suggestions: [] };
                        }
                        ensureLatestDocumentSent();
                        const completionStartMs = perfNow();
                        if (state.completionRequestInFlight) {
                            return state.completionRequestInFlight;
                        }

                        state.completionRequestsSent += 1;
                        state.completionRequestInFlight = sendRequestWithRetry('textDocument/completion', {
                            textDocument: { uri: currentModel.uri.toString() },
                            position: {
                                line: position.lineNumber - 1,
                                character: position.column - 1
                            },
                            context: {
                                triggerKind: context && context.triggerKind ? context.triggerKind : 1,
                                triggerCharacter: context && context.triggerCharacter ? context.triggerCharacter : undefined
                            }
                        }, 1, 120, 4000).then(function (result) {
                            const items = normalizeCompletionItems(result);
                            if (!state.metrics.firstCompletionLogged) {
                                state.metrics.firstCompletionLogged = true;
                                perfMark('lsp-' + lspSessionId + '-first-completion-end');
                                perfMeasure(
                                    'lsp-' + lspSessionId + '-first-completion-latency',
                                    'lsp-' + lspSessionId + '-ws-connect-start',
                                    'lsp-' + lspSessionId + '-first-completion-end'
                                );
                                logPerf('first-completion-roundtrip', {
                                    sessionId: lspSessionId,
                                    roundtripMs: Math.round(perfNow() - completionStartMs),
                                    sinceWsConnectMs: Math.round(perfNow() - state.metrics.wsConnectStartMs)
                                });
                            }
                            return {
                                suggestions: items.map(function (item) {
                                    return {
                                        label: item.label || '',
                                        kind: mapCompletionKind(monaco, item.kind),
                                        detail: item.detail || '',
                                        documentation: item.documentation && item.documentation.value ? item.documentation.value : item.documentation,
                                        insertText: item.insertText || item.label || '',
                                        sortText: item.sortText || item.label || ''
                                    };
                                })
                            };
                        }).catch(function () {
                            return { suggestions: [] };
                        }).finally(function () {
                            state.completionRequestInFlight = null;
                        });
                        return state.completionRequestInFlight;
                    });
                }
            });

            const hoverDisposable = monaco.languages.registerHoverProvider('java', {
                provideHover: function (currentModel, position) {
                    if (currentModel.uri.toString() !== state.model.uri.toString()) {
                        return Promise.resolve(null);
                    }

                    return waitUntilReady(1500).then(function (ready) {
                        if (!ready) {
                            return null;
                        }
                        ensureLatestDocumentSent();

                        return sendRequestWithRetry('textDocument/hover', {
                            textDocument: { uri: currentModel.uri.toString() },
                            position: {
                                line: position.lineNumber - 1,
                                character: position.column - 1
                            }
                        }, 1, 120, 4000).then(function (result) {
                            if (!result || !result.contents) {
                                return null;
                            }

                            const value = toMarkdown(result.contents);
                            if (!value) {
                                return null;
                            }

                            return { contents: [{ value: value }] };
                        }).catch(function () {
                            return null;
                        });
                    });
                }
            });

            const signatureDisposable = monaco.languages.registerSignatureHelpProvider('java', {
                signatureHelpTriggerCharacters: ['(', ','],
                provideSignatureHelp: function (currentModel, position) {
                    if (currentModel.uri.toString() !== state.model.uri.toString()) {
                        return Promise.resolve(null);
                    }

                    return waitUntilReady(1500).then(function (ready) {
                        if (!ready) {
                            return null;
                        }
                        ensureLatestDocumentSent();

                        return sendRequestWithRetry('textDocument/signatureHelp', {
                            textDocument: { uri: currentModel.uri.toString() },
                            position: {
                                line: position.lineNumber - 1,
                                character: position.column - 1
                            }
                        }, 1, 120, 4000).then(function (result) {
                            if (!result || !result.signatures) {
                                return null;
                            }
                            return {
                                value: {
                                    signatures: result.signatures.map(function (sig) {
                                        return {
                                            label: sig.label,
                                            documentation: sig.documentation && sig.documentation.value ? sig.documentation.value : sig.documentation,
                                            parameters: sig.parameters || []
                                        };
                                    }),
                                    activeSignature: result.activeSignature || 0,
                                    activeParameter: result.activeParameter || 0
                                },
                                dispose: function () {}
                            };
                        }).catch(function () {
                            return null;
                        });
                    });
                }
            });

            state.providerDisposables.push(completionDisposable, hoverDisposable, signatureDisposable);
        }

        function normalizeCompletionItems(result) {
            if (!result) {
                return [];
            }
            if (Array.isArray(result)) {
                return result;
            }
            if (Array.isArray(result.items)) {
                return result.items;
            }
            return [];
        }

        function mapCompletionKind(monaco, kind) {
            const map = {
                1: monaco.languages.CompletionItemKind.Text,
                2: monaco.languages.CompletionItemKind.Method,
                3: monaco.languages.CompletionItemKind.Function,
                4: monaco.languages.CompletionItemKind.Constructor,
                5: monaco.languages.CompletionItemKind.Field,
                6: monaco.languages.CompletionItemKind.Variable,
                7: monaco.languages.CompletionItemKind.Class,
                8: monaco.languages.CompletionItemKind.Interface,
                9: monaco.languages.CompletionItemKind.Module,
                10: monaco.languages.CompletionItemKind.Property,
                11: monaco.languages.CompletionItemKind.Unit,
                12: monaco.languages.CompletionItemKind.Value,
                13: monaco.languages.CompletionItemKind.Enum,
                14: monaco.languages.CompletionItemKind.Keyword,
                15: monaco.languages.CompletionItemKind.Snippet,
                16: monaco.languages.CompletionItemKind.Color,
                17: monaco.languages.CompletionItemKind.File,
                18: monaco.languages.CompletionItemKind.Reference
            };
            return map[kind] || monaco.languages.CompletionItemKind.Text;
        }

        function toMarkdown(contents) {
            if (typeof contents === 'string') {
                return contents;
            }
            if (Array.isArray(contents)) {
                return contents.map(toMarkdown).filter(Boolean).join('\n\n');
            }
            if (contents && contents.value) {
                return contents.value;
            }
            return '';
        }

        state.model.onDidChangeContent(function () {
            state.hasUnsyncedChanges = true;
            if (state.isReady) {
                scheduleDidChange(250);
            }
        });
        setLspState('connecting', 'client-created');

        registerProviders();
        editor.addCommand(monaco.KeyMod.CtrlCmd | monaco.KeyCode.Space, function () {
            editor.trigger('keyboard', 'editor.action.triggerSuggest', {});
        });
        connect();

        return {
            getState: function () {
                return state.lspState;
            },
            getCompletionRequestsSent: function () {
                return state.completionRequestsSent;
            },
            getStateEvents: function () {
                return Array.isArray(window.__lspStateEvents) ? window.__lspStateEvents.slice() : [];
            },
            debugForceSocketClose: function () {
                if (state.socket && state.socket.readyState === WebSocket.OPEN) {
                    state.socket.close();
                }
            },
            dispose: function () {
                clearTimeout(state.changeTimer);
                if (state.isReady) {
                    sendNotification('textDocument/didClose', {
                        textDocument: { uri: state.model.uri.toString() }
                    });
                }
                if (state.socket && state.socket.readyState === WebSocket.OPEN) {
                    state.socket.close();
                }
                monaco.editor.setModelMarkers(state.model, 'jdtls', []);
                state.providerDisposables.forEach(function (d) { d.dispose(); });
                setLspState('degraded', 'disposed');
            }
        };
    }

    window.initExerciseLsp = function (options) {
        if (!options || !options.enabled || !options.editor || !window.monaco) {
            return null;
        }

        try {
            return createClient(options.editor, options.exerciseId, options.workspaceUri, options.wsUrl);
        } catch (error) {
            const indicator = document.getElementById('lsp-status-indicator');
            if (indicator) {
                indicator.className = 'lsp-status lsp-status--failed';
                const text = indicator.querySelector('.lsp-status__text');
                if (text) {
                    text.textContent = 'LSP nicht verfügbar';
                }
            }
            if (window.showNotification) {
                window.showNotification('Java-Hinweise nicht verfügbar. Editor läuft ohne LSP.', 'error');
            }
            return null;
        }
    };
})();
