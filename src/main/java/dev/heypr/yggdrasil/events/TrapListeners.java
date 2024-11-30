package dev.heypr.yggdrasil.events;

import dev.heypr.yggdrasil.Yggdrasil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import java.time.Instant;
import java.util.*;

/**
 * A class that attempts to handle multiple cases where someone dies via a trap left by another player
 */
public class TrapListeners implements Listener {
    public class LavaData {
        private final UUID uuid;
        private final Location loc;
        private long epochSecond;

        private long removedAt = -1; // also epoch second

        public LavaData(final UUID uuid, final Location loc, final long epochSecond) {
            this.uuid = uuid;
            this.loc = loc;
            this.epochSecond = epochSecond;
        }

        public UUID uuid() {
            return this.uuid;
        }

        public Location loc() {
            return this.loc;
        }

        public long epochSecond() {
            return this.epochSecond;
        }

        public long getRemovedAt() {
            return this.removedAt;
        }

        public void setRemovedAt(final long removedAt) {
            this.removedAt = removedAt;
        }

        public long secondsSinceRemoved() {
            return diff(this.removedAt);
        }
    }

    private static final long LAVA_ACTIVE_TRAP_DURATION = 300; // 5 minutes
    private static final long LAVA_REMOVED_EXTRA_TIME = 3L;

    private final Yggdrasil plugin;

    private final Map<Location, LavaData> lavaPlaceMap = new HashMap<>();
    private final Map<UUID, Long> lastPlacedLava = new HashMap<>();

    public TrapListeners(final Yggdrasil plugin) {
        this.plugin = plugin;
    }

    private LavaData findLavaData(final Location loc) {
        final List<Location> toCheck = Arrays.asList(loc, loc.getBlock().getRelative(BlockFace.UP).getLocation());

        for (final Map.Entry<Location, LavaData> entry : this.lavaPlaceMap.entrySet()) {
            for (final Location check : toCheck) {
                final Location lavaLocation = entry.getKey();

                if (!lavaLocation.getWorld().equals(check.getWorld()))
                    continue;

                if (lavaLocation.distance(check) <= 1D)
                    return entry.getValue();
            }
        }

        return null;
    }

    /**
     * How many seconds ago was the provided epoch second
     * @return
     */
    protected static long diff(final long epoch, final boolean ms) {
        final long now = ms ? System.currentTimeMillis() : Instant.now().getEpochSecond();
        final long diff = now - epoch;

        return diff;
    }

    protected static long diff(final long epoch) {
        return diff(epoch, false);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerPlaceLava(final PlayerInteractEvent e) {
        final Player player = e.getPlayer();
        final ItemStack item = e.getItem();

        if (item == null)
            return;

        final Action action = e.getAction();

        if (action != Action.RIGHT_CLICK_BLOCK)
            return;

        final Block block = e.getClickedBlock();

        if (block == null)
            return;

        final BlockFace face = e.getBlockFace();
        final Block relative = block.getRelative(face);

        if (item.getType() != Material.LAVA_BUCKET) {
            if (item.getType() == Material.BUCKET) {
                if (diff(this.lastPlacedLava.getOrDefault(player.getUniqueId(), 0L), true) < 100)
                    return;

                final LavaData fromData = this.lavaPlaceMap.get(relative.getLocation());

                if (fromData == null)
                    return;

                fromData.setRemovedAt(Instant.now().getEpochSecond());
            }

            return;
        }

        final LavaData data = new LavaData(player.getUniqueId(), relative.getLocation(), Instant.now().getEpochSecond());

        this.lavaPlaceMap.put(relative.getLocation(), data);
        this.lastPlacedLava.put(player.getUniqueId(), System.currentTimeMillis());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onLavaSpread(final BlockFromToEvent e) {
        final Block to = e.getToBlock();
        final BlockFace face = e.getFace();
        final Block from = to.getRelative(face.getOppositeFace());

        if (from.getType() != Material.LAVA)
            return;

        final LavaData fromData = this.findLavaData(from.getLocation());

        if (fromData == null)
            return;

        final LavaData data = new LavaData(fromData.uuid(), to.getLocation(), Instant.now().getEpochSecond());
        this.lavaPlaceMap.put(to.getLocation(), data);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onLavaDamagePlayer(final EntityDamageEvent e) {
        final Entity entity = e.getEntity();

        if (!(entity instanceof Player player))
            return;

        if (e.getFinalDamage() < player.getHealth())
            return;

        final EntityDamageEvent.DamageCause cause = e.getCause();

        if (cause != EntityDamageEvent.DamageCause.LAVA)
            return;

        final Location loc = player.getLocation();
        final LavaData data = this.findLavaData(loc);

        if (data == null)
            return;

        final long placed = data.epochSecond();
        final boolean removedRecently = data.getRemovedAt() != -1 && data.secondsSinceRemoved() < LAVA_REMOVED_EXTRA_TIME;

        if ((data.loc().getBlock().getType() != Material.LAVA && !removedRecently) || diff(placed) > LAVA_ACTIVE_TRAP_DURATION) {
            this.lavaPlaceMap.remove(data.loc());
            return;
        }

        final Player killer = Bukkit.getPlayer(data.uuid());

        if (killer == null || !killer.isOnline())
            return;

        if (player.equals(killer))
            return;

        player.setKiller(killer);

        this.lastPlacedLava.remove(killer.getUniqueId());
        this.lavaPlaceMap.remove(data.loc());
    }
}