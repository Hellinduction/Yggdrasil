package dev.heypr.yggdrasil.misc.customitem.objects;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public record OnUse(Player player, PlayerInteractEvent event, EquipmentSlot slot) {}