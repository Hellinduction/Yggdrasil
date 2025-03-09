package dev.heypr.yggdrasil.events;

import dev.heypr.yggdrasil.Yggdrasil;
import dev.heypr.yggdrasil.misc.customitem.AbstractCustomItem;
import dev.heypr.yggdrasil.misc.customitem.ICustomItem;
import dev.heypr.yggdrasil.misc.customitem.objects.*;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Predicate;

public final class CustomItemListener implements Listener {
    private final Yggdrasil plugin;
    private final Map<UUID, UsedItem> usedItems = new HashMap<>();

    public CustomItemListener(final Yggdrasil plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDropItem(final PlayerDropItemEvent event) {
        final Player player = event.getPlayer();

        if (player.getGameMode() == GameMode.SPECTATOR)
            return;

        final Item itemEntity = event.getItemDrop();
        final ItemStack item = itemEntity.getItemStack();

        if (!item.hasItemMeta())
            return;

        final ItemMeta meta = item.getItemMeta();

        if (!meta.hasDisplayName())
            return;

        plugin.customItemManager.getItem(meta.getDisplayName())
                .ifPresent(customItem -> {
                    if (!customItem.verify(item))
                        return;

                    if (!(customItem instanceof AbstractCustomItem abstractCustomItem))
                        return;

                    if (abstractCustomItem.isAllowDropping())
                        return;

                    event.setCancelled(true);
                });
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onNonMoveableItemClick(final InventoryClickEvent event) {
        final HumanEntity humanEntity = event.getWhoClicked();

        if (!(humanEntity instanceof Player))
            return;

        final Player player = (Player) humanEntity;

        if (player.getOpenInventory() == null)
            return;

        final InventoryView open = player.getOpenInventory();
        final Inventory inv = open.getTopInventory();

        if (inv == null)
            return;

        final Predicate<ItemStack> isValid = getItemStackPredicate();
        final ItemStack currentItem = event.getCurrentItem();
        final ItemStack cursor = event.getCursor();
        final ItemStack hotbar = event.getHotbarButton() == -1 ? null : player.getInventory().getItem(event.getHotbarButton());

        ItemStack item = currentItem;

        if (!isValid.test(item))
            item = cursor;

        if (!isValid.test(item) && event.getHotbarButton() != -1)
            item = hotbar;

        if (!isValid.test(item))
            return;

        final boolean isMovingIntoOtherInventory = this.isMovingIntoOtherInventory(event, player.getInventory());

        if (isMovingIntoOtherInventory)
            event.setCancelled(true);
    }

    private @NotNull Predicate<ItemStack> getItemStackPredicate() {
        final Predicate<ItemStack> isInvalidMaterial = item -> item == null || item.getType() == Material.AIR;
        final Predicate<ItemStack> isValid = item -> {
            if (isInvalidMaterial.test(item))
                return false;

            item = item.clone();
            item.setAmount(1);

            if (!item.hasItemMeta())
                return false;

            final ItemMeta meta = item.getItemMeta();
            if (!meta.hasDisplayName())
                return false;

            final Optional<ICustomItem> optional = plugin.customItemManager.getItem(meta.getDisplayName());

            if (optional.isEmpty())
                return false;

            final ICustomItem customItem = optional.get();

            if (!customItem.verify(item))
                return false;

            if (!(customItem instanceof AbstractCustomItem abstractCustomItem))
                return false;

            if (abstractCustomItem.isAllowDropping())
                return false;

            return true;
        };
        return isValid;
    }

    private boolean isMovingIntoOtherInventory(final InventoryClickEvent e, final PlayerInventory inventory) {
        final Inventory clickedInventory = e.getClickedInventory();
        final InventoryAction action = e.getAction();

        switch (action) {
            case PLACE_ALL, PLACE_SOME, PLACE_ONE, HOTBAR_SWAP, HOTBAR_MOVE_AND_READD, SWAP_WITH_CURSOR:
                return clickedInventory != null && !clickedInventory.equals(inventory);

            case MOVE_TO_OTHER_INVENTORY:
                return clickedInventory != null && clickedInventory.getType() == InventoryType.PLAYER && e.getView() != null && !clickedInventory.equals(e.getView().getTopInventory());

            default:
                return false;
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(final PlayerDeathEvent event) {
        final Player player = event.getPlayer();

        if (player.getGameMode() == GameMode.SPECTATOR)
            return;

        final List<ItemStack> drops = event.getDrops();

        for (final ItemStack item : new ArrayList<>(drops)) {
            if (!item.hasItemMeta())
                continue;

            final ItemMeta meta = item.getItemMeta();

            if (!meta.hasDisplayName())
                continue;

            plugin.customItemManager.getItem(meta.getDisplayName())
                    .ifPresent(customItem -> {
                        if (!customItem.verify(item))
                            return;

                        if (!(customItem instanceof AbstractCustomItem abstractCustomItem))
                            return;

                        if (abstractCustomItem.isDropOnDeath())
                            return;

                        drops.remove(item);
                    });
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        final Player player = event.getPlayer();

        if (player.getGameMode() == GameMode.SPECTATOR)
            return;

        ItemStack item = event.getItem();
        if (item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName() && event.getAction().toString().contains("RIGHT")) {

            // Chest check
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK && event.getClickedBlock().getType() == Material.CHEST) return;

            final OnUse onUse = new OnUse(event.getPlayer(), event, event.getHand());
            plugin.customItemManager.getItem(item.getItemMeta().getDisplayName())
                    .ifPresent(customItem -> {
                        if (!customItem.verify(item))
                            return;

                        customItem.onUse(onUse);

                        final UsedItem usedItem = new UsedItem(item, customItem, System.currentTimeMillis());
                        this.usedItems.put(event.getPlayer().getUniqueId(), usedItem);
                    });
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteractEntity(final PlayerInteractEntityEvent event) {
        final Player player = event.getPlayer();

        if (player.getGameMode() == GameMode.SPECTATOR)
            return;

        ItemStack item = player.getInventory().getItem(event.getHand());

        if (item != null && item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
            final OnEntityRightClick entityRightClick = new OnEntityRightClick(player, event, event.getHand(), event.getRightClicked());

            plugin.customItemManager.getItem(item.getItemMeta().getDisplayName())
                    .ifPresent(customItem -> {
                        if (!customItem.verify(item))
                            return;

                        customItem.onEntityRightClick(entityRightClick);

                        final UsedItem usedItem = new UsedItem(item, customItem, System.currentTimeMillis());
                        this.usedItems.put(event.getPlayer().getUniqueId(), usedItem);
                    });
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        ItemStack itemInHand = event.getItemInHand();

        if (itemInHand != null && itemInHand.hasItemMeta() && itemInHand.getItemMeta().hasDisplayName()) {
            plugin.customItemManager.getItem(itemInHand.getItemMeta().getDisplayName())
                    .ifPresent(customItem -> {
                        if (!customItem.verify(itemInHand))
                            return;

                        event.setCancelled(true);

                        final OnPlace onPlace = new OnPlace(player, event.getBlockPlaced(), itemInHand, event, event.getHand());
                        customItem.onPlace(onPlace);
                    });
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onProjectileLaunch(final ProjectileLaunchEvent e) {
        final Entity entity = e.getEntity();

        if (!(entity instanceof Projectile))
            return;

        final Projectile projectile = (Projectile) entity;

        if (!(projectile.getShooter() instanceof Player))
            return;

        final Player shooter = (Player) projectile.getShooter();
        final OnProjectileLaunch projectileLaunch = new OnProjectileLaunch(projectile, shooter, e);

        ItemStack hand = shooter.getItemInHand();
        final ItemStack finalHand = hand;

        final Predicate<ItemStack> isInvalid = item -> item == null || !item.hasItemMeta() || item.getType() == Material.AIR || item.getAmount() <= 0;

        if (isInvalid.test(hand)) {
            final UsedItem usedItem = this.usedItems.get(shooter.getUniqueId());

            if (usedItem != null) {
                final long then = usedItem.time();
                final long now = System.currentTimeMillis();
                final long diff = now - then;

                if (diff <= 50) {
                    hand = usedItem.item().clone();
                    hand.setAmount(1);
                }
            }
        }

        if (isInvalid.test(hand))
            return;

        final ItemMeta meta = hand.getItemMeta();
        if (!meta.hasDisplayName())
            return;

        plugin.customItemManager.getItem(meta.getDisplayName())
                .ifPresent(customItem -> {
                    if (!customItem.verify(finalHand))
                        return;

                    if (!(customItem instanceof AbstractCustomItem))
                        return;

                    final AbstractCustomItem abstractCustomItem = (AbstractCustomItem) customItem;
                    abstractCustomItem.onProjectileLaunch(projectileLaunch);
                });
    }
}