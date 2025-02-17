package dev.heypr.yggdrasil.commands.impl;

import dev.heypr.yggdrasil.Yggdrasil;
import dev.heypr.yggdrasil.data.PlayerData;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public final class ListBoogeyMenCommand implements CommandExecutor {
    private final Yggdrasil plugin;

    public ListBoogeyMenCommand(final Yggdrasil plugin) {
        this.plugin = plugin;
    }

    private String formatStringList(final Collection<String> list) {
        if (list.isEmpty())
            return ChatColor.GRAY + ".";

        final StringBuilder builder = new StringBuilder();

        for (final String str : list)
            builder.append(ChatColor.GREEN + str + ChatColor.GRAY + ", ");

        return builder.substring(0, builder.length() - 2) + ChatColor.GRAY + ".";
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        final List<String> boogeyMen = plugin.getPlayerData().values().stream()
                .filter(PlayerData::isBoogeyman)
                .map(PlayerData::getDisplayName)
                .collect(Collectors.toList());

        final String list = this.formatStringList(boogeyMen);

        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format("&7Boogeymen (%s): %s", boogeyMen.size(), list)));
        return true;
    }
}