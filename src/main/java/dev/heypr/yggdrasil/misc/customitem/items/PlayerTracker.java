package dev.heypr.yggdrasil.misc.customitem.items;

import dev.heypr.yggdrasil.Yggdrasil;
import dev.heypr.yggdrasil.data.PlayerData;
import dev.heypr.yggdrasil.misc.ColorManager;
import dev.heypr.yggdrasil.misc.customitem.AbstractCustomItem;
import dev.heypr.yggdrasil.misc.customitem.objects.OnPlace;
import dev.heypr.yggdrasil.misc.customitem.objects.OnUse;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public final class PlayerTracker extends AbstractCustomItem {
    public PlayerTracker() {
        super(Material.COMPASS, "&bPlayer Tracker", "&7&oTracks the closest player to you.");
        super.setDropOnDeath(false);

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!Yggdrasil.plugin.isSessionRunning)
                    return;

                for (final Player p : Bukkit.getOnlinePlayers()) {
                    final Player player = getNearestPlayer(p);

                    if (player == null)
                        continue;

                    final PlayerInventory inv = p.getInventory();

                    for (int index = 0; index < inv.getSize(); ++index) {
                        final ItemStack item = inv.getItem(index);

                        if (item == null)
                            continue;

                        final int finalIndex = index;

                        Yggdrasil.plugin.customItemManager.getItem(item.getItemMeta().getDisplayName())
                                .ifPresent(customItem -> {
                                    if (!customItem.verify(item))
                                        return;

                                    if (item.getType() != Material.COMPASS)
                                        return;

                                    final Player nearestPlayer = getNearestPlayer(p);

                                    if (nearestPlayer == null)
                                        return;

                                    final ChatColor color = getColor(player);
                                    final Location nearest = nearestPlayer.getLocation();

                                    p.setCompassTarget(nearest);
                                    p.sendActionBar(ChatColor.translateAlternateColorCodes('&', String.format("&bCurrently tracking %s%s&b.", color, nearestPlayer.getName())));

                                    final CompassMeta meta = (CompassMeta) item.getItemMeta();

                                    meta.setLodestone(nearest);

                                    item.setItemMeta(meta);
                                    inv.setItem(finalIndex, item);
                                });
                    }
                }
            }
        }.runTaskTimer(Yggdrasil.plugin, 20L, 20L);
    }

    @Override
    public ItemStack getItem() {
        final ItemStack item = super.getItem();

        final CompassMeta meta = (CompassMeta) item.getItemMeta();

        meta.setLodestoneTracked(false);

        item.setItemMeta(meta);

        return item;
    }

    private Player getNearestPlayer(final Player p) {
        final Location loc = p.getLocation();
        final World world = loc.getWorld();
        final Collection<Entity> entities = world.getNearbyEntities(loc, 5000D, 384D, 5000D);
        final List<Player> players = entities.stream()
                .filter(entity -> entity instanceof Player && !entity.getUniqueId().equals(p.getUniqueId()))
                .map(entity -> (Player) entity)
                .filter(ps -> Yggdrasil.plugin.getPlayerData().containsKey(ps.getUniqueId()))
                .filter(ps -> !Yggdrasil.plugin.getPlayerData().get(ps.getUniqueId()).isDead())
                .filter(ps -> ps.getGameMode() != GameMode.SPECTATOR) // Just extra filter condition in case isDeath() glitches
                .collect(Collectors.toList());

        Player found = null;
        double distance = Double.POSITIVE_INFINITY;

        for (final Player op : players) {
            final double distanceTo = loc.distance(op.getLocation());

            if (distanceTo > distance)
                continue;

            distance = distanceTo;
            found = op;
        }

        return found;
    }

    private ChatColor getColor(final Player player) {
        final int lives = PlayerData.retrieveLives(player.getUniqueId());
        final ChatColor color = ColorManager.getColor(lives);

        return color;
    }

    @Override
    public void onUse(final OnUse use) {}

    @Override
    public void onPlace(final OnPlace place) {}
}