package dev.heypr.yggdrasil.misc.customitem.objects;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public record OnPlace(Player player, Block block, ItemStack itemInHand, BlockPlaceEvent event, EquipmentSlot slot) {}