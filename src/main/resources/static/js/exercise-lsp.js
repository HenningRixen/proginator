(function () {
    'use strict';

    function createClient(editor, exerciseId, workspaceUri) {
        const monaco = window.monaco;
        if (!monaco || !editor) {
            return null;
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
            requestId: 1,
            pending: new Map(),
            model: model,
            version: 1,
            changeTimer: null,
            providerDisposables: []
        };

        function send(payload) {
            if (!state.socket || state.socket.readyState !== WebSocket.OPEN) {
                return;
            }
            state.socket.send(JSON.stringify(payload));
        }

        function sendRequest(method, params) {
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
                setTimeout(function () {
                    if (state.pending.has(id)) {
                        state.pending.delete(id);
                        resolve(null);
                    }
                }, 4000);
            });
        }

        function sendNotification(method, params) {
            send({
                jsonrpc: '2.0',
                method: method,
                params: params
            });
        }

        function flushDidChange() {
            if (!state.isReady) {
                return;
            }

            state.version += 1;
            state.changeTimer = null;
            sendNotification('textDocument/didChange', {
                textDocument: {
                    uri: state.model.uri.toString(),
                    version: state.version
                },
                contentChanges: [{ text: state.model.getValue() }]
            });
        }

        function ensureLatestDocumentSent() {
            if (state.changeTimer) {
                clearTimeout(state.changeTimer);
                flushDidChange();
            }
        }

        function connect() {
            const protocol = window.location.protocol === 'https:' ? 'wss' : 'ws';
            const wsUrl = protocol + '://' + window.location.host + '/api/lsp/ws';
            state.socket = new WebSocket(wsUrl);

            state.socket.addEventListener('open', function () {
                sendRequest('initialize', {
                    processId: null,
                    rootUri: normalizedWorkspaceUri,
                    capabilities: {
                        textDocument: {
                            completion: {
                                completionItem: { snippetSupport: true }
                            },
                            hover: {},
                            signatureHelp: {},
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
                }).then(function () {
                    sendNotification('initialized', {});
                    sendNotification('textDocument/didOpen', {
                        textDocument: {
                            uri: state.model.uri.toString(),
                            languageId: 'java',
                            version: state.version,
                            text: state.model.getValue()
                        }
                    });
                    state.isReady = true;
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
            });

            state.socket.addEventListener('error', function () {
                state.isReady = false;
            });
        }

        function applyDiagnostics(params) {
            if (!params || params.uri !== state.model.uri.toString() || !Array.isArray(params.diagnostics)) {
                return;
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
                    if (!state.isReady || currentModel.uri.toString() !== state.model.uri.toString()) {
                        return { suggestions: [] };
                    }
                    ensureLatestDocumentSent();

                    return sendRequest('textDocument/completion', {
                        textDocument: { uri: currentModel.uri.toString() },
                        position: {
                            line: position.lineNumber - 1,
                            character: position.column - 1
                        },
                        context: {
                            triggerKind: context && context.triggerKind ? context.triggerKind : 1,
                            triggerCharacter: context && context.triggerCharacter ? context.triggerCharacter : undefined
                        }
                    }).then(function (result) {
                        const items = normalizeCompletionItems(result);
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
                    });
                }
            });

            const hoverDisposable = monaco.languages.registerHoverProvider('java', {
                provideHover: function (currentModel, position) {
                    if (!state.isReady || currentModel.uri.toString() !== state.model.uri.toString()) {
                        return null;
                    }
                    ensureLatestDocumentSent();

                    return sendRequest('textDocument/hover', {
                        textDocument: { uri: currentModel.uri.toString() },
                        position: {
                            line: position.lineNumber - 1,
                            character: position.column - 1
                        }
                    }).then(function (result) {
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
                }
            });

            const signatureDisposable = monaco.languages.registerSignatureHelpProvider('java', {
                signatureHelpTriggerCharacters: ['(', ','],
                provideSignatureHelp: function (currentModel, position) {
                    if (!state.isReady || currentModel.uri.toString() !== state.model.uri.toString()) {
                        return null;
                    }
                    ensureLatestDocumentSent();

                    return sendRequest('textDocument/signatureHelp', {
                        textDocument: { uri: currentModel.uri.toString() },
                        position: {
                            line: position.lineNumber - 1,
                            character: position.column - 1
                        }
                    }).then(function (result) {
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
            clearTimeout(state.changeTimer);
            state.changeTimer = setTimeout(flushDidChange, 250);
        });

        registerProviders();
        editor.addCommand(monaco.KeyMod.CtrlCmd | monaco.KeyCode.Space, function () {
            editor.trigger('keyboard', 'editor.action.triggerSuggest', {});
        });
        connect();

        return {
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
            }
        };
    }

    window.initExerciseLsp = function (options) {
        if (!options || !options.enabled || !options.editor || !window.monaco) {
            return null;
        }

        try {
            return createClient(options.editor, options.exerciseId, options.workspaceUri);
        } catch (error) {
            if (window.showNotification) {
                window.showNotification('Java-Hinweise nicht verfügbar. Editor läuft ohne LSP.', 'error');
            }
            return null;
        }
    };
})();
