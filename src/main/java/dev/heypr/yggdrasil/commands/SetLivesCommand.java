package dev.heypr.yggdrasil.commands;

import dev.heypr.yggdrasil.Yggdrasil;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

public class SetLivesCommand implements CommandExecutor {

    private final Yggdrasil plugin;

    public SetLivesCommand(Yggdrasil plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (args.length < 2) {
            sender.sendMessage("Usage: /setlives <player> <amount>");
            return true;
        }

        Player target = sender.getServer().getPlayer(args[0]);

        if (target == null) {
            sender.sendMessage("Player not found");
            return true;
        }

        if (args.length == 2) {

            int amount = Integer.parseInt(args[1]);
            if (amount < 1) {
                sender.sendMessage("Invalid amount.");
                return true;
            }

            Player player = (Player) sender;

            int targetLives = plugin.getPlayerData().get(target.getUniqueId()).getLives();

            if (targetLives + amount > 6) {
                sender.sendMessage("Player cannot have more than 6 lives.");
                return true;
            }

            player.sendMessage("You have given " + amount + " lives to " + target.getName());
            target.sendMessage("You have been given " + amount + " lives");

            if (targetLives == 0) {
                target.sendTitle("You have been revived!", "", 10, 20, 10);
                target.removePotionEffect(PotionEffectType.MINING_FATIGUE);
                target.removePotionEffect(PotionEffectType.WEAKNESS);
                target.removePotionEffect(PotionEffectType.RESISTANCE);
                target.setGameMode(GameMode.SURVIVAL);
            }
            plugin.getPlayerData().get(target.getUniqueId()).addLives(amount);
            return true;
        }

        else {
            sender.sendMessage("Invalid amount.");
        }

        return true;
    }
}
