package dev.heypr.yggdrasil.misc.customitem.objects;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.EquipmentSlot;

public record OnEntityRightClick(Player player, PlayerInteractEntityEvent event, EquipmentSlot slot, Entity entity) {}