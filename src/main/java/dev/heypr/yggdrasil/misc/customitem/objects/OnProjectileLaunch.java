package dev.heypr.yggdrasil.misc.customitem.objects;

import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.ProjectileLaunchEvent;

public record OnProjectileLaunch(Projectile projectile, Player shooter, ProjectileLaunchEvent event) {
}