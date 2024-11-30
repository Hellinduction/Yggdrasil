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
//    private final Map<ItemStack, Long> protectedDrops = new HashMap<>();

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

//        event.setKeepInventory(true);
//        event.setKeepLevel(true);
//        event.setShouldDropExperience(false);
//        event.getDrops().clear();

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
        else if (data.getLives() == 1) {
            player.getWorld().strikeLightningEffect(player.getLocation());

            if (keepInv) {
                event.setKeepInventory(false);
                event.setKeepLevel(false);
                event.setShouldDropExperience(true);
                event.getDrops().addAll(originalDrops);
            }

//            final long now = Instant.now().getEpochSecond();
//
//            for (final ItemStack drop : event.getDrops())
//                this.protectedDrops.put(drop, now);
        }

        data.decreaseLives(1);
        player.sendActionBar(Component.text("Lives: " + data.getLives()));

        ColorManager.setTabListName(player, data);
    }

//    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
//    public void onEntityDamage(final EntityDamageEvent e) {
//        if (e.getCause() != EntityDamageEvent.DamageCause.LIGHTNING)
//            return;
//
//        final Entity entity = e.getEntity();
//
//        if (!(entity instanceof Item item))
//            return;
//
//        if (!this.protectedDrops.containsKey(item.getItemStack()))
//            return;
//
//        final long now = Instant.now().getEpochSecond();
//        final long then = this.protectedDrops.get(item.getItemStack());
//        final long diff = now - then;
//
//        if (diff > 3) {
//            this.protectedDrops.remove(item.getItemStack());
//            return;
//        }
//
//        e.setCancelled(true);
//        this.protectedDrops.remove(item.getItemStack());
//    }
}
