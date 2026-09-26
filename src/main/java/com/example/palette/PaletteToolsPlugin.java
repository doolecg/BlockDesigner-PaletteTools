package com.example.palette;

import io.blockdesigner.plugin.BlockDesignerPlugin;
import io.blockdesigner.plugin.PluginContext;

/**
 * Registers the transforms (also usable in the view through the Palette tool), a panel, an exporter with options, an
 * importer and the Palette tool. Everything is removed again automatically when the plugin is disabled.
 */
public final class PaletteToolsPlugin implements BlockDesignerPlugin {

    @Override
    public void enable(PluginContext ctx) {
        PaletteSwapTransform swap = new PaletteSwapTransform();
        WeatheringTransform weathering = new WeatheringTransform();
        GradientTransform gradient = new GradientTransform();
        ctx.registerTransform(weathering);
        ctx.registerTransform(swap);
        ctx.registerTransform(gradient);
        ctx.registerPanel(new PalettePanel());
        ctx.registerPanel(new GradientsPanel("palette"));
        ctx.registerExporter(new GimpPaletteExporter(ctx.blocks()));
        ctx.registerImporter(new PixelArtImporter());
        ctx.registerTool(new PaletteTool(swap, weathering, gradient));
    }
}
