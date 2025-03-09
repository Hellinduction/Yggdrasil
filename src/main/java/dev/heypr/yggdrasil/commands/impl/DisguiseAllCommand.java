package dev.heypr.yggdrasil.commands.impl;

import dev.heypr.yggdrasil.Yggdrasil;
import dev.heypr.yggdrasil.data.PlayerData;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public final class DisguiseAllCommand implements CommandExecutor {
    private final Yggdrasil plugin;

    public DisguiseAllCommand(final Yggdrasil plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (plugin.getPlayerData().size() < 2) {
            sender.sendMessage(ChatColor.RED + "There are not enough players to disguise all.");
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /disguiseall <player>");
            return true;
        }

        final PlayerData targetData = PlayerData.fromUsername(args[0]);

        if (targetData == null) {
            sender.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }

        final Collection<PlayerData> playerData = plugin.getPlayerData().values();
        final Map<UUID, UUID> disguiseMap = plugin.getDisguiseMap();

        disguiseMap.clear();

        for (final PlayerData data : playerData) {
            if (data.equals(targetData))
                continue;

            disguiseMap.put(data.getUuid(), targetData.getUuid());
        }

        for (final PlayerData data : playerData) {
            if (data.isOnline())
                data.update(PlayerData.UpdateFrom.SHUFFLE_NAMES, -1);
        }

        sender.sendMessage(ChatColor.GREEN + String.format("Attempted to disguise everyone as %s.", targetData.getDisplayName()));
        return true;
    }
}