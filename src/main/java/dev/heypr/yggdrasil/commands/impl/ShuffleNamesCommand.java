package dev.heypr.yggdrasil.commands.impl;

import dev.heypr.yggdrasil.Yggdrasil;
import dev.heypr.yggdrasil.data.PlayerData;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.*;
import java.util.stream.Collectors;

public final class ShuffleNamesCommand implements CommandExecutor {
    private final Yggdrasil plugin;

    public ShuffleNamesCommand(final Yggdrasil plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        final Collection<PlayerData> playerData = plugin.getPlayerData().values();

        if (plugin.getPlayerData().size() < 2) {
            sender.sendMessage(ChatColor.RED + "There are not enough players to shuffle names.");
            return true;
        }

        final List<OfflinePlayer> players = playerData.stream()
                .filter(data -> data.getLives() > 0)
                .map(data -> data.getOfflinePlayer())
                .collect(Collectors.toUnmodifiableList());

        final List<OfflinePlayer> shuffledPlayers = new ArrayList<>(players);
        Collections.shuffle(shuffledPlayers);

        final Map<UUID, UUID> disguiseMap = plugin.getDisguiseMap();
        final int size = shuffledPlayers.size();

        for (int i = 0; i < size; i++) {
            final OfflinePlayer current = shuffledPlayers.get(i);
            final OfflinePlayer next = shuffledPlayers.get((i + 1) % size);

            disguiseMap.put(current.getUniqueId(), next.getUniqueId());
        }

        for (final PlayerData data : playerData) {
            if (data.isOnline())
                data.update(PlayerData.UpdateFrom.SHUFFLE_NAMES, -1);
        }

        sender.sendMessage(ChatColor.GREEN + "Attempted to shuffle names/skins.");
        return true;
    }

    public static boolean isEnabled() {
        return !Yggdrasil.plugin.getDisguiseMap().isEmpty();
    }
}