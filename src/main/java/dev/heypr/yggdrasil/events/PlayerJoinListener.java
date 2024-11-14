package dev.heypr.yggdrasil.events;

import dev.heypr.yggdrasil.Yggdrasil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PlayerJoinListener implements Listener {

    private final Yggdrasil plugin;

    public PlayerJoinListener(Yggdrasil plugin) {
        this.plugin = plugin;
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!plugin.isSessionRunning) {
            player.sendTitle(ChatColor.RED + "Game not started", "Please wait for the game to start", 10, 70, 20);
            return;
        }
        if (!plugin.getPlayerData().containsKey(player.getUniqueId())) {
            player.sendTitle(ChatColor.RED + "Game in progress", ChatColor.RED + "You are not part of the game", 10, 70, 20);
            player.sendMessage(ChatColor.GREEN + "Game in progress. Request an admin to add you to the game using /addplayer <player>");

            plugin.getServer().getOperators().forEach(op -> op.getPlayer().sendMessage(ChatColor.DARK_RED + "[URGENT] Player " + player.getName() + " does not have game data. They will be treated as a dead player. Add them to the list of players using /addplayer <player>"));
            return;
        }

        player.sendActionBar(Component.text("Lives: " + plugin.getPlayerData().get(player.getUniqueId()).getLives()));
        Component lives = Component.text(" (" + plugin.getPlayerData().get(player.getUniqueId()).getLives() + " lives)").decoration(TextDecoration.ITALIC, false).color(TextColor.color(128, 128, 128));

        player.playerListName(player.name().append(lives));
        switch (plugin.getPlayerData().get(player.getUniqueId()).getLives()) {
            case 1:
                player.playerListName(player.name().color(TextColor.color(255, 0, 0)).append(lives));
                break;
            case 0:
                player.playerListName(player.name().color(TextColor.color(128, 128, 128)).append(lives));
                player.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, PotionEffect.INFINITE_DURATION, 500));
                player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, PotionEffect.INFINITE_DURATION, 500));
                player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, PotionEffect.INFINITE_DURATION, 500));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, PotionEffect.INFINITE_DURATION, 500));
                player.setAllowFlight(true);
                player.setCollidable(false);
                break;
        }
    }
}
