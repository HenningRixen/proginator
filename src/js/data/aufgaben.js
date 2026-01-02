// Aufgaben-Datenbank für alle Themenbereiche
const aufgabenData = {
    // Thema 1: Einstieg & Werkzeuge
    "task1-1": {
        id: "task1-1",
        themaId: "thema1",
        title: "Hello World",
        difficulty: "einfach",
        time: "15 min",
        type: "Einführung",
        tags: "main-Methode, Ausgabe",
        completed: false,
        description: `
            <div class="section">
                <h3><i class="fas fa-bullseye"></i> Lernziele</h3>
                <ul class="requirements">
                    <li>Erste Java-Klasse erstellen</li>
                    <li>Programm kompilieren und ausführen</li>
                    <li>Konsolenausgabe verwenden</li>
                </ul>
            </div>
            
            <div class="section">
                <h3><i class="fas fa-tasks"></i> Aufgabenstellung</h3>
                <p>Schreiben Sie ein Java-Programm, das <strong>"Hallo Programmieren 1!"</strong> auf der Konsole ausgibt.</p>
                <p>Speichern Sie das Programm in einer Datei namens <code>HelloWorld.java</code>.</p>
            </div>
            
            <div class="section">
                <h3><i class="fas fa-code"></i> Starter-Code</h3>
                <div class="code-header">
                    <span>HelloWorld.java</span>
                    <button class="copy-btn">Kopieren</button>
                </div>
                <pre><code class="language-java">public class HelloWorld {
    public static void main(String[] args) {
        // Ihr Code hier
    }
}</code></pre>
            </div>
            
            <div class="section">
                <h3><i class="fas fa-check-circle"></i> Anforderungen</h3>
                <ul class="requirements">
                    <li>Die Klasse muss <code>HelloWorld</code> heißen</li>
                    <li>Verwenden Sie <code>System.out.println()</code></li>
                    <li>Die Ausgabe muss genau lauten: <code>"Hallo Programmieren 1!"</code></li>
                </ul>
            </div>
            
            <div class="hint-box">
                <h4><i class="fas fa-lightbulb"></i> Tipp</h4>
                <p>In Java muss der Dateiname exakt mit dem Klassennamen übereinstimmen (Groß-/Kleinschreibung beachten!).</p>
            </div>
        `,
        solution: `
            <div class="section">
                <h3><i class="fas fa-check"></i> Musterlösung</h3>
                <div class="code-header">
                    <span>HelloWorld.java</span>
                    <button class="copy-btn">Kopieren</button>
                </div>
                <pre><code class="language-java">public class HelloWorld {
    public static void main(String[] args) {
        System.out.println("Hallo Programmieren 1!");
    }
}</code></pre>
            </div>
            
            <div class="section">
                <h3><i class="fas fa-graduation-cap"></i> Erklärung</h3>
                <p>Dieses Programm demonstriert die Grundstruktur eines Java-Programms:</p>
                <ul class="requirements">
                    <li><code>public class HelloWorld</code> definiert eine öffentliche Klasse</li>
                    <li><code>public static void main(String[] args)</code> ist der Programm-Einstiegspunkt</li>
                    <li><code>System.out.println()</code> gibt Text auf der Konsole aus</li>
                    <li>Jede Anweisung endet mit einem Semikolon (<code>;</code>)</li>
                </ul>
            </div>
            
            <div class="section">
                <h3><i class="fas fa-terminal"></i> Kompilieren und Ausführen</h3>
                <p>So führen Sie das Programm aus:</p>
                <div class="code-header">
                    <span>Kommandozeile</span>
                    <button class="copy-btn">Kopieren</button>
                </div>
                <pre><code class="language-bash"># Kompilieren
javac HelloWorld.java

# Ausführen
java HelloWorld</code></pre>
            </div>
        `,
        resources: `
            <div class="section">
                <h3><i class="fas fa-download"></i> Installation</h3>
                <ul class="requirements">
                    <li><a href="https://www.oracle.com/java/technologies/javase-downloads.html" target="_blank">JDK Download</a> - Offizielles Java Development Kit</li>
                    <li><a href="https://www.jetbrains.com/idea/download/" target="_blank">IntelliJ IDEA</a> - Empfohlene IDE</li>
                    <li><a href="https://www.eclipse.org/downloads/" target="_blank">Eclipse</a> - Alternative IDE</li>
                </ul>
            </div>
            
            <div class="section">
                <h3><i class="fas fa-play-circle"></i> Video-Tutorials</h3>
                <ul class="requirements">
                    <li><a href="#">IntelliJ Einrichtung für Java</a></li>
                    <li><a href="#">Eclipse Installation und Konfiguration</a></li>
                    <li><a href="#">Erstes Java-Programm (Step-by-Step)</a></li>
                </ul>
            </div>
        `
    },
    "task1-2": {
        id: "task1-2",
        themaId: "thema1",
        title: "Kommandozeilenparameter",
        difficulty: "einfach",
        time: "20 min",
        type: "Einführung",
        tags: "main, Parameter, Eingabe",
        completed: false,
        description: `
            <div class="section">
                <h3><i class="fas fa-tasks"></i> Aufgabe: Personalisiertes Hello World</h3>
                <p>Erweitern Sie das Hello World Programm, so dass es einen Namen als Parameter entgegennimmt und personalisiert begrüßt.</p>
            </div>
        `,
        solution: `<div class="section"><h3>Lösung wird geladen...</h3></div>`,
        resources: `<div class="section"><h3>Ressourcen werden geladen...</h3></div>`
    },
    "task1-3": {
        id: "task1-3",
        themaId: "thema1",
        title: "Projektstruktur erstellen",
        difficulty: "mittel",
        time: "30 min",
        type: "Organisation",
        tags: "Package, Projektstruktur, IDE",
        completed: false,
        description: `<div class="section"><h3>Aufgabe wird geladen...</h3></div>`,
        solution: `<div class="section"><h3>Lösung wird geladen...</h3></div>`,
        resources: `<div class="section"><h3>Ressourcen werden geladen...</h3></div>`
    },

    // Thema 2: Lexikalisches & Datentypen
    "task2-1": {
        id: "task2-1",
        themaId: "thema2",
        title: "Temperaturumrechner",
        difficulty: "einfach",
        time: "25 min",
        type: "Datentypen",
        tags: "float, double, Berechnung",
        completed: false,
        description: `
            <div class="section">
                <h3><i class="fas fa-tasks"></i> Celsius zu Fahrenheit Umrechner</h3>
                <p>Schreiben Sie ein Programm, das Celsius-Temperaturen in Fahrenheit umrechnet.</p>
                <p>Formel: <code>°F = °C × 1,8 + 32</code></p>
            </div>
        `,
        solution: `<div class="section"><h3>Lösung wird geladen...</h3></div>`,
        resources: `<div class="section"><h3>Ressourcen werden geladen...</h3></div>`
    },
    "task2-2": {
        id: "task2-2",
        themaId: "thema2",
        title: "Zahlensystem-Umrechnung",
        difficulty: "mittel",
        time: "30 min",
        type: "Literale",
        tags: "hex, binary, octal",
        completed: false,
        description: `<div class="section"><h3>Aufgabe wird geladen...</h3></div>`,
        solution: `<div class="section"><h3>Lösung wird geladen...</h3></div>`,
        resources: `<div class="section"><h3>Ressourcen werden geladen...</h3></div>`
    },
    "task2-3": {
        id: "task2-3",
        themaId: "thema2",
        title: "Konstanten verwenden",
        difficulty: "einfach",
        time: "20 min",
        type: "Konstanten",
        tags: "final, Konstanten, PI",
        completed: false,
        description: `<div class="section"><h3>Aufgabe wird geladen...</h3></div>`,
        solution: `<div class="section"><h3>Lösung wird geladen...</h3></div>`,
        resources: `<div class="section"><h3>Ressourcen werden geladen...</h3></div>`
    },

    // Weitere Aufgaben für die anderen Themenbereiche...
    // Hier können Sie weitere Aufgaben hinzufügen
};

// Hilfsfunktionen für Aufgaben
const aufgabenManager = {
    getAufgabenByThema(themaId) {
        return Object.values(aufgabenData).filter(aufgabe => aufgabe.themaId === themaId);
    },

    getAufgabeById(aufgabeId) {
        return aufgabenData[aufgabeId];
    },

    markAsCompleted(aufgabeId) {
        if (aufgabenData[aufgabeId]) {
            aufgabenData[aufgabeId].completed = true;
            this.saveProgress();
        }
    },

    markAsIncomplete(aufgabeId) {
        if (aufgabenData[aufgabeId]) {
            aufgabenData[aufgabeId].completed = false;
            this.saveProgress();
        }
    },

    getProgress() {
        const completed = Object.values(aufgabenData).filter(a => a.completed).length;
        const total = Object.keys(aufgabenData).length;
        return {
            completed,
            total,
            percentage: total > 0 ? Math.round((completed / total) * 100) : 0
        };
    },

    getThemaProgress(themaId) {
        const themaAufgaben = this.getAufgabenByThema(themaId);
        const completed = themaAufgaben.filter(a => a.completed).length;
        const total = themaAufgaben.length;
        return {
            completed,
            total,
            percentage: total > 0 ? Math.round((completed / total) * 100) : 0
        };
    },

    saveProgress() {
        const progress = {};
        Object.keys(aufgabenData).forEach(key => {
            progress[key] = aufgabenData[key].completed;
        });
        localStorage.setItem('programmieren1_progress', JSON.stringify(progress));
    },

    loadProgress() {
        const saved = localStorage.getItem('programmieren1_progress');
        if (saved) {
            const progress = JSON.parse(saved);
            Object.keys(progress).forEach(key => {
                if (aufgabenData[key]) {
                    aufgabenData[key].completed = progress[key];
                }
            });
        }
    }
};

// Progress beim Start laden
aufgabenManager.loadProgress();