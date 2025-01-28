package dev.heypr.yggdrasil.events;

import dev.heypr.yggdrasil.Yggdrasil;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;

public final class PlayerChangeWorldListener implements Listener {
    private final Yggdrasil plugin;

    public PlayerChangeWorldListener(final Yggdrasil plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChangeWorld(final PlayerChangedWorldEvent e) {
        final Player player = e.getPlayer();

        if (!plugin.isSessionRunning || !plugin.getPlayerData().containsKey(player.getUniqueId()))
            plugin.getScheduler().runTaskLater(plugin, () -> player.setGameMode(GameMode.ADVENTURE), 10L);
    }
}