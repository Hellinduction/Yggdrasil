package dev.heypr.yggdrasil.commands.impl;

import dev.heypr.yggdrasil.Yggdrasil;
import dev.heypr.yggdrasil.data.PlayerData;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public final class RealNameCommand implements CommandExecutor {
    private final Yggdrasil plugin;

    public RealNameCommand(final Yggdrasil plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!ShuffleNamesCommand.isEnabled()) {
            sender.sendMessage(ChatColor.RED + "Nobody's name is shuffled.");
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /realname <username>");
            return true;
        }

        final String username = args[0];
        final PlayerData data = PlayerData.fromDisguiseName(username);

        if (data == null) {
            sender.sendMessage(ChatColor.RED + "The player data of that player could not be found.");
            return true;
        }

        sender.sendMessage(ChatColor.GREEN + String.format("The real name of %s is %s.", data.getDisguiseData().getUsername(), data.getUsername()));
        return true;
    }
}