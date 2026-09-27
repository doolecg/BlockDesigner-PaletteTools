# Palette Tools 1.1.3

Kept up to date with BlockDesigner 0.4.23: built and tested against its plugin API. Nothing changes in how it works.

**Needs BlockDesigner 0.4.17 or later** (plugin API 5). BlockDesigner 0.4.16 and later update to it by themselves.

## Changed
- Built against the BlockDesigner 0.4.23 plugin API.

---

# Palette Tools 1.1.2

Kept up to date with BlockDesigner 0.4.22: built and tested against its plugin API. Nothing changes in how it works.

**Needs BlockDesigner 0.4.17 or later** (plugin API 5). BlockDesigner 0.4.16 and later update to it by themselves.

## Changed
- Built against the BlockDesigner 0.4.22 plugin API.

---

# Palette Tools 1.1.1

Kept up to date with BlockDesigner 0.4.18: built and tested against its plugin API. Nothing changes in how it works.

**Needs BlockDesigner 0.4.17 or later** (plugin API 5). BlockDesigner 0.4.16 and later update to it by themselves.

## Changed
- Built against the BlockDesigner 0.4.18 plugin API.
- README in the same format as BlockDesigner's.

---

# Palette Tools 1.1.0

A **Palette tool** that repaints the selection right in the 3D view, preset gradients, and more ways to make a gradient. The Wall tool is gone: BlockDesigner's own Build-mode shapes do walls.

**Needs BlockDesigner 0.4.17 or later** (plugin API 5). BlockDesigner 0.4.16 and later update to it automatically once BlockDesigner itself is on 0.4.17; older ones keep 1.0.2.

## New
- **Palette tool** (Shift+P):
  - Select blocks right in the tool with the left button (click, drag a box, Shift adds, Ctrl removes), or bring a selection from Select mode.
  - Pick **Palette swap**, **Weathering** or **Gradient** in its options (bottom left); only that mode's options show.
  - The result shows as ghosts straight away and follows every change. **Right-click or Enter** applies it as one undo step, **R** rolls new random picks, **Esc** hides the preview.
- **Gradients panel:** preset gradients shown like hotbars of up to nine blocks (Deepslate to stone, Snowy peak, Mossy ruin, Sandstone, Wood, Nether, Copper ageing, Sunset, Ocean, Rainbow, Greys).
  - Click one to paint with it in the Palette tool.
  - **Hotbar** puts its blocks in the hotbar.
  - **Save the hotbar as a gradient** keeps your own; ✕ deletes one.
- **More gradient options:**
  - **Along:** out from the middle, in to the middle, or out from the middle flat, as well as up, down and the four sides.
  - **Edges:** a random blend, an even dither pattern, or hard bands.
  - **Repeats**, optionally mirrored (1 2 3 2 1), for stripes.

## Changed
- Blocks in the options are small hotbar-style slots with the block's icon. Click one for the held block, drop one from the block list, right-click to remove it from a mix, and use the wheel to change its share. The pencil still edits them as text.
- Built against the BlockDesigner 0.4.17 plugin API.

## Removed
- **Wall tool.** Use Build mode's Wall and Walls shapes instead (hold Alt).

---

# Palette Tools 1.0.2

Palette Tools now updates itself.

**Needs BlockDesigner 0.4.4 or later** (plugin API 2); automatic updates need BlockDesigner 0.4.16 or later. Install this version once by hand: download `palette-tools-1.0.2.jar` below, then in BlockDesigner open **Plugins › Manage plugins… › Install…** and pick it (it replaces the older version).

## New
- **Updates itself.** Its manifest now links this repository as its release source, so BlockDesigner 0.4.16 and later install new releases of it automatically.

## Changed
- Built against the BlockDesigner 0.4.16 plugin API.

---

# Palette Tools 1.0.1

Kept up to date with BlockDesigner 0.4.15: built and tested against its plugin API. Nothing changes in how it works.

**Needs BlockDesigner 0.4.4 or later** (plugin API 2); BlockDesigner 0.4.15 is recommended. Install: download `palette-tools-1.0.1.jar` below, then in BlockDesigner open **Plugins › Manage plugins… › Install…** and pick it (it replaces the older version).

## Changed
- Built against the BlockDesigner 0.4.15 plugin API.
- In BlockDesigner 0.4.14 and later it has its own tab on the right: it shows the plugin is running and has buttons for everything it adds.

---

# Palette Tools 1.0.0

The first release of Palette Tools on its own.

**Needs BlockDesigner 0.4.4 or later** (plugin API 2). Install: download `palette-tools-1.0.0.jar` below, then in BlockDesigner open **Plugins › Manage plugins… › Install…** and pick it.

## New
- Weathering, palette swap and gradient transforms with a live preview.
- A Palette panel, a GIMP colour palette exporter, a pixel art importer and a Wall tool.

---
