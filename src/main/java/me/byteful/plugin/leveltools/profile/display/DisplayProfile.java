package me.byteful.plugin.leveltools.profile.display;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public final class DisplayProfile {
    private final String id;
    private final NameDisplay nameDisplay;
    private final ActionBarDisplay actionBarDisplay;
    private final LoreDisplay loreDisplay;
    private final ProgressBarConfig progressBar;

    public DisplayProfile(
            @NotNull String id,
            @NotNull NameDisplay nameDisplay,
            @NotNull ActionBarDisplay actionBarDisplay,
            @NotNull LoreDisplay loreDisplay,
            @NotNull ProgressBarConfig progressBar
    ) {
        this.id = id;
        this.nameDisplay = nameDisplay;
        this.actionBarDisplay = actionBarDisplay;
        this.loreDisplay = loreDisplay;
        this.progressBar = progressBar;
    }

    public static Builder builder(@NotNull String id) {
        return new Builder(id);
    }

    @NotNull
    public String getId() {
        return id;
    }

    @NotNull
    public NameDisplay getNameDisplay() {
        return nameDisplay;
    }

    @NotNull
    public ActionBarDisplay getActionBarDisplay() {
        return actionBarDisplay;
    }

    @NotNull
    public LoreDisplay getLoreDisplay() {
        return loreDisplay;
    }

    @NotNull
    public ProgressBarConfig getProgressBar() {
        return progressBar;
    }

    public static final class NameDisplay {
        private final boolean enabled;
        private final String text;

        public NameDisplay(boolean enabled, @Nullable String text) {
            this.enabled = enabled;
            this.text = text != null ? text : "";
        }

        public static NameDisplay disabled() {
            return new NameDisplay(false, null);
        }

        public boolean isEnabled() {
            return enabled;
        }

        @NotNull
        public String getText() {
            return text;
        }
    }

    public static final class ActionBarDisplay {
        private final boolean enabled;
        private final String text;

        public ActionBarDisplay(boolean enabled, @Nullable String text) {
            this.enabled = enabled;
            this.text = text != null ? text : "";
        }

        public static ActionBarDisplay disabled() {
            return new ActionBarDisplay(false, null);
        }

        public boolean isEnabled() {
            return enabled;
        }

        @NotNull
        public String getText() {
            return text;
        }
    }

    public static final class LoreDisplay {
        private final boolean enabled;
        private final List<String> lines;

        public LoreDisplay(boolean enabled, @Nullable List<String> lines) {
            this.enabled = enabled;
            this.lines = lines != null ? Collections.unmodifiableList(lines) : Collections.emptyList();
        }

        public static LoreDisplay disabled() {
            return new LoreDisplay(false, null);
        }

        public boolean isEnabled() {
            return enabled;
        }

        @NotNull
        public List<String> getLines() {
            return lines;
        }
    }

    public static final class Builder {
        private final String id;
        private NameDisplay nameDisplay = NameDisplay.disabled();
        private ActionBarDisplay actionBarDisplay = ActionBarDisplay.disabled();
        private LoreDisplay loreDisplay = LoreDisplay.disabled();
        private ProgressBarConfig progressBar = ProgressBarConfig.defaultConfig();

        private Builder(@NotNull String id) {
            this.id = id;
        }

        public Builder nameDisplay(@NotNull NameDisplay nameDisplay) {
            this.nameDisplay = nameDisplay;
            return this;
        }

        public Builder actionBarDisplay(@NotNull ActionBarDisplay actionBarDisplay) {
            this.actionBarDisplay = actionBarDisplay;
            return this;
        }

        public Builder loreDisplay(@NotNull LoreDisplay loreDisplay) {
            this.loreDisplay = loreDisplay;
            return this;
        }

        public Builder progressBar(@NotNull ProgressBarConfig progressBar) {
            this.progressBar = progressBar;
            return this;
        }

        public DisplayProfile build() {
            return new DisplayProfile(id, nameDisplay, actionBarDisplay, loreDisplay, progressBar);
        }
    }
}
