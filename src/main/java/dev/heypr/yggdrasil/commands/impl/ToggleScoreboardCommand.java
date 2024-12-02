package dev.heypr.yggdrasil.commands.impl;

import dev.heypr.yggdrasil.Yggdrasil;
import dev.heypr.yggdrasil.data.PlayerData;
import dev.heypr.yggdrasil.misc.ColorManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class ToggleScoreboardCommand implements CommandExecutor {
    private final Yggdrasil plugin;

    public ToggleScoreboardCommand(final Yggdrasil plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        final boolean enabled = isEnabled();

        plugin.getConfig().set("scoreboard_enabled", !enabled);
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format("&7You have toggled the scoreboard %s&7.", !enabled ? "&aon" : "&coff")));
        plugin.saveConfig();

        if (!enabled && plugin.getScoreboard() == null) {
            plugin.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());

            for (final Player player : Bukkit.getOnlinePlayers()) {
                player.setScoreboard(plugin.getScoreboard());

                final PlayerData data = plugin.getPlayerData().get(player.getUniqueId());

                if (data == null)
                    continue;

                ColorManager.setTabListName(player, data);
            }
        }

        return true;
    }

    public static boolean isEnabled() {
        return Yggdrasil.plugin.getConfig().contains("scoreboard_enabled") && Yggdrasil.plugin.getConfig().getBoolean("scoreboard_enabled");
    }
}