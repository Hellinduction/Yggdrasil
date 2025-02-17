package dev.heypr.yggdrasil.commands.impl;

import dev.heypr.yggdrasil.Yggdrasil;
import dev.heypr.yggdrasil.data.PlayerData;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class RemoveBoogeymanCommand implements CommandExecutor {

    private final Yggdrasil plugin;

    public RemoveBoogeymanCommand(Yggdrasil plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /removeboogeyman <player>");
            return true;
        }

        if (args.length == 1) {

            Player target = sender.getServer().getPlayer(args[0]);

            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Player not found.");
                return true;
            }

            final PlayerData targetData = plugin.getPlayerData().get(target.getUniqueId());

            if (!targetData.isBoogeyman()) {
                sender.sendMessage(ChatColor.RED + targetData.getDisplayName() + " was not a Boogeyman.");
                return true;
            }

            targetData.setBoogeyman(false);
            sender.sendMessage(ChatColor.GREEN + targetData.getDisplayName() + " is no longer a Boogeyman.");
            return true;
        }
        else {
            sender.sendMessage(ChatColor.RED + "Usage: /removeboogeyman <player>");
        }

        return true;
    }
}
