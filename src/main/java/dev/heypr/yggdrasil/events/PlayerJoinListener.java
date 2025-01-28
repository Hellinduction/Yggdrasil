package dev.heypr.yggdrasil.events;

import dev.heypr.yggdrasil.Yggdrasil;
import dev.heypr.yggdrasil.commands.impl.ToggleScoreboardCommand;
import dev.heypr.yggdrasil.data.PlayerData;
import dev.heypr.yggdrasil.misc.ColorManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    private final Yggdrasil plugin;

    public PlayerJoinListener(Yggdrasil plugin) {
        this.plugin = plugin;
    }

    @SuppressWarnings("all")
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (ToggleScoreboardCommand.isEnabled()) {
            if (plugin.getScoreboard() == null)
                plugin.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());

            player.setScoreboard(plugin.getScoreboard());
        }

        if (!plugin.isSessionRunning) {
            plugin.getScheduler().runTaskLater(plugin, () -> player.setGameMode(GameMode.ADVENTURE), 10L);

            player.sendTitle(ChatColor.RED + "Game not started", "Please wait for the game to start", 10, 70, 20);
            return;
        }

        if (!plugin.getPlayerData().containsKey(player.getUniqueId())) {
            plugin.getScheduler().runTaskLater(plugin, () -> player.setGameMode(GameMode.ADVENTURE), 10L);

            player.sendTitle(ChatColor.RED + "Game in progress", ChatColor.RED + "You are not part of the game", 10, 70, 20);
            player.sendMessage(ChatColor.RED + "Game in progress. Request an admin to add you to the game using /addplayer <player>");

            plugin.getServer().getOperators().forEach((op) -> {
                if (!(op.isOnline())) return;
                op.getPlayer().sendMessage(ChatColor.DARK_RED + "[URGENT] Player " + player.getName() + " does not have game data. They will be treated as a dead player. Add them to the list of players using /addplayer <player>");
                return;
            });
            return;
        }

        final PlayerData playerData = plugin.getPlayerData().get(player.getUniqueId());

        playerData.update(-1);
        plugin.getSchedulerWrapper().runTaskLater(plugin, () -> playerData.checkLives(), 10L, true);

        ColorManager.setTabListName(player, plugin.getPlayerData().get(player.getUniqueId()));

        plugin.getSchedulerWrapper().runTaskLater(plugin, () -> ColorManager.setTabListName(player, plugin.getPlayerData().get(player.getUniqueId())), 20L, true); // To fix it incase the skin thing removes it
    }
}
