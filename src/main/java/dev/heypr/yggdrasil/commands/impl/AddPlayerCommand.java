package dev.heypr.yggdrasil.commands.impl;

import dev.heypr.yggdrasil.Yggdrasil;
import dev.heypr.yggdrasil.data.PlayerData;
import dev.heypr.yggdrasil.data.TemporaryPlayerData;
import dev.heypr.yggdrasil.misc.object.Pair;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;

public class AddPlayerCommand implements CommandExecutor {

    private final Yggdrasil plugin;

    public AddPlayerCommand(Yggdrasil plugin) {
        this.plugin = plugin;
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /addplayer <player>");
            return true;
        }

        Player target = sender.getServer().getPlayer(args[0]);

        if (target == null) {
            sender.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }

        if (plugin.getPlayerData().containsKey(target.getUniqueId())) {
            sender.sendMessage(ChatColor.RED + "Player already added.");
            return true;
        }

        final Pair<Integer, Boolean> pair = PlayerData.retrieveLivesOrDefaultAsPair(target.getUniqueId(), plugin.randomLives());
        final PlayerData data = new PlayerData(target, pair.getKey());
        final boolean culling = plugin.isCullingSession && data.getLives() == 0;

        if (culling || !plugin.isCullingSession) {
            target.setGameMode(GameMode.SURVIVAL);

            target.clearActivePotionEffects();

            final TemporaryPlayerData temporaryPlayerData = TemporaryPlayerData.get(target);

            for (final PotionEffect effect : temporaryPlayerData.getEffects())
                target.addPotionEffect(effect);

            if (culling && PlayerData.retrieveLives(target.getUniqueId()) == 0)
                data.setLastChance(true);

            plugin.getPlayerData().putIfAbsent(target.getUniqueId(), data);

            data.checkLives();
            data.displayLives(pair.getValue(), true);
        }

        sender.sendMessage(ChatColor.GREEN + "Player " + target.getName() + " added.");
        return true;
    }
}
