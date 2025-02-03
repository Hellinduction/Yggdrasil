package dev.heypr.yggdrasil.events;

import dev.heypr.yggdrasil.Yggdrasil;
import io.papermc.paper.event.player.PrePlayerAttackEntityEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class PlayerPreSessionStartAttackListener implements Listener {

    private final Yggdrasil plugin;

    public PlayerPreSessionStartAttackListener(Yggdrasil plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerPreSessionStartAttack(PrePlayerAttackEntityEvent event) {
        if (!plugin.isSessionRunning) {
            event.getPlayer().sendMessage("The session has not started yet!");
            event.setCancelled(true);
        }
    }
}
