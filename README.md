[![Java CI with Gradle](https://github.com/byteful/LevelTools/actions/workflows/gradle.yml/badge.svg?branch=master)](https://github.com/byteful/LevelTools/actions/workflows/gradle.yml)
[![Discord](https://img.shields.io/discord/911029017472270357?color=7289da&logo=discord)](https://discord.gg/G8BDgqsuyw)
[![](https://jitpack.io/v/byteful/LevelTools.svg)](https://jitpack.io/#byteful/LevelTools)

<h5 align="center">SpigotMC: https://www.spigotmc.org/resources/leveltools-rpg-item-leveling.97516/</h5>
<h5 align="center">Support/Help Server: https://discord.gg/G8BDgqsuyw</h5>
<h5 align="center">WIKI: https://github.com/byteful/LevelTools/wiki</h5>

![Logo](https://github.com/byteful/LevelTools/blob/master/LevelTools%20Large%20Logo.png?raw=true)

<h3 align="center">A plugin that adds a leveling system to tools, swords, and bows.</h3>

## Features

- Supports versions 1.8 - 1.21.
- Supports Folia
- No dependencies.
- Super efficient, no lag.
- Simple developer API.
- Commands & enchants on level up.
- Supports blacklisting for blocks and items for XP.
- ActionBar notifications.
- Item lore modification.

## Developer API

### Gradle:

```groovy
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    compileOnly 'com.github.byteful:LevelTools:Tag' // Replace Tag with the version. (Ex: v1.4.0)
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
    <version>Tag</version> <!-- Replace Tag with the version. (Ex: v1.4.0) -->
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
