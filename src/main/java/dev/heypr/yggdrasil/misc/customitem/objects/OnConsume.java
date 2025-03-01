package dev.heypr.yggdrasil.misc.customitem.objects;

import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

public record OnConsume(Player player, ItemStack item, PlayerItemConsumeEvent event) {}