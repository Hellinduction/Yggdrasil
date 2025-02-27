package dev.heypr.yggdrasil.commands.impl;

import dev.heypr.yggdrasil.Yggdrasil;
import dev.heypr.yggdrasil.data.PlayerData;
import dev.heypr.yggdrasil.misc.ColorManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

/**
 * The admin command for adding/setting a certain number of lives to a player
 */
public class AddLivesCommand implements CommandExecutor {

    private final Yggdrasil plugin;

    public AddLivesCommand(Yggdrasil plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + String.format("Usage: /%s <player> <amount>", label));
            return true;
        }

        PlayerData.useFromRealName(sender, args[0], (target, targetData) -> {
            boolean isSet = label.toLowerCase().endsWith("setlives");

            if (args.length == 2) {
                int amount = Integer.parseInt(args[1]);
                boolean tooLow = isSet ? amount < 0 : amount < 1;

                if (tooLow) {
                    sender.sendMessage(ChatColor.RED + "Invalid amount.");
                    return;
                }

                int targetLives = plugin.getPlayerData().get(target.getUniqueId()).getLives();
                boolean aboveMax = isSet ? amount > Yggdrasil.MAX_LIVES : targetLives + amount > Yggdrasil.MAX_LIVES;

                if (aboveMax) {
                    sender.sendMessage(ChatColor.RED + String.format("Player cannot have more than %s lives.", Yggdrasil.MAX_LIVES));
                    return;
                }

                if (targetLives < 0 && !isSet)
                    amount = 2;

                boolean currentlyDead = targetLives <= 0;
                boolean revival = isSet ? currentlyDead && amount > 0 : currentlyDead;

                if (revival)
                    targetData.revive();

                if (isSet) {
                    targetData.setLives(amount);
                    targetData.checkLives();
                }
                else {
                    targetData.addLives(amount);
                    targetData.checkLives();
                }

                final ChatColor color = ColorManager.getColor(amount);

                if (!isSet) {
                    sender.sendMessage(ChatColor.GREEN + "You have given " + color + amount + ChatColor.GREEN + " lives to " + targetData.getDisplayName() + ".");
                    target.sendMessage(ChatColor.GREEN + "You have been given " + color + amount + ChatColor.GREEN + " lives.");
                } else {
                    sender.sendMessage(ChatColor.GREEN + "You have set the lives of " + targetData.getDisplayName() + " to " + color + amount + ChatColor.GREEN + ".");
                    target.sendMessage(ChatColor.GREEN + "Your lives have been set to " + color + amount + ChatColor.GREEN + ".");
                }
            } else {
                sender.sendMessage(ChatColor.RED + "Invalid amount.");
            }
        });

        return true;
    }
}
