package dev.heypr.yggdrasil.commands.impl;

import dev.heypr.yggdrasil.Yggdrasil;
import dev.heypr.yggdrasil.data.PlayerData;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public final class WipeLivesCommand implements CommandExecutor {
    private final Yggdrasil plugin;

    public WipeLivesCommand(final Yggdrasil plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (Yggdrasil.plugin.isSessionRunning) {
            sender.sendMessage(ChatColor.RED + "The session cannot be started when using this command.");
            return true;
        }

        final int size = PlayerData.clearPlayers();

        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format("&a%s.", size != -1 ? String.format("Successfully cleared the players data of %s players", size) : "No player data found")));
        return true;
    }
}