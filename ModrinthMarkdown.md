**Support/Help Server:** https://discord.gg/G8BDgqsuyw

**Wiki:** https://github.com/byteful/LevelTools/wiki

![Logo](https://github.com/byteful/LevelTools/blob/main/LevelTools%20Large%20Logo.png?raw=true)

## A plugin that adds a leveling system to any item.

## Features

- Supports Minecraft 1.8.8+
- Supports Folia.
- No required dependencies; PlaceholderAPI is optional.
- Profile-based configuration system.
- Any item can level up with custom triggers.
- Configurable global, item-profile, and permission-based XP formulas.
- Commands & enchants on level up.
- Supports blacklisting/whitelisting for blocks, entities, and items.
- ActionBar notifications.
- Item lore modification.
- Optional enchanted-book blocking for LevelTools items.
- Farming trigger support for fully grown player-planted crops.

## What's New in 2.2

- `ARMOR_DURABILITY` trigger awards XP when armor takes damage.
- Configurable XP formulas under `xp_formulas`, selectable per player via `leveltools.formula.<id>`.
- Toggle to block enchanted books from being applied to LevelTools items.
- `block_data_storage` option (`SQLITE`) for persisting per-block placement data.
- Farming trigger gains `ignore_player_placed_blocks_for_fully_grown_crops` for fully-grown crop handling.
- Automatic config migration from v1.x and earlier v2.x layouts.

## Profile System

LevelTools uses a modular profile-based configuration system. Any item can be configured to level up.

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
- `FARMING` - XP when tilling soil and breaking fully-grown crops
- `ARMOR_DURABILITY` - XP when worn armor takes damage

### Default Supported Items

Out of the box, LevelTools ships profiles for:
- Pickaxes, Axes, Shovels (block mining)
- Swords (combat)
- Bows, Crossbows (ranged)
- Fishing Rods (fishing)
- Tridents (combat / ranged)
- Hoes (farming)

Add any item by creating custom profiles. See the [Wiki](https://github.com/byteful/LevelTools/wiki) for details.

### Migration from v1.x

Your old config will be automatically backed up to `old_config.yml` and migrated to the new profile system.

Earlier v2.x configs are also updated automatically: `level_xp_formula` is moved to `xp_formulas.global`.

## Commands

| Command | Description | Permission |
|---------|-------------|------------|
| `/leveltools help [page]` | Paginated command help | None |
| `/leveltools reload` | Reloads configuration | `leveltools.admin` |
| `/leveltools reset <player>` | Reset hand item for player | `leveltools.admin` |
| `/leveltools reset <player> --all` | Reset all items for player | `leveltools.admin` |
| `/leveltools xp <amount>` | Set hand item XP | `leveltools.admin` |
| `/leveltools level <level>` | Set hand item level | `leveltools.admin` |
| `/leveltools levelup` | Increase hand item level by 1 | `leveltools.admin` |
| `/leveltools debug` | Show debug information | `leveltools.admin` |

## Permissions

| Permission | Description | Default |
|------------|-------------|---------|
| `leveltools.admin` | Access to admin commands | op |
| `leveltools.enabled` | Allow leveling for this player | true |
| `leveltools.formula.<id>` | Selects an XP formula defined under `xp_formulas` | false |

## Developer API

**View detailed API usage [here](https://github.com/byteful/LevelTools/wiki/Developer-API).**

## PlaceholderAPI

| Placeholder | Description |
|-------------|-------------|
| `%leveltools_level%` | Current item level (main hand) |
| `%leveltools_xp%` | Current XP (main hand) |
| `%leveltools_max_xp%` | XP required for the next level |
| `%leveltools_progress%` | Progress percentage (0-100, 1 decimal) |
| `%leveltools_progress_bar%` | Rendered progress bar |
| `%leveltools_item_profile%` | Item profile id for the hand item |
| `%leveltools_max_level%` | Max level for the current item profile |

## Documentation

- [Configuration](https://github.com/byteful/LevelTools/wiki/Configuration)
- [Trigger Profiles](https://github.com/byteful/LevelTools/wiki/Trigger-Profiles)
- [Reward Profiles](https://github.com/byteful/LevelTools/wiki/Reward-Profiles)
- [Display Profiles](https://github.com/byteful/LevelTools/wiki/Display-Profiles)
- [Item Profiles](https://github.com/byteful/LevelTools/wiki/Item-Profiles)

![bStats Graph](https://bstats.org/signatures/bukkit/LevelTools%20-%20RPG%20Item%20Leveling.svg)
