// Haupt-JavaScript für die Programmieren 1 Lernplattform

class Lernplattform {
    constructor() {
        // DOM Elemente
        this.themenList = document.getElementById('themenList');
        this.themaTitle = document.getElementById('themaTitle');
        this.themaMeta = document.getElementById('themaMeta');
        this.wocheNr = document.getElementById('wocheNr');
        this.aufgabenCount = document.getElementById('aufgabenCount');
        this.themaDescription = document.getElementById('themaDescription');
        this.aufgabenTabs = document.getElementById('aufgabenTabs');
        this.tabContents = {
            uebersicht: document.getElementById('uebersicht'),
            aufgaben: document.getElementById('aufgaben'),
            loesungen: document.getElementById('loesungen'),
            ressourcen: document.getElementById('ressourcen')
        };
        this.prevBtn = document.getElementById('prevBtn');
        this.nextBtn = document.getElementById('nextBtn');
        this.overallProgress = document.getElementById('overallProgress');
        this.overallProgressBar = document.getElementById('overallProgressBar');

        // State
        this.currentThemaIndex = 0;
        this.currentAufgabeIndex = 0;
        this.currentTab = 'uebersicht';

        // Initialisierung
        this.init();
    }

    init() {
        // Progress aktualisieren
        this.updateProgress();

        // Sidebar initialisieren
        this.initSidebar();

        // Event Listener
        this.initEventListeners();

        // Erstes Thema laden
        this.loadThema(themenData[0]);
    }

    initSidebar() {
        this.themenList.innerHTML = '';

        themenData.forEach((thema, index) => {
            const progress = aufgabenManager.getThemaProgress(thema.id);

            const themaItem = document.createElement('li');
            themaItem.className = 'thema-item';
            themaItem.innerHTML = `
                <button class="thema-btn ${index === 0 ? 'active' : ''}" 
                        data-thema="${thema.id}" 
                        data-index="${index}">
                    <div class="thema-icon" style="background: ${thema.color}">
                        <i class="${thema.icon}"></i>
                    </div>
                    <div class="thema-info">
                        <div class="thema-title">${thema.title}</div>
                        <div class="thema-subtitle">${thema.subtitle}</div>
                    </div>
                    <div class="thema-progress">
                        ${progress.percentage}%
                    </div>
                </button>
            `;

            this.themenList.appendChild(themaItem);
        });
    }

    initEventListeners() {
        // Thema-Buttons
        document.addEventListener('click', (e) => {
            if (e.target.closest('.thema-btn')) {
                const btn = e.target.closest('.thema-btn');
                const themaId = btn.getAttribute('data-thema');
                const index = parseInt(btn.getAttribute('data-index'));

                this.currentThemaIndex = index;
                this.loadThema(themenData[index]);

                // Active State setzen
                document.querySelectorAll('.thema-btn').forEach(b => b.classList.remove('active'));
                btn.classList.add('active');

                // Navigation Buttons aktualisieren
                this.updateNavButtons();
            }

            // Tab-Buttons
            if (e.target.closest('.tab-btn')) {
                const tabBtn = e.target.closest('.tab-btn');
                const tabId = tabBtn.getAttribute('data-tab');
                this.showTab(tabId);
            }

            // Copy Buttons
            if (e.target.closest('.copy-btn')) {
                this.copyCode(e.target.closest('.copy-btn'));
            }

            // Aufgabe als erledigt markieren
            if (e.target.closest('.complete-btn')) {
                const btn = e.target.closest('.complete-btn');
                const aufgabeId = btn.getAttribute('data-aufgabe');
                this.toggleAufgabeStatus(aufgabeId);
            }
        });

        // Navigation Buttons
        this.prevBtn.addEventListener('click', () => {
            if (this.currentThemaIndex > 0) {
                this.currentThemaIndex--;
                this.loadThema(themenData[this.currentThemaIndex]);
                this.updateActiveThemaButton();
                this.updateNavButtons();
            }
        });

        this.nextBtn.addEventListener('click', () => {
            if (this.currentThemaIndex < themenData.length - 1) {
                this.currentThemaIndex++;
                this.loadThema(themenData[this.currentThemaIndex]);
                this.updateActiveThemaButton();
                this.updateNavButtons();
            }
        });

        // Theme Toggle
        document.getElementById('themeToggle').addEventListener('click', () => {
            document.body.classList.toggle('dark-theme');
            const icon = document.querySelector('#themeToggle i');
            const text = document.querySelector('#themeToggle span');

            if (document.body.classList.contains('dark-theme')) {
                icon.className = 'fas fa-sun';
                text.textContent = 'Light Mode';
                localStorage.setItem('theme', 'dark');
            } else {
                icon.className = 'fas fa-moon';
                text.textContent = 'Dark Mode';
                localStorage.setItem('theme', 'light');
            }
        });

        // Progress Button
        document.getElementById('progressBtn').addEventListener('click', () => {
            this.showProgressModal();
        });

        // Quick Links
        document.getElementById('linkEinstieg').addEventListener('click', (e) => {
            e.preventDefault();
            this.loadThema(themenData[0]);
            this.currentThemaIndex = 0;
            this.updateActiveThemaButton();
        });

        document.getElementById('linkOOP').addEventListener('click', (e) => {
            e.preventDefault();
            this.loadThema(themenData[6]); // OOP Thema
            this.currentThemaIndex = 6;
            this.updateActiveThemaButton();
        });

        // Theme aus localStorage laden
        const savedTheme = localStorage.getItem('theme');
        if (savedTheme === 'dark') {
            document.body.classList.add('dark-theme');
            document.querySelector('#themeToggle i').className = 'fas fa-sun';
            document.querySelector('#themeToggle span').textContent = 'Light Mode';
        }
    }

    loadThema(thema) {
        // Header aktualisieren
        this.themaTitle.textContent = thema.title;
        this.wocheNr.textContent = thema.woche;

        // Meta Informationen
        const aufgaben = aufgabenManager.getAufgabenByThema(thema.id);
        this.aufgabenCount.textContent = `${aufgaben.length} ${aufgaben.length === 1 ? 'Aufgabe' : 'Aufgaben'}`;

        // Thema Meta anzeigen
        this.themaMeta.style.display = 'flex';

        // Beschreibung laden
        this.themaDescription.innerHTML = thema.beschreibung;

        // Tabs anzeigen
        this.aufgabenTabs.style.display = 'flex';

        // Übersichts-Tab aktualisieren
        this.updateUebersichtTab(thema);

        // Aufgaben-Tab aktualisieren
        this.updateAufgabenTab(thema);

        // Lösungs-Tab aktualisieren
        this.updateLoesungenTab(thema);

        // Ressourcen-Tab aktualisieren
        this.updateRessourcenTab(thema);

        // Code Highlighting
        setTimeout(() => hljs.highlightAll(), 100);

        // Progress aktualisieren
        this.updateThemaProgress(thema.id);
    }

    updateUebersichtTab(thema) {
        // Learning Path aktualisieren
        const learningPath = document.querySelector('.learning-path');
        learningPath.innerHTML = '';

        themenData.forEach((t, index) => {
            const progress = aufgabenManager.getThemaProgress(t.id);

            const step = document.createElement('div');
            step.className = `path-step ${t.id === thema.id ? 'active' : ''}`;
            step.innerHTML = `
                <div class="step-number">${index + 1}</div>
                <div class="step-content">
                    <h4>${t.title}</h4>
                    <p>${t.subtitle}</p>
                    <div class="progress-info">
                        <span>Fortschritt: ${progress.percentage}%</span>
                        <span>${progress.completed}/${progress.total}</span>
                    </div>
                </div>
            `;

            // Klick-Event für Learning Path Steps
            step.addEventListener('click', () => {
                this.currentThemaIndex = index;
                this.loadThema(t);
                this.updateActiveThemaButton();
                this.updateNavButtons();
            });

            learningPath.appendChild(step);
        });
    }

    updateAufgabenTab(thema) {
        const aufgaben = aufgabenManager.getAufgabenByThema(thema.id);

        let html = `
            <div class="section">
                <h3><i class="fas fa-tasks"></i> Verfügbare Aufgaben</h3>
                <p>Dieser Themenbereich enthält ${aufgaben.length} praktische Übungsaufgaben.</p>
            </div>
            
            <div class="aufgaben-grid">
        `;

        aufgaben.forEach(aufgabe => {
            html += `
                <div class="aufgabe-card ${aufgabe.completed ? 'completed' : ''}">
                    <div class="aufgabe-header">
                        <div class="aufgabe-title">${aufgabe.title}</div>
                        <span class="aufgabe-difficulty difficulty-${aufgabe.difficulty}">
                            ${aufgabe.difficulty}
                        </span>
                    </div>
                    <div class="aufgabe-meta">
                        <span><i class="far fa-clock"></i> ${aufgabe.time}</span>
                        <span><i class="fas fa-tag"></i> ${aufgabe.type}</span>
                    </div>
                    <div class="aufgabe-description">
                        ${this.extractFirstParagraph(aufgabe.description)}
                    </div>
                    <div class="aufgabe-actions">
                        <button class="btn btn-primary aufgabe-detail-btn" data-aufgabe="${aufgabe.id}">
                            <i class="fas fa-eye"></i> Details anzeigen
                        </button>
                        <button class="btn ${aufgabe.completed ? 'btn-secondary' : 'btn-success'} complete-btn" 
                                data-aufgabe="${aufgabe.id}">
                            <i class="fas ${aufgabe.completed ? 'fa-undo' : 'fa-check'}"></i>
                            ${aufgabe.completed ? 'Rückgängig' : 'Erledigt'}
                        </button>
                    </div>
                </div>
            `;
        });

        html += `</div>`;

        this.tabContents.aufgaben.innerHTML = html;

        // Event Listener für Detail-Buttons
        document.querySelectorAll('.aufgabe-detail-btn').forEach(btn => {
            btn.addEventListener('click', (e) => {
                const aufgabeId = e.target.closest('.aufgabe-detail-btn').getAttribute('data-aufgabe');
                this.showAufgabeDetail(aufgabeId);
            });
        });
    }

    updateLoesungenTab(thema) {
        const aufgaben = aufgabenManager.getAufgabenByThema(thema.id);

        let html = `
            <div class="section">
                <h3><i class="fas fa-check-circle"></i> Musterlösungen</h3>
                <p>Hier finden Sie die Lösungen zu allen Aufgaben dieses Themenbereichs.</p>
            </div>
        `;

        aufgaben.forEach(aufgabe => {
            html += `
                <div class="aufgabe-detail">
                    <div class="aufgabe-detail-header">
                        <h3 class="aufgabe-detail-title">${aufgabe.title}</h3>
                        <span class="aufgabe-difficulty difficulty-${aufgabe.difficulty}">
                            ${aufgabe.difficulty}
                        </span>
                    </div>
                    ${aufgabe.solution}
                </div>
            `;
        });

        this.tabContents.loesungen.innerHTML = html;
    }

    updateRessourcenTab(thema) {
        // Beispiel-Ressourcen für jedes Thema
        const themaRessourcen = {
            thema1: `
                <div class="section">
                    <h3><i class="fas fa-download"></i> Installationsanleitungen</h3>
                    <ul class="requirements">
                        <li><a href="https://www.oracle.com/java/technologies/javase-downloads.html" target="_blank">JDK 17 Download</a> - Aktuelle Java Version</li>
                        <li><a href="https://www.jetbrains.com/idea/download/" target="_blank">IntelliJ IDEA Community Edition</a> - Kostenlose IDE</li>
                        <li><a href="https://www.eclipse.org/downloads/" target="_blank">Eclipse IDE for Java Developers</a> - Alternative IDE</li>
                    </ul>
                </div>
                
                <div class="section">
                    <h3><i class="fas fa-book"></i> Dokumentation</h3>
                    <ul class="requirements">
                        <li><a href="https://docs.oracle.com/javase/tutorial/" target="_blank">Offizielles Java Tutorial</a> - Oracle</li>
                        <li><a href="https://dev.java/learn/" target="_blank">Java Learning Path</a> - Modernes Java Lernen</li>
                    </ul>
                </div>
            `,
            thema2: `
                <div class="section">
                    <h3><i class="fas fa-table"></i> Datentypen Referenz</h3>
                    <ul class="requirements">
                        <li><a href="#">Primitive Datentypen Cheat Sheet</a> - Übersicht aller Typen</li>
                        <li><a href="#">Literale in Java</a> - Hex, Octal, Binary Darstellung</li>
                        <li><a href="#">Typkonvertierung</a> - Casting und Konvertierung</li>
                    </ul>
                </div>
            `
            // Weitere Themen hier ergänzen...
        };

        this.tabContents.ressourcen.innerHTML = themaRessourcen[thema.id] || `
            <div class="section">
                <h3><i class="fas fa-book"></i> Ressourcen</h3>
                <p>Spezifische Ressourcen für dieses Thema werden demnächst hinzugefügt.</p>
                <p>Allgemeine Java-Ressourcen finden Sie im ersten Themenbereich.</p>
            </div>
        `;
    }

    showAufgabeDetail(aufgabeId) {
        const aufgabe = aufgabenManager.getAufgabeById(aufgabeId);

        const modalHTML = `
            <div class="modal-overlay">
                <div class="modal-content">
                    <div class="modal-header">
                        <h3>${aufgabe.title}</h3>
                        <button class="modal-close">&times;</button>
                    </div>
                    <div class="modal-body">
                        ${aufgabe.description}
                        <div class="modal-actions">
                            <button class="btn btn-primary" id="showSolutionBtn">
                                <i class="fas fa-lightbulb"></i> Lösung anzeigen
                            </button>
                            <button class="btn ${aufgabe.completed ? 'btn-secondary' : 'btn-success'}" 
                                    id="toggleCompleteBtn" data-aufgabe="${aufgabe.id}">
                                <i class="fas ${aufgabe.completed ? 'fa-undo' : 'fa-check'}"></i>
                                ${aufgabe.completed ? 'Als nicht erledigt markieren' : 'Als erledigt markieren'}
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        `;

        // Modal erstellen
        const modal = document.createElement('div');
        modal.innerHTML = modalHTML;
        document.body.appendChild(modal);

        // Event Listener für Modal
        modal.querySelector('.modal-close').addEventListener('click', () => {
            document.body.removeChild(modal);
        });

        modal.querySelector('.modal-overlay').addEventListener('click', (e) => {
            if (e.target === modal.querySelector('.modal-overlay')) {
                document.body.removeChild(modal);
            }
        });

        // Lösung anzeigen Button
        modal.querySelector('#showSolutionBtn').addEventListener('click', () => {
            const solutionSection = document.createElement('div');
            solutionSection.className = 'solution-modal';
            solutionSection.innerHTML = `
                <h4><i class="fas fa-check-circle"></i> Lösung</h4>
                ${aufgabe.solution}
            `;
            modal.querySelector('.modal-body').appendChild(solutionSection);
            modal.querySelector('#showSolutionBtn').style.display = 'none';

            // Code Highlighting
            setTimeout(() => hljs.highlightAll(), 100);
        });

        // Complete Toggle Button
        modal.querySelector('#toggleCompleteBtn').addEventListener('click', () => {
            this.toggleAufgabeStatus(aufgabe.id);
            document.body.removeChild(modal);
            this.loadThema(themenData[this.currentThemaIndex]);
        });

        // Code Highlighting
        setTimeout(() => hljs.highlightAll(), 100);
    }

    toggleAufgabeStatus(aufgabeId) {
        const aufgabe = aufgabenManager.getAufgabeById(aufgabeId);
        if (aufgabe.completed) {
            aufgabenManager.markAsIncomplete(aufgabeId);
        } else {
            aufgabenManager.markAsCompleted(aufgabeId);
        }

        this.updateProgress();
        this.loadThema(themenData[this.currentThemaIndex]);
    }

    showTab(tabId) {
        // Tab Buttons aktualisieren
        document.querySelectorAll('.tab-btn').forEach(btn => {
            btn.classList.toggle('active', btn.getAttribute('data-tab') === tabId);
        });

        // Tab Inhalte aktualisieren
        Object.keys(this.tabContents).forEach(key => {
            this.tabContents[key].classList.toggle('active', key === tabId);
        });

        this.currentTab = tabId;
    }

    updateActiveThemaButton() {
        document.querySelectorAll('.thema-btn').forEach((btn, index) => {
            btn.classList.toggle('active', index === this.currentThemaIndex);
        });
    }

    updateNavButtons() {
        this.prevBtn.disabled = this.currentThemaIndex === 0;
        this.nextBtn.disabled = this.currentThemaIndex === themenData.length - 1;
    }

    updateProgress() {
        const progress = aufgabenManager.getProgress();

        this.overallProgress.textContent = `${progress.percentage}%`;
        this.overallProgressBar.style.width = `${progress.percentage}%`;

        // Progress in Sidebar aktualisieren
        themenData.forEach((thema, index) => {
            const themaProgress = aufgabenManager.getThemaProgress(thema.id);
            const progressElement = document.querySelector(`[data-thema="${thema.id}"] .thema-progress`);
            if (progressElement) {
                progressElement.textContent = `${themaProgress.percentage}%`;
            }
        });
    }

    updateThemaProgress(themaId) {
        const progress = aufgabenManager.getThemaProgress(themaId);
        const progressElement = document.querySelector(`[data-thema="${themaId}"] .thema-progress`);
        if (progressElement) {
            progressElement.textContent = `${progress.percentage}%`;
        }
    }

    copyCode(button) {
        const codeElement = button.closest('.code-header').nextElementSibling;
        const code = codeElement.querySelector('code').textContent;

        navigator.clipboard.writeText(code).then(() => {
            const originalText = button.textContent;
            button.textContent = 'Kopiert!';
            button.style.backgroundColor = '#27ae60';

            setTimeout(() => {
                button.textContent = originalText;
                button.style.backgroundColor = '';
            }, 2000);
        });
    }

    extractFirstParagraph(html) {
        const temp = document.createElement('div');
        temp.innerHTML = html;
        const firstParagraph = temp.querySelector('p');
        return firstParagraph ? firstParagraph.textContent.substring(0, 150) + '...' : 'Aufgabenbeschreibung...';
    }

    showProgressModal() {
        const progress = aufgabenManager.getProgress();

        const modalHTML = `
            <div class="modal-overlay">
                <div class="modal-content">
                    <div class="modal-header">
                        <h3><i class="fas fa-chart-line"></i> Lernfortschritt</h3>
                        <button class="modal-close">&times;</button>
                    </div>
                    <div class="modal-body">
                        <div class="overall-progress">
                            <h4>Gesamtfortschritt</h4>
                            <div class="progress-large">
                                <div class="progress-bar-large" style="width: ${progress.percentage}%"></div>
                            </div>
                            <div class="progress-numbers">
                                <span>${progress.completed} von ${progress.total} Aufgaben erledigt</span>
                                <span>${progress.percentage}%</span>
                            </div>
                        </div>
                        
                        <div class="themen-progress">
                            <h4>Fortschritt nach Themen</h4>
                            ${themenData.map(thema => {
            const themaProgress = aufgabenManager.getThemaProgress(thema.id);
            return `
                                    <div class="thema-progress-item">
                                        <div class="thema-progress-header">
                                            <span>${thema.title}</span>
                                            <span>${themaProgress.percentage}%</span>
                                        </div>
                                        <div class="progress-small">
                                            <div class="progress-bar-small" style="width: ${themaProgress.percentage}%"></div>
                                        </div>
                                        <div class="thema-progress-details">
                                            <span>${themaProgress.completed}/${themaProgress.total} Aufgaben</span>
                                            <span>Woche ${thema.woche}</span>
                                        </div>
                                    </div>
                                `;
        }).join('')}
                        </div>
                    </div>
                </div>
            </div>
        `;

        const modal = document.createElement('div');
        modal.innerHTML = modalHTML;
        document.body.appendChild(modal);

        // Event Listener für Modal
        modal.querySelector('.modal-close').addEventListener('click', () => {
            document.body.removeChild(modal);
        });

        modal.querySelector('.modal-overlay').addEventListener('click', (e) => {
            if (e.target === modal.querySelector('.modal-overlay')) {
                document.body.removeChild(modal);
            }
        });
    }
}

// App starten wenn DOM geladen
document.addEventListener('DOMContentLoaded', () => {
    const app = new Lernplattform();

    // Globale Hilfsfunktionen
    window.copyCode = function(button) {
        const codeElement = button.closest('.code-header').nextElementSibling;
        const code = codeElement.querySelector('code').textContent;

        navigator.clipboard.writeText(code).then(() => {
            const originalText = button.textContent;
            button.textContent = 'Kopiert!';
            button.style.backgroundColor = '#27ae60';

            setTimeout(() => {
                button.textContent = originalText;
                button.style.backgroundColor = '';
            }, 2000);
        });
    };
});