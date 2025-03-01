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

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public final class PlayerTracker extends AbstractCustomItem {
    public PlayerTracker() {
        super(Material.COMPASS, "&bPlayer Tracker", "&7&oRight click to track the nearest player.");
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

                                    final Location nearest = getNearestPlayer(p).getLocation();
                                    p.setCompassTarget(nearest);

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

    private String format(final double d) {
        final DecimalFormat formatter = new DecimalFormat("#,###");
        return formatter.format(d);
    }

    @Override
    public void onUse(final OnUse use) {
        if (!Yggdrasil.plugin.isSessionRunning)
            return;

        final Player p = use.player();
        final Player nearestPlayer = this.getNearestPlayer(p);

        if (nearestPlayer == null) {
            p.sendMessage(ChatColor.RED + "No nearby players found :(");
            return;
        }

        final int lives = PlayerData.retrieveLives(nearestPlayer.getUniqueId());
        final ChatColor color = ColorManager.getColor(lives);
        final double dist = Math.floor(p.getLocation().distance(nearestPlayer.getLocation()));

        p.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format("%s%s&7 is %s blocks away.", color, nearestPlayer.getName(), this.format(dist))));
    }

    @Override
    public void onPlace(final OnPlace place) {

    }
}