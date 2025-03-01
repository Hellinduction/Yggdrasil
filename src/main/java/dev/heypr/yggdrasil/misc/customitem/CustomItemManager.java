package dev.heypr.yggdrasil.misc.customitem;

import dev.heypr.yggdrasil.misc.customitem.items.PlayerTracker;
import org.bukkit.ChatColor;

import java.util.*;
import java.util.stream.Collectors;

public class CustomItemManager {
    private final Map<String, ICustomItem> CUSTOM_ITEMS = new HashMap<>();

    public CustomItemManager() {
        this.registerCustomItems();
    }

    private void registerCustomItems() {
        registerItem(new PlayerTracker());
    }

    public void registerItem(ICustomItem item) {
        CUSTOM_ITEMS.put(item.getRawName().toUpperCase().replace(" ", "_"), item);
    }

    public List<String> getAllItemNames() {
        return CUSTOM_ITEMS.keySet().stream()
                .map(name -> name.toUpperCase().replace(" ", "_"))
                .collect(Collectors.toList());
    }

    public Optional<ICustomItem> getItem(String name) {
        return Optional.ofNullable(CUSTOM_ITEMS.get(ChatColor.stripColor(name).toUpperCase().replace(" ", "_")));
    }

    public Collection<ICustomItem> getItems() {
        return CUSTOM_ITEMS.values();
    }

    public <T extends ICustomItem> T getItem(final Class<? extends ICustomItem> clazz) {
        for (final ICustomItem item : CUSTOM_ITEMS.values()) {
            if (item.getClass().equals(clazz))
                return (T) item;
        }

        return null;
    }
}