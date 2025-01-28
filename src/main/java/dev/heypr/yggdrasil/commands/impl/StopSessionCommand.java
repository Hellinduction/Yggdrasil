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

//        plugin.getDeadPlayers().forEach(player -> {
//            player.ban("Banned for dying.", new Date().toInstant().plus(30, ChronoUnit.DAYS),null, true);
//            plugin.getDeadPlayers().remove(player);
//        });

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
//            Player player = plugin.getServer().getPlayer(uuid);

//            NamespacedKey livesKey = new NamespacedKey(plugin, "lives");
//
//            player.getPersistentDataContainer().set(livesKey, PersistentDataType.INTEGER, plugin.getPlayerData().get(player.getUniqueId()).getLives());
        });

        Yggdrasil.plugin.cancelTasks(Yggdrasil.plugin.getCancelOnSessionStop());

        plugin.isSessionRunning = false;
        return true;
    }
}
