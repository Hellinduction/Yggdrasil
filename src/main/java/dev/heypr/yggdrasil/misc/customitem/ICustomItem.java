package dev.heypr.yggdrasil.misc.customitem;

import dev.heypr.yggdrasil.misc.customitem.objects.*;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public interface ICustomItem {
    ItemStack getItem();
    String getRawName();
    void onUse(final OnUse use);
    void onPlace(final OnPlace place);
    boolean verify(final ItemStack item);
    default void onProjectileLaunch(final OnProjectileLaunch projectileLaunch) {

    }

    default void onConsume(final OnConsume consume) {

    }

    default void onEntityRightClick(final OnEntityRightClick entityRightClick) {

    }

    default void removeItem(final Player p, final ItemStack item, final EquipmentSlot slot) {
        if (p.getGameMode() != GameMode.SURVIVAL && p.getGameMode() != GameMode.ADVENTURE)
            return;

        if (item == null)
            return;

        if (item.getAmount() == 1) {
            final PlayerInventory inv = p.getInventory();
            inv.setItem(slot, null);
        } else
            item.setAmount(item.getAmount() - 1);

        p.updateInventory();
    }
}