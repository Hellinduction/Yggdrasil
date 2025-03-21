package dev.heypr.yggdrasil.commands.impl;

import dev.heypr.yggdrasil.Yggdrasil;
import dev.heypr.yggdrasil.data.PlayerData;
import dev.heypr.yggdrasil.data.TemporaryPlayerData;
import dev.heypr.yggdrasil.misc.object.Pair;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class StartSessionCommand implements CommandExecutor {

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

        int numBoogeymen = plugin.randomBoogeyMenCount();
        List<Player> boogeyMen = plugin.pickBoogeyMen(numBoogeymen);

        for (final Player boogeyman : boogeyMen) {
            final Pair<Integer, Boolean> pair = PlayerData.retrieveLivesOrDefaultAsPair(boogeyman.getUniqueId(), plugin.randomLives());
            final PlayerData data = new PlayerData(boogeyman, pair.getKey());

            playerData.put(boogeyman.getUniqueId(), data);
            playerData.get(boogeyman.getUniqueId()).setBoogeyman(true);
            data.checkLives();

            data.displayLives(pair.getValue());

            plugin.getSchedulerWrapper().runTaskLater(plugin, () -> {
                boogeyman.sendTitle(ChatColor.GREEN + "3", "", 10, 20, 10);
                plugin.getSchedulerWrapper().runTaskLater(plugin, () -> {
                    boogeyman.sendTitle(ChatColor.YELLOW + "2", "", 10, 20, 10);
                    plugin.getSchedulerWrapper().runTaskLater(plugin, () -> {
                        boogeyman.sendTitle(ChatColor.RED + "1", "", 10, 20, 10);
                        plugin.getSchedulerWrapper().runTaskLater(plugin, () -> {
                            boogeyman.sendTitle(ChatColor.YELLOW + "You are...", "", 10, 70, 20);
                            plugin.getSchedulerWrapper().runTaskLater(plugin, () -> {
                                boogeyman.sendTitle(ChatColor.RED + "THE BOOGEYMAN!", "", 10, 70, 20);

                                boogeyman.sendMessage(ChatColor.translateAlternateColorCodes('&', "&4&lYou are the &6&lBoogeyman&4&l!!!"));
                                boogeyman.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7&lRemember, as the &6&lBoogeyman&7&l your goal is to kill 1 &c&lnon-red&7&l name during this session."));

                                data.setRevealedData(true);
                            }, 60L, true);
                        }, 20L, true);
                    }, 20L, true);
                }, 20L, true);
            }, 260L, true);
        }

        final List<Player> playerPool = players;

        sender.sendMessage(ChatColor.GREEN + "The session is starting...");

        playerPool.forEach(player -> {
            final Pair<Integer, Boolean> pair = PlayerData.retrieveLivesOrDefaultAsPair(player.getUniqueId(), plugin.randomLives());
            final PlayerData data = new PlayerData(player, pair.getKey());

            playerData.putIfAbsent(player.getUniqueId(), data);
            player.setGameMode(GameMode.SURVIVAL);

            player.clearActivePotionEffects();

            final TemporaryPlayerData temporaryPlayerData = TemporaryPlayerData.get(player);

            for (final PotionEffect effect : temporaryPlayerData.getEffects())
                player.addPotionEffect(effect);

            if (plugin.isCullingSession && PlayerData.retrieveLives(player.getUniqueId()) == 0)
                data.setLastChance(true);

            if (!playerData.get(player.getUniqueId()).isBoogeyman()) {
                data.checkLives();
                data.displayLives(pair.getValue());

                final boolean showTitle = plugin.isCullingSession || PlayerData.retrieveLives(player.getUniqueId()) != 0;

                if (showTitle) {
                    plugin.getSchedulerWrapper().runTaskLater(plugin, () -> {
                        player.sendTitle(ChatColor.GREEN + "3", "", 10, 20, 10);
                        plugin.getSchedulerWrapper().runTaskLater(plugin, () -> {
                            player.sendTitle(ChatColor.YELLOW + "2", "", 10, 20, 10);
                            plugin.getSchedulerWrapper().runTaskLater(plugin, () -> {
                                player.sendTitle(ChatColor.RED + "1", "", 10, 20, 10);
                                plugin.getSchedulerWrapper().runTaskLater(plugin, () -> {
                                    player.sendTitle(ChatColor.YELLOW + "You are...", "", 10, 70, 20);
                                    plugin.getSchedulerWrapper().runTaskLater(plugin, () -> {
                                        player.sendTitle(ChatColor.GREEN + "NOT THE BOOGEYMAN!", "", 10, 70, 20);

                                        data.setRevealedData(true);
                                    }, 60L, true);
                                }, 20L, true);
                            }, 20L, true);
                        }, 20L, true);
                    }, 260L, true);
                }
            }
        });

        if (plugin.isCullingSession)
            Bukkit.broadcastMessage(ChatColor.DARK_RED + "" + ChatColor.BOLD + "A culling session has begun!");

        plugin.isSessionRunning = true;
        return true;
    }
}
