package dev.heypr.yggdrasil.events;

import dev.heypr.yggdrasil.Yggdrasil;
import dev.heypr.yggdrasil.data.PlayerData;
import dev.heypr.yggdrasil.misc.ColorManager;
import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.GameRule;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class PlayerDeathListener implements Listener {

    private final Yggdrasil plugin;

    public PlayerDeathListener(Yggdrasil plugin) {
        this.plugin = plugin;
    }

    @SuppressWarnings("all")
    @EventHandler
    public void onPlayerDeath(final PlayerDeathEvent event) {
        final Player player = event.getEntity();
        final List<ItemStack> originalDrops = Arrays.asList(player.getInventory().getContents()).stream()
                .filter(item -> item != null)
                .filter(item -> {
                    final boolean hasVanishingCurse = item.getItemMeta().hasEnchant(Enchantment.VANISHING_CURSE);
                    final boolean emptyItem = item.getType() == Material.AIR || item.getAmount() <= 0;

                    return !hasVanishingCurse && !emptyItem;
                })
                .collect(Collectors.toList());
        final boolean keepInv = player.getWorld().getGameRuleValue(GameRule.KEEP_INVENTORY).booleanValue() || player.getGameMode() == GameMode.SPECTATOR;

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

        if (data.getLives() == -1) {
            return;
        } else if (data.getLives() == 1 || (data.getLives() == 0 && plugin.isCullingSession)) {
            player.getWorld().strikeLightningEffect(player.getLocation());

            if (keepInv) {
                event.setKeepInventory(false);
                event.setKeepLevel(false);
                event.setShouldDropExperience(true);
                event.getDrops().addAll(originalDrops);
            }

            data.resetNameAndSkin();
            plugin.getDisguiseMap().remove(data.getUuid());
        }

        data.decreaseLives(1);
        player.sendActionBar(Component.text("Lives: " + data.getDisplayLives()));

        ColorManager.setTabListName(player, data);
    }
}
