package dev.heypr.yggdrasil.commands.impl;

import dev.heypr.yggdrasil.Yggdrasil;
import dev.heypr.yggdrasil.data.PlayerData;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public final class TogglePlayerTracker implements CommandExecutor {
    private final Yggdrasil plugin;

    public TogglePlayerTracker(final Yggdrasil plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        final boolean enabled = isEnabled();

        plugin.getConfig().set("player_tracker_enabled", !enabled);
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format("&7You have toggled player tracker %s&7 for players on their last chance.", !enabled ? "&aon" : "&coff")));
        plugin.saveConfig();

        if (plugin.isSessionRunning) {
            for (final PlayerData data : Yggdrasil.plugin.getPlayerData().values())
                data.checkGivePlayerCompass();
        }

        return true;
    }

    public static boolean isEnabled() {
        return Yggdrasil.plugin.getConfig().contains("player_tracker_enabled") && Yggdrasil.plugin.getConfig().getBoolean("player_tracker_enabled");
    }
}