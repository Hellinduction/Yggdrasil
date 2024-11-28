package dev.heypr.yggdrasil;

import dev.heypr.yggdrasil.commands.CommandWrapper;
import dev.heypr.yggdrasil.commands.impl.*;
import dev.heypr.yggdrasil.data.PlayerData;
import dev.heypr.yggdrasil.events.*;
import dev.heypr.yggdrasil.misc.SkinManager;
import dev.heypr.yggdrasil.misc.discord.Bot;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandExecutor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.stream.Collectors;

public final class Yggdrasil extends JavaPlugin {
    public static final int MAX_LIVES = 6;

    public static Yggdrasil plugin;

    Map<UUID, PlayerData> playerData = new HashMap<>();
    List<Player> deadPlayers = new ArrayList<>();
    public boolean isSessionRunning = false;

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

        this.saveConfig();
    }

    @Override
    public void onEnable() {
        plugin = this;

        ConfigurationSerialization.registerClass(SkinManager.SkinData.class);

        this.initConfig();

        this.skinManager = new SkinManager(this);

        registerEvent(new PlayerJoinListener(this));
        registerEvent(new PlayerDeathListener(this));
        registerEvent(new PlayerLeaveListener(this));
        registerEvent(new PlayerChatListener(this));
        registerEvent(new PlayerRespawnListener(this));

        registerCommand("givelife", new CommandWrapper(new GiveLifeCommand(this)));
        registerCommand("addlives", new CommandWrapper(new AddLivesCommand(this)));
        registerCommand("lives", new CommandWrapper(new LivesCommand(this), true));
        registerCommand("removeboogeyman", new CommandWrapper(new RemoveBoogeymanCommand(this)));
        registerCommand("setboogeyman", new CommandWrapper(new SetBoogeymanCommand(this)));
        registerCommand("randomizeboogeyman", new CommandWrapper(new RandomizeBoogeymanCommand(this), true));
        registerCommand("startsession", new CommandWrapper(new StartSessionCommand(this)));
        registerCommand("stopsession", new CommandWrapper(new StopSessionCommand(this)));
        registerCommand("addplayer", new CommandWrapper(new AddPlayerCommand(this), true));
        registerCommand("skin", new CommandWrapper(new SkinCommand(this)));
        registerCommand("setdiscordtoken", new CommandWrapper(new SetDiscordTokenCommand(this)));

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
        Bot.bot.shutdownNow();
    }

    public BukkitScheduler getScheduler() {
        return this.plugin.getServer().getScheduler();
    }

    public Map<UUID, PlayerData> getPlayerData() {
        return playerData;
    }

    public List<Player> getDeadPlayers() {
        return deadPlayers;
    }

    public void registerEvent(Listener listener) {
        getServer().getPluginManager().registerEvents(listener, this);
    }

    public void registerCommand(String command, CommandExecutor executor) {
        getCommand(command).setExecutor(executor);
    }

    public int randomNumber(int lower, int upper) {
        return (int) (Math.random() * (upper - lower + 1)) + lower;
    }

    public TextComponent deserializeText(String text) {
        LegacyComponentSerializer legacy = LegacyComponentSerializer.legacyAmpersand();
        MiniMessage mm = MiniMessage.miniMessage();
        return legacy.deserialize(legacy.serialize(mm.deserialize(text).asComponent()));
    }

    public List<Player> getBoogieManPool() {
        final List<Player> players = new ArrayList<>(plugin.getServer().getOnlinePlayers());
        final List<Player> potentialBoogyMen = players.stream()
                .filter(player -> PlayerData.retrieveLives(player.getUniqueId()) != 0)
                .collect(Collectors.toList());

        Collections.shuffle(potentialBoogyMen);

        return potentialBoogyMen;
    }
}
