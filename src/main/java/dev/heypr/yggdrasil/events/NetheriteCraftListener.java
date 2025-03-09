package dev.heypr.yggdrasil.events;

import dev.heypr.yggdrasil.Yggdrasil;
import dev.heypr.yggdrasil.commands.impl.ToggleNetheriteCommand;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.function.Predicate;

public final class NetheriteCraftListener implements Listener {
    private final Yggdrasil plugin;

    public NetheriteCraftListener(final Yggdrasil plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onNonMoveableItemClick(final InventoryClickEvent event) {
        if (!ToggleNetheriteCommand.isDisabled())
            return;

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

        final boolean isMovingIntoSmithingTable = this.isMovingIntoSmithingTable(event);

        if (isMovingIntoSmithingTable)
            event.setCancelled(true);
    }

    private @NotNull Predicate<ItemStack> getItemStackPredicate() {
        final Predicate<ItemStack> isInvalidMaterial = item -> item == null || item.getType() == Material.AIR;
        final Predicate<ItemStack> isValid = item -> {
            if (isInvalidMaterial.test(item))
                return false;

            return item.getType().name().contains("NETHERITE");
        };
        return isValid;
    }

    private boolean isMovingIntoSmithingTable(final InventoryClickEvent e) {
        final Inventory clickedInventory = e.getClickedInventory();
        final InventoryAction action = e.getAction();

        switch (action) {
            case PLACE_ALL, PLACE_SOME, PLACE_ONE, HOTBAR_SWAP, HOTBAR_MOVE_AND_READD, SWAP_WITH_CURSOR:
                return clickedInventory != null && clickedInventory.getType() != null && clickedInventory.getType() == InventoryType.SMITHING;

            case MOVE_TO_OTHER_INVENTORY:
                return clickedInventory != null && clickedInventory.getType() == InventoryType.PLAYER;

            default:
                return false;
        }
    }
}