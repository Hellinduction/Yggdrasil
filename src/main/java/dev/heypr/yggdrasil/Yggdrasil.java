package dev.heypr.yggdrasil;

import dev.heypr.yggdrasil.commands.CommandWrapper;
import dev.heypr.yggdrasil.commands.impl.*;
import dev.heypr.yggdrasil.data.PlayerData;
import dev.heypr.yggdrasil.events.*;
import dev.heypr.yggdrasil.misc.BukkitSchedulerWrapper;
import dev.heypr.yggdrasil.misc.SkinManager;
import dev.heypr.yggdrasil.misc.discord.Bot;
import dev.heypr.yggdrasil.misc.object.SkinData;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.scoreboard.Scoreboard;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.*;
import java.util.stream.Collectors;

public final class Yggdrasil extends JavaPlugin {
    public static final int MAX_LIVES = 6;

    public static Yggdrasil plugin;

    Map<UUID, PlayerData> playerData = new HashMap<>();
    List<Player> deadPlayers = new ArrayList<>();
    List<BukkitTask> cancelOnShutdown = new ArrayList<>();
    List<BukkitTask> cancelOnSessionStop = new ArrayList<>();

    public BukkitScheduler schedulerWrapper;
    public boolean isSessionRunning = false;
    public boolean isCullingSession = false;
    private Scoreboard scoreboard;

    public SkinManager skinManager;
    private FileConfiguration config;
    private Bot bot;

    private void initConfig() {
        this.saveDefaultConfig();
        this.config = super.getConfig();

        ConfigurationSection section;

        if (!this.config.contains("skins")) {
            section = this.config.createSection("skins");

            if (!section.contains("stored"))
                section.createSection("stored");
        }

        if (!this.config.contains("discord")) {
            section = this.config.createSection("discord");

            if (!section.contains("guilds"))
                section.createSection("guilds");

            if (!section.contains("linked"))
                section.createSection("linked");
        }

        if (!this.config.contains("players"))
            this.config.createSection("players");

        if (!this.config.contains("chat")) {
            section = this.config.createSection("chat");
            section.set("disabled", true);
        }

        if (!this.config.contains("scoreboard_enabled"))
            this.config.set("scoreboard_enabled", true);

        if (!this.config.contains("values")) {
            section = this.config.createSection("values");

            section.setComments("rand_lives_lower_bound", Arrays.asList("The random range for how many lives a player will recieve"));
            section.set("rand_lives_lower_bound", 2);
            section.set("rand_lives_upper_bound", 6);

            section.setComments("rand_boogey_lower_bound", Arrays.asList("The random range for how many Boogeyman there are each session"));
            section.set("rand_boogey_lower_bound", 1);
            section.set("rand_boogey_upper_bound", 3);

            section.setComments("min_lives_through_give_life_command", Arrays.asList("The minimum amount of lives a player can go down to after giving some of their lives away"));
            section.set("min_lives_through_give_life_command", 1);
        }

        this.saveConfig();
    }

    @Override
    public void onEnable() {
        plugin = this;

        this.schedulerWrapper = new BukkitSchedulerWrapper();

        ConfigurationSerialization.registerClass(SkinData.class);

        this.initConfig();

        this.skinManager = new SkinManager(this);

        registerEvent(new PlayerJoinListener(this));
        registerEvent(new PlayerDeathListener(this));
        registerEvent(new PlayerLeaveListener(this));
        registerEvent(new PlayerChatListener(this));
        registerEvent(new PlayerRespawnListener(this));
        registerEvent(new PlayerChangeWorldListener(this));
        registerEvent(new TrapListeners(this));
        registerEvent(new PlayerItemDropListener(this));
        registerEvent(new PlayerItemPickupListener(this));
        registerEvent(new NetheriteCraftListener(this));
        registerEvent(new PlayerPreSessionStartAttackListener(this));

        registerCommand("givelife", new CommandWrapper(new GiveLifeCommand(this), true));
        registerCommand("addlives", new CommandWrapper(new AddLivesCommand(this)));
        registerCommand("lives", new CommandWrapper(new LivesCommand(this), true, true));
        registerCommand("removeboogeyman", new CommandWrapper(new RemoveBoogeymanCommand(this)));
        registerCommand("setboogeyman", new CommandWrapper(new SetBoogeymanCommand(this)));
        registerCommand("randomizeboogeyman", new CommandWrapper(new RandomizeBoogeymanCommand(this), true));
        registerCommand("startsession", new CommandWrapper(new StartSessionCommand(this)));
        registerCommand("stopsession", new CommandWrapper(new StopSessionCommand(this)));
        registerCommand("addplayer", new CommandWrapper(new AddPlayerCommand(this), true));
        registerCommand("skin", new CommandWrapper(new SkinCommand(this), false, true));
        registerCommand("setdiscordtoken", new CommandWrapper(new SetDiscordTokenCommand(this)));
        registerCommand("listboogeymen", new CommandWrapper(new ListBoogeyMenCommand(this), true, false));
        registerCommand("togglechat", new CommandWrapper(new ToggleChatCommand(this)));
        registerCommand("wipelives", new CommandWrapper(new WipeLivesCommand(this), false, false, true));
        registerCommand("preloadskins", new CommandWrapper(new PreloadSkinsCommand(this), false, false, false));
        registerCommand("togglenetherite", new CommandWrapper(new ToggleNetheriteCommand(this)));
        registerCommand("togglescoreboard", new CommandWrapper(new ToggleScoreboardCommand(this)));

        registerCommand("fentanyl", new CommandWrapper(new FentanylCommand(this), false, false, false));

        this.initPlaceholders();
        this.loadBot();
    }

    public void loadBot() {
        final ConfigurationSection section = this.getConfig().getConfigurationSection("discord");

        if (!section.contains("token"))
            return;

        final String token = section.getString("token");
        this.bot = new Bot(this, token);
    }

    public Bot getBot() {
        return this.bot;
    }

    @Override
    public FileConfiguration getConfig() {
        return this.config;
    }

    private void initPlaceholders() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null)
            return;

        // Have to use reflection just in case PlaceholderAPI is not on the server
        try {
            final Class<?> clazz = Class.forName("dev.heypr.yggdrasil.misc.papi.YggdrasilExpansion");
            final Object instance = clazz.getDeclaredConstructor(Yggdrasil.class).newInstance(this.plugin);
            final Method method = clazz.getSuperclass().getDeclaredMethod("register");

            method.invoke(instance);
        } catch (final ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InstantiationException |
                       InvocationTargetException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        if (Bot.bot != null)
            Bot.bot.shutdownNow();

        this.cancelTasks(this.getCancelOnShutdown());
    }

    public void cancelTasks(final List<BukkitTask> tasks) {
        for (final BukkitTask task : tasks) {
            if (task != null)
                task.cancel();
        }
    }

    public <T extends BukkitScheduler> T getScheduler() {
        return (T) this.schedulerWrapper;
    }

    public BukkitSchedulerWrapper getSchedulerWrapper() {
        return this.getScheduler();
    }

    public Map<UUID, PlayerData> getPlayerData() {
        return playerData;
    }

    public List<Player> getDeadPlayers() {
        return deadPlayers;
    }

    public List<BukkitTask> getCancelOnShutdown() {
        return this.cancelOnShutdown;
    }

    public List<BukkitTask> getCancelOnSessionStop() {
        return this.cancelOnSessionStop;
    }

    public void registerEvent(Listener listener) {
        getServer().getPluginManager().registerEvents(listener, this);
    }

    public void registerCommand(String command, CommandExecutor executor) {
        getCommand(command).setExecutor(executor);

        if (executor instanceof TabCompleter tabCompleter)
            getCommand(command).setTabCompleter(tabCompleter);
        else if (executor instanceof CommandWrapper wrapper && wrapper.getExecutor() instanceof TabCompleter tabCompleter)
            getCommand(command).setTabCompleter(tabCompleter);
    }

    public int randomNumber(int lower, int upper) {
        return (int) (Math.random() * (upper - lower + 1)) + lower;
    }

    public int randomLives() {
        return this.randomNumber(config.getInt("values.rand_lives_lower_bound"), config.getInt("values.rand_lives_upper_bound"));
    }

    public int randomBoogeyMenCount() {
        return this.randomNumber(config.getInt("values.rand_boogey_lower_bound"), config.getInt("values.rand_boogey_upper_bound"));
    }

    public TextComponent deserializeText(String text) {
        LegacyComponentSerializer legacy = LegacyComponentSerializer.legacyAmpersand();
        MiniMessage mm = MiniMessage.miniMessage();
        return legacy.deserialize(legacy.serialize(mm.deserialize(text).asComponent()));
    }

    /**
     * Returns all people that could be boogeyman
     * @return
     */
    private List<Player> getBoogeyManPool() {
        final List<Player> players = new ArrayList<>(plugin.getServer().getOnlinePlayers());

        final List<Player> potentialBoogyMen = players.stream()
                .filter(player -> PlayerData.retrieveLives(player.getUniqueId()) > 1)
                .collect(Collectors.toList());

        return potentialBoogyMen;
    }

    /**
     * Randomly picks a number of Boogeymen from the eligible pool.
     * @param boogeyMen Number of Boogeymen to select.
     * @return List of players who were selected as Boogeymen.
     */
    public List<Player> pickBoogeyMen(int boogeyMen) {
        final List<Player> boogeyManPool = this.getBoogeyManPool();
        final int boogeyManPoolCount = boogeyManPool.size();

        if (boogeyMen >= boogeyManPoolCount && boogeyManPoolCount != 1)
            boogeyMen = boogeyManPoolCount - 1;

        if (boogeyMen < 0)
            boogeyMen = 0;

        Collections.shuffle(boogeyManPool);

        return boogeyManPool.stream()
                .limit(boogeyMen)
                .collect(Collectors.toList());
    }

    public Scoreboard getScoreboard() {
        return this.scoreboard;
    }

    public void setScoreboard(final Scoreboard scoreboard) {
        this.scoreboard = scoreboard;
    }

    public static String hashFile(final File file) {
        try {
            final MessageDigest digest = MessageDigest.getInstance("SHA-256");
            final byte[] data = Files.readAllBytes(file.toPath());
            digest.update(data);
            final String checksum = new BigInteger(1, digest.digest()).toString(16);
            return checksum;
        } catch (final Exception exception) {
            return null;
        }
    }
}
