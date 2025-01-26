package dev.heypr.yggdrasil.commands.impl;

import dev.heypr.yggdrasil.Yggdrasil;
import dev.heypr.yggdrasil.data.PlayerData;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RandomizeBoogeymanCommand implements CommandExecutor {

    private final Yggdrasil plugin;

    public RandomizeBoogeymanCommand(Yggdrasil plugin) {
        this.plugin = plugin;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<Player> players = new ArrayList<>(plugin.getServer().getOnlinePlayers());
        Map<UUID, PlayerData> playerData = plugin.getPlayerData();

        if (players.isEmpty()) return true;

        for (final PlayerData data : playerData.values()) {
            data.setRevealedData(false);

            if (data.isBoogeyman()) {
                data.setBoogeyman(false);

                final Player player = data.getPlayer();

                if (player != null && player.isOnline())
                    player.sendMessage(ChatColor.RED + "You have been cured! You are no longer the boogeyman.");
            }
        }

        int numBoogeymen = plugin.randomNumber(1, 3);
        List<Player> boogeyMen = plugin.pickBoogeyMen(numBoogeymen);

        for (final Player boogeyman : boogeyMen) {
            final int lives = PlayerData.retrieveLivesOrDefault(boogeyman.getUniqueId(), plugin.randomNumber(2, 6));
            playerData.putIfAbsent(boogeyman.getUniqueId(), new PlayerData(boogeyman.getUniqueId(), lives));
            playerData.get(boogeyman.getUniqueId()).setBoogeyman(true);

            final PlayerData data = playerData.get(boogeyman.getUniqueId());

            plugin.getScheduler().runTaskLater(plugin, () -> {
                boogeyman.sendTitle(ChatColor.GREEN + "3", "", 10, 20, 10);
                plugin.getScheduler().runTaskLater(plugin, () -> {
                    boogeyman.sendTitle(ChatColor.YELLOW + "2", "", 10, 20, 10);
                    plugin.getScheduler().runTaskLater(plugin, () -> {
                        boogeyman.sendTitle(ChatColor.RED + "1", "", 10, 20, 10);
                        plugin.getScheduler().runTaskLater(plugin, () -> {
                            boogeyman.sendTitle(ChatColor.YELLOW + "You are...", "", 10, 70, 20);
                            plugin.getScheduler().runTaskLater(plugin, () -> {
                                boogeyman.sendTitle(ChatColor.RED + "THE BOOGEYMAN!", "", 10, 70, 20);

                                boogeyman.sendMessage(ChatColor.translateAlternateColorCodes('&', "&4&lYou are the &6&lBoogeyman&4&l!!!"));
                                boogeyman.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7&lRemember, as the &6&lBoogeyman&7&l your goal is to kill 1 player during this session."));

                                data.setRevealedData(true);
                            }, 60L);
                        }, 20L);
                    }, 20L);
                }, 20L);
            }, 40L);
        }

        players.forEach(player -> {
            if (!playerData.get(player.getUniqueId()).isBoogeyman()) {
                plugin.getScheduler().runTaskLater(plugin, () -> {
                    final int lives = PlayerData.retrieveLivesOrDefault(player.getUniqueId(), plugin.randomNumber(2, 6));
                    playerData.putIfAbsent(player.getUniqueId(), new PlayerData(player.getUniqueId(), lives));

                    final boolean showTitle = plugin.isCullingSession || PlayerData.retrieveLives(player.getUniqueId()) != 0;
                    final PlayerData data = playerData.get(player.getUniqueId());

                    if (showTitle) {
                        player.sendTitle(ChatColor.GREEN + "3", "", 10, 20, 10);
                        plugin.getScheduler().runTaskLater(plugin, () -> {
                            player.sendTitle(ChatColor.YELLOW + "2", "", 10, 20, 10);
                            plugin.getScheduler().runTaskLater(plugin, () -> {
                                player.sendTitle(ChatColor.RED + "1", "", 10, 20, 10);
                                plugin.getScheduler().runTaskLater(plugin, () -> {
                                    player.sendTitle(ChatColor.YELLOW + "You are...", "", 10, 70, 20);
                                    plugin.getScheduler().runTaskLater(plugin, () -> {
                                        player.sendTitle(ChatColor.GREEN + "NOT THE BOOGEYMAN!", "", 10, 70, 20);

                                        data.setRevealedData(true);
                                    }, 60L);
                                }, 20L);
                            }, 20L);
                        }, 20L);
                    }
                }, 40L);
            }
        });

        return true;
    }
}
