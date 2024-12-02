package dev.heypr.yggdrasil.commands.impl;

import dev.heypr.yggdrasil.Yggdrasil;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public final class ToggleNetheriteCommand implements CommandExecutor {
    private final Yggdrasil plugin;

    public ToggleNetheriteCommand(final Yggdrasil plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        final boolean disabled = isDisabled();

        plugin.getConfig().set("netherite_disabled", !disabled);
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format("&7You have toggled netherite %s&7.", disabled ? "&aon" : "&coff")));
        plugin.saveConfig();

        return true;
    }

    public static boolean isDisabled() {
        return Yggdrasil.plugin.getConfig().contains("netherite_disabled") && Yggdrasil.plugin.getConfig().getBoolean("netherite_disabled");
    }
}