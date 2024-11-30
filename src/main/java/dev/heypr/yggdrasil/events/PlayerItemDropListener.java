package dev.heypr.yggdrasil.events;

import dev.heypr.yggdrasil.Yggdrasil;
import dev.heypr.yggdrasil.data.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;

public final class PlayerItemDropListener implements Listener {
    private final Yggdrasil plugin;

    public PlayerItemDropListener(final Yggdrasil plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerItemDrop(final PlayerDropItemEvent e) {
        final Player player = e.getPlayer();
        final PlayerData data = plugin.getPlayerData().get(player.getUniqueId());

        if (data == null || data.isDead())
            e.setCancelled(true);
    }
}