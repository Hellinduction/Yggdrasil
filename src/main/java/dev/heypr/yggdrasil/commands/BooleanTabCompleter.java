package dev.heypr.yggdrasil.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class BooleanTabCompleter implements TabCompleter {
    @Override
    public @Nullable List<String> onTabComplete(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (args.length != 1)
            return Collections.emptyList();

        return Arrays.asList(String.valueOf(true), String.valueOf(false)).stream()
                .filter(bool -> args[0].isEmpty() || bool.startsWith(args[0].toLowerCase()))
                .collect(Collectors.toList());
    }
}
