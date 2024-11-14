package dev.heypr.yggdrasil.commands;

import dev.heypr.yggdrasil.Yggdrasil;
import dev.heypr.yggdrasil.data.PlayerData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class AddPlayerCommand implements CommandExecutor {

    private final Yggdrasil plugin;

    public AddPlayerCommand(Yggdrasil plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (args.length < 1) {
            sender.sendMessage("Usage: /addplayer <player>");
            return true;
        }

        Player target = sender.getServer().getPlayer(args[0]);

        if (target == null) {
            sender.sendMessage("Player not found.");
            return true;
        }

        if (plugin.getPlayerData().containsKey(target.getUniqueId())) {
            sender.sendMessage("Player already added.");
            return true;
        }

        plugin.getPlayerData().putIfAbsent(target.getUniqueId(), new PlayerData(plugin.randomNumber(2, 6)));
        sender.sendMessage("Player " + target.name() + " added.");
        return true;
    }
}
