package com.example.palette;

import io.blockdesigner.core.model.BlockState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GradientPresetsTest {
    @TempDir
    Path dir;

    @Test
    void builtInPresetsFitInAHotbar() {
        assertThat(GradientPresets.BUILT_IN).isNotEmpty().allSatisfy(p -> assertThat(p.blocks()).hasSizeBetween(2, GradientPresets.MAX));
        assertThatThrownBy(() -> new GradientPresets.Preset("Too long", java.util.Collections.nCopies(10, BlockState.of("stone")), true))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void ownPresetsAreSavedAndReadBack() throws Exception {
        Path file = dir.resolve("data/gradients.json");
        assertThat(GradientPresets.read(file, null)).isEmpty();
        var mine = new GradientPresets.Preset("Mine", List.of(BlockState.of("oak_planks"), BlockState.parse("oak_stairs[facing=east]")), true);
        GradientPresets.write(file, List.of(mine));
        assertThat(GradientPresets.read(file, null)).containsExactly(mine);
    }
}
