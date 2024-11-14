package dev.heypr.yggdrasil;

import dev.heypr.yggdrasil.commands.*;
import dev.heypr.yggdrasil.data.PlayerData;
import dev.heypr.yggdrasil.events.PlayerDeathListener;
import dev.heypr.yggdrasil.events.PlayerJoinListener;
import dev.heypr.yggdrasil.events.PlayerLeaveListener;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public final class Yggdrasil extends JavaPlugin {

    Map<UUID, PlayerData> playerData = new HashMap<>();
    List<Player> deadPlayers = new ArrayList<>();
    public boolean isSessionRunning = false;
    public boolean isGameRunning = false;

    Yggdrasil plugin;

    @Override
    public void onEnable() {
        plugin = this;

        registerEvent(new PlayerJoinListener(this));
        registerEvent(new PlayerDeathListener(this));
        registerEvent(new PlayerLeaveListener(this));

        registerCommand("givelife", new GiveLifeCommand(this));
        registerCommand("lives", new LivesCommand(this));
        registerCommand("removeboogeyman", new RemoveBoogeymanCommand(this));
        registerCommand("setboogeyman", new SetBoogeymanCommand(this));
        registerCommand("startsession", new StartSessionCommand(this));
        registerCommand("stopsession", new StopSessionCommand(this));
        registerCommand("addplayer", new AddPlayerCommand(this));

    }

    @Override
    public void onDisable() {

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
}
