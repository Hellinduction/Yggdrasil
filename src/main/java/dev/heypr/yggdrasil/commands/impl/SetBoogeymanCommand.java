package dev.heypr.yggdrasil.commands.impl;

import dev.heypr.yggdrasil.Yggdrasil;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SetBoogeymanCommand implements CommandExecutor {

    private final Yggdrasil plugin;

    public SetBoogeymanCommand(Yggdrasil plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /setboogeyman <player>");
            return true;
        }

        if (args.length == 1) {

            Player target = sender.getServer().getPlayer(args[0]);

            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Player not found.");
                return true;
            }

            if (plugin.getPlayerData().get(target.getUniqueId()).isBoogeyman()) {
                sender.sendMessage(ChatColor.RED + target.getName() + " is already a Boogeyman.");
                return true;
            }

            plugin.getPlayerData().get(target.getUniqueId()).setBoogeyman(true);
            sender.sendMessage(ChatColor.GREEN + target.getName() + " is now a Boogeyman.");
            return true;
        }
        else {
            sender.sendMessage(ChatColor.RED + "Usage: /setboogeyman <player>");
        }

        return true;
    }
}