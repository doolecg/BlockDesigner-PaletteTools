package com.example.palette;

import io.blockdesigner.core.model.BlockPos;
import io.blockdesigner.core.model.Box;
import io.blockdesigner.plugin.OptionValues;
import io.blockdesigner.plugin.Options;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

class PaletteToolTest {
    private final PaletteTool tool = new PaletteTool(new PaletteSwapTransform(), new WeatheringTransform(), new GradientTransform());

    @Test
    void optionsShowOnlyTheChosenMode() {
        Options o = tool.options();
        OptionValues v = o.defaults();
        assertThat(v.choice("mode")).isEqualTo("Palette swap");
        assertThat(o.shown("from", v)).isTrue();
        assertThat(o.shown("amount", v)).isFalse();
        assertThat(o.shown("blocks", v)).isFalse();
        OptionValues g = v.with("mode", "Gradient");
        assertThat(o.shown("blocks", g)).isTrue();
        assertThat(o.shown("repeat", g)).isTrue();
        assertThat(o.shown("from", g)).isFalse();
        assertThat(o.shown("mode", g)).isTrue();
        assertThat(o.shown("blend", g)).isTrue();
        assertThat(o.shown("blend", g.with("style", GradientTransform.BANDS))).as("hard bands have no blend").isFalse();
    }

    @Test
    void gradientRunsOnceOrRepeatedAndMirrored() {
        assertThat(GradientTransform.repeated(0.25, 1, false)).isCloseTo(0.25, within(1e-9));
        assertThat(GradientTransform.repeated(0.75, 2, false)).isCloseTo(0.5, within(1e-9));
        // Mirrored: the second run goes back down.
        assertThat(GradientTransform.repeated(0.75, 2, true)).isCloseTo(0.5, within(1e-9));
        assertThat(GradientTransform.repeated(0.9, 2, true)).isCloseTo(0.2, within(1e-9));
        assertThat(GradientTransform.repeated(1.0, 3, false)).isLessThan(1);
    }

    @Test
    void gradientPositions() {
        Box b = new Box(0, 0, 0, 9, 9, 9);
        assertThat(GradientTransform.position("up", new BlockPos(0, 0, 0), b)).isLessThan(0.1);
        assertThat(GradientTransform.position("up", new BlockPos(0, 9, 0), b)).isGreaterThan(0.9);
        assertThat(GradientTransform.position("down", new BlockPos(0, 9, 0), b)).isLessThan(0.1);
        // From the middle: small in the middle, near 1 at a corner, and the other way round inwards.
        double mid = GradientTransform.position(GradientTransform.OUT, new BlockPos(4, 4, 4), b);
        double corner = GradientTransform.position(GradientTransform.OUT, new BlockPos(0, 0, 0), b);
        assertThat(mid).isLessThan(0.15);
        assertThat(corner).isGreaterThan(0.8);
        assertThat(GradientTransform.position(GradientTransform.IN, new BlockPos(0, 0, 0), b)).isCloseTo(1 - corner, within(1e-9));
        // Flat ignores height.
        assertThat(GradientTransform.position(GradientTransform.OUT_FLAT, new BlockPos(4, 0, 4), b))
                .isCloseTo(GradientTransform.position(GradientTransform.OUT_FLAT, new BlockPos(4, 9, 4), b), within(1e-9));
    }

    @Test
    void ditherUsesEveryThresholdOfTheFourByFourPattern() {
        Set<Double> seen = new HashSet<>();
        for (int x = 0; x < 4; x++)
            for (int z = 0; z < 4; z++) {
                double d = GradientTransform.dither("up", new BlockPos(x, 5, z));
                assertThat(d).isBetween(0.0, 1.0);
                seen.add(d);
            }
        assertThat(seen).hasSize(16);
    }
}
