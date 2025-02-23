package dev.heypr.yggdrasil.commands.impl;

import dev.heypr.yggdrasil.Yggdrasil;
import dev.heypr.yggdrasil.data.PlayerData;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public final class BoogeyMenCountCommand implements CommandExecutor {
    private final Yggdrasil plugin;

    public BoogeyMenCountCommand(final Yggdrasil plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        final long count = plugin.getPlayerData().values().stream()
                .filter(PlayerData::isBoogeyman)
                .count();

        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format("&7Boogeymen count: &a%s", count)));
        return true;
    }
}