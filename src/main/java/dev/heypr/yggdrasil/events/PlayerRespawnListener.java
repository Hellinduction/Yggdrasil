package dev.heypr.yggdrasil.events;

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import dev.heypr.yggdrasil.Yggdrasil;
import dev.heypr.yggdrasil.data.PlayerData;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.UUID;

public class PlayerRespawnListener implements Listener {

    private final Yggdrasil plugin;

    public PlayerRespawnListener(Yggdrasil plugin) {
        this.plugin = plugin;
    }

    @SuppressWarnings("all")
    @EventHandler
    public void onPlayerPostRespawn(PlayerPostRespawnEvent event) {
        Player player = event.getPlayer();

        if (!plugin.getPlayerData().containsKey(player.getUniqueId())) {
            plugin.getServer().getOperators().forEach((op) -> {
                if (!(op.isOnline())) return;
                op.getPlayer().sendMessage(ChatColor.DARK_RED + "[URGENT] Player " + player.getName() + " does not have game data. They will be treated as a dead player. Add them to the list of players using /addplayer <player>");
                return;
            });
            return;
        }

        UUID uuid = player.getUniqueId();
        PlayerData data = plugin.getPlayerData().get(uuid);

        player.sendActionBar(Component.text("Lives: " + data.getLives()));
        if (data.getLives() == 0) {
            player.setGameMode(GameMode.ADVENTURE);

            player.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, PotionEffect.INFINITE_DURATION, 500));
            player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, PotionEffect.INFINITE_DURATION, 500));
            player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, PotionEffect.INFINITE_DURATION, 500));
        }
    }
}
