package dev.heypr.yggdrasil.commands.impl;

import dev.heypr.yggdrasil.Yggdrasil;
import dev.heypr.yggdrasil.data.PlayerData;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public final class ShuffleNamesCommand implements CommandExecutor {
    private final Yggdrasil plugin;

    public ShuffleNamesCommand(final Yggdrasil plugin) {
        this.plugin = plugin;
    }

    private <T> void modifyList(final List<T> list) {
        final List<T> tempList = new ArrayList<>(list);
        list.clear();

        Collections.shuffle(list);
        int index = 0;

        while (list.size() < tempList.size()) {
            final Random random = new Random();
            final T element = tempList.get(index++);
            final int duplicateCount = random.nextInt(2) + 1;

            for (int i = 0; i <= duplicateCount; i++) {
                if (list.size() < tempList.size())
                    list.add(element);
            }
        }
    }

    private void loopThroughPairs(final boolean multi, final BiConsumer<OfflinePlayer, OfflinePlayer> callback) {
        final Collection<PlayerData> playerData = plugin.getPlayerData().values();

        final List<OfflinePlayer> players = playerData.stream()
                .filter(data -> data.getLives() > 0)
                .map(data -> data.getOfflinePlayer())
                .collect(Collectors.toUnmodifiableList());

        final List<OfflinePlayer> shuffledPlayers = new ArrayList<>(players);
        Collections.shuffle(shuffledPlayers);

        final int size = shuffledPlayers.size();

        if (multi) {
            final List<OfflinePlayer> clone = new ArrayList<>(shuffledPlayers);
            this.modifyList(clone);

            for (int i = 0; i < size; ++i) {
                final OfflinePlayer current = shuffledPlayers.get(i);
                final OfflinePlayer next = clone.get(i);

                callback.accept(current, next);
            }
            return;
        }

        for (int i = 0; i < size; i++) {
            final OfflinePlayer current = shuffledPlayers.get(i);
            final OfflinePlayer next = shuffledPlayers.get((i + 1) % size);

            callback.accept(current, next);
        }
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (plugin.getPlayerData().size() < 2) {
            sender.sendMessage(ChatColor.RED + "There are not enough players to shuffle names.");
            return true;
        }

        final Collection<PlayerData> playerData = plugin.getPlayerData().values();
        final boolean multi = args.length > 0 && Boolean.parseBoolean(args[0]);

        final Map<UUID, UUID> disguiseMap = plugin.getDisguiseMap();

        this.loopThroughPairs(multi, (current, next) -> disguiseMap.put(current.getUniqueId(), next.getUniqueId()));

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