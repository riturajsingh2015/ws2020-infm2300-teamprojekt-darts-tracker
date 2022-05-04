# INFM2300 - Teamprojekt WS2020 - Darts Tracker

# DartsTracker

Dieses Projekt beschäftigt sich mit der Analyse von Videodaten zum Thema "Dart-Spielen". Es geht dabei darum das Zielfeld, die Darts und entsprechende Treffer im Feld zu erkennen und Feedback darüber zu geben.

# Ideensammlung
## Funktionale Anforderungen
- Oberfläche(Andriod - App)
    - Schritt 1: Einfache Oberfläche mit Dart-Scheiben Icon + Start / Stopp - Knopf
    - Schritt 2: Rückgabe der Analyseergebnisse (Markierungen in dem Icon)

- Bilderanalyse
    - Erkennung des Feldes
        - Schritt 1: "einfachere" direkte Kamerastellung
        - Schritt 2: Distanzaufnahme
        - Schritt 3: beliebige Kameraposition
    - Erkennung möglicher Pfeile
        - Schritt 1: Auswertung eines Punktes auf der Bord
        - Schritt 2: Auswertung eines Pfeiles auf dem Bord
    - Erkennung Anzahl der Pfeile:
        - Schritt 1: Erkennung 1x Pfeil
        - Schritt 2: Erkennung beliebig vieler Pfeile
        - Schritt 3: Erkennung unterschiedlicher Pfeile
    - Trefferauswertung:
        - Schritt 1: Klassifizierung von Treffern (gelb, rot, blau, schwarz)
        - Schritt 2: Clusterung/Gruppierung der Treffer (im Bsp.-Bild sind alle Treffer in einem Punktefeld)
    - Ausschluss bereits vorhandener Treffer
        - Schritt 1: "Alte Treffer" werden nicht berücksichtig
        - Schritt 2: Doppeltreffer sind zu vernachlässigen
- Livebetrieb

## Nicht-Funktionale Anforderungen
- Android Studio
- Android Version >6.0
- Java + Gradle
- OpenCV
- GitLab

## Kurzbeschreibung Aufgaben:
-> Dartscheibe Icon + Knöpfe
-> Kalibrieren-Knopf - Knopf soll die Scheibe entdecken und erfassen
-> Start - Spiel und Erfassung für Pfeile startet
-> Fullscreen Applikation

Aufgabe 1: (Ricardo)
Erstellung eines Prototypen (App) mit den notwendigstens Oberflächenfunktionen der App.
Erstellung eines Kalibrierungsbuttons für das Erkennen und Einrichten des Dartboards.

Aufgabe 2: (Andreas)
Die Erstellung eines Testdatensatzes (Testbilder) während eines anfänglichen Testaufbaus ist durchzuführen.

Aufgabe 3: (Rituraj)
Es muss die Biblithek OpenCV über Gradle in eine laufendes Programm integriert werden.
Darüber hinaus sollten grundlegenden Funktionalitäten/Analysefunktionen in einem Testprogramm anhand von Beispiel - Dart - Bildern getestet werden.

Aufgabe 4 (optional):
Es muss die Biblithek OpenCV über Gradle in eine laufendes Programm integriert werden.
Entwicklung eines Algorithmus für die Erkennung von beliebigen Objekten in einem Beispielbild.

### Build configuration (`.gitlab-ci.yml`)

The sample project also contains a basic `.gitlab-ci.yml` which will successfully
build the Android application.

Note that for publishing to the test channels or production, you'll need to set
up your secret API key. The stub code is here for that, but please see our
[blog post](https://about.gitlab.com/2019/01/28/android-publishing-with-gitlab-and-fastlane/) for
details on how to set this up completely. In the meantime, publishing steps will fail.

The build script also handles automatic versioning by relying on the CI pipeline
ID to generate a unique, ever increasing number. If you have a different versioning
scheme you may want to change this.

```yaml
    - "export VERSION_CODE=$(($CI_PIPELINE_IID)) && echo $VERSION_CODE"
    - "export VERSION_SHA=`echo ${CI_COMMIT_SHA:0:8}` && echo $VERSION_SHA"
```

### Fastlane files

It also has fastlane setup per our [blog post](https://about.gitlab.com/2019/01/28/android-publishing-with-gitlab-and-fastlane/) on
getting GitLab CI set up with fastlane. Note that you may want to update your
fastlane bundle to the latest version; if a newer version is available, the pipeline
job output will tell you.

### Dockerfile build environment

In the root there is a Dockerfile which defines a build environment which will be
used to ensure consistent and reliable builds of your Android application using
the correct Android SDK and other details you expect. Feel free to add any
build-time tools or whatever else you need here.

We generate this environment as needed because installing the Android SDK
for every pipeline run would be very slow.

### Reference links

- [GitLab CI Documentation](https://docs.gitlab.com/ee/ci/)
- [Blog post: Android publishing with GitLab and fastlane](https://ab