// Themenbereiche für Programmieren 1
const themenData = [
    {
        id: "thema1",
        title: "Einstieg & Werkzeuge",
        subtitle: "Java installieren, IDE kennenlernen, erste Programme",
        woche: 1,
        icon: "fas fa-tools",
        progress: 0,
        color: "#3498db",
        beschreibung: `
            <h3><i class="fas fa-rocket"></i> Einstieg in die Java-Programmierung</h3>
            <p>In diesem ersten Themenbereich lernen Sie die Grundlagen der Java-Entwicklungsumgebung kennen.</p>
            
            <div class="learning-goals">
                <h4><i class="fas fa-bullseye"></i> Lernziele</h4>
                <ul class="requirements">
                    <li>Java Development Kit (JDK) installieren und konfigurieren</li>
                    <li>Integrierte Entwicklungsumgebung (IDE) wie IntelliJ oder Eclipse einrichten</li>
                    <li>Erstes Java-Programm schreiben, kompilieren und ausführen</li>
                    <li>Projektstruktur und Packages verstehen</li>
                    <li>Kommandozeilenparameter verwenden</li>
                </ul>
            </div>
            
            <div class="topic-structure">
                <h4><i class="fas fa-sitemap"></i> Themenstruktur</h4>
                <div class="structure-grid">
                    <div class="structure-item">
                        <i class="fas fa-download"></i>
                        <h5>JDK Installation</h5>
                        <p>Java Development Kit installieren</p>
                    </div>
                    <div class="structure-item">
                        <i class="fas fa-code"></i>
                        <h5>IDE Einrichtung</h5>
                        <p>IntelliJ/Eclipse konfigurieren</p>
                    </div>
                    <div class="structure-item">
                        <i class="fas fa-play-circle"></i>
                        <h5>Hello World</h5>
                        <p>Erstes Programm schreiben</p>
                    </div>
                    <div class="structure-item">
                        <i class="fas fa-cogs"></i>
                        <h5>Kompilieren & Ausführen</h5>
                        <p>Programme kompilieren und starten</p>
                    </div>
                </div>
            </div>
            
            <div class="resources-overview">
                <h4><i class="fas fa-book-open"></i> Verfügbare Ressourcen</h4>
                <p>Zu diesem Thema stehen zur Verfügung:</p>
                <ul class="requirements">
                    <li><strong>3 praktische Aufgaben</strong> mit steigendem Schwierigkeitsgrad</li>
                    <li><strong>Vollständige Musterlösungen</strong> mit detaillierten Erklärungen</li>
                    <li><strong>Video-Tutorials</strong> zur IDE-Einrichtung</li>
                    <li><strong>Cheat Sheets</strong> für wichtige Befehle</li>
                    <li><strong>Übungsblätter</strong> zur Vertiefung</li>
                </ul>
            </div>
            
            <div class="tip-box">
                <h4><i class="fas fa-lightbulb"></i> Tipp für den Einstieg</h4>
                <p>Beginnen Sie mit der Installation des JDK und der IDE. Machen Sie sich mit der Entwicklungsumgebung vertraut, bevor Sie mit dem Programmieren starten. Das spart später viel Zeit!</p>
            </div>
        `,
        aufgabenIds: ["task1-1", "task1-2", "task1-3"]
    },
    {
        id: "thema2",
        title: "Lexikalisches & Datentypen",
        subtitle: "Java-Syntax, primitive Typen, Literale, Konstanten",
        woche: 2,
        icon: "fas fa-keyboard",
        progress: 0,
        color: "#9b59b6",
        beschreibung: `
            <h3><i class="fas fa-code"></i> Grundlagen der Java-Syntax</h3>
            <p>In diesem Themenbereich lernen Sie die lexikalischen Elemente von Java und die verschiedenen Datentypen kennen.</p>
            
            <div class="learning-goals">
                <h4><i class="fas fa-bullseye"></i> Lernziele</h4>
                <ul class="requirements">
                    <li>Java-Bezeichner und Schlüsselwörter verstehen</li>
                    <li>Primitive Datentypen (int, long, float, double, char, boolean) anwenden</li>
                    <li>Literale in verschiedenen Zahlensystemen verwenden</li>
                    <li>Konstanten mit <code>final</code> deklarieren</li>
                    <li>Typkonvertierungen durchführen</li>
                </ul>
            </div>
            
            <div class="datatype-overview">
                <h4><i class="fas fa-table"></i> Übersicht primitiver Datentypen</h4>
                <table class="datatype-table">
                    <thead>
                        <tr>
                            <th>Typ</th>
                            <th>Größe</th>
                            <th>Wertebereich</th>
                            <th>Beispiel</th>
                        </tr>
                    </thead>
                    <tbody>
                        <tr>
                            <td><code>byte</code></td>
                            <td>8 Bit</td>
                            <td>-128 bis 127</td>
                            <td><code>byte b = 100;</code></td>
                        </tr>
                        <tr>
                            <td><code>short</code></td>
                            <td>16 Bit</td>
                            <td>-32.768 bis 32.767</td>
                            <td><code>short s = 1000;</code></td>
                        </tr>
                        <tr>
                            <td><code>int</code></td>
                            <td>32 Bit</td>
                            <td>-2³¹ bis 2³¹-1</td>
                            <td><code>int i = 100000;</code></td>
                        </tr>
                        <tr>
                            <td><code>long</code></td>
                            <td>64 Bit</td>
                            <td>-2⁶³ bis 2⁶³-1</td>
                            <td><code>long l = 100000L;</code></td>
                        </tr>
                        <tr>
                            <td><code>float</code></td>
                            <td>32 Bit</td>
                            <td>±3.4×10³⁸</td>
                            <td><code>float f = 3.14f;</code></td>
                        </tr>
                        <tr>
                            <td><code>double</code></td>
                            <td>64 Bit</td>
                            <td>±1.8×10³⁰⁸</td>
                            <td><code>double d = 3.14159;</code></td>
                        </tr>
                        <tr>
                            <td><code>char</code></td>
                            <td>16 Bit</td>
                            <td>0 bis 65.535</td>
                            <td><code>char c = 'A';</code></td>
                        </tr>
                        <tr>
                            <td><code>boolean</code></td>
                            <td>1 Bit</td>
                            <td>true/false</td>
                            <td><code>boolean flag = true;</code></td>
                        </tr>
                    </tbody>
                </table>
            </div>
            
            <div class="tip-box">
                <h4><i class="fas fa-lightbulb"></i> Wichtiger Hinweis</h4>
                <p>Java ist eine streng typisierte Sprache. Das bedeutet, dass jeder Variable ein Datentyp zugewiesen werden muss und dieser Typ nicht einfach geändert werden kann. Achten Sie auf korrekte Typdeklarationen!</p>
            </div>
        `,
        aufgabenIds: ["task2-1", "task2-2", "task2-3"]
    },
    {
        id: "thema3",
        title: "Variablen, Ausdrücke & Operatoren",
        subtitle: "Zuweisungen, Arithmetik, Präzedenz, Typumwandlungen",
        woche: 3,
        icon: "fas fa-calculator",
        progress: 0,
        color: "#e74c3c",
        beschreibung: `
            <h3><i class="fas fa-superscript"></i> Arbeiten mit Operatoren und Ausdrücken</h3>
            <p>In diesem Themenbereich lernen Sie, wie Sie mit Variablen arbeiten, Berechnungen durchführen und die Operator-Präzedenz verstehen.</p>
        `,
        aufgabenIds: ["task3-1", "task3-2", "task3-3"]
    },
    {
        id: "thema4",
        title: "Kontrollstrukturen",
        subtitle: "if/else, switch, Schleifen, break/continue",
        woche: 4,
        icon: "fas fa-project-diagram",
        progress: 0,
        color: "#f39c12",
        beschreibung: `
            <h3><i class="fas fa-code-branch"></i> Programmfluss steuern</h3>
            <p>Lernen Sie, wie Sie den Ablauf Ihres Programms mit Bedingungen und Schleifen kontrollieren können.</p>
        `,
        aufgabenIds: ["task4-1", "task4-2", "task4-3", "task4-4"]
    },
    {
        id: "thema5",
        title: "Felder & Listen",
        subtitle: "Arrays, ArrayList, mehrdimensionale Felder",
        woche: 5,
        icon: "fas fa-layer-group",
        progress: 0,
        color: "#1abc9c",
        beschreibung: `
            <h3><i class="fas fa-th-large"></i> Daten strukturiert speichern</h3>
            <p>Erfahren Sie, wie Sie mehrere Werte in Arrays und Listen speichern und verarbeiten können.</p>
        `,
        aufgabenIds: ["task5-1", "task5-2", "task5-3", "task5-4"]
    },
    {
        id: "thema6",
        title: "Methoden / Funktionen",
        subtitle: "Parameter, Rückgabewerte, static, Rekursion",
        woche: 6,
        icon: "fas fa-puzzle-piece",
        progress: 0,
        color: "#34495e",
        beschreibung: `
            <h3><i class="fas fa-cube"></i> Code strukturieren und wiederverwenden</h3>
            <p>Lernen Sie, wie Sie Code in Methoden organisieren, um Wiederholungen zu vermeiden und die Lesbarkeit zu verbessern.</p>
        `,
        aufgabenIds: ["task6-1", "task6-2", "task6-3"]
    },
    {
        id: "thema7",
        title: "Objektorientierung — Klassen & Objekte",
        subtitle: "Klassenaufbau, Konstruktoren, Kapselung",
        woche: 7,
        icon: "fas fa-cube",
        progress: 0,
        color: "#16a085",
        beschreibung: `
            <h3><i class="fas fa-shapes"></i> Grundlagen der Objektorientierung</h3>
            <p>Einführung in die Konzepte der Objektorientierten Programmierung mit Java.</p>
        `,
        aufgabenIds: ["task7-1", "task7-2", "task7-3"]
    },
    {
        id: "thema8",
        title: "Vererbung & Polymorphismus",
        subtitle: "extends, super, abstract, Methodenüberschreibung",
        woche: 8,
        icon: "fas fa-sitemap",
        progress: 0,
        color: "#8e44ad",
        beschreibung: `
            <h3><i class="fas fa-project-diagram"></i> Objektbeziehungen modellieren</h3>
            <p>Lernen Sie, wie Sie mit Vererbung Code wiederverwenden und mit Polymorphismus flexible Systeme erstellen.</p>
        `,
        aufgabenIds: ["task8-1", "task8-2", "task8-3"]
    },
    {
        id: "thema9",
        title: "Interfaces & Collections",
        subtitle: "interface, Comparable, List/Set/Map",
        woche: 9,
        icon: "fas fa-network-wired",
        progress: 0,
        color: "#d35400",
        beschreibung: `
            <h3><i class="fas fa-plug"></i> Verträge definieren und Sammlungen nutzen</h3>
            <p>Erfahren Sie, wie Sie mit Interfaces flexible Architekturen erstellen und mit Collections Framework arbeiten.</p>
        `,
        aufgabenIds: ["task9-1", "task9-2", "task9-3"]
    },
    {
        id: "thema10",
        title: "Pakete, Ausnahmen, Java-Doc",
        subtitle: "package, try/catch, Exception-Handling",
        woche: 10,
        icon: "fas fa-exclamation-triangle",
        progress: 0,
        color: "#c0392b",
        beschreibung: `
            <h3><i class="fas fa-shield-alt"></i> Robuste Programme schreiben</h3>
            <p>Lernen Sie, wie Sie Code organisieren, Ausnahmen behandeln und Dokumentation erstellen.</p>
        `,
        aufgabenIds: ["task10-1", "task10-2"]
    },
    {
        id: "thema11",
        title: "Nützliche Erweiterungen / Ausblick",
        subtitle: "Generics, Lambdas, Streams, Multithreading",
        woche: 11,
        icon: "fas fa-chart-line",
        progress: 0,
        color: "#7f8c8d",
        beschreibung: `
            <h3><i class="fas fa-forward"></i> Weiterführende Konzepte</h3>
            <p>Einblick in fortgeschrittene Java-Konzepte, die in Programmieren 2 vertieft werden.</p>
        `,
        aufgabenIds: ["task11-1", "task11-2"]
    }
];