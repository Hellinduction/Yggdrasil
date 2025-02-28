package dev.heypr.yggdrasil.commands.impl;

import dev.heypr.yggdrasil.Yggdrasil;
import dev.heypr.yggdrasil.data.PlayerData;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public final class ToggleGlowCommand implements CommandExecutor {
    private static final int DISTANCE = 15; // If another player is within this distance of the player being checked, it will exclude them from the check, as they could be teaming

    private final Yggdrasil plugin;

    private BukkitTask task;

    public ToggleGlowCommand(final Yggdrasil plugin) {
        this.plugin = plugin;

        this.startScheduler();
    }

    private boolean canBeSeen(final Player target) {
        final Collection<PlayerData> playerData = plugin.getPlayerData().values();

        final List<Player> players = playerData.stream()
                .filter(data -> data.getLives() > 0)
                .filter(PlayerData::isOnline)
                .map(data -> data.getPlayer())
                .filter(player -> !target.equals(player))
                .filter(player -> target.getLocation().distance(player.getLocation()) > DISTANCE)
                .collect(Collectors.toUnmodifiableList());

        return players.isEmpty() || players.stream().anyMatch(player -> !player.canSee(target) || player.hasLineOfSight(target));
    }

    private void startScheduler() {
        final boolean enabled = isEnabled();

        if (!enabled)
            return;

        if (this.task != null)
            this.task.cancel();

        this.task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!plugin.isSessionRunning)
                    return;

                final Collection<PlayerData> playerData = plugin.getPlayerData().values();
                final List<Player> players = playerData.stream()
                        .filter(data -> data.getLives() > 0)
                        .filter(PlayerData::isOnline)
                        .map(data -> data.getPlayer())
                        .collect(Collectors.toUnmodifiableList());

                for (final Player player : players) {
                    final boolean canBeSeen = canBeSeen(player);
                    player.setGlowing(!canBeSeen);
                }
            }
        }.runTaskTimer(plugin, 10L, 10L);
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        final boolean enabled = isEnabled();

        plugin.getConfig().set("glow_enabled", !enabled);
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format("&7You have toggled glow effect %s&7 for players who are hiding.", !enabled ? "&aon" : "&coff")));
        plugin.saveConfig();

        if (!enabled)
            this.startScheduler();
        else if (this.task != null)
            this.task.cancel();

        return true;
    }

    public static boolean isEnabled() {
        return Yggdrasil.plugin.getConfig().contains("glow_enabled") && Yggdrasil.plugin.getConfig().getBoolean("glow_enabled");
    }
}