package com.example.prog1learnapp.config.lessons.lesson4;

import com.example.prog1learnapp.config.lessons.LessonDataInitializer;
import com.example.prog1learnapp.config.seed.ExerciseSeedService;
import com.example.prog1learnapp.model.Exercise;
import com.example.prog1learnapp.model.Lesson;
import com.example.prog1learnapp.repository.LessonRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class Lesson4DataInitializer implements LessonDataInitializer {

    private static final Logger log =
            LoggerFactory.getLogger(Lesson4DataInitializer.class);

    private final LessonRepository lessonRepository;
    private final ExerciseSeedService exerciseSeedService;

    public Lesson4DataInitializer(LessonRepository lessonRepository,
                                  ExerciseSeedService exerciseSeedService) {
        this.lessonRepository = lessonRepository;
        this.exerciseSeedService = exerciseSeedService;
    }
    public void init() {

// Lektion 4: Methoden vertiefen
        Lesson lesson4 = lessonRepository.findById(4L)
                .orElseGet(() -> lessonRepository.save(
                        new Lesson(
                                4L,
                                "Methoden vertiefen",
                                "Rekursion, Listenverarbeitung, Algorithmen, Hilfsmethoden",
                                "<h2>Methoden vertiefen</h2>\n" +
                                        "<p>In dieser Lektion vertiefen wir unser Wissen über Methoden mit komplexeren Algorithmen, " +
                                        "Rekursion und der Verarbeitung von Listen und Arrays.</p>\n" +
                                        "\n" +
                                        "<h3>Rekursive Methoden</h3>\n" +
                                        "<ul>\n" +
                                        "    <li>Eine Methode ruft sich selbst auf</li>\n" +
                                        "    <li>Benötigt immer eine <b>Abbruchbedingung</b> (Base Case)</li>\n" +
                                        "    <li>Nützlich für Probleme, die sich in kleinere Teilprobleme zerlegen lassen</li>\n" +
                                        "</ul>\n" +
                                        "\n" +
                                        "<h3>Methoden mit Rückgabewerten</h3>\n" +
                                        "<ul>\n" +
                                        "    <li><code>return</code> gibt einen Wert zurück</li>\n" +
                                        "    <li>Der Rückgabetyp muss in der Methodendeklaration angegeben werden</li>\n" +
                                        "    <li><code>void</code> bedeutet keine Rückgabe</li>\n" +
                                        "</ul>\n" +
                                        "\n" +
                                        "<h3>Listen als Parameter und Rückgabewerte</h3>\n" +
                                        "<pre><code>// Methode mit Liste als Parameter\n" +
                                        "public static double summeBerechnen(ArrayList<Double> liste) {\n" +
                                        "    double summe = 0;\n" +
                                        "    for (double wert : liste) {\n" +
                                        "        summe += wert;\n" +
                                        "    }\n" +
                                        "    return summe;\n" +
                                        "}\n" +
                                        "\n" +
                                        "// Methode mit Liste als Rückgabewert\n" +
                                        "public static ArrayList<Integer> filterGerade(int[] zahlen) {\n" +
                                        "    ArrayList<Integer> gerade = new ArrayList<>();\n" +
                                        "    for (int zahl : zahlen) {\n" +
                                        "        if (zahl % 2 == 0) {\n" +
                                        "            gerade.add(zahl);\n" +
                                        "        }\n" +
                                        "    }\n" +
                                        "    return gerade;\n" +
                                        "}</code></pre>\n" +
                                        "\n" +
                                        "<h3>Algorithmen implementieren</h3>\n" +
                                        "<p>Methoden ermöglichen die Kapselung von Algorithmen:</p>\n" +
                                        "<ul>\n" +
                                        "    <li><b>GGT (größter gemeinsamer Teiler)</b> - Euklidischer Algorithmus</li>\n" +
                                        "    <li><b>Sortieralgorithmen</b> - Insertion Sort, Merge, etc.</li>\n" +
                                        "    <li><b>Histogramm-Erstellung</b> - Daten in Intervalle gruppieren</li>\n" +
                                        "</ul>"
                        )
                ));

        if (!lessonRepository.existsById(4L)) {
            lessonRepository.save(lesson4);
        }

// Aufgabe 1: Fakultät und GGT
        Exercise ex4_1 = new Exercise();
        ex4_1.setTitle("Rekursive Methoden: Fakultät und GGT");
        ex4_1.setDescription(
                "Implementieren Sie zwei Methoden:\n" +
                        "a) berechneFakultaet(int zahl) - berechnet die Fakultät (rekursiv oder iterativ)\n" +
                        "b) berechneGGT(long zahl1, long zahl2) - berechnet den größten gemeinsamen Teiler"
        );
        ex4_1.setStarterCode(
                "public class RekursiveMethoden {\n" +
                        "    public static void main(String[] args) {\n" +
                        "        // Test der Methoden\n" +
                        "        System.out.println(\"Fakultät von 5: \" + berechneFakultaet(5));\n" +
                        "        System.out.println(\"GGT von 56 und 32: \" + berechneGGT(56, 32));\n" +
                        "    }\n" +
                        "    \n" +
                        "    // TODO a): Methode berechneFakultaet\n" +
                        "    // Fakultät von n = n * (n-1) * (n-2) * ... * 1\n" +
                        "    // Fakultät von 0 = 1\n" +
                        "    public static long berechneFakultaet(int zahl) {\n" +
                        "        // Ihre Implementierung\n" +
                        "        return 0;\n" +
                        "    }\n" +
                        "    \n" +
                        "    // TODO b): Methode berechneGGT (größter gemeinsamer Teiler)\n" +
                        "    // Tipp: Euklidischer Algorithmus\n" +
                        "    // solange b != 0:\n" +
                        "    //     temp = b\n" +
                        "    //     b = a % b\n" +
                        "    //     a = temp\n" +
                        "    public static long berechneGGT(long zahl1, long zahl2) {\n" +
                        "        // Ihre Implementierung\n" +
                        "        return 0;\n" +
                        "    }\n" +
                        "}"
        );
        ex4_1.setSolution(
                "public class RekursiveMethoden {\n" +
                        "    public static void main(String[] args) {\n" +
                        "        System.out.println(\"Fakultät von 5: \" + berechneFakultaet(5));\n" +
                        "        System.out.println(\"GGT von 56 und 32: \" + berechneGGT(56, 32));\n" +
                        "    }\n" +
                        "    \n" +
                        "    // Iterative Lösung für Fakultät\n" +
                        "    public static long berechneFakultaet(int zahl) {\n" +
                        "        if (zahl < 0) return -1; // Fehlerfall\n" +
                        "        long ergebnis = 1;\n" +
                        "        for (int i = 2; i <= zahl; i++) {\n" +
                        "            ergebnis *= i;\n" +
                        "        }\n" +
                        "        return ergebnis;\n" +
                        "    }\n" +
                        "    \n" +
                        "    // Alternative: Rekursive Lösung\n" +
                        "    /*\n" +
                        "    public static long berechneFakultaet(int zahl) {\n" +
                        "        if (zahl < 0) return -1;\n" +
                        "        if (zahl == 0) return 1;\n" +
                        "        return zahl * berechneFakultaet(zahl - 1);\n" +
                        "    }\n" +
                        "    */\n" +
                        "    \n" +
                        "    // Euklidischer Algorithmus für GGT\n" +
                        "    public static long berechneGGT(long zahl1, long zahl2) {\n" +
                        "        long a = Math.abs(zahl1);\n" +
                        "        long b = Math.abs(zahl2);\n" +
                        "        \n" +
                        "        while (b != 0) {\n" +
                        "            long temp = b;\n" +
                        "            b = a % b;\n" +
                        "            a = temp;\n" +
                        "        }\n" +
                        "        return a;\n" +
                        "    }\n" +
                        "}"
        );
        ex4_1.setDifficulty("MEDIUM");
        ex4_1.setLesson(lesson4);
        exerciseSeedService.saveExerciseIfNotExists(ex4_1);

// Aufgabe 2: Summe im Intervall
        Exercise ex4_2 = new Exercise();
        ex4_2.setTitle("Summe der Werte in einem Intervall berechnen");
        ex4_2.setDescription(
                "Schreiben Sie eine Methode summeWerteImIntervall, die die Summe der Werte " +
                        "im geschlossenen Intervall [min, max] aus einer ArrayList<Double> berechnet.\n\n" +
                        "Beispiel: Liste: [3.0, 4.0, 1.0, 8.0, 4.0], min: 2.0, max: 4.0\n" +
                        "Ergebnis: 3.0 + 4.0 + 4.0 = 11.0"
        );
        ex4_2.setStarterCode(
                "import java.util.ArrayList;\n" +
                        "\n" +
                        "public class SummeIntervall {\n" +
                        "    public static void main(String[] args) {\n" +
                        "        ArrayList<Double> liste = new ArrayList<>();\n" +
                        "        liste.add(3.0);\n" +
                        "        liste.add(4.0);\n" +
                        "        liste.add(1.0);\n" +
                        "        liste.add(8.0);\n" +
                        "        liste.add(4.0);\n" +
                        "        \n" +
                        "        double summe = summeWerteImIntervall(liste, 2.0, 4.0);\n" +
                        "        System.out.println(\"Summe im Intervall [2.0, 4.0]: \" + summe); // Sollte 11.0 sein\n" +
                        "    }\n" +
                        "    \n" +
                        "    // TODO: Implementieren Sie die Methode summeWerteImIntervall\n" +
                        "    public static double summeWerteImIntervall(ArrayList<Double> liste, double min, double max) {\n" +
                        "        double summe = 0.0;\n" +
                        "        // Ihre Implementierung\n" +
                        "        return summe;\n" +
                        "    }\n" +
                        "}"
        );
        ex4_2.setSolution(
                "import java.util.ArrayList;\n" +
                        "\n" +
                        "public class SummeIntervall {\n" +
                        "    public static void main(String[] args) {\n" +
                        "        ArrayList<Double> liste = new ArrayList<>();\n" +
                        "        liste.add(3.0);\n" +
                        "        liste.add(4.0);\n" +
                        "        liste.add(1.0);\n" +
                        "        liste.add(8.0);\n" +
                        "        liste.add(4.0);\n" +
                        "        \n" +
                        "        double summe = summeWerteImIntervall(liste, 2.0, 4.0);\n" +
                        "        System.out.println(\"Summe im Intervall [2.0, 4.0]: \" + summe);\n" +
                        "    }\n" +
                        "    \n" +
                        "    public static double summeWerteImIntervall(ArrayList<Double> liste, double min, double max) {\n" +
                        "        double summe = 0.0;\n" +
                        "        \n" +
                        "        for (double wert : liste) {\n" +
                        "            // Prüfen, ob Wert im geschlossenen Intervall liegt\n" +
                        "            if (wert >= min && wert <= max) {\n" +
                        "                summe += wert;\n" +
                        "            }\n" +
                        "        }\n" +
                        "        \n" +
                        "        return summe;\n" +
                        "    }\n" +
                        "}"
        );
        ex4_2.setDifficulty("EASY");
        ex4_2.setLesson(lesson4);
        exerciseSeedService.saveExerciseIfNotExists(ex4_2);

// Aufgabe 3: Insertion Sort
        Exercise ex4_3 = new Exercise();
        ex4_3.setTitle("Insertion Sort als Methode implementieren");
        ex4_3.setDescription(
                "Implementieren Sie zwei Varianten des Insertion Sort:\n\n" +
                        "1. sortiere(ArrayList<Integer> liste) - gibt eine neue, sortierte Liste zurück\n" +
                        "2. sortiereInPlace(ArrayList<Integer> liste) - sortiert die übergebene Liste direkt\n\n" +
                        "Was muss getan werden, damit die übergebene Liste selbst sortiert wird?"
        );
        ex4_3.setStarterCode(
                "import java.util.ArrayList;\n" +
                        "import java.util.Arrays;\n" +
                        "\n" +
                        "public class InsertionSort {\n" +
                        "    public static void main(String[] args) {\n" +
                        "        ArrayList<Integer> liste = new ArrayList<>(Arrays.asList(5, 2, 8, 1, 9, 3));\n" +
                        "        \n" +
                        "        System.out.println(\"Original: \" + liste);\n" +
                        "        \n" +
                        "        // Variante 1: Neue sortierte Liste\n" +
                        "        ArrayList<Integer> sortiert = sortiere(liste);\n" +
                        "        System.out.println(\"Sortiert (neue Liste): \" + sortiert);\n" +
                        "        System.out.println(\"Original unverändert: \" + liste);\n" +
                        "        \n" +
                        "        // Variante 2: In-place Sortierung\n" +
                        "        sortiereInPlace(liste);\n" +
                        "        System.out.println(\"Original jetzt sortiert: \" + liste);\n" +
                        "    }\n" +
                        "    \n" +
                        "    // TODO 1: Methode, die eine neue sortierte Liste zurückgibt\n" +
                        "    public static ArrayList<Integer> sortiere(ArrayList<Integer> liste) {\n" +
                        "        ArrayList<Integer> sortierteListe = new ArrayList<>();\n" +
                        "        // Implementieren Sie Insertion Sort\n" +
                        "        return sortierteListe;\n" +
                        "    }\n" +
                        "    \n" +
                        "    // TODO 2: Methode, die die übergebene Liste direkt sortiert\n" +
                        "    public static void sortiereInPlace(ArrayList<Integer> liste) {\n" +
                        "        // Implementieren Sie Insertion Sort in-place\n" +
                        "        // Tipp: Elemente innerhalb derselben Liste verschieben\n" +
                        "    }\n" +
                        "}"
        );
        ex4_3.setSolution(
                "import java.util.ArrayList;\n" +
                        "import java.util.Arrays;\n" +
                        "\n" +
                        "public class InsertionSort {\n" +
                        "    public static void main(String[] args) {\n" +
                        "        ArrayList<Integer> liste = new ArrayList<>(Arrays.asList(5, 2, 8, 1, 9, 3));\n" +
                        "        \n" +
                        "        System.out.println(\"Original: \" + liste);\n" +
                        "        \n" +
                        "        ArrayList<Integer> sortiert = sortiere(liste);\n" +
                        "        System.out.println(\"Sortiert (neue Liste): \" + sortiert);\n" +
                        "        System.out.println(\"Original unverändert: \" + liste);\n" +
                        "        \n" +
                        "        sortiereInPlace(liste);\n" +
                        "        System.out.println(\"Original jetzt sortiert: \" + liste);\n" +
                        "    }\n" +
                        "    \n" +
                        "    // Variante 1: Neue sortierte Liste zurückgeben\n" +
                        "    public static ArrayList<Integer> sortiere(ArrayList<Integer> liste) {\n" +
                        "        ArrayList<Integer> sortierteListe = new ArrayList<>();\n" +
                        "        \n" +
                        "        for (int i = 0; i < liste.size(); i++) {\n" +
                        "            int aktuell = liste.get(i);\n" +
                        "            int pos = 0;\n" +
                        "            \n" +
                        "            // Finde die richtige Position in der neuen Liste\n" +
                        "            while (pos < sortierteListe.size() && sortierteListe.get(pos) < aktuell) {\n" +
                        "                pos++;\n" +
                        "            }\n" +
                        "            sortierteListe.add(pos, aktuell);\n" +
                        "        }\n" +
                        "        \n" +
                        "        return sortierteListe;\n" +
                        "    }\n" +
                        "    \n" +
                        "    // Variante 2: In-place Sortierung\n" +
                        "    public static void sortiereInPlace(ArrayList<Integer> liste) {\n" +
                        "        for (int i = 1; i < liste.size(); i++) {\n" +
                        "            int aktuell = liste.get(i);\n" +
                        "            int j = i - 1;\n" +
                        "            \n" +
                        "            // Verschiebe Elemente nach rechts, um Platz zu schaffen\n" +
                        "            while (j >= 0 && liste.get(j) > aktuell) {\n" +
                        "                liste.set(j + 1, liste.get(j));\n" +
                        "                j--;\n" +
                        "            }\n" +
                        "            liste.set(j + 1, aktuell);\n" +
                        "        }\n" +
                        "        \n" +
                        "        // Erklärung: Bei der In-place-Sortierung wird die ursprüngliche Liste verändert.\n" +
                        "        // Die Methode muss nichts zurückgeben (void), da die Änderungen direkt\n" +
                        "        // am übergebenen Objekt vorgenommen werden.\n" +
                        "    }\n" +
                        "}"
        );
        ex4_3.setDifficulty("MEDIUM");
        ex4_3.setLesson(lesson4);
        exerciseSeedService.saveExerciseIfNotExists(ex4_3);

// Aufgabe 4: Listen zusammenführen (Merge)
        Exercise ex4_4 = new Exercise();
        ex4_4.setTitle("Zwei Listen abwechselnd zusammenführen");
        ex4_4.setDescription(
                "Schreiben Sie eine Methode merge, die zwei Listen zu einer neuen Liste zusammenführt:\n" +
                        "- Erstes Element von liste1, dann erstes Element von liste2\n" +
                        "- Zweites Element von liste1, dann zweites Element von liste2\n" +
                        "- Wenn eine Liste länger ist, werden die restlichen Elemente hinten angehängt\n\n" +
                        "Beispiel: [1,2,3,4,5,6] und [10,9,8,7] → [1,10,2,9,3,8,4,7,5,6]"
        );
        ex4_4.setStarterCode(
                "import java.util.ArrayList;\n" +
                        "import java.util.Arrays;\n" +
                        "\n" +
                        "public class ListenMerge {\n" +
                        "    public static void main(String[] args) {\n" +
                        "        ArrayList<Integer> list1 = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5, 6));\n" +
                        "        ArrayList<Integer> list2 = new ArrayList<>(Arrays.asList(10, 9, 8, 7));\n" +
                        "        \n" +
                        "        ArrayList<Integer> gemergt = merge(list1, list2);\n" +
                        "        System.out.println(\"Gemergte Liste: \" + gemergt);\n" +
                        "        // Erwartet: [1, 10, 2, 9, 3, 8, 4, 7, 5, 6]\n" +
                        "    }\n" +
                        "    \n" +
                        "    public static ArrayList<Integer> merge(ArrayList<Integer> list1, ArrayList<Integer> list2) {\n" +
                        "        ArrayList<Integer> ergebnis = new ArrayList<>();\n" +
                        "        \n" +
                        "        // TODO: Implementieren Sie den Merge-Algorithmus\n" +
                        "        \n" +
                        "        return ergebnis;\n" +
                        "    }\n" +
                        "}"
        );
        ex4_4.setSolution(
                "import java.util.ArrayList;\n" +
                        "import java.util.Arrays;\n" +
                        "\n" +
                        "public class ListenMerge {\n" +
                        "    public static void main(String[] args) {\n" +
                        "        ArrayList<Integer> list1 = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5, 6));\n" +
                        "        ArrayList<Integer> list2 = new ArrayList<>(Arrays.asList(10, 9, 8, 7));\n" +
                        "        \n" +
                        "        ArrayList<Integer> gemergt = merge(list1, list2);\n" +
                        "        System.out.println(\"Gemergte Liste: \" + gemergt);\n" +
                        "    }\n" +
                        "    \n" +
                        "    public static ArrayList<Integer> merge(ArrayList<Integer> list1, ArrayList<Integer> list2) {\n" +
                        "        ArrayList<Integer> ergebnis = new ArrayList<>();\n" +
                        "        \n" +
                        "        int i = 0, j = 0;\n" +
                        "        \n" +
                        "        // Solange beide Listen Elemente haben\n" +
                        "        while (i < list1.size() && j < list2.size()) {\n" +
                        "            ergebnis.add(list1.get(i));\n" +
                        "            ergebnis.add(list2.get(j));\n" +
                        "            i++;\n" +
                        "            j++;\n" +
                        "        }\n" +
                        "        \n" +
                        "        // Restliche Elemente von list1\n" +
                        "        while (i < list1.size()) {\n" +
                        "            ergebnis.add(list1.get(i));\n" +
                        "            i++;\n" +
                        "        }\n" +
                        "        \n" +
                        "        // Restliche Elemente von list2\n" +
                        "        while (j < list2.size()) {\n" +
                        "            ergebnis.add(list2.get(j));\n" +
                        "            j++;\n" +
                        "        }\n" +
                        "        \n" +
                        "        return ergebnis;\n" +
                        "    }\n" +
                        "}"
        );
        ex4_4.setDifficulty("MEDIUM");
        ex4_4.setLesson(lesson4);
        exerciseSeedService.saveExerciseIfNotExists(ex4_4);

// Aufgabe 5: Histogramm erstellen
        Exercise ex4_5 = new Exercise();
        ex4_5.setTitle("Histogramm aus Daten erzeugen");
        ex4_5.setDescription(
                "Implementieren Sie die Methode erzeugeHistogramm:\n" +
                        "- Teilt den Wertebereich der Daten in eine angegebene Anzahl von Intervallen\n" +
                        "- Zählt, wie viele Werte in jedes Intervall fallen\n" +
                        "- Gibt ein Array mit den Häufigkeiten zurück\n\n" +
                        "Dies ist eine Verallgemeinerung von Aufgabenzettel 2/Aufgabe 6."
        );
        ex4_5.setStarterCode(
                "import java.util.Arrays;\n" +
                        "\n" +
                        "public class Histogramm {\n" +
                        "    public static void main(String[] args) {\n" +
                        "        int[] daten = {12, 5, 8, 20, 15, 3, 18, 10, 7, 22, 14, 6, 9, 16, 11};\n" +
                        "        int intervalle = 5;\n" +
                        "        \n" +
                        "        int[] histogramm = erzeugeHistogramm(daten, intervalle);\n" +
                        "        \n" +
                        "        System.out.println(\"Daten: \" + Arrays.toString(daten));\n" +
                        "        System.out.println(\"Histogramm mit \" + intervalle + \" Intervallen:\");\n" +
                        "        System.out.println(Arrays.toString(histogramm));\n" +
                        "    }\n" +
                        "    \n" +
                        "    public static int[] erzeugeHistogramm(int[] daten, int anzahlIntervalle) {\n" +
                        "        // TODO 1: Minimum und Maximum finden\n" +
                        "        \n" +
                        "        // TODO 2: Intervallbreite berechnen\n" +
                        "        \n" +
                        "        // TODO 3: Histogramm-Array erstellen\n" +
                        "        \n" +
                        "        // TODO 4: Daten in Intervalle einordnen und zählen\n" +
                        "        \n" +
                        "        return new int[0]; // Platzhalter\n" +
                        "    }\n" +
                        "}"
        );
        ex4_5.setSolution(
                "import java.util.Arrays;\n" +
                        "\n" +
                        "public class Histogramm {\n" +
                        "    public static void main(String[] args) {\n" +
                        "        int[] daten = {12, 5, 8, 20, 15, 3, 18, 10, 7, 22, 14, 6, 9, 16, 11};\n" +
                        "        int intervalle = 5;\n" +
                        "        \n" +
                        "        int[] histogramm = erzeugeHistogramm(daten, intervalle);\n" +
                        "        \n" +
                        "        System.out.println(\"Daten: \" + Arrays.toString(daten));\n" +
                        "        System.out.println(\"Histogramm mit \" + intervalle + \" Intervallen:\");\n" +
                        "        System.out.println(Arrays.toString(histogramm));\n" +
                        "        \n" +
                        "        // Optional: Ausgabe mit Intervallgrenzen\n" +
                        "        int min = Arrays.stream(daten).min().getAsInt();\n" +
                        "        int max = Arrays.stream(daten).max().getAsInt();\n" +
                        "        double breite = (double)(max - min) / intervalle;\n" +
                        "        \n" +
                        "        System.out.println(\"\\nIntervallgrenzen:\");\n" +
                        "        for (int i = 0; i < intervalle; i++) {\n" +
                        "            double untergrenze = min + i * breite;\n" +
                        "            double obergrenze = min + (i + 1) * breite;\n" +
                        "            System.out.printf(\"Intervall %d: [%.2f, %.2f): %d Werte\\n\", \n" +
                        "                i, untergrenze, obergrenze, histogramm[i]);\n" +
                        "        }\n" +
                        "    }\n" +
                        "    \n" +
                        "    public static int[] erzeugeHistogramm(int[] daten, int anzahlIntervalle) {\n" +
                        "        // 1. Minimum und Maximum finden\n" +
                        "        int min = daten[0];\n" +
                        "        int max = daten[0];\n" +
                        "        \n" +
                        "        for (int wert : daten) {\n" +
                        "            if (wert < min) min = wert;\n" +
                        "            if (wert > max) max = wert;\n" +
                        "        }\n" +
                        "        \n" +
                        "        // 2. Intervallbreite berechnen\n" +
                        "        double bereich = max - min;\n" +
                        "        double intervallBreite = bereich / anzahlIntervalle;\n" +
                        "        \n" +
                        "        // 3. Histogramm-Array erstellen\n" +
                        "        int[] histogramm = new int[anzahlIntervalle];\n" +
                        "        \n" +
                        "        // 4. Daten zählen\n" +
                        "        for (int wert : daten) {\n" +
                        "            // Index des Intervalls berechnen\n" +
                        "            int index = (int)((wert - min) / intervallBreite);\n" +
                        "            \n" +
                        "            // Sonderfall: Wert entspricht genau dem Maximum\n" +
                        "            if (index == anzahlIntervalle) {\n" +
                        "                index = anzahlIntervalle - 1;\n" +
                        "            }\n" +
                        "            \n" +
                        "            histogramm[index]++;\n" +
                        "        }\n" +
                        "        \n" +
                        "        return histogramm;\n" +
                        "    }\n" +
                        "}"
        );
        ex4_5.setDifficulty("HARD");
        ex4_5.setLesson(lesson4);
        exerciseSeedService.saveExerciseIfNotExists(ex4_5);

// Aufgabe 6: String split implementieren
        Exercise ex4_6 = new Exercise();
        ex4_6.setTitle("String in Teilstrings aufteilen (split-Methode)");
        ex4_6.setDescription(
                "Implementieren Sie eine split-Methode, die einen String anhand eines Trennzeichens " +
                        "in mehrere Teilstrings aufteilt.\n\n" +
                        "Beispiel: split(\"1234;Hans Hansen;6\", ';') → {\"1234\", \"Hans Hansen\", \"6\"}"
        );
        ex4_6.setStarterCode(
                "import java.util.ArrayList;\n" +
                        "\n" +
                        "public class StringSplit {\n" +
                        "    public static void main(String[] args) {\n" +
                        "        String test = \"1234;Hans Hansen;6\";\n" +
                        "        char trennzeichen = ';';\n" +
                        "        \n" +
                        "        String[] teile = split(test, trennzeichen);\n" +
                        "        \n" +
                        "        System.out.println(\"Original: \" + test);\n" +
                        "        System.out.println(\"Geteilte Teile:\");\n" +
                        "        for (int i = 0; i < teile.length; i++) {\n" +
                        "            System.out.println(\"  [\" + i + \"]: '\" + teile[i] + \"'\");\n" +
                        "        }\n" +
                        "    }\n" +
                        "    \n" +
                        "    public static String[] split(String zeile, char trennzeichen) {\n" +
                        "        // TODO 1: Teile in einer ArrayList sammeln\n" +
                        "        \n" +
                        "        // TODO 2: Durch den String iterieren und bei Trennzeichen teilen\n" +
                        "        \n" +
                        "        // TODO 3: ArrayList in Array umwandeln\n" +
                        "        \n" +
                        "        return new String[0]; // Platzhalter\n" +
                        "    }\n" +
                        "}"
        );
        ex4_6.setSolution(
                "import java.util.ArrayList;\n" +
                        "\n" +
                        "public class StringSplit {\n" +
                        "    public static void main(String[] args) {\n" +
                        "        String test = \"1234;Hans Hansen;6\";\n" +
                        "        char trennzeichen = ';';\n" +
                        "        \n" +
                        "        String[] teile = split(test, trennzeichen);\n" +
                        "        \n" +
                        "        System.out.println(\"Original: \" + test);\n" +
                        "        System.out.println(\"Geteilte Teile:\");\n" +
                        "        for (int i = 0; i < teile.length; i++) {\n" +
                        "            System.out.println(\"  [\" + i + \"]: '\" + teile[i] + \"'\");\n" +
                        "        }\n" +
                        "        \n" +
                        "        // Weitere Tests\n" +
                        "        System.out.println(\"\\nWeitere Tests:\");\n" +
                        "        System.out.println(Arrays.toString(split(\"a,b,c,d\", ',')));\n" +
                        "        System.out.println(Arrays.toString(split(\"eins\", ','))); // Nur ein Teil\n" +
                        "        System.out.println(Arrays.toString(split(\";a;b;\", ';'))); // Leere Teile\n" +
                        "    }\n" +
                        "    \n" +
                        "    public static String[] split(String zeile, char trennzeichen) {\n" +
                        "        ArrayList<String> teileListe = new ArrayList<>();\n" +
                        "        \n" +
                        "        int start = 0;\n" +
                        "        \n" +
                        "        for (int i = 0; i < zeile.length(); i++) {\n" +
                        "            if (zeile.charAt(i) == trennzeichen) {\n" +
                        "                // Teil von start bis i (exklusiv) extrahieren\n" +
                        "                teileListe.add(zeile.substring(start, i));\n" +
                        "                start = i + 1; // Nächster Teil startet nach dem Trennzeichen\n" +
                        "            }\n" +
                        "        }\n" +
                        "        \n" +
                        "        // Letzten Teil hinzufügen (nach dem letzten Trennzeichen)\n" +
                        "        teileListe.add(zeile.substring(start));\n" +
                        "        \n" +
                        "        // ArrayList in Array umwandeln\n" +
                        "        String[] ergebnis = new String[teileListe.size()];\n" +
                        "        return teileListe.toArray(ergebnis);\n" +
                        "    }\n" +
                        "}"
        );
        ex4_6.setDifficulty("MEDIUM");
        ex4_6.setLesson(lesson4);
        exerciseSeedService.saveExerciseIfNotExists(ex4_6);

    }


}
