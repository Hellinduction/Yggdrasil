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

        final List<Player> boogeyMen = plugin.pickBoogeyMen(1);

        if (boogeyMen.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "There was applicable boogeyman that could be added.");
            return true;
        }

        final Player boogeyman = boogeyMen.get(0);
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

        sender.sendMessage(ChatColor.GREEN + "Successfully added a random boogeyman.");
        return true;
    }
}