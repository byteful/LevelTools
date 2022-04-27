package me.byteful.plugin.leveltools.config;

import redempt.redlib.config.annotations.ConfigName;

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
    public static String display;
    @ConfigName("level_up_sound.sound")
    public static String sound;
    @ConfigName("level_up_sound.pitch")
    public static double pitch;
    @ConfigName("level_up_sound.volume")
    public static double volume;
    @ConfigName("default_block_xp_modifier.min")
    public static double blockXPModifierMin;
    @ConfigName("default_block_xp_modifier.max")
    public static double blockXPModifierMax;
    @ConfigName("default_combat_xp_modifier.min")
    public static double combatXPModifierMin;
    @ConfigName("default_combat_xp_modifier.max")
    public static double combatXPModifierMax;
}
