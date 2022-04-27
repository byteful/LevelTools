package me.byteful.plugin.leveltools.config;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import redempt.redlib.config.annotations.ConfigName;

import java.util.ArrayList;
import java.util.List;

public class Config {

    @ConfigName("anvil_combine")
    public static String anvil_combine;
    @ConfigName("level_xp_start")
    public static double level_xp_start;
    @ConfigName("level_xp_increase.mode")
    public static String mode;
    @ConfigName("level_xp_increase.amount")
    public static double amount;
    @ConfigName("playerPlacedBlocks")
    public static boolean playerPlacedBlocks;
    @ConfigName("actionBar.enabled")
    public static boolean actionBarEnabled;
    @ConfigName("actionBar.display")
    public static String actionBarDisplay;
    @ConfigName("level_up_sound.sound")
    public static String levelUpSound;
    @ConfigName("level_up_sound.pitch")
    public static float pitch;
    @ConfigName("level_up_sound.volume")
    public static float volume;
    @ConfigName("default_block_xp_modifier.min")
    public static double blockXPModifierMin;
    @ConfigName("default_block_xp_modifier.max")
    public static double blockXPModifierMax;
    @ConfigName("default_combat_xp_modifier.min")
    public static double combatXPModifierMin;
    @ConfigName("default_combat_xp_modifier.max")
    public static double combatXPModifierMax;
    @ConfigName("blockBlacklist")
    public static List<Material> blockBlacklist = new ArrayList<>();
    @ConfigName("entityBlacklist")
    public static List<EntityType> entityBlacklist = new ArrayList<>();
}
