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
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.UUID;
import java.util.function.Predicate;

public class PlayerData {
    private UUID uuid;
    private String username;
    private boolean revealedData = false;
    private int lives; // Persistent
    private boolean isBoogeyman;
    private boolean lastChance;
    private int kills;

    public PlayerData(UUID uuid, String username, int lives) {
        this.uuid = uuid;
        this.username = username;
        this.lives = lives;
        this.isBoogeyman = false;
        this.lastChance = false;
        this.kills = 0;

        this.update(Integer.MIN_VALUE);
    }

    public boolean isDead() {
        if (this.lives > 0 || (Yggdrasil.plugin.isCullingSession && !(this.lives < 0)))
            return false;

        return true;
    }

    private void givePotionEffects(final Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, PotionEffect.INFINITE_DURATION, 0));
        player.addPotionEffect(new PotionEffect(PotionEffectType.MINING_FATIGUE, PotionEffect.INFINITE_DURATION, 0));
    }

    private void removePotionEffects(final Player player) {
        player.removePotionEffect(PotionEffectType.SLOWNESS);
        player.removePotionEffect(PotionEffectType.MINING_FATIGUE);
    }

    public void checkLives() {
        final Player player = this.getPlayer();

        if (player == null || !player.isOnline())
            return;

        if (!this.isDead()) {
            if (this.lives == 0)
                this.givePotionEffects(player);
            else if (this.lives > 0)
                this.removePotionEffects(player);

            return;
        }

        this.removePotionEffects(player);
        player.setGameMode(GameMode.SPECTATOR);
    }

    public void revive() {
        final Player player = this.getPlayer();

        if (player == null || !player.isOnline())
            return;

        player.sendTitle(ChatColor.GREEN + "You have been revived!", "", 10, 20, 10);
        player.setGameMode(GameMode.SURVIVAL);
    }

    public UUID getUuid() {
        return uuid;
    }

    /**
     * Returns this player's real username regardless of their disguise
     * @return
     */
    public String getUsername() {
        return this.username;
    }

    /**
     * This will either just simply get their username, or their nickname with their username is brackets depending on whether they are nicked
     * @return
     */
    public String getDisplayName() {
        final PlayerData data = this.getDisguiseData();

        if (data == null)
            return this.getUsername();

        return String.format("%s (%s)", data.getUsername(), this.getUsername());
    }

    public String getUsernameOrNick() {
        final PlayerData data = this.getDisguiseData();

        return data == null ? this.getUsername() : data.getUsername();
    }

    public boolean isOnline() {
        final Player player = this.getPlayer();

        return player != null && player.isOnline();
    }

    public int getLives() {
        return lives;
    }

    public int getDisplayLives() {
        return lives > -1 ? lives : 0;
    }

    public boolean hasRevealedData() {
        if (!Yggdrasil.plugin.isSessionRunning)
            return false;

        return revealedData;
    }

    public void setRevealedData(final boolean value) {
        if (!Yggdrasil.plugin.isSessionRunning)
            return;

        revealedData = value;
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
        this.updateColor(); // Set their lives to display again (setting skin messes it up)

        if (player.getGameMode() == GameMode.SPECTATOR) {
            player.setGameMode(GameMode.SURVIVAL);
            player.setGameMode(GameMode.SPECTATOR);
        }
    }

    private UUID getDisguise() {
        return Yggdrasil.plugin.getDisguiseMap().get(this.uuid);
    }

    public OfflinePlayer getDisguisePlayer() {
        final UUID uuid = this.getDisguise();
        return uuid == null ? null : Bukkit.getOfflinePlayer(uuid);
    }

    public PlayerData getDisguiseData() {
        final UUID uuid = this.getDisguise();
        return uuid == null ? null : Yggdrasil.plugin.getPlayerData().get(uuid);
    }

    private void updateSkin() {
        final Player player = this.getPlayer();

        if (player == null || !player.isOnline())
            return;

        final OfflinePlayer disguisedAs = this.getDisguisePlayer();
        final PlayerData disguisedAsData = this.getDisguiseData();
        final File skinFile = ColorManager.getSkinFile(Yggdrasil.plugin, disguisedAs == null ? player : disguisedAs, this);

        Yggdrasil.plugin.skinManager.skin(player, skinFile, disguisedAsData == null ? null : disguisedAsData.getUsername(), success -> Yggdrasil.plugin.getSchedulerWrapper().runTaskLater(Yggdrasil.plugin, () -> this.fixPlayerData(player), 20L, true));
    }

    public String updateNick() {
        final Player player = this.getPlayer();

        if (player == null || !player.isOnline())
            return null;

        final PlayerData disguisedAs = this.getDisguiseData();

        if (disguisedAs == null)
            return null;

        Yggdrasil.plugin.skinManager.nick(player, disguisedAs.getUsername(), exception -> {
            if (exception != null)
                exception.printStackTrace();
        });

        return disguisedAs.getUsername();
    }

    private void removeOtherColorRoles(final Guild guild, final Member member, final ColorManager.Colors exclude) {
        final ConfigurationSection rolesSection = EventListener.getRoles(guild.getId());

        if (rolesSection == null)
            return;

        for (final ColorManager.Colors colors : ColorManager.Colors.values()) {
            if (colors == exclude)
                continue;

            final String roleName = colors.getRoleName();
            final String id = rolesSection.getString(roleName);
            final Role role = guild.getRoleById(id);

            if (role == null)
                continue;

            if (!BotUtils.hasRole(member, id))
                continue;

            guild.removeRoleFromMember(member, role).queue();
        }
    }

    private void updateDiscordColor() {
        final ColorManager.Colors colors = ColorManager.Colors.from(this.lives);
        final String roleName = colors.getRoleName();
        final LinkCommand command = CommandManager.getCommand("link");
        final ConfigurationSection section = command.getUserSection(this.uuid);

        if (section == null)
            return;

        final String discordId = section.getName();
        final User user = Bot.bot.getUserById(discordId);
        final Guild guild = EventListener.guild;
        final Member member = guild.getMember(user);
        final ConfigurationSection rolesSection = EventListener.getRoles(guild.getId());

        if (rolesSection == null)
            return;

        final Role role = guild.getRoleById(rolesSection.getString(roleName));

        if (role == null)
            return;

        if (BotUtils.hasRole(member, role.getId()))
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

        if (previousLives != Integer.MIN_VALUE)
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
                player.sendMessage(ChatColor.GREEN + "You successfully got 3 kills, you have regained a life!");
        }
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(this.uuid);
    }

    public void displayLives(final boolean randomizer) {
        this.displayLives(randomizer, false);
    }

    public void displayLives(final boolean randomizer, final boolean addPlayer) {
        final Player player = this.getPlayer();
        final Yggdrasil plugin = Yggdrasil.plugin;

        if (player == null || !player.isOnline())
            return;

        player.sendTitle(ChatColor.GRAY + "You will have...", "", 10, 20, 10);

        new BukkitRunnable() {
            int e = randomizer ? 15 : 0;

            @Override
            public void run() {
                if (e > 0) {
                    int lives = plugin.randomLives();
                    ChatColor color = ColorManager.getColor(lives);

                    player.sendTitle(color + "" + lives,
                            "",
                            10, 20, 10);
                    e--;
                } else {
                    ChatColor color = ColorManager.getColor(lives);

                    player.sendTitle(color + "" + getDisplayLives() + " lives",
                            "",
                            10, 20, 10);

                    updateColor();
                    updateSkin();
                    cancel();

                    if (addPlayer)
                        revealedData = true;
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
            return Integer.MIN_VALUE;

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

        if (lives == Integer.MIN_VALUE)
            return new Pair<>(defaultLives, true);

        return new Pair<>(lives, false);
    }

    public static int retrieveLivesOrDefault(final UUID uuid, final int defaultLives) {
        return retrieveLivesOrDefaultAsPair(uuid, defaultLives).getKey();
    }

    public static int clearPlayers() {
        if (!Yggdrasil.plugin.getConfig().contains("players"))
            return -1;

        final int size = Yggdrasil.plugin.getConfig().getConfigurationSection("players").getKeys(false).size();

        Yggdrasil.plugin.getConfig().set("players", null);
        Yggdrasil.plugin.getConfig().createSection("players");
        Yggdrasil.plugin.getPlayerData().clear();

        Yggdrasil.plugin.saveConfig();

        return size;
    }

    public static PlayerData find(final Predicate<PlayerData> predicate) {
        for (final PlayerData data : Yggdrasil.plugin.getPlayerData().values()) {
            if (predicate.test(data))
                return data;
        }

        return null;
    }

    /**
     * Get from the players real username
     * @return
     */
    public static PlayerData fromUsername(final String username) {
        if (username == null)
            return null;

        return find(data -> data.getUsername().equalsIgnoreCase(username));
    }

    public static PlayerData fromDisguiseName(final String disguiseName) {
        if (disguiseName == null)
            return null;

        return find(data -> data.getDisguiseData() != null && data.getDisguiseData().getUsername().equalsIgnoreCase(disguiseName));
    }

    public static PlayerData fromUsernameOrDisguiseName(final String name) {
        final PlayerData disguiseData = fromDisguiseName(name);
        return disguiseData == null ? fromUsername(name) : disguiseData;
    }
}
