package dev.heypr.yggdrasil.events;

import dev.heypr.yggdrasil.Yggdrasil;
import dev.heypr.yggdrasil.commands.impl.ShuffleNamesCommand;
import dev.heypr.yggdrasil.commands.impl.ToggleScoreboardCommand;
import dev.heypr.yggdrasil.data.PlayerData;
import dev.heypr.yggdrasil.data.TemporaryPlayerData;
import dev.heypr.yggdrasil.misc.ColorManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Collection;

public class PlayerJoinListener implements Listener {

    private final Yggdrasil plugin;

    public PlayerJoinListener(Yggdrasil plugin) {
        this.plugin = plugin;
    }

    private void removeFormerlyKnownAs(final PlayerJoinEvent event) {
        if (event.getJoinMessage() == null)
            return;

        if (!event.getJoinMessage().toLowerCase().contains("formerly known as"))
            return;

        event.setJoinMessage(event.getJoinMessage().replaceAll("\\s*\\(formerly known as .*?\\)", ""));
    }

    @SuppressWarnings("all")
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(final PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        final String originalUsername = player.getName();

        player.setGlowing(false);

        plugin.getOriginalUsernameMap().put(player.getUniqueId(), originalUsername);
        plugin.skinManager.saveSkinData(player);

        this.removeFormerlyKnownAs(event);

        if (ToggleScoreboardCommand.isEnabled()) {
            if (plugin.getScoreboard() == null)
                plugin.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());

            player.setScoreboard(plugin.getScoreboard());
        }

        if (!plugin.isSessionRunning) {
            plugin.getScheduler().runTaskLater(plugin, () ->  {
                player.setGameMode(GameMode.ADVENTURE);

                final Collection<PotionEffect> originalEffects = new ArrayList<>(player.getActivePotionEffects());
                final TemporaryPlayerData data = TemporaryPlayerData.get(player);

                originalEffects.removeIf(effect -> effect.getDuration() == PotionEffect.INFINITE_DURATION);
                data.setEffects(originalEffects);

                player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, PotionEffect.INFINITE_DURATION, 50, false, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, PotionEffect.INFINITE_DURATION, 50, false, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, PotionEffect.INFINITE_DURATION, 50, false, false));
            }, 10L);

            player.sendTitle(ChatColor.RED + "Game not started", "Please wait for the game to start", 10, 70, 20);
            return;
        }

        if (!plugin.getPlayerData().containsKey(player.getUniqueId())) {
            plugin.getScheduler().runTaskLater(plugin, () -> player.setGameMode(GameMode.ADVENTURE), 10L);

            player.sendTitle(ChatColor.RED + "Game in progress", ChatColor.RED + "You are not part of the game", 10, 70, 20);
            player.sendMessage(ChatColor.RED + "Game in progress. Request an admin to add you to the game using /addplayer <player>");

            plugin.getServer().getOperators().forEach((op) -> {
                if (!(op.isOnline())) return;
                op.getPlayer().sendMessage(ChatColor.DARK_RED + "[URGENT] Player " + player.getName() + " does not have game data. They will be treated as a dead player. Add them to the list of players using /addplayer <player>");
                return;
            });
            return;
        }

        final PlayerData playerData = plugin.getPlayerData().get(player.getUniqueId());

        if (ShuffleNamesCommand.isEnabled()) {
            final PlayerData disguisedAs = playerData.getDisguiseData();
            final String username = disguisedAs == null ? null : disguisedAs.getUsername();

            if (username != null)
                event.setJoinMessage(event.getJoinMessage().replace(originalUsername, username));
        }

        playerData.update(PlayerData.UpdateFrom.JOIN, -1);
        plugin.getSchedulerWrapper().runTaskLater(plugin, () -> playerData.checkLives(), 10L, true);

        ColorManager.setTabListName(player, plugin.getPlayerData().get(player.getUniqueId()));

        plugin.getSchedulerWrapper().runTaskLater(plugin, () -> ColorManager.setTabListName(player, plugin.getPlayerData().get(player.getUniqueId())), 20L, true); // To fix it incase the skin thing removes it
    }
}