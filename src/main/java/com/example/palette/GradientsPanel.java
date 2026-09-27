package com.example.palette;

import com.example.palette.GradientPresets.Preset;
import io.blockdesigner.core.model.BlockState;
import io.blockdesigner.plugin.BlockPattern;
import io.blockdesigner.plugin.PanelContext;
import io.blockdesigner.plugin.PluginContext;
import io.blockdesigner.plugin.PluginPanel;
import io.blockdesigner.plugin.ui.Controls;
import io.blockdesigner.plugin.ui.EmptyState;
import io.blockdesigner.plugin.ui.Icon;
import io.blockdesigner.plugin.ui.PanelScaffold;
import io.blockdesigner.plugin.ui.Section;
import io.blockdesigner.plugin.ui.Theme;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Gradients, each drawn as a strip of up to nine block slots: your own (saved from the hotbar, kept in the plugin's
 * data folder) and the built-in ones. Click a strip to paint with it: the Palette tool is picked with that gradient.
 * The Hotbar button puts the blocks in the hotbar instead.
 */
final class GradientsPanel implements PluginPanel {
    private static final double SLOT = 26;

    private final String paletteTool;
    private PanelContext panel;
    private VBox own;
    private VBox builtIn;

    /** @param paletteTool the id of the tool the presets are used with */
    GradientsPanel(String paletteTool) {
        this.paletteTool = paletteTool;
    }

    @Override
    public String id() {
        return "gradients";
    }

    @Override
    public String title() {
        return "Gradients";
    }

    @Override
    public String icon() {
        return "M2 3 H14 V13 H2 Z M5 3 V13 M8 3 V13 M11 3 V13";
    }

    @Override
    public Node create(PanelContext context) {
        this.panel = context;
        own = new VBox(Theme.MD);
        builtIn = new VBox(Theme.MD);
        Section yours = new Section("Your gradients", own)
                .actions(Controls.iconButton(Icon.ADD, "Save the hotbar as a gradient…", this::saveHotbar));
        PanelScaffold page = new PanelScaffold()
                .add(yours, new Section("Built-in", builtIn))
                .footer(Controls.hint("Click a gradient to paint the selection with it (Palette tool)."));
        // Icons need Minecraft's assets, which can load after the panel was first built.
        context.onShown(this::refresh);
        refresh();
        return page;
    }

    private void refresh() {
        if (panel == null) return;
        List<Node> mine = new ArrayList<>();
        for (Preset p : load(panel.plugin())) mine.add(row(p));
        if (mine.isEmpty()) {
            mine.add(new EmptyState(null, "No gradients saved.")
                    .hint("Put a gradient's blocks in the hotbar, first to last, then save it.")
                    .action(Controls.button("Save the hotbar…", "Save the hotbar as a gradient", this::saveHotbar)));
        }
        own.getChildren().setAll(mine);
        List<Node> built = new ArrayList<>();
        for (Preset p : GradientPresets.BUILT_IN) built.add(row(p));
        builtIn.getChildren().setAll(built);
        // Minecraft's assets (the icons) can load after the page was built: draw it again shortly, for up to a minute.
        BlockState first = GradientPresets.BUILT_IN.getFirst().blocks().getFirst();
        if (panel.plugin().blockIcon(first).isEmpty() && iconRetries++ < 30) {
            javafx.animation.PauseTransition retry = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(2));
            retry.setOnFinished(e -> refresh());
            retry.play();
        }
    }

    /** How often the page was redrawn waiting for block icons. */
    private int iconRetries;

    private Node row(Preset p) {
        PluginContext ctx = panel.plugin();
        Label name = new Label(p.name());
        name.getStyleClass().add("bd-row-title");
        name.setMinWidth(0);
        Button hotbar = Controls.button("Hotbar", "Put these blocks in the hotbar", () -> {
            ctx.setHotbar(p.blocks());
            ctx.toast(p.name() + " is in the hotbar");
        });
        hotbar.getStyleClass().addAll("flat", "small");
        HBox top = new HBox(Theme.XS, name, Controls.spacer(), hotbar);
        top.setAlignment(Pos.CENTER_LEFT);
        if (p.own()) {
            Button delete = Controls.iconButton(Icon.TRASH, "Delete " + p.name() + "…", () -> delete(p));
            delete.getStyleClass().add("small");
            top.getChildren().add(delete);
        }
        FlowPane strip = new FlowPane(2, 2);
        for (BlockState st : p.blocks()) strip.getChildren().add(slot(ctx, st));
        strip.setCursor(Cursor.HAND);
        Tooltip.install(strip, new Tooltip("Paint the selection with " + p.name()));
        strip.setOnMouseClicked(e -> use(p));
        return new VBox(Theme.XS, top, strip);
    }

    /** A block slot: the block's icon, or its colour while no icons are available. */
    private static Node slot(PluginContext ctx, BlockState st) {
        Node n = Controls.blockIcon(ctx.blockIcon(st).orElse(null), ctx.blocks().averageColor(st), SLOT);
        Tooltip.install(n, new Tooltip(ctx.blocks().displayName(st)));
        return n;
    }

    /** Picks the Palette tool in Gradient mode with this preset's blocks. */
    private void use(Preset p) {
        PluginContext ctx = panel.plugin();
        ctx.setToolOptions(paletteTool, v -> v.with("mode", "Gradient").with("blocks", BlockPattern.of(p.blocks())));
        ctx.pickTool(paletteTool);
        ctx.toast(p.name() + " · select blocks, then right-click or Enter to apply");
    }

    private void delete(Preset p) {
        PluginContext ctx = panel.plugin();
        if (!ctx.ui().confirm("Delete gradient", "Delete “" + p.name() + "”? This can't be undone.", "Delete", true)) return;
        List<Preset> mine = new ArrayList<>(load(ctx));
        mine.removeIf(o -> o.name().equals(p.name()));
        save(ctx, mine);
        refresh();
    }

    private void saveHotbar() {
        PluginContext ctx = panel.plugin();
        List<BlockState> blocks = ctx.hotbar().stream().filter(b -> !b.isAir()).toList();
        if (blocks.isEmpty()) {
            ctx.toast("The hotbar is empty: put the blocks of your gradient in it first, first to last");
            return;
        }
        Optional<String> name = ctx.ui().askText("Save gradient",
                "A name for this gradient of " + blocks.size() + " block" + (blocks.size() == 1 ? "" : "s"), "My gradient");
        if (name.isEmpty()) return;
        List<Preset> mine = new ArrayList<>(load(ctx));
        mine.removeIf(o -> o.name().equals(name.get()));
        mine.addFirst(new Preset(name.get(), blocks, true));
        save(ctx, mine);
        refresh();
    }

    private static List<Preset> load(PluginContext ctx) {
        return GradientPresets.read(GradientPresets.file(ctx), ctx);
    }

    private static void save(PluginContext ctx, List<Preset> own) {
        try {
            GradientPresets.write(GradientPresets.file(ctx), own);
        } catch (IOException e) {
            ctx.toast("Could not save the gradients: " + e.getMessage());
        }
    }

    @Override
    public void dispose() {
        panel = null;
    }
}
