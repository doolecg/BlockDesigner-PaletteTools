package com.example.palette;

import io.blockdesigner.core.model.BlockPos;
import io.blockdesigner.core.model.BlockState;
import io.blockdesigner.core.model.Box;
import io.blockdesigner.plugin.Options;
import io.blockdesigner.plugin.PluginTransform;
import io.blockdesigner.plugin.TransformContext;

import java.util.List;

/**
 * Repaints the blocks with a list of blocks from one end to the other (dark stone at the bottom of a wall fading into
 * light stone at the top) or out from the middle, with a random blend, an even dither pattern or hard bands between
 * them, once or repeated (optionally mirrored, for stripes). Keeps each block's shape: a stair becomes a
 * stair of the new material when there is one.
 */
final class GradientTransform implements PluginTransform {

    @Override
    public String id() {
        return "gradient";
    }

    @Override
    public String name() {
        return "Gradient";
    }

    @Override
    public String description() {
        return "Fades from the first block to the last along an axis or out from the middle, blended, dithered or in hard bands, once or repeated.";
    }

    @Override
    public String icon() {
        return "M2 2 H14 V14 H2 Z M2 6 H14 M2 10 H14 M5 10 L5 14 M9 6 V10 M11 10 V14";
    }

    static final String BLEND = "Random blend", DITHER = "Dither pattern", BANDS = "Hard bands";
    static final String OUT = "out from the middle", IN = "in to the middle", OUT_FLAT = "out from the middle, flat";

    @Override
    public Options options() {
        return Options.builder()
                .blockList("blocks", "Blocks, first to last", List.of(BlockState.of("deepslate_bricks"), BlockState.of("stone_bricks"),
                        BlockState.of("andesite"), BlockState.of("diorite")))
                .choice("axis", "Along", List.of("up", "down", "east", "west", "south", "north", OUT, IN, OUT_FLAT), "up")
                .choice("style", "Edges", List.of(BLEND, DITHER, BANDS), BLEND)
                .help("Random blend mixes neighbouring blocks at random, Dither pattern in a regular pattern, Hard bands keeps sharp edges.")
                .decimal("blend", "Blend", 0.5, 0, 1).unit("%")
                .showWhen("style", BLEND, DITHER)
                .help("How far each block reaches into its neighbours.")
                .integer("repeat", "Repeats", 1, 1, 16)
                .toggle("mirror", "Mirror the repeats", false)
                .help("Runs back and forth (1 2 3 2 1) instead of starting over.")
                .build();
    }

    @Override
    public boolean randomized() {
        return true;
    }

    @Override
    public void apply(TransformContext c) {
        List<BlockState> blocks = c.options().blockList("blocks").blocks();
        String axis = c.options().choice("axis"), style = c.options().choice("style");
        double blend = style.equals(BANDS) ? 0 : c.options().decimal("blend");
        int repeat = c.options().integer("repeat");
        boolean mirror = c.options().toggle("mirror");
        Box b = c.bounds();
        for (BlockPos p : c.solidBlocks()) {
            // Drawn for every block whatever the style, so each block's result depends only on the seed.
            double r = c.random().nextDouble();
            double t = repeated(position(axis, p, b), repeat, mirror);
            // Jitter by up to half a band either way, so neighbouring bands mix: at random, or in a fixed dot pattern.
            double jitter = style.equals(DITHER) ? dither(axis, p) : r;
            double band = t * blocks.size() + (jitter - 0.5) * blend;
            BlockState pick = blocks.get(Math.clamp((int) Math.floor(band), 0, blocks.size() - 1));
            BlockState st = c.world().get(p);
            // Keep the shape: a stair stays a stair of the new material if the game has one, else it becomes the full block.
            BlockState out = c.blocks().family(pick).flatMap(f -> c.blocks().sameShape(st, f)).orElse(pick);
            c.world().set(p, out);
        }
    }

    /** Where {@code p} is along the gradient, 0 (first block) to 1 (last). */
    static double position(String axis, BlockPos p, Box b) {
        return switch (axis) {
            case "up" -> fraction(p.y(), b.minY(), b.maxY());
            case "down" -> 1 - fraction(p.y(), b.minY(), b.maxY());
            case "east" -> fraction(p.x(), b.minX(), b.maxX());
            case "west" -> 1 - fraction(p.x(), b.minX(), b.maxX());
            case "south" -> fraction(p.z(), b.minZ(), b.maxZ());
            case "north" -> 1 - fraction(p.z(), b.minZ(), b.maxZ());
            case IN -> 1 - radial(p, b, false);
            case OUT_FLAT -> radial(p, b, true);
            default -> radial(p, b, false);
        };
    }

    /** Distance of the block's middle from the box's middle, 0 there to 1 at the corners ({@code flat}: ignoring height). */
    private static double radial(BlockPos p, Box b, boolean flat) {
        double cx = (b.minX() + b.maxX() + 1) / 2.0, cy = (b.minY() + b.maxY() + 1) / 2.0, cz = (b.minZ() + b.maxZ() + 1) / 2.0;
        double dx = p.x() + 0.5 - cx, dy = flat ? 0 : p.y() + 0.5 - cy, dz = p.z() + 0.5 - cz;
        double hx = b.sizeX() / 2.0, hy = flat ? 0 : b.sizeY() / 2.0, hz = b.sizeZ() / 2.0;
        double max = Math.sqrt(hx * hx + hy * hy + hz * hz);
        return max == 0 ? 0.5 : Math.min(1, Math.sqrt(dx * dx + dy * dy + dz * dz) / max);
    }

    /** Runs the gradient {@code repeat} times over the length, every other run backwards when {@code mirror}. */
    static double repeated(double t, int repeat, boolean mirror) {
        double u = Math.min(t * repeat, repeat - 1e-9);
        int run = (int) Math.floor(u);
        double f = u - run;
        return mirror && run % 2 == 1 ? 1 - f : f;
    }

    private static final int[][] BAYER = {{0, 8, 2, 10}, {12, 4, 14, 6}, {3, 11, 1, 9}, {15, 7, 13, 5}};

    /** An ordered-dither threshold (0..1) from a 4×4 pattern laid across the gradient, so the mix is an even dot pattern. */
    static double dither(String axis, BlockPos p) {
        int a, c;
        switch (axis) {
            case "up", "down" -> {
                a = p.x();
                c = p.z();
            }
            case "east", "west" -> {
                a = p.z();
                c = p.y();
            }
            case "south", "north" -> {
                a = p.x();
                c = p.y();
            }
            default -> {
                a = p.x() + p.y();
                c = p.z() + 2 * p.y();
            }
        }
        return (BAYER[a & 3][c & 3] + 0.5) / 16;
    }

    private static double fraction(int v, int min, int max) {
        return max == min ? 0.5 : (v - min) / (double) (max - min + 1) + 0.5 / (max - min + 1);
    }
}
