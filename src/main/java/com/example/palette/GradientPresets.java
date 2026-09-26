package com.example.palette;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.blockdesigner.core.model.BlockState;
import io.blockdesigner.plugin.PluginContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** The gradients the Gradients panel offers: built-in ones, and your own kept as JSON in the plugin's data folder. */
final class GradientPresets {
    private GradientPresets() {
    }

    static final int MAX = 9;

    /** A named gradient of up to {@link #MAX} blocks, first to last. */
    record Preset(String name, List<BlockState> blocks, boolean own) {
        Preset {
            if (blocks.isEmpty() || blocks.size() > MAX) throw new IllegalArgumentException("A preset has 1 to " + MAX + " blocks");
            blocks = List.copyOf(blocks);
        }
    }

    static final List<Preset> BUILT_IN = List.of(
            preset("Deepslate to stone", "deepslate_tiles", "deepslate_bricks", "polished_deepslate", "cobbled_deepslate", "tuff",
                    "stone_bricks", "stone", "andesite", "polished_andesite"),
            preset("Snowy peak", "stone", "andesite", "diorite", "calcite", "snow_block"),
            preset("Mossy ruin", "mossy_cobblestone", "mossy_stone_bricks", "cracked_stone_bricks", "stone_bricks", "chiseled_stone_bricks"),
            preset("Sandstone", "smooth_red_sandstone", "red_sandstone", "cut_red_sandstone", "cut_sandstone", "sandstone", "smooth_sandstone"),
            preset("Wood, dark to light", "dark_oak_planks", "spruce_planks", "jungle_planks", "oak_planks", "birch_planks"),
            preset("Nether", "blackstone", "polished_blackstone_bricks", "nether_bricks", "red_nether_bricks", "crimson_planks", "netherrack"),
            preset("Copper ageing", "copper_block", "exposed_copper", "weathered_copper", "oxidized_copper"),
            preset("Sunset", "purple_concrete", "magenta_concrete", "pink_concrete", "red_concrete", "orange_concrete", "yellow_concrete"),
            preset("Ocean", "blue_concrete", "cyan_concrete", "light_blue_concrete", "prismarine", "prismarine_bricks", "white_concrete"),
            preset("Rainbow", "red_wool", "orange_wool", "yellow_wool", "lime_wool", "green_wool", "cyan_wool", "light_blue_wool",
                    "blue_wool", "purple_wool"),
            preset("Greys", "black_concrete", "gray_concrete", "light_gray_concrete", "white_concrete"));

    private static final ObjectMapper JSON = new ObjectMapper();

    private static Preset preset(String name, String... ids) {
        return new Preset(name, Arrays.stream(ids).map(BlockState::of).toList(), false);
    }

    static Path file(PluginContext ctx) {
        return ctx.dataFolder().resolve("gradients.json");
    }

    /** Your own presets from {@code file}; empty when there is none or it can't be read (logged against {@code ctx}). */
    static List<Preset> read(Path file, PluginContext ctx) {
        if (!Files.isRegularFile(file)) return List.of();
        try {
            List<Preset> out = new ArrayList<>();
            for (JsonNode n : JSON.readTree(file.toFile())) {
                List<BlockState> blocks = new ArrayList<>();
                for (JsonNode b : n.path("blocks")) blocks.add(BlockState.parse(b.asText()));
                if (!blocks.isEmpty() && blocks.size() <= MAX) out.add(new Preset(n.path("name").asText("Gradient"), blocks, true));
            }
            return out;
        } catch (IOException | RuntimeException e) {
            if (ctx != null) ctx.log("Could not read " + file + ": " + e.getMessage());
            return List.of();
        }
    }

    static void write(Path file, List<Preset> own) throws IOException {
        ArrayNode arr = JSON.createArrayNode();
        for (Preset p : own) {
            ObjectNode o = arr.addObject();
            o.put("name", p.name());
            ArrayNode b = o.putArray("blocks");
            for (BlockState st : p.blocks()) b.add(st.toString());
        }
        Files.createDirectories(file.getParent());
        JSON.writerWithDefaultPrettyPrinter().writeValue(file.toFile(), arr);
    }
}
