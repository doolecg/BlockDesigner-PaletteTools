<p align="center">
  <img src="docs/images/logo.png" alt="BlockDesigner logo" width="128" height="128">
</p>

<h1 align="center">Palette Tools</h1>

<p align="center">
  Block palette tools for BlockDesigner: weathering, palette swap and gradients with a live preview,<br>
  a Palette panel, a colour palette exporter, a pixel art importer and a Wall tool.
</p>

<p align="center">
  <a href="https://github.com/doolecg/BlockDesigner-PaletteTools/releases/latest"><img alt="Latest release" src="https://img.shields.io/github/v/release/doolecg/BlockDesigner-PaletteTools?label=release"></a>
  <a href="https://github.com/doolecg/BlockDesigner-PaletteTools/releases"><img alt="Downloads" src="https://img.shields.io/github/downloads/doolecg/BlockDesigner-PaletteTools/total"></a>
  <a href="LICENSE"><img alt="License: MIT" src="https://img.shields.io/github/license/doolecg/BlockDesigner-PaletteTools"></a>
  <a href="https://github.com/doolecg/BlockDesigner"><img alt="BlockDesigner plugin API 2" src="https://img.shields.io/badge/BlockDesigner-plugin%20API%202-46C46E"></a>
</p>

---

**Palette Tools** is a plugin for [BlockDesigner](https://github.com/doolecg/BlockDesigner), the Windows editor for Minecraft builds. It is released
on its own, separately from the app. It needs **BlockDesigner 0.4.4 or later** (plugin API 2).

**Contents:** [Download](#download-and-install) · [Features](#features) · [Building from source](#building-from-source) · [Project layout](#project-layout)

## Download and install

1. Download `palette-tools-<version>.jar` from the [releases page](https://github.com/doolecg/BlockDesigner-PaletteTools/releases/latest).
2. In BlockDesigner open **Plugins (puzzle icon) › Manage plugins… › Install…** and pick the jar.

It is on straight away. You can switch it off, reload or uninstall it in the same window. From 1.0.2 on it **updates itself** in BlockDesigner 0.4.16 and later (Plugins › Manage plugins… › Update plugins automatically). Plugins run with the same
access as BlockDesigner itself, so only install ones you trust.

## Features

- **Transforms with a live preview** (Plugins menu), each shown as a ghost before you apply it:
  - **Weathering** turns a share of stone blocks cracked or mossy (stairs, slabs and walls too).
  - **Palette swap** swaps one material for another (oak → spruce turns planks, stairs, slabs, fences and doors),
    keeping each block's facing and shape.
  - **Gradient** repaints from one end to the other through a list of blocks, blended so the bands don't show.
- **Palette panel:** the blocks of the selection (or every visible layer) with a colour swatch and a count, most used
  first.
- **Colour palette exporter:** writes the build's blocks as a GIMP / Krita / Inkscape palette (`.gpl`).
- **Pixel art importer:** turns a picture into blocks, matching each pixel to wool, concrete or terracotta, upright or
  lying flat.
- **Wall tool:** drag out a straight wall from a block mix (70% stone bricks, 30% mossy by default) or the held block;
  the wheel changes its height.

It is also the worked example for plugin API 2: transforms, panels, tools, importers, options and scene events.

## Building from source

You need Windows and a JDK 26 (Temurin 26 is what BlockDesigner uses; set `org.gradle.java.home` in
`gradle.properties` to yours). Then:

```
./gradlew jar      # build/libs/palette-tools-<version>.jar
./gradlew test     # run the tests
```

The plugin compiles against the BlockDesigner plugin API jars in [`libs/`](libs) (from BlockDesigner 0.4.16). The app
provides them, and JavaFX, at runtime, so they are never bundled into the plugin. To target a newer API, replace them
with the jars from a newer BlockDesigner build (`./gradlew :plugin-api:jar :core:jar` in the
[BlockDesigner repository](https://github.com/doolecg/BlockDesigner)) and update the file names in `build.gradle.kts`.

The version is set in `build.gradle.kts` and copied into the jar's `blockdesigner-plugin.json`. To release a new
version, change it there, add a section to [RELEASE_NOTES.md](RELEASE_NOTES.md), build the jar and attach it to a
GitHub release tagged with the version.

For writing plugins, see BlockDesigner's [plugin guide](https://github.com/doolecg/BlockDesigner/blob/main/PLUGINS.md) and
[API reference](https://github.com/doolecg/BlockDesigner/blob/main/docs/plugin-api-reference.md).

## Project layout

| Path | What it is |
|---|---|
| `src/main/java` | The plugin's code |
| `src/main/resources/blockdesigner-plugin.json` | The manifest BlockDesigner reads: id, name, version, main class, API level |
| `src/test/java` | Tests (where the plugin has logic that can be tested without the app) |
| `libs/` | The BlockDesigner plugin API jars it compiles against |

## License

[MIT](LICENSE).
