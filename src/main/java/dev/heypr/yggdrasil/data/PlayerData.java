package dev.heypr.yggdrasil.data;

import dev.heypr.yggdrasil.Yggdrasil;
import dev.heypr.yggdrasil.misc.ColorManager;
import dev.heypr.yggdrasil.misc.discord.Bot;
import dev.heypr.yggdrasil.misc.discord.BotUtils;
import dev.heypr.yggdrasil.misc.discord.command.CommandManager;
import dev.heypr.yggdrasil.misc.discord.command.impl.LinkCommand;
import dev.heypr.yggdrasil.misc.discord.listeners.EventListener;
import dev.heypr.yggdrasil.misc.object.Pair;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.UUID;

public class PlayerData {
    private UUID uuid;
    private int lives; // Persistent
    private boolean isBoogeyman;
    private boolean lastChance;
    private int kills;

    public PlayerData(UUID uuid, int lives) {
        this.uuid = uuid;
        this.lives = lives;
        this.isBoogeyman = false;
        this.lastChance = false;
        this.kills = 0;

        this.update(-1);
    }

    public void checkDead() {
        final Player player = this.getPlayer();

        if (player == null || !player.isOnline())
            return;

        if (this.lives != 0 || this.lastChance)
            return;

        player.setGameMode(GameMode.SPECTATOR);

//        player.setGameMode(GameMode.ADVENTURE);
//
//        player.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, PotionEffect.INFINITE_DURATION, 500));
//        player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, PotionEffect.INFINITE_DURATION, 500));
//        player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, PotionEffect.INFINITE_DURATION, 500));
    }

    public void revive() {
        final Player player = this.getPlayer();

        if (player == null || !player.isOnline())
            return;

        player.sendTitle(ChatColor.GREEN + "You have been revived!", "", 10, 20, 10);
//        player.removePotionEffect(PotionEffectType.MINING_FATIGUE);
//        player.removePotionEffect(PotionEffectType.WEAKNESS);
//        player.removePotionEffect(PotionEffectType.RESISTANCE);
        player.setGameMode(GameMode.SURVIVAL);
    }

    public UUID getUuid() {
        return uuid;
    }

    public int getLives() {
        return lives;
    }

    public void setLives(int amount) {
        final int originalLives = this.lives;

        this.lives = amount;
        this.update(originalLives);
        this.updateColor();
    }

    public void addLives(int amount) {
        final int originalLives = this.lives;

        this.lives += amount;
        this.update(originalLives);
        this.updateColor();
    }

    public void decreaseLives(int amount) {
        final int originalLives = this.lives;

        this.lives -= amount;
        this.update(originalLives);
        this.updateColor();
    }

    private void updateColor() {
        final Player player = this.getPlayer();

        if (player == null)
            return;

        ColorManager.setTabListName(player, this);
    }

    private void fixPlayerData(final Player player) {
        ColorManager.setTabListName(player, this); // Set their lives to display again (setting skin messes it up)

        if (player.getGameMode() == GameMode.SPECTATOR) {
            player.setGameMode(GameMode.SURVIVAL);
            player.setGameMode(GameMode.SPECTATOR);
        }
    }

    private void updateSkin() {
        final Player player = this.getPlayer();

        if (player == null || !player.isOnline())
            return;

        final File skinFile = ColorManager.getSkinFile(Yggdrasil.plugin, player, this);

        if (skinFile == null)
            return;

        Yggdrasil.plugin.skinManager.skin(player, skinFile, success -> Yggdrasil.plugin.getScheduler().runTaskLater(Yggdrasil.plugin, () -> {
            this.fixPlayerData(player);
        }, 20L));
    }

    private void removeOtherColorRoles(final Guild guild, final Member member, final ColorManager.Colors exclude) {
        for (final ColorManager.Colors colors : ColorManager.Colors.values()) {
            if (colors == exclude)
                continue;

            final String roleName = colors.name().toLowerCase();
            final Role role = guild.getRolesByName(roleName, false).get(0);

            if (role == null)
                continue;

            if (!BotUtils.hasRole(member, roleName))
                continue;

            guild.removeRoleFromMember(member, role).queue();
        }
    }

    private void updateDiscordColor() {
        final ColorManager.Colors colors = ColorManager.Colors.from(this.lives);
        final String roleName = colors.name().toLowerCase();
        final LinkCommand command = CommandManager.getCommand("link");
        final ConfigurationSection section = command.getUserSection(this.uuid);

        if (section == null)
            return;

        final String discordId = section.getName();
        final User user = Bot.bot.getUserById(discordId);
        final Guild guild = EventListener.guild;
        final Member member = guild.getMember(user);
        final Role role = guild.getRolesByName(roleName, false).get(0);

        if (role == null)
            return;

        if (BotUtils.hasRole(member, roleName))
            return;

        this.removeOtherColorRoles(guild, member, colors);

        guild.addRoleToMember(member, role).queue();
    }

    private void saveLives() {
        final ConfigurationSection section = Yggdrasil.plugin.getConfig().getConfigurationSection("players");
        ConfigurationSection playerSection;

        if (!section.contains(this.uuid.toString()))
            playerSection = section.createSection(this.uuid.toString());
        else
            playerSection = section.getConfigurationSection(this.uuid.toString());

        playerSection.set("lives", this.lives);
        Yggdrasil.plugin.saveConfig();
    }

    /**
     * Updates some stuff based on the PlayerData state
     */
    public void update(final int previousLives) {
        if (previousLives == this.lives)
            return;

        this.saveLives();
        this.updateSkin();

        if (this.lives > 0 && this.lastChance)
            this.lastChance = false;

        if (Yggdrasil.plugin.getBot() != null)
            this.updateDiscordColor();
    }

    public boolean isBoogeyman() {
        return isBoogeyman;
    }

    public void setBoogeyman(boolean boogeyman) {
        this.isBoogeyman = boogeyman;
    }

    public boolean hasLastChance() {
        return this.lastChance;
    }

    public void setLastChance(final boolean lastChance) {
        this.lastChance = lastChance;
        this.updateColor();
    }

    public int getKills() {
        return this.kills;
    }

    public void incrementKills() {
        ++this.kills;
    }

    public void checkGraduate() {
        if (Yggdrasil.plugin.isCullingSession && this.lastChance && this.kills >= 3 && this.lives == 0) {
            this.addLives(1);

            final Player player = this.getPlayer();

            if (player != null && player.isOnline())
                player.sendMessage(ChatColor.GREEN + "You successfully got 3 killed, you have regained a life!");
        }
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(this.uuid);
    }

    public void displayLives(final boolean randomizer) {
        final Player player = this.getPlayer();
        final Yggdrasil plugin = Yggdrasil.plugin;

        if (player == null)
            return;

        player.sendTitle(ChatColor.GRAY + "You will have...", "", 10, 20, 10);

        new BukkitRunnable() {
            int e = randomizer ? 5 : 0;

            @Override
            public void run() {
                if (e > 0) {
                    int lives = plugin.randomNumber(2, 6);
                    ChatColor color = ColorManager.getColor(lives);

                    player.sendTitle(color + "" + lives,
                            "",
                            10, 20, 10);
                    e--;
                } else {
                    ChatColor color = ColorManager.getColor(lives);

                    player.sendTitle(color + "" + lives + " lives",
                            "",
                            10, 20, 10);

                    ColorManager.setTabListName(player, plugin.getPlayerData().get(player.getUniqueId()));
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 40L, 5L);
    }

    /**
     * Returns -1 if never stored
     * @param uuid
     * @return
     */
    public static int retrieveLives(final UUID uuid) {
        if (!Yggdrasil.plugin.getConfig().contains(String.format("players.%s", uuid.toString())))
            return -1;

        final ConfigurationSection playerSection = Yggdrasil.plugin.getConfig().getConfigurationSection(String.format("players.%s", uuid));
        final int lives = playerSection.getInt("lives");

        return lives;
    }

    /**
     * Returns a Pair with the Integer being the number of lives and the Boolean being whether it used the provided defaultLives
     * @param uuid
     * @param defaultLives
     * @return
     */
    public static Pair<Integer, Boolean> retrieveLivesOrDefaultAsPair(final UUID uuid, final int defaultLives) {
        final int lives = retrieveLives(uuid);

        if (lives <= -1)
            return new Pair<>(defaultLives, true);

        return new Pair<>(lives, false);
    }

    public static int retrieveLivesOrDefault(final UUID uuid, final int defaultLives) {
        return retrieveLivesOrDefaultAsPair(uuid, defaultLives).getKey();
    }
}
