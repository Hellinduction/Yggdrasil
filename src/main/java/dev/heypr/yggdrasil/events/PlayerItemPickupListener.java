package dev.heypr.yggdrasil.events;

import dev.heypr.yggdrasil.Yggdrasil;
import dev.heypr.yggdrasil.data.PlayerData;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;

public final class PlayerItemPickupListener implements Listener {
    private final Yggdrasil plugin;

    public PlayerItemPickupListener(final Yggdrasil plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerItemPickup(final EntityPickupItemEvent e) {
        final LivingEntity entity = e.getEntity();

        if (!(entity instanceof Player player))
            return;

        final PlayerData data = plugin.getPlayerData().get(player.getUniqueId());

        if (data == null || data.isDead())
            e.setCancelled(true);
    }
}