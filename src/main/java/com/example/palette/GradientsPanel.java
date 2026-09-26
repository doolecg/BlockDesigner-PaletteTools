package com.example.palette;

import com.example.palette.GradientPresets.Preset;
import io.blockdesigner.core.model.BlockState;
import io.blockdesigner.plugin.BlockPattern;
import io.blockdesigner.plugin.PanelContext;
import io.blockdesigner.plugin.PluginContext;
import io.blockdesigner.plugin.PluginPanel;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Preset gradients, each drawn like a hotbar of up to nine blocks. Click one to paint with it: the Palette tool is
 * picked with that gradient. The hotbar button puts the blocks in the hotbar instead; the current hotbar can be saved
 * as a preset of your own (kept in the plugin's data folder).
 */
final class GradientsPanel implements PluginPanel {
    private static final double SLOT = 26;

    private final String paletteTool;
    private PanelContext panel;
    private VBox rows;

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
        rows = new VBox(6);
        Label hint = new Label("Click a gradient to paint the selection with it (Palette tool).");
        hint.setWrapText(true);
        hint.setStyle("-fx-text-fill: -color-fg-muted; -fx-font-size: 11px;");
        Button save = new Button("Save the hotbar as a gradient");
        save.setMaxWidth(Double.MAX_VALUE);
        save.setOnAction(e -> saveHotbar());
        ScrollPane scroll = new ScrollPane(rows);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);
        VBox root = new VBox(8, hint, save, scroll);
        root.setPadding(new Insets(10));
        // Icons need Minecraft's assets, which can load after the panel was first built.
        context.onShown(this::refresh);
        refresh();
        return root;
    }

    private void refresh() {
        if (panel == null) return;
        List<Node> out = new ArrayList<>();
        List<Preset> own = load(panel.plugin());
        for (Preset p : own) out.add(row(p));
        for (Preset p : GradientPresets.BUILT_IN) out.add(row(p));
        rows.getChildren().setAll(out);
    }

    private Node row(Preset p) {
        PluginContext ctx = panel.plugin();
        Label name = new Label(p.name());
        name.setStyle("-fx-font-size: 11.5px;");
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);
        Button hotbar = new Button("Hotbar");
        hotbar.getStyleClass().add("flat");
        hotbar.setStyle("-fx-font-size: 10.5px; -fx-padding: 1 6 1 6;");
        hotbar.setTooltip(new Tooltip("Put these blocks in the hotbar"));
        hotbar.setOnAction(e -> {
            ctx.setHotbar(p.blocks());
            ctx.toast(p.name() + " is in the hotbar");
        });
        HBox top = new HBox(6, name, sp, hotbar);
        top.setAlignment(Pos.CENTER_LEFT);
        if (p.own()) {
            Button delete = new Button("✕");
            delete.getStyleClass().add("flat");
            delete.setStyle("-fx-font-size: 10.5px; -fx-padding: 1 5 1 5;");
            delete.setTooltip(new Tooltip("Delete this gradient"));
            delete.setOnAction(e -> {
                List<Preset> own = new ArrayList<>(load(ctx));
                own.removeIf(o -> o.name().equals(p.name()));
                save(ctx, own);
                refresh();
            });
            top.getChildren().add(delete);
        }
        HBox strip = new HBox(2);
        strip.setAlignment(Pos.CENTER_LEFT);
        for (BlockState st : p.blocks()) strip.getChildren().add(slot(ctx, st));
        strip.setStyle("-fx-cursor: hand;");
        Tooltip.install(strip, new Tooltip("Paint the selection with " + p.name()));
        strip.setOnMouseClicked(e -> use(p));
        VBox box = new VBox(3, top, strip);
        box.setPadding(new Insets(4, 4, 6, 4));
        return box;
    }

    /** A small hotbar slot: the block's icon, or its colour while no icons are available. */
    private static Node slot(PluginContext ctx, BlockState st) {
        StackPane p = new StackPane();
        p.getStyleClass().addAll("hotbar-slot", "option-slot");
        p.setMinSize(SLOT, SLOT);
        p.setPrefSize(SLOT, SLOT);
        p.setMaxSize(SLOT, SLOT);
        Optional<javafx.scene.image.Image> icon = ctx.blockIcon(st);
        if (icon.isPresent()) {
            ImageView iv = new ImageView(icon.get());
            iv.setFitWidth(SLOT - 6);
            iv.setFitHeight(SLOT - 6);
            iv.setSmooth(false);
            p.getChildren().add(iv);
        } else {
            Region swatch = new Region();
            swatch.setMaxSize(SLOT - 8, SLOT - 8);
            swatch.setStyle(String.format("-fx-background-color: #%06X; -fx-background-radius: 3;", ctx.blocks().averageColor(st) & 0xFFFFFF));
            p.getChildren().add(swatch);
        }
        Tooltip.install(p, new Tooltip(ctx.blocks().displayName(st)));
        return p;
    }

    /** Picks the Palette tool in Gradient mode with this preset's blocks. */
    private void use(Preset p) {
        PluginContext ctx = panel.plugin();
        ctx.setToolOptions(paletteTool, v -> v.with("mode", "Gradient").with("blocks", BlockPattern.of(p.blocks())));
        ctx.pickTool(paletteTool);
        ctx.toast(p.name() + " · select blocks, then right-click or Enter to apply");
    }

    private void saveHotbar() {
        PluginContext ctx = panel.plugin();
        List<BlockState> blocks = ctx.hotbar().stream().filter(b -> !b.isAir()).toList();
        if (blocks.isEmpty()) {
            ctx.toast("The hotbar is empty: put the blocks of your gradient in it first, first to last");
            return;
        }
        TextInputDialog d = new TextInputDialog("My gradient");
        d.setTitle("Save gradient");
        d.setHeaderText("A name for this gradient of " + blocks.size() + " block" + (blocks.size() == 1 ? "" : "s"));
        if (rows.getScene() != null) d.initOwner(rows.getScene().getWindow());
        Optional<String> name = d.showAndWait().map(String::strip).filter(s -> !s.isEmpty());
        if (name.isEmpty()) return;
        List<Preset> own = new ArrayList<>(load(ctx));
        own.removeIf(o -> o.name().equals(name.get()));
        own.addFirst(new Preset(name.get(), blocks, true));
        save(ctx, own);
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
