package dev.heypr.yggdrasil.events;

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import dev.heypr.yggdrasil.Yggdrasil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

public class PlayerRespawnListener implements Listener {

    private final Yggdrasil plugin;

    public PlayerRespawnListener(Yggdrasil plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerRespawn(PlayerPostRespawnEvent event) {
        Player player = event.getPlayer();

        if (!plugin.getPlayerData().containsKey(player.getUniqueId())) {
            plugin.getServer().getOperators().forEach(op -> op.getPlayer().sendMessage(ChatColor.DARK_RED + "[URGENT] Player " + player.getName() + " does not have game data. They will be treated as a dead player. Add them to the list of players using /addplayer <player>"));
            return;
        }

        if (plugin.getPlayerData().get(player.getUniqueId()).getLives() == 0) {
            player.setCollidable(false);
        }

    }
}
