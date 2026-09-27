<p align="center">
  <img src="docs/images/logo.png" alt="BlockDesigner logo" width="128" height="128">
</p>

<h1 align="center">Palette Tools</h1>

<p align="center">
  Block palette tools for BlockDesigner: a Palette tool that repaints the selection with a live preview<br>
  (palette swap, weathering, gradients), preset gradients, a Palette panel, a colour palette exporter and a pixel art importer.
</p>

<p align="center">
  <a href="https://github.com/doolecg/BlockDesigner-PaletteTools/releases/latest"><img alt="Latest release" src="https://img.shields.io/github/v/release/doolecg/BlockDesigner-PaletteTools?label=release"></a>
  <a href="https://github.com/doolecg/BlockDesigner-PaletteTools/releases"><img alt="Downloads" src="https://img.shields.io/github/downloads/doolecg/BlockDesigner-PaletteTools/total"></a>
  <a href="LICENSE"><img alt="License: MIT" src="https://img.shields.io/github/license/doolecg/BlockDesigner-PaletteTools"></a>
  <img alt="Platform: Windows" src="https://img.shields.io/badge/platform-Windows-0078D6">
  <a href="https://github.com/doolecg/BlockDesigner"><img alt="BlockDesigner plugin API 6" src="https://img.shields.io/badge/BlockDesigner-plugin%20API%206-46C46E"></a>
</p>

---

Palette Tools is a plugin for [BlockDesigner](https://github.com/doolecg/BlockDesigner), the Windows editor for Minecraft builds. It is released
on its own, separately from the app. It needs **BlockDesigner 0.4.24 or later** (plugin API 6).

**Contents:** [Download](#download-and-install) · [Features](#features) · [Building from source](#building-from-source) · [Project layout](#project-layout)

## Download and install

Get the latest version from the [releases page](https://github.com/doolecg/BlockDesigner-PaletteTools/releases/latest):

1. Download `palette-tools-<version>.jar`.
2. In BlockDesigner open **Plugins (puzzle icon) › Manage plugins… › Install…** and pick the jar.

It is on straight away. You can switch it off, reload or uninstall it in the same window. From 1.0.2 on it **updates itself** in BlockDesigner 0.4.16 and later (Plugins › Manage plugins… › Update plugins automatically). Plugins run with the same
access as BlockDesigner itself, so only install ones you trust.

## Features

### Palette tool

- **Palette tool** (Shift+P, or any key you give it in Settings › Keybinds): repaints the selection in the 3D view.
  - **Select right in the tool:** the left button selects as in Select mode (click, drag a box, Shift adds, Ctrl
    removes), or bring a selection from Select mode.
  - **Choose what to do** in its options, bottom left: **Palette swap**, **Weathering** or **Gradient**. Only the
    chosen mode's options show, each with a line of help, and blocks are picked in small hotbar-style slots (click one
    to use the held block, drop a block on it, right-click to take it out of a mix, the wheel to change its share).
  - **Live preview:** the result shows as ghosts and follows every change. **Right-click or Enter** applies it as one
    undo step, **R** rolls new random picks, **Esc** hides the preview.

### Transforms

- **Transforms** with the same three (Plugins menu), each in a window with a live preview:
  - **Weathering** turns a share of stone blocks cracked or mossy (stairs, slabs and walls too).
  - **Palette swap** swaps one material for another (oak → spruce turns planks, stairs, slabs, fences and doors),
    keeping each block's facing and shape.
  - **Gradient** repaints from the first block to the last along an axis or out from the middle, with a random blend,
    an even dither pattern or hard bands, once or repeated (mirrored for stripes).

### Pages

Its tab on the right has two pages:

- **Palette:** every kind of block in the selection (or every visible layer) with its icon and a count, most used
  first, with a filter on top. What it counts is at the bottom.
- **Gradients:** your own gradients and the built-in ones, each a strip of up to nine blocks. Click a strip to paint
  with it in the Palette tool, **Hotbar** puts its blocks in the hotbar, **+** saves your hotbar as a gradient, and the
  bin deletes one of yours (it asks first).

### Import and export

- **Colour palette exporter:** writes the build's blocks as a GIMP / Krita / Inkscape palette (`.gpl`).
- **Simple pixel art importer:** turns a picture into blocks, matching each pixel to wool, concrete or terracotta,
  upright or lying flat. With Pixel Art Generator also installed, Import asks which of the two to use.

## Building from source

You need Windows and a JDK 26 (Temurin 26 is what BlockDesigner uses; set `org.gradle.java.home` in
`gradle.properties` to yours). Then:

```
./gradlew jar      # build/libs/palette-tools-<version>.jar
```

The plugin compiles against the BlockDesigner plugin API jars in [`libs/`](libs) (from BlockDesigner 0.4.24). The app
provides them, and JavaFX, at runtime, so they are never bundled into the plugin. To target a newer API, replace them
with the jars from a newer BlockDesigner build (`./gradlew :plugin-api:jar :core:jar` in the
[BlockDesigner repository](https://github.com/doolecg/BlockDesigner)) and update the file names in `build.gradle.kts`.

The version is set in `build.gradle.kts` and copied into the jar's `blockdesigner-plugin.json`. To release a new
version, change it there, add a section to [RELEASE_NOTES.md](RELEASE_NOTES.md), build the jar and attach it to a
GitHub release tagged with the version.

For writing plugins, see BlockDesigner's [plugin guide](https://github.com/doolecg/BlockDesigner/blob/main/PLUGINS.md) and
[API reference](https://github.com/doolecg/BlockDesigner/blob/main/docs/plugin-api-reference.md).

### Tests

```
./gradlew test
```

The tests cover the plugin's logic that runs without the app, against the API jars in `libs/`.

## Project layout

| Path | What it does |
|---|---|
| `src/main/java` | The plugin's code |
| `src/main/resources/blockdesigner-plugin.json` | The manifest BlockDesigner reads: id, name, version, main class, API level |
| `src/test/java` | Tests (where the plugin has logic that can be tested without the app) |
| `libs/` | The BlockDesigner plugin API jars it compiles against |

## License

[MIT](LICENSE)
