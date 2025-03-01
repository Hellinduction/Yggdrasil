package dev.heypr.yggdrasil.misc.customitem.objects;

import dev.heypr.yggdrasil.misc.customitem.ICustomItem;
import org.bukkit.inventory.ItemStack;

public record UsedItem(ItemStack item, ICustomItem customItem, long time) {}