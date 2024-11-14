package dev.heypr.yggdrasil.commands;

import dev.heypr.yggdrasil.data.PlayerData;
import dev.heypr.yggdrasil.Yggdrasil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class StartSessionCommand implements CommandExecutor {

    private final Yggdrasil plugin;

    public StartSessionCommand(Yggdrasil plugin) {
        this.plugin = plugin;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (plugin.isSessionRunning) {
            sender.sendMessage("Game is already running.");
            return true;
        }

        Map<UUID, PlayerData> playerData = plugin.getPlayerData();

        playerData.clear();

        List<Player> players = new ArrayList<>(plugin.getServer().getOnlinePlayers());
        if (players.isEmpty()) return true;

        int numBoogeymen = plugin.randomNumber(1, 3);

        for (int i = 0; i < numBoogeymen && i < players.size() - 1; i++) {
            Player boogeyman = players.get(i);

            playerData.put(boogeyman.getUniqueId(), new PlayerData(plugin.randomNumber(2, 6)));
            playerData.get(boogeyman.getUniqueId()).setBoogeyman(true);

            boogeyman.sendTitle(ChatColor.RED + "You ARE the Boogeyman!", "Lives: " + plugin.getPlayerData().get(boogeyman.getUniqueId()).getLives(), 10, 70, 20);
        }
        
        players.forEach(player -> {
            playerData.putIfAbsent(player.getUniqueId(), new PlayerData(plugin.randomNumber(2, 6)));
            if (!playerData.get(player.getUniqueId()).isBoogeyman()) {
                player.sendTitle(ChatColor.GREEN + "You are NOT the Boogeyman!", "Lives: " + plugin.getPlayerData().get(player.getUniqueId()).getLives(), 10, 70, 20);
            }
            Component lives = Component.text(" (" + plugin.getPlayerData().get(player.getUniqueId()).getLives() + " lives)").decoration(TextDecoration.ITALIC, false).color(TextColor.color(128, 128, 128));
            player.playerListName(player.name().append(lives));
        });

        plugin.isSessionRunning = true;
        return true;
    }
}
