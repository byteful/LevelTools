[![Java CI with Gradle](https://github.com/byteful/LevelTools/actions/workflows/gradle.yml/badge.svg?branch=master)](https://github.com/byteful/LevelTools/actions/workflows/gradle.yml)
[![Discord](https://img.shields.io/discord/911029017472270357?color=7289da&logo=discord)](https://discord.gg/G8BDgqsuyw)
[![](https://jitpack.io/v/byteful/LevelTools.svg)](https://jitpack.io/#byteful/LevelTools)

<h5 align="center">SpigotMC: https://www.spigotmc.org/resources/leveltools-rpg-item-leveling.97516/</h5>
<h5 align="center">Support/Help Server: https://discord.gg/G8BDgqsuyw</h5>
<h5 align="center">WIKI: https://github.com/byteful/LevelTools/wiki</h5>

![Logo](https://github.com/byteful/LevelTools/blob/main/LevelTools%20Large%20Logo.png?raw=true)

<h3 align="center">A plugin that adds a leveling system to any item.</h3>

## Features

- Supports versions 1.8 - 1.21.
- Supports Folia.
- No dependencies.
- Super efficient, no lag.
- Simple developer API.
- Profile-based configuration system.
- Any item can level up with custom triggers.
- Commands & enchants on level up.
- Supports blacklisting/whitelisting for blocks, entities, and items.
- ActionBar notifications.
- Item lore modification.

## v2.0.0 - Profile System

LevelTools v2.0 introduces a modular profile-based configuration system. Instead of hardcoded tool types, you can now configure any item to level up.

### Profile Types

| Profile Type | Purpose | File |
|-------------|---------|------|
| **Trigger Profiles** | Define how XP is gained | `trigger_profiles.yml` |
| **Reward Profiles** | Define rewards per level | `reward_profiles.yml` |
| **Display Profiles** | Define name, lore, action bar | `display_profiles.yml` |
| **Item Profiles** | Tie everything together | `item_profiles.yml` |

### Trigger Types

- `BLOCK_BREAK` - XP when breaking blocks
- `ENTITY_KILL` - XP when killing entities
- `FISHING` - XP when catching items
- `RIGHT_CLICK` / `LEFT_CLICK` - XP on click
- `CONSUME` - XP when consuming items

### Default Supported Items

Out of the box, LevelTools supports:
- Pickaxes, Axes, Shovels (block mining)
- Swords, Bows, Crossbows (combat)
- Fishing Rods (fishing)

Add any item by creating custom profiles. See the [Wiki](https://github.com/byteful/LevelTools/wiki) for details.

### Migration from v1.x

Your old config will be automatically backed up to `old_config.yml` and migrated to the new profile system.

## Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/leveltools help` | Shows command help | None |
| `/leveltools reload` | Reloads configuration | `leveltools.admin` |
| `/leveltools reset <player>` | Reset hand item for player | `leveltools.admin` |
| `/leveltools reset <player> --all` | Reset all items for player | `leveltools.admin` |
| `/leveltools xp <amount>` | Set hand item XP | `leveltools.admin` |
| `/leveltools level <level>` | Set hand item level | `leveltools.admin` |
| `/leveltools levelup` | Increase hand item level by 1 | `leveltools.admin` |
| `/leveltools debug` | Show debug information | `leveltools.admin` |

## Developer API

### Gradle:

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    compileOnly 'com.github.byteful:LevelTools:Tag' // Replace Tag with the version. (Ex: v2.0.0)
}
```

### Maven:

```xml

<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependency>
    <groupId>com.github.byteful</groupId>
    <artifactId>LevelTools</artifactId>
    <version>Tag</version> <!-- Replace Tag with the version. (Ex: v2.0.0) -->
</dependency>
```

### Example Usage:

```java
// Items

ItemStack hand = player.getInventory().getItemInMainHand();
LevelToolsItem tool = LevelToolsUtil.createLevelToolsItem(hand);
tool.setLevel(69);
tool.setXp(420);
player.getInventory().setItemInMainHand(tool.getItemStack());

// Events

@EventHandler
public void onLevelEvent(LevelToolsLevelIncreaseEvent event) {
  event.setNewLevel(69);
}

@EventHandler
public void onXPEvent(LevelToolsXPIncreaseEvent event) {
  event.setNewXp(420);
}
```

## PlaceholderAPI

| Placeholder | Description |
|-------------|-------------|
| `%leveltools_level%` | Item level (main hand) |
| `%leveltools_xp%` | Current XP (main hand) |
| `%leveltools_max_xp%` | XP needed for next level |
| `%leveltools_progress%` | Progress percentage |

## Wiki

Full documentation available at: https://github.com/byteful/LevelTools/wiki

- [Configuration](https://github.com/byteful/LevelTools/wiki/Configuration)
- [Trigger Profiles](https://github.com/byteful/LevelTools/wiki/Trigger-Profiles)
- [Reward Profiles](https://github.com/byteful/LevelTools/wiki/Reward-Profiles)
- [Display Profiles](https://github.com/byteful/LevelTools/wiki/Display-Profiles)
- [Item Profiles](https://github.com/byteful/LevelTools/wiki/Item-Profiles)
