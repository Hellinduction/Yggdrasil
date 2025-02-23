package dev.heypr.yggdrasil.commands.impl;

import dev.heypr.yggdrasil.Yggdrasil;
import dev.heypr.yggdrasil.data.PlayerData;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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

        final List<Player> players = playerData.stream()
                .filter(PlayerData::isOnline)
                .filter(data -> data.getLives() > 0)
                .map(data -> data.getPlayer())
                .collect(Collectors.toUnmodifiableList());

        final List<Player> shuffledPlayers = new ArrayList<>(players);
        Collections.shuffle(shuffledPlayers);

        final Map<UUID, UUID> disguiseMap = plugin.getDisguiseMap();
        final int size = shuffledPlayers.size();

        for (int i = 0; i < size; i++) {
            final Player current = shuffledPlayers.get(i);
            final Player next = shuffledPlayers.get((i + 1) % size);

            disguiseMap.put(current.getUniqueId(), next.getUniqueId());
        }

        for (final PlayerData data : playerData)
            data.update(PlayerData.UpdateFrom.SHUFFLE_NAMES, -1);

        sender.sendMessage(ChatColor.GREEN + "Attempted to shuffle names/skins.");
        return true;
    }

    public static boolean isEnabled() {
        return !Yggdrasil.plugin.getDisguiseMap().isEmpty();
    }
}