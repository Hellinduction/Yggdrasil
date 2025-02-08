package dev.heypr.yggdrasil.commands.impl;

import dev.heypr.yggdrasil.Yggdrasil;
import dev.heypr.yggdrasil.data.PlayerData;
import dev.heypr.yggdrasil.misc.ColorManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * The player command for giving another player one of your lives
 */
public class GiveLifeCommand implements CommandExecutor {

    private final Yggdrasil plugin;

    public GiveLifeCommand(Yggdrasil plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /givelife <player> <amount>");
            return true;
        }

        Player target = sender.getServer().getPlayer(args[0]);

        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }

        if (args.length == 2) {
            int amount = Integer.parseInt(args[1]);

            if (amount < 1) {
                sender.sendMessage(ChatColor.RED + "Invalid amount.");
                return true;
            }

            Player player = (Player) sender;

            if (target.equals(player)) {
                sender.sendMessage(ChatColor.RED + "You cannot give yourself lives. If you are an admin please use /addlives instead.");
                return true;
            }

            int playerLives = plugin.getPlayerData().get(player.getUniqueId()).getLives();
            int targetLives = plugin.getPlayerData().get(target.getUniqueId()).getLives();

            if (targetLives <= 0) {
                sender.sendMessage(ChatColor.RED + "You cannot revive dead players.");
                return true;
            }

            if (playerLives < amount) {
                sender.sendMessage(ChatColor.RED + "You do not have enough lives.");
                return true;
            }

            final int minLives = plugin.getConfig().getInt("values.min_lives_through_give_life_command");

            if (playerLives - amount < minLives) {
                sender.sendMessage(ChatColor.RED + "You do not have enough lives or would die if you gave that many.");
                return true;
            }

            if (targetLives + amount > Yggdrasil.MAX_LIVES) {
                sender.sendMessage(ChatColor.RED + String.format("Player cannot have more than %s lives.", Yggdrasil.MAX_LIVES));
                return true;
            }

            final ChatColor color = ColorManager.getColor(amount);

            player.sendMessage(ChatColor.GREEN + "You have given " + color + amount + ChatColor.GREEN + " lives to " + target.getName() + ".");
            target.sendMessage(ChatColor.GREEN + "You have been given " + color + amount + ChatColor.GREEN + " lives.");

            final PlayerData playerData = plugin.getPlayerData().get(player.getUniqueId());
            final PlayerData targetData = plugin.getPlayerData().get(target.getUniqueId());

//            if (targetLives == 0)
//                targetData.revive();

            playerData.decreaseLives(amount);
            playerData.checkLives();

            targetData.addLives(amount);
            targetData.checkLives();

            return true;
        }

        else {
            sender.sendMessage(ChatColor.RED + "Invalid amount.");
        }

        return true;
    }
}
