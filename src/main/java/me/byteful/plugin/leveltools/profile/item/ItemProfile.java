package me.byteful.plugin.leveltools.profile.item;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Set;

public final class ItemProfile {
    private final String id;
    private final Set<Material> materials;
    private final String triggerProfileId;
    private final String rewardProfileId;
    private final String displayProfileId;
    private final int maxLevel;
    private final String levelXpFormula;
    private final String extendsProfileId;

    public ItemProfile(
            @NotNull String id,
            @NotNull Set<Material> materials,
            @NotNull String triggerProfileId,
            @NotNull String rewardProfileId,
            @NotNull String displayProfileId,
            int maxLevel,
            @Nullable String levelXpFormula,
            @Nullable String extendsProfileId
    ) {
        this.id = id;
        this.materials = Collections.unmodifiableSet(materials);
        this.triggerProfileId = triggerProfileId;
        this.rewardProfileId = rewardProfileId;
        this.displayProfileId = displayProfileId;
        this.maxLevel = maxLevel;
        this.levelXpFormula = levelXpFormula;
        this.extendsProfileId = extendsProfileId;
    }

    public static Builder builder(@NotNull String id) {
        return new Builder(id);
    }

    @NotNull
    public String getId() {
        return id;
    }

    @NotNull
    public Set<Material> getMaterials() {
        return materials;
    }

    @NotNull
    public String getTriggerProfileId() {
        return triggerProfileId;
    }

    @NotNull
    public String getRewardProfileId() {
        return rewardProfileId;
    }

    @NotNull
    public String getDisplayProfileId() {
        return displayProfileId;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    @Nullable
    public String getLevelXpFormula() {
        return levelXpFormula;
    }

    @Nullable
    public String getExtendsProfileId() {
        return extendsProfileId;
    }

    public boolean hasCustomXpFormula() {
        return levelXpFormula != null && !levelXpFormula.isEmpty();
    }

    public boolean extendsProfile() {
        return extendsProfileId != null && !extendsProfileId.isEmpty();
    }

    public boolean matchesMaterial(@NotNull Material material) {
        return materials.contains(material);
    }

    public static final class Builder {
        private final String id;
        private Set<Material> materials = Collections.emptySet();
        private String triggerProfileId;
        private String rewardProfileId;
        private String displayProfileId = "default";
        private int maxLevel = 100;
        private String levelXpFormula;
        private String extendsProfileId;

        private Builder(@NotNull String id) {
            this.id = id;
        }

        public Builder materials(@NotNull Set<Material> materials) {
            this.materials = materials;
            return this;
        }

        public Builder triggerProfile(@NotNull String triggerProfileId) {
            this.triggerProfileId = triggerProfileId;
            return this;
        }

        public Builder rewardProfile(@NotNull String rewardProfileId) {
            this.rewardProfileId = rewardProfileId;
            return this;
        }

        public Builder displayProfile(@NotNull String displayProfileId) {
            this.displayProfileId = displayProfileId;
            return this;
        }

        public Builder maxLevel(int maxLevel) {
            this.maxLevel = maxLevel;
            return this;
        }

        public Builder levelXpFormula(@Nullable String levelXpFormula) {
            this.levelXpFormula = levelXpFormula;
            return this;
        }

        public Builder extendsProfile(@Nullable String extendsProfileId) {
            this.extendsProfileId = extendsProfileId;
            return this;
        }

        public ItemProfile build() {
            if (triggerProfileId == null) {
                throw new IllegalStateException("Trigger profile ID must be set");
            }
            if (rewardProfileId == null) {
                throw new IllegalStateException("Reward profile ID must be set");
            }
            return new ItemProfile(
                    id,
                    materials,
                    triggerProfileId,
                    rewardProfileId,
                    displayProfileId,
                    maxLevel,
                    levelXpFormula,
                    extendsProfileId
            );
        }
    }
}
