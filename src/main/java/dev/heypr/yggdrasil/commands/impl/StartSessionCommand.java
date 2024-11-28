package dev.heypr.yggdrasil.commands.impl;

import dev.heypr.yggdrasil.Yggdrasil;
import dev.heypr.yggdrasil.data.PlayerData;
import dev.heypr.yggdrasil.misc.object.Pair;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public class StartSessionCommand implements CommandExecutor, TabCompleter {

    private final Yggdrasil plugin;

    public StartSessionCommand(Yggdrasil plugin) {
        this.plugin = plugin;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (plugin.isSessionRunning) {
            sender.sendMessage(ChatColor.RED + "Game is already running.");
            return true;
        }

        plugin.isCullingSession = args.length > 0 && Boolean.parseBoolean(args[0]);

        Map<UUID, PlayerData> playerData = plugin.getPlayerData();

        playerData.clear();

        List<Player> players = new ArrayList<>(plugin.getServer().getOnlinePlayers());

        if (players.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "No players online.");
            return true;
        }

        int numBoogeymen = plugin.randomNumber(1, 3);
        List<Player> potentialBoogyMen = plugin.getBoogieManPool();

        for (int i = 0; i < numBoogeymen && i < potentialBoogyMen.size() - 1; i++) {
            final Player boogeyman = potentialBoogyMen.get(i);
            final Pair<Integer, Boolean> pair = PlayerData.retrieveLivesOrDefaultAsPair(boogeyman.getUniqueId(), plugin.randomNumber(2, 6));
            final PlayerData data = new PlayerData(boogeyman.getUniqueId(), pair.getKey());

            playerData.put(boogeyman.getUniqueId(), data);
            playerData.get(boogeyman.getUniqueId()).setBoogeyman(true);
            data.checkDead();

            data.displayLives(pair.getValue());

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
                            }, 60L);
                        }, 20L);
                    }, 20L);
                }, 20L);
            }, 260L);
        }

        final List<Player> playerPool = players;

        sender.sendMessage(ChatColor.GREEN + "The session is starting...");

        playerPool.forEach(player -> {
            final Pair<Integer, Boolean> pair = PlayerData.retrieveLivesOrDefaultAsPair(player.getUniqueId(), plugin.randomNumber(2, 6));
            final PlayerData data = new PlayerData(player.getUniqueId(), pair.getKey());

            playerData.putIfAbsent(player.getUniqueId(), data);
            player.setGameMode(GameMode.SURVIVAL);

            if (plugin.isCullingSession && PlayerData.retrieveLives(player.getUniqueId()) == 0)
                data.setLastChance(true);

            if (!playerData.get(player.getUniqueId()).isBoogeyman()) {
                data.checkDead();
                data.displayLives(pair.getValue());

                plugin.getScheduler().runTaskLater(plugin, () -> {
                    player.sendTitle(ChatColor.GREEN + "3", "", 10, 20, 10);
                    plugin.getScheduler().runTaskLater(plugin, () -> {
                        player.sendTitle(ChatColor.YELLOW + "2", "", 10, 20, 10);
                        plugin.getScheduler().runTaskLater(plugin, () -> {
                            player.sendTitle(ChatColor.RED + "1", "", 10, 20, 10);
                            plugin.getScheduler().runTaskLater(plugin, () -> {
                                player.sendTitle(ChatColor.YELLOW + "You are...", "", 10, 70, 20);
                                plugin.getScheduler().runTaskLater(plugin, () -> {
                                    player.sendTitle(ChatColor.GREEN + "NOT THE BOOGEYMAN!", "", 10, 70, 20);
                                }, 60L);
                            }, 20L);
                        }, 20L);
                    }, 20L);
                }, 260L);
            }
        });

        if (plugin.isCullingSession)
            Bukkit.broadcastMessage(ChatColor.DARK_RED + "" + ChatColor.BOLD + "A culling session has begun!");

        plugin.isSessionRunning = true;
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (args.length != 1)
            return Collections.emptyList();

        return Arrays.asList(String.valueOf(true), String.valueOf(false)).stream()
                .filter(bool -> args[0].isEmpty() || bool.startsWith(args[0].toLowerCase()))
                .collect(Collectors.toList());
    }
}
