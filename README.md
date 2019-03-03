# Gui4YtDl

Gui4YtDl aims to provide a graphical user interface for the commandline only [youtube-dl](https://rg3.github.io/youtube-dl/) which downloads videos from hundreds of different sites.

![Screenshot of main GUI](../../raw/gh-page/screenshot.png)

## Technical
Instead of integrating (a potentially old version of) youtube-dl which other GUI implementations did, Gui4YtDl calls `youtube-dl` directly and it is therefore possible to update it as long as the output format does not change. The program is built with Java 8 and JavaFX and therefore requires both to work. Examples for compatible runtimes is the old Oracle Java 8 distribution and [ZuluFX](https://www.azul.com/downloads/zulu/zulufx/), but not the one from [adoptopenjdk](https://adoptopenjdk.net/) because it does not include JavaFX.

