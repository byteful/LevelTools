package me.byteful.plugin.leveltools.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

final class AdventureHelper {
    private AdventureHelper() {
    }

    static void setDisplayNameWithTranslatable(ItemMeta meta, String text, ItemStack stack) {
        Component translatable = Component.translatable(stack.getType().translationKey());
        TextComponent parsed = LegacyComponentSerializer.legacySection().deserialize(text);

        TextComponent.Builder result = Component.text();
        processComponent(parsed, translatable, result);

        meta.displayName(result.build());
    }

    private static void processComponent(TextComponent component, Component translatable, TextComponent.Builder result) {
        String content = component.content();
        if (content.contains("{item}")) {
            String[] parts = content.split("\\{item}", -1);
            for (int i = 0; i < parts.length; i++) {
                if (!parts[i].isEmpty()) {
                    result.append(Component.text(parts[i]).style(component.style()));
                }
                if (i < parts.length - 1) {
                    result.append(translatable);
                }
            }
        } else if (!content.isEmpty()) {
            result.append(Component.text(content).style(component.style()));
        }

        for (Component child : component.children()) {
            if (child instanceof TextComponent) {
                processComponent((TextComponent) child, translatable, result);
            } else {
                result.append(child);
            }
        }
    }
}
