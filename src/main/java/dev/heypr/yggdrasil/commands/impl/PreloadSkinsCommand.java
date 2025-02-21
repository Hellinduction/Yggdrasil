package dev.heypr.yggdrasil.commands.impl;

import dev.heypr.yggdrasil.Yggdrasil;
import dev.heypr.yggdrasil.misc.WaitForCompletion;
import dev.heypr.yggdrasil.misc.object.SkinData;
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

        final List<String> toCheckStrings = Arrays.asList("gray.png", "green.png", "red.png", "yellow.png");
        final List<File> toCheck = Arrays.stream(plugin.getDataFolder().listFiles())
                .filter(File::isDirectory)
                .filter(dir -> {
                    try {
                        UUID.fromString(dir.getName());
                        return true;
                    } catch (final IllegalArgumentException ignored) {
                        return false;
                    }
                })
                .map(File::listFiles)
                .filter(files -> files != null)
                .flatMap(Arrays::stream)
                .filter(file -> toCheckStrings.contains(file.getName()))
                .collect(Collectors.toList());

        final WaitForCompletion<SkinData> wait = new WaitForCompletion(toCheck.size());

        wait.wait(list -> {
            final long count = list.stream()
                    .filter(data -> data != null && !data.isRetrievedFromSave())
                    .count();

            if (count <= 0)
                return;

            sender.sendMessage(ChatColor.GOLD + String.format("Successfully preloaded %s skins.", count));
        });

        for (final File file : toCheck) {
            this.plugin.skinManager.getSkinData(file, (data, exception) -> {
                if (exception != null && data == null) {
                    sender.sendMessage(ChatColor.RED + String.format("Failed to preload skin '%s' for reason '%s'.", file.getPath(), exception.getMessage()));
                    wait.accept(null);
                } else if (data != null) {
                    if (data.isRetrievedFromSave()) {
                        wait.accept(data);
                        return;
                    }

                    sender.sendMessage(ChatColor.GREEN + String.format("Successfully preloaded skin '%s'.", file.getPath()));
                    wait.accept(data);
                }
            });
        }

        return true;
    }
}