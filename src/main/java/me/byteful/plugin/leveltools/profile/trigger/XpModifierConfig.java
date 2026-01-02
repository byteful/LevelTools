package me.byteful.plugin.leveltools.profile.trigger;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public final class XpModifierConfig {
    private final XpModifierRange defaultRange;
    private final Map<String, XpModifierRange> customModifiers;

    public XpModifierConfig(double defaultMin, double defaultMax, @NotNull Map<String, XpModifierRange> customModifiers) {
        this.defaultRange = new XpModifierRange(defaultMin, defaultMax);
        this.customModifiers = Collections.unmodifiableMap(customModifiers);
    }

    public double getDefaultMin() {
        return defaultRange.getMin();
    }

    public double getDefaultMax() {
        return defaultRange.getMax();
    }

    @NotNull
    public Map<String, XpModifierRange> getCustomModifiers() {
        return customModifiers;
    }

    public double calculateModifier(@NotNull String source) {
        XpModifierRange range = customModifiers.get(source);
        if (range != null) {
            return range.random();
        }
        return defaultRange.random();
    }

    public static final class XpModifierRange {
        private final double min;
        private final double max;

        public XpModifierRange(double min, double max) {
            this.min = min;
            this.max = max;
        }

        public double getMin() {
            return min;
        }

        public double getMax() {
            return max;
        }

        public double random() {
            if (min == max) {
                return min;
            }
            return ThreadLocalRandom.current().nextDouble(min, max);
        }
    }
}
