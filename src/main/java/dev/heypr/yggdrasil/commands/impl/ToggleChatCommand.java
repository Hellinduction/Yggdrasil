package dev.heypr.yggdrasil.commands.impl;

import dev.heypr.yggdrasil.Yggdrasil;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;

public final class ToggleChatCommand implements CommandExecutor {
    private final Yggdrasil plugin;

    public ToggleChatCommand(final Yggdrasil plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        final ConfigurationSection section = plugin.getConfig().getConfigurationSection("chat");
        final boolean disabled = section.getBoolean("disabled");

        section.set("disabled", !disabled);
        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format("&7You have toggled chat %s&7.", disabled ? "&aon" : "&coff")));
        plugin.saveConfig();

        return true;
    }
}