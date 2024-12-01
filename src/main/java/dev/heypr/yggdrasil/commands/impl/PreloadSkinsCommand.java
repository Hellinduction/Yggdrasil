package dev.heypr.yggdrasil.commands.impl;

import dev.heypr.yggdrasil.Yggdrasil;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public final class PreloadSkinsCommand implements CommandExecutor {
    private final Yggdrasil plugin;

    public PreloadSkinsCommand(final Yggdrasil plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        sender.sendMessage(ChatColor.GREEN + String.format("Attempting to preload all skins..."));

        for (final File dir : plugin.getDataFolder().listFiles()) {
            if (!dir.isDirectory())
                continue;

            final String name = dir.getName();

            try {
                UUID.fromString(name);
            } catch (final IllegalArgumentException ignored) {
                continue;
            }

            final List<String> toCheckStrings = Arrays.asList("gray.png", "green.png", "red.png", "yellow.png");
            final List<File> toCheck = toCheckStrings.stream()
                    .map(str -> new File(dir, str))
                    .collect(Collectors.toList());

            for (final File file : toCheck) {
                this.plugin.skinManager.getSkinData(file, (data, exception) -> {
                    if (exception != null && data == null) {
                        sender.sendMessage(ChatColor.RED + String.format("Failed to preload skin '%s' for reason '%s'.", file.getPath(), exception.getMessage()));
                    } else if (data != null) {
                        if (data.isRetrievedFromSave())
                            return;

                        sender.sendMessage(ChatColor.GREEN + String.format("Successfully preloaded skin '%s'.", file.getPath()));
                    }
                });
            }
        }
        return true;
    }
}