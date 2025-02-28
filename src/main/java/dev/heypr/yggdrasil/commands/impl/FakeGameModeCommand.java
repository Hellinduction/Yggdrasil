package dev.heypr.yggdrasil.commands.impl;

import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.world.level.GameType;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

public final class FakeGameModeCommand implements CommandExecutor {
    private void setFakeGameMode(final Player player, final GameMode gameMode) {
        final CraftPlayer craftPlayer = (CraftPlayer) player;
        final ClientboundGameEventPacket packet = new ClientboundGameEventPacket(
                ClientboundGameEventPacket.CHANGE_GAME_MODE,
                GameType.valueOf(gameMode.name()).getId()
        );

        craftPlayer.getHandle().connection.send(packet);
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + String.format("Usage: /%s <player>", label));
            return true;
        }

        final Player target = sender.getServer().getPlayer(args[0]);

        if (target == null || !target.isOnline()) {
            sender.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }

        this.setFakeGameMode(target, GameMode.CREATIVE);
        sender.sendMessage(ChatColor.GREEN + String.format("Tricked player's client into thinking it is in creative mode."));
        return true;
    }
}