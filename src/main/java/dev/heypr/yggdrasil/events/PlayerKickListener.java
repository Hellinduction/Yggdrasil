package dev.heypr.yggdrasil.events;

import dev.heypr.yggdrasil.Yggdrasil;
import dev.heypr.yggdrasil.commands.impl.ShuffleNamesCommand;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerKickEvent;

public final class PlayerKickListener implements Listener {
    private final Yggdrasil plugin;

    public PlayerKickListener(final Yggdrasil plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerKick(final PlayerKickEvent e) {
        if (!ShuffleNamesCommand.isEnabled())
            return;

        if (!e.getReason().equals("You logged in from another location"))
            return;

        e.setCancelled(true);
    }
}