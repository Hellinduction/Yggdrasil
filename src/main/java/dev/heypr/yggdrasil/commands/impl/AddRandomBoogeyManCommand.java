package dev.heypr.yggdrasil.commands.impl;

import dev.heypr.yggdrasil.Yggdrasil;
import dev.heypr.yggdrasil.data.PlayerData;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public final class AddRandomBoogeyManCommand implements CommandExecutor {
    private final Yggdrasil plugin;

    public AddRandomBoogeyManCommand(Yggdrasil plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        final Map<UUID, PlayerData> playerData = plugin.getPlayerData();

        if (playerData.isEmpty() || playerData.values().stream()
                .allMatch(data -> data.isBoogeyman())) {
            sender.sendMessage(ChatColor.RED + "There are not enough players for there to be another boogeyman.");
            return true;
        }

        int count = 1;

        if (args.length > 0) {
            try {
                count = Integer.parseInt(args[0]);
            } catch (final NumberFormatException ignored) {}
        }

        final List<Player> boogeyMen = plugin.pickBoogeyMen(count, player -> {
            final PlayerData data = plugin.getPlayerData().get(player.getUniqueId());

            if (data == null)
                return false;

            return !data.isBoogeyman();
        });

        if (boogeyMen.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "There was not applicable boogeyman that could be added.");
            return true;
        }

        for (final Player boogeyman : boogeyMen) {
            final int lives = PlayerData.retrieveLivesOrDefault(boogeyman.getUniqueId(), plugin.randomLives());

            playerData.putIfAbsent(boogeyman.getUniqueId(), new PlayerData(boogeyman, lives));

            final PlayerData data = playerData.get(boogeyman.getUniqueId());

            data.setRevealedData(false);
            data.setBoogeyman(true);

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
                                boogeyman.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7&lRemember, as the &6&lBoogeyman&7&l your goal is to kill 1 player during this session."));

                                data.setRevealedData(true);
                            }, 60L, true);
                        }, 20L, true);
                    }, 20L, true);
                }, 20L, true);
            }, 40L, true);
        }

        final List<Player> players = plugin.getServer().getOnlinePlayers().stream()
                .filter(player -> playerData.containsKey(player.getUniqueId()))
                .filter(player -> !boogeyMen.contains(player))
                .filter(player -> !playerData.get(player.getUniqueId()).isDead())
                .collect(Collectors.toUnmodifiableList());

        players.forEach(player -> {
            if (!playerData.get(player.getUniqueId()).isBoogeyman()) {
                plugin.getSchedulerWrapper().runTaskLater(plugin, () -> {
                    final int lives = PlayerData.retrieveLivesOrDefault(player.getUniqueId(), plugin.randomLives());
                    playerData.putIfAbsent(player.getUniqueId(), new PlayerData(player, lives));

                    final boolean showTitle = plugin.isCullingSession || PlayerData.retrieveLives(player.getUniqueId()) != 0;
                    final PlayerData data = playerData.get(player.getUniqueId());

                    if (showTitle) {
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
                    }
                }, 40L, true);
            }
        });

        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format("&7Successfully added &a%s&7 random boogeymen.", count)));
        return true;
    }
}