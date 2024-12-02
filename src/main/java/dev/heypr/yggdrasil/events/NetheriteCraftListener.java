package dev.heypr.yggdrasil.events;

import dev.heypr.yggdrasil.Yggdrasil;
import dev.heypr.yggdrasil.commands.impl.ToggleNetheriteCommand;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;

public final class NetheriteCraftListener implements Listener {
    private final Yggdrasil plugin;

    public NetheriteCraftListener(final Yggdrasil plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onOpenInventory(final InventoryOpenEvent e) {
        if (e.getInventory().getType() != InventoryType.SMITHING)
            return;

        if (!ToggleNetheriteCommand.isDisabled())
            return;

        e.setCancelled(true);
        e.getPlayer().sendMessage(ChatColor.RED + "The smithing table is currently disabled.");
    }
}