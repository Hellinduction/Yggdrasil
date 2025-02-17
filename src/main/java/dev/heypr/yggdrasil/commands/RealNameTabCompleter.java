package dev.heypr.yggdrasil.commands;

import dev.heypr.yggdrasil.Yggdrasil;
import dev.heypr.yggdrasil.commands.impl.ShuffleNamesCommand;
import dev.heypr.yggdrasil.data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class RealNameTabCompleter implements TabCompleter {
    private final Yggdrasil plugin;
    private final List<Integer> argumentPositions;

    public RealNameTabCompleter(final Yggdrasil plugin, final Integer... argumentPositions) {
        this.plugin = plugin;
        this.argumentPositions = argumentPositions.length == 0 ? Arrays.asList(1) : Arrays.asList(argumentPositions);
    }

    @Override
    public @Nullable List<String> onTabComplete(final CommandSender sender, final Command command, final String label, final String[] args) {
        final int argsLength = args.length;

        if (!this.argumentPositions.contains(argsLength))
            return Collections.emptyList();

        final String arg = args[argsLength - 1];

        if (!this.plugin.isSessionRunning || !ShuffleNamesCommand.isEnabled())
            return Bukkit.getOnlinePlayers().stream()
                    .map(player -> player.getName())
                    .filter(str -> arg.isEmpty() || str.toLowerCase().startsWith(arg.toLowerCase()))
                    .collect(Collectors.toUnmodifiableList());

        return plugin.getPlayerData().values().stream()
                .filter(PlayerData::isOnline)
                .map(PlayerData::getUsername)
                .filter(str -> arg.isEmpty() || str.toLowerCase().startsWith(arg.toLowerCase()))
                .collect(Collectors.toUnmodifiableList());
    }
}