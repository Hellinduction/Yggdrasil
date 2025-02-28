package dev.heypr.yggdrasil.events;

import dev.heypr.yggdrasil.Yggdrasil;
import io.papermc.paper.event.player.PrePlayerAttackEntityEvent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;

public class PlayerPreSessionStartAttackListener implements Listener {

    private final Yggdrasil plugin;

    public PlayerPreSessionStartAttackListener(Yggdrasil plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerPreSessionStartAttack(PrePlayerAttackEntityEvent event) {
        if (!plugin.isSessionRunning) {
            event.getPlayer().sendMessage(ChatColor.DARK_RED + "The session has not started yet!");
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onMobTarget(final EntityTargetEvent e) {
        if (!plugin.isSessionRunning)
            e.setCancelled(true);
    }

    @EventHandler
    public void onEntityDamage(final EntityDamageEvent e) {
        if (!plugin.isSessionRunning && e.getEntity() instanceof Player)
            e.setCancelled(true);
    }

    @EventHandler
    public void onExplosionPrime(final ExplosionPrimeEvent e) {
        if (!plugin.isSessionRunning)
            e.setCancelled(true);
    }
}
