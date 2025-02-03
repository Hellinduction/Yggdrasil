package dev.heypr.yggdrasil.commands.impl;

import dev.heypr.yggdrasil.Yggdrasil;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class StopSessionCommand implements CommandExecutor {

    private final Yggdrasil plugin;

    public StopSessionCommand(Yggdrasil plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (!plugin.isSessionRunning) {
            sender.sendMessage(ChatColor.RED + "Session is not running.");
            return true;
        }

        sender.sendMessage(ChatColor.GREEN + "The session is stopping...");

        plugin.getPlayerData().forEach((uuid, playerData) -> {
            if (playerData.isBoogeyman()) {
                playerData.setBoogeyman(false);
            }

            playerData.checkGraduate();

            if (playerData.hasLastChance()) {
                playerData.setLastChance(false);
                playerData.checkLives();
            }

            playerData.setRevealedData(false);
        });

        Yggdrasil.plugin.cancelTasks(Yggdrasil.plugin.getCancelOnSessionStop());

        plugin.isSessionRunning = false;
        return true;
    }
}
