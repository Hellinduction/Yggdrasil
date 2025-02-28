package dev.heypr.yggdrasil.commands.impl;

import dev.heypr.yggdrasil.Yggdrasil;
import dev.heypr.yggdrasil.data.PlayerData;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Collection;

public final class UnshuffleNamesCommand implements CommandExecutor {
    private final Yggdrasil plugin;

    public UnshuffleNamesCommand(final Yggdrasil plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        plugin.getDisguiseMap().clear();

        final Collection<PlayerData> playerData = plugin.getPlayerData().values();
        playerData.forEach(data -> data.update(PlayerData.UpdateFrom.COMMAND, -1));

        sender.sendMessage(ChatColor.GREEN + "Reset everyone's name and skin.");
        return true;
    }
}