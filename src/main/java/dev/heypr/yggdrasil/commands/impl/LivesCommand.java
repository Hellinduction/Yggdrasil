package dev.heypr.yggdrasil.commands.impl;

import dev.heypr.yggdrasil.Yggdrasil;
import dev.heypr.yggdrasil.misc.ColorManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class LivesCommand implements CommandExecutor {

    private final Yggdrasil plugin;

    public LivesCommand(Yggdrasil plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1) {
            final int lives = plugin.getPlayerData().get(((Player) sender).getUniqueId()).getDisplayLives();
            final ChatColor color = ColorManager.getColor(lives);

            sender.sendMessage(ChatColor.GREEN + "You have " + color + lives + ChatColor.GREEN + String.format(" %s.", lives == 1 ? "life" : "lives"));
        }
        else if (args.length == 1) {
            Player target = sender.getServer().getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage(ChatColor.RED + "Player not found.");
                return true;
            }

            final int lives = plugin.getPlayerData().get(target.getUniqueId()).getDisplayLives();
            final ChatColor color = ColorManager.getColor(lives);

            sender.sendMessage(ChatColor.GREEN + target.getName() + " has " + color + lives + ChatColor.GREEN + String.format(" %s.", lives == 1 ? "life" : "lives"));
            return true;
        }
        else {
            sender.sendMessage(ChatColor.RED + "Usage: /lives [player]");
        }

        return true;
    }
}
