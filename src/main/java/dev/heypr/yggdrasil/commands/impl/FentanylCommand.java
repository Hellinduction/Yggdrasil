package dev.heypr.yggdrasil.commands.impl;

import dev.heypr.yggdrasil.Yggdrasil;
import org.bukkit.ChatColor;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class FentanylCommand implements CommandExecutor {
    private static final int DURATION = 15; // Seconds
    private static final Map<UUID, Integer> TOLERANCE = new HashMap<>();

    private final Yggdrasil plugin;

    public FentanylCommand(final Yggdrasil plugin) {
        this.plugin = plugin;
    }

    private static double cappedGrowth(final int n) {
        if (n >= 20)
            return 20;

        return 20 * (Math.log(n + 1) / Math.log(21));
    }

    private double calculateToleranceHealthDamage(final Player player) {
        final UUID uuid = player.getUniqueId();
        final int tolerance = TOLERANCE.getOrDefault(uuid, 0);
        final double damage = cappedGrowth(tolerance);

        return damage;
    }

    private void increaseTolerance(final Player player) {
        final UUID uuid = player.getUniqueId();
        final int tolerance = TOLERANCE.getOrDefault(uuid, 0);

        TOLERANCE.put(uuid, tolerance + 1);
    }

    private void playSounds(final Player player) {
        if (player == null) return;

        final Sound[] sounds = {
                Sound.ENTITY_ENDER_DRAGON_GROWL,
                Sound.ENTITY_GHAST_SCREAM,
                Sound.ENTITY_WITHER_SPAWN,
                Sound.ENTITY_LIGHTNING_BOLT_THUNDER,
                Sound.BLOCK_ANVIL_LAND,
                Sound.ENTITY_CREEPER_PRIMED,
                Sound.BLOCK_GLASS_BREAK,
                Sound.ENTITY_WARDEN_HEARTBEAT
        };

        for (final Sound sound : sounds) {
            player.playSound(player.getLocation(), sound, 1.0F, 1.0F);
        }
    }

    private void playEffects(final Player player) {
        final Location loc = player.getLocation();

        player.playEffect(loc, Effect.ANVIL_BREAK, null);
        player.playEffect(loc, Effect.GHAST_SHOOT, null);
    }

    private boolean isCooked(final Player player, final double originalHealth) {
        final double damage = this.calculateToleranceHealthDamage(player);
        return damage >= originalHealth;
    }

    private void overdose(final Player player) {
        final double originalHealth = player.getHealth();
        final int durationTicks = DURATION * 20;

        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, durationTicks, 2, false, false, false), true);
        player.addPotionEffect(new PotionEffect(PotionEffectType.LEVITATION, durationTicks, 2, false, false, false), true);

        new BukkitRunnable() {
            private float timeElapsed = 0F; // In seconds
            private boolean toggle = false;

            @Override
            public void run() {
                if (((int) this.timeElapsed) > DURATION) {
                    super.cancel();
                    player.setHealth(originalHealth);

                    increaseTolerance(player);
                    double damage = calculateToleranceHealthDamage(player);

                    if (damage > player.getHealth()) { // If damage is above players health, remove difference
                        final double diff = damage - player.getHealth();
                        damage -= diff;
                    }

                    player.damage(damage);
                    player.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, durationTicks, 0, false, false, false), true);
                    player.addPotionEffect(new PotionEffect(PotionEffectType.HUNGER, durationTicks, 0, false, false, false), true);
                    return;
                }

                if (timeElapsed == 3) {
                    player.sendTitle(ChatColor.AQUA + "Your vision is going black!", ChatColor.GOLD + "better hold on tight.", 10, 120, 20);
                } else if (timeElapsed == 4) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.DARKNESS, durationTicks, 2, false, false, false));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, durationTicks, 1, false, false, false), true);
                } else if (timeElapsed == 5) {
                    player.getWorld().strikeLightningEffect(player.getLocation());
                } else if (timeElapsed == 13 && isCooked(player, originalHealth)) {
                    player.sendTitle(ChatColor.DARK_RED + "Your are cooked buddy!", ChatColor.AQUA + "You decided to take it a step too far and overdose on fentanyl!! Goodbye...");
                }

                final int rand;

                if (!toggle) {
                    rand = plugin.randomNumber(1, 4);
                } else {
                    rand = plugin.randomNumber(17, 19);
                }

                player.setHealth(rand);

                playSounds(player);
                playEffects(player);

                player.setVelocity(player.getVelocity().setY(-1000));

                this.toggle = !this.toggle;
                this.timeElapsed += 0.5F;
            }
        }.runTaskTimer(Yggdrasil.plugin, 10L, 10L);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + String.format("Usage: /%s <player>", label));
            return true;
        }

        final Player target = sender.getServer().getPlayer(args[0]);

        if (target == null || !target.isOnline()) {
            sender.sendMessage(ChatColor.RED + "Player not found.");
            return true;
        }

        this.overdose(target);
        sender.sendMessage(ChatColor.GREEN + String.format("Attempted to overdose %s.", target.getName()));
        return true;
    }
}