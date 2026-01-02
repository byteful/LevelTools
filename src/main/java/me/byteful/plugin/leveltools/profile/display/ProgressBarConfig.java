package me.byteful.plugin.leveltools.profile.display;

import org.jetbrains.annotations.NotNull;

public final class ProgressBarConfig {
    private final int totalBars;
    private final char barSymbol;
    private final char prefixSymbol;
    private final char suffixSymbol;
    private final char prefixColor;
    private final char suffixColor;
    private final char completedColor;
    private final char placeholderColor;

    public ProgressBarConfig(int totalBars, char barSymbol, char prefixSymbol, char suffixSymbol,
                             char prefixColor, char suffixColor, char completedColor, char placeholderColor) {
        this.totalBars = totalBars;
        this.barSymbol = barSymbol;
        this.prefixSymbol = prefixSymbol;
        this.suffixSymbol = suffixSymbol;
        this.prefixColor = prefixColor;
        this.suffixColor = suffixColor;
        this.completedColor = completedColor;
        this.placeholderColor = placeholderColor;
    }

    @NotNull
    public static ProgressBarConfig defaultConfig() {
        return new ProgressBarConfig(50, '|', '[', ']', '8', '8', 'e', '7');
    }

    public static Builder builder() {
        return new Builder();
    }

    public int getTotalBars() {
        return totalBars;
    }

    public char getBarSymbol() {
        return barSymbol;
    }

    public char getPrefixSymbol() {
        return prefixSymbol;
    }

    public char getSuffixSymbol() {
        return suffixSymbol;
    }

    public char getPrefixColor() {
        return prefixColor;
    }

    public char getSuffixColor() {
        return suffixColor;
    }

    public char getCompletedColor() {
        return completedColor;
    }

    public char getPlaceholderColor() {
        return placeholderColor;
    }

    @NotNull
    public String buildProgressBar(double currentXp, double maxXp) {
        int completedBars = (int) Math.round((currentXp / maxXp) * totalBars);
        completedBars = Math.min(completedBars, totalBars);

        StringBuilder bar = new StringBuilder();
        bar.append("&").append(prefixColor).append(prefixSymbol);
        bar.append("&").append(completedColor);

        for (int i = 0; i < completedBars; i++) {
            bar.append(barSymbol);
        }

        bar.append("&").append(placeholderColor);

        for (int i = completedBars; i < totalBars; i++) {
            bar.append(barSymbol);
        }

        bar.append("&").append(suffixColor).append(suffixSymbol);

        return bar.toString();
    }

    public static final class Builder {
        private int totalBars = 50;
        private char barSymbol = '|';
        private char prefixSymbol = '[';
        private char suffixSymbol = ']';
        private char prefixColor = '8';
        private char suffixColor = '8';
        private char completedColor = 'e';
        private char placeholderColor = '7';

        public Builder totalBars(int totalBars) {
            this.totalBars = totalBars;
            return this;
        }

        public Builder barSymbol(char barSymbol) {
            this.barSymbol = barSymbol;
            return this;
        }

        public Builder prefixSymbol(char prefixSymbol) {
            this.prefixSymbol = prefixSymbol;
            return this;
        }

        public Builder suffixSymbol(char suffixSymbol) {
            this.suffixSymbol = suffixSymbol;
            return this;
        }

        public Builder prefixColor(char prefixColor) {
            this.prefixColor = prefixColor;
            return this;
        }

        public Builder suffixColor(char suffixColor) {
            this.suffixColor = suffixColor;
            return this;
        }

        public Builder completedColor(char completedColor) {
            this.completedColor = completedColor;
            return this;
        }

        public Builder placeholderColor(char placeholderColor) {
            this.placeholderColor = placeholderColor;
            return this;
        }

        public ProgressBarConfig build() {
            return new ProgressBarConfig(
                    totalBars,
                    barSymbol,
                    prefixSymbol,
                    suffixSymbol,
                    prefixColor,
                    suffixColor,
                    completedColor,
                    placeholderColor
            );
        }
    }
}
