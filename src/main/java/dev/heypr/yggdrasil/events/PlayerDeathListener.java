package dev.heypr.yggdrasil.events;

import dev.heypr.yggdrasil.Yggdrasil;
import dev.heypr.yggdrasil.data.PlayerData;
import dev.heypr.yggdrasil.misc.ColorManager;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.UUID;

public class PlayerDeathListener implements Listener {

    private final Yggdrasil plugin;

    public PlayerDeathListener(Yggdrasil plugin) {
        this.plugin = plugin;
    }

    @SuppressWarnings("all")
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        if (!plugin.isSessionRunning)
            return; // No action taken since the session is not running

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

        if (player.getKiller() != null) {
            PlayerData killerData = plugin.getPlayerData().get(player.getKiller().getUniqueId());

            if (killerData != null)
                killerData.incrementKills();

            // If above 3 kills they graduate to 1 life
            killerData.checkGraduate();
        }

        if (data.hasLastChance())
            data.setLastChance(false);

        if (data.getLives() == 0)
            return;

        data.decreaseLives(1);
        player.sendActionBar(Component.text("Lives: " + data.getLives()));

        ColorManager.setTabListName(player, data);
    }
}
