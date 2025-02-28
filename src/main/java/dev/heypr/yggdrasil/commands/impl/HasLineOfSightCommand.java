package dev.heypr.yggdrasil.commands.impl;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public final class HasLineOfSightCommand implements CommandExecutor {
    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (!(sender instanceof LivingEntity entity)) {
            sender.sendMessage(ChatColor.RED + "You must be alive to use this command.");
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + String.format("Usage: /%s <player>", label));
            return true;
        }

        final Player target = sender.getServer().getPlayer(args[0]);

        if (target == null || !target.isOnline()) {
            sender.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }

        final boolean canSee = entity.hasLineOfSight(target);
        sender.sendMessage(ChatColor.GREEN + String.format("You %s see %s.", (canSee ? "can" : "cannot"), target.getName()));
        return true;
    }
}