package com.example.palette;

import io.blockdesigner.plugin.Options;
import io.blockdesigner.plugin.PluginTool;
import io.blockdesigner.plugin.PluginTransform;
import io.blockdesigner.plugin.SceneEvent;
import io.blockdesigner.plugin.Subscription;
import io.blockdesigner.plugin.ToolContext;
import io.blockdesigner.plugin.ToolEvent;
import io.blockdesigner.plugin.ToolHandler;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Repaints the selection in the view. Select blocks with the left button right here (click, drag a box, Shift adds,
 * Ctrl removes; or bring a selection from Select mode) and choose Palette swap, Weathering or Gradient in the options.
 * The result shows as ghosts straight away and follows every change to the options or the selection; a right-click or
 * Enter applies it as one undo step, R rolls new random picks and Esc hides the preview. It runs the same transforms
 * as the Plugins menu, so both give the same result.
 */
final class PaletteTool implements PluginTool {
    /** The transforms, by the name shown in the Mode choice. */
    private final Map<String, PluginTransform> modes = new LinkedHashMap<>();

    PaletteTool(PluginTransform... transforms) {
        for (PluginTransform t : transforms) modes.put(t.name(), t);
    }

    @Override
    public String id() {
        return "palette";
    }

    @Override
    public String name() {
        return "Palette";
    }

    @Override
    public String description() {
        return "left-drag selects · right-click or Enter applies · R new random · Esc hides the preview";
    }

    @Override
    public String icon() {
        return "M8 2 C4.5 2 2 4.5 2 7.5 C2 10.5 4.5 13 7 13 C8 13 8.5 12.3 8.2 11.5 C7.9 10.6 8.5 10 9.3 10 H11 C12.7 10 14 8.7 14 7 C14 4.2 11.3 2 8 2 Z M5 7 H5.1 M7 4.5 H7.1 M10.5 5 H10.6";
    }

    /** The left button selects blocks as in Select mode; the tool gets the right button. */
    @Override
    public boolean selects() {
        return true;
    }

    @Override
    public String defaultKey() {
        return "Shift+P";
    }

    /** A Mode choice, then each transform's own options, shown only while its mode is picked. */
    @Override
    public Options options() {
        Options.Builder b = Options.builder().choice("mode", "Mode", List.copyOf(modes.keySet()), modes.keySet().iterator().next());
        modes.forEach((name, t) -> {
            Options own = t.options();
            for (Options.Option o : own.all()) {
                b.option(o);
                // Its help, unit and on/off toggle come along, so the tool's options read like the transform's.
                own.help(o.key()).ifPresent(b::help);
                own.unit(o.key()).ifPresent(b::unit);
                own.enabledWhen(o.key()).ifPresent(b::enabledWhen);
                // Shown only in its own mode, and still only when the transform's own conditions hold (the gradient's
                // blend only with soft edges).
                b.showWhen("mode", name);
                for (Options.Condition c : own.conditions(o.key())) b.showWhen(c.choiceKey(), c.values().toArray(String[]::new));
            }
        });
        return b.build();
    }

    @Override
    public ToolHandler activate(ToolContext ctx) {
        return new ToolHandler() {
            private long seed = new Random().nextLong();
            private boolean hidden;
            /** Set by our own apply, so the scene change it causes doesn't bring the preview straight back. */
            private boolean applied;
            private final Subscription selection = ctx.plugin().on(SceneEvent.SelectionChanged.class, e -> {
                hidden = false;
                show();
            });
            private final Subscription blocks = ctx.plugin().on(SceneEvent.BlocksChanged.class, e -> {
                if (applied) applied = false;
                else show();
            });

            {
                show();
            }

            private PluginTransform transform() {
                PluginTransform t = modes.get(ctx.options().choice("mode"));
                return t != null ? t : modes.values().iterator().next();
            }

            private void show() {
                if (hidden) return;
                ctx.previewTransform(transform(), ctx.options(), seed);
            }

            /** Applies what the preview shows; with the preview hidden, shows it first instead of applying blind. */
            private void apply() {
                if (ctx.selection().isEmpty()) {
                    ctx.preview().clear();
                    ctx.plugin().toast("Select some blocks first (Select mode, or //pos1 and //pos2)");
                    return;
                }
                if (hidden) {
                    hidden = false;
                    show();
                    return;
                }
                PluginTransform t = transform();
                int n = ctx.applyTransform(t, ctx.options(), seed);
                applied = n > 0;
                ctx.plugin().toast(n == 0 ? t.name() + " · nothing changed" : String.format("%s · changed %,d block%s", t.name(), n, n == 1 ? "" : "s"));
                // The next go picks afresh, and waits for a click, a change or R before showing again.
                seed = new Random().nextLong();
                hidden = true;
            }

            @Override
            public void press(ToolEvent e) {
                if (e.button() == ToolEvent.Button.SECONDARY) apply();
            }

            @Override
            public void optionsChanged() {
                hidden = false;
                show();
            }

            @Override
            public boolean key(String key) {
                switch (key) {
                    case "Enter" -> apply();
                    case "R" -> {
                        seed = new Random().nextLong();
                        hidden = false;
                        show();
                    }
                    case "Esc" -> {
                        if (hidden) return false;
                        hidden = true;
                        ctx.preview().clear();
                    }
                    default -> {
                        return false;
                    }
                }
                return true;
            }

            @Override
            public void deactivate() {
                selection.cancel();
                blocks.cancel();
            }
        };
    }
}
