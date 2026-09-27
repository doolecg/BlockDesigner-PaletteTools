package com.example.palette;

import io.blockdesigner.core.model.BlockPos;
import io.blockdesigner.core.model.BlockState;
import io.blockdesigner.core.model.Box;
import io.blockdesigner.core.model.Layer;
import io.blockdesigner.plugin.PanelContext;
import io.blockdesigner.plugin.PluginContext;
import io.blockdesigner.plugin.PluginPanel;
import io.blockdesigner.plugin.SceneEvent;
import io.blockdesigner.plugin.Subscription;
import io.blockdesigner.plugin.ui.Controls;
import io.blockdesigner.plugin.ui.EmptyState;
import io.blockdesigner.plugin.ui.Icon;
import io.blockdesigner.plugin.ui.ItemList;
import io.blockdesigner.plugin.ui.ItemRow;
import io.blockdesigner.plugin.ui.PanelScaffold;
import javafx.animation.PauseTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Lists the blocks of the selection (or of every visible layer when nothing is selected) with their icon and a count,
 * most used first, with a filter on top and what is counted at the bottom. Listens for scene events and recounts
 * (a quarter of a second after the last change) only while it is on screen.
 */
final class PalettePanel implements PluginPanel {
    /** One kind of block and how many there are. */
    record Entry(String id, String name, long count, Image icon, int color) {
    }

    private final List<Subscription> subscriptions = new ArrayList<>();
    // JavaFX nodes are only made in create(): the plugin is enabled before the panel is ever shown.
    private final ObservableList<Entry> entries = FXCollections.observableArrayList();
    private FilteredList<Entry> shown;
    private Label status;
    private EmptyState empty;
    private PauseTransition later;
    private PanelContext panel;
    private boolean stale = true;

    @Override
    public String id() {
        return "palette";
    }

    @Override
    public String title() {
        return "Palette";
    }

    @Override
    public String icon() {
        return "M8 2 C4.5 2 2 4.5 2 8 C2 11.5 4.5 14 8 14 C9 14 9.5 13.3 9.5 12.5 C9.5 11.5 10 11 11 11 H12 C13.2 11 14 10 14 8.5 "
                + "C14 4.8 11.3 2 8 2 Z M5 7 H5.01 M7.5 4.5 H7.51 M10.5 5.5 H10.51";
    }

    @Override
    public Node create(PanelContext context) {
        this.panel = context;
        PluginContext ctx = context.plugin();
        later = new PauseTransition(Duration.millis(250));
        later.setOnFinished(e -> {
            if (panel != null && panel.isShowing()) refresh();
        });
        // Any of these can change what the panel shows; events come at most once per frame.
        subscriptions.add(ctx.on(SceneEvent.BlocksChanged.class, e -> changed()));
        subscriptions.add(ctx.on(SceneEvent.SelectionChanged.class, e -> changed()));
        subscriptions.add(ctx.on(SceneEvent.LayersChanged.class, e -> changed()));
        subscriptions.add(ctx.on(SceneEvent.ProjectOpened.class, e -> changed()));
        context.onShown(() -> {
            if (stale) refresh();
        });

        TextField search = Controls.search("Filter blocks");
        shown = new FilteredList<>(entries);
        search.textProperty().addListener((o, a, text) -> {
            String q = text.strip().toLowerCase(Locale.ROOT);
            shown.setPredicate(q.isEmpty() ? null : e -> e.name().toLowerCase(Locale.ROOT).contains(q) || e.id().contains(q));
        });
        empty = new EmptyState(Icon.INFO, "No blocks here.").hint("Build something, or select blocks to count them.");
        ItemList<Entry> list = new ItemList<Entry>(e -> (e.icon() != null ? ItemRow.of(e.name()).image(e.icon()) : ItemRow.of(e.name()).swatch(e.color()))
                .trailing(Controls.caption(String.format("%,d", e.count())))
                .tooltip(e.id()))
                .empty(empty);
        list.setItems(shown);
        status = Controls.caption("");

        PanelScaffold page = new PanelScaffold();
        page.add(search);
        page.grow(list);
        page.footer(status);
        refresh();
        return page;
    }

    private void changed() {
        stale = true;
        if (panel != null && panel.isShowing()) later.playFromStart();
    }

    private void refresh() {
        stale = false;
        PluginContext ctx = panel.plugin();
        Optional<Box> sel = ctx.selection();
        Map<String, Long> counts = new HashMap<>();
        for (Layer l : ctx.scene().layers()) {
            if (!l.visible()) continue;
            l.structure().forEachBlock((x, y, z, st) -> {
                if (sel.isPresent()) {
                    BlockPos w = l.toWorld(x, y, z);
                    if (!sel.get().contains(w.x(), w.y(), w.z())) return;
                }
                counts.merge(st.name(), 1L, Long::sum);
            });
        }
        String where = sel.isPresent() ? "In the selection" : "In the visible layers";
        status.setText(where + " · " + counts.size() + (counts.size() == 1 ? " kind of block" : " kinds of block"));
        empty.hint(sel.isPresent() ? "The selection holds no blocks." : "Build something, or select blocks to count them.");
        panel.setBadge(counts.isEmpty() ? null : String.valueOf(counts.size()));
        List<Map.Entry<String, Long>> sorted = new ArrayList<>(counts.entrySet());
        sorted.sort(Map.Entry.<String, Long>comparingByValue().reversed());
        List<Entry> out = new ArrayList<>(sorted.size());
        for (var e : sorted) {
            BlockState st = BlockState.of(e.getKey());
            out.add(new Entry(e.getKey(), ctx.blocks().displayName(st), e.getValue(), ctx.blockIcon(st).orElse(null),
                    ctx.blocks().averageColor(st)));
        }
        entries.setAll(out);
        // Minecraft's assets (icons, colours) can load after the first count: look again shortly, for up to a minute.
        if (!out.isEmpty() && out.stream().allMatch(e -> e.icon() == null) && iconRetries++ < 30) {
            stale = true;
            PauseTransition retry = new PauseTransition(Duration.seconds(2));
            retry.setOnFinished(e -> changed());
            retry.play();
        }
    }

    /** How often the list was redrawn waiting for block icons. */
    private int iconRetries;

    @Override
    public void dispose() {
        subscriptions.forEach(Subscription::cancel);
        subscriptions.clear();
        if (later != null) later.stop();
        panel = null;
    }
}
