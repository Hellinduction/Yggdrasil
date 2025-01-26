package dev.heypr.yggdrasil.misc.papi;

import dev.heypr.yggdrasil.Yggdrasil;
import dev.heypr.yggdrasil.data.PlayerData;
import dev.heypr.yggdrasil.misc.papi.impl.*;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class YggdrasilExpansion extends PlaceholderExpansion {
    public record PlaceholderMetaData(String name, String[] aliases) {}

    private final Yggdrasil plugin;
    private final Map<PlaceholderMetaData, IPlaceholder> placeholdersMap = new HashMap<>();

    public YggdrasilExpansion(final Yggdrasil plugin) {
        this.plugin = plugin;

        this.registerPlaceholders();
    }

    private void registerPlaceholder(final String name, final IPlaceholder placeholder, final String... aliases) {
        this.placeholdersMap.put(new PlaceholderMetaData(name.toLowerCase(), aliases), placeholder);
    }

    private void registerPlaceholders() {
        this.registerPlaceholder("lives", new LivesPlaceholder());
        this.registerPlaceholder("is_boogey_man", new BoogeyManPlaceholder(), "is_boogey_man");
        this.registerPlaceholder("kills", new KillsPlaceholder());
        this.registerPlaceholder("has_last_chance", new LastChancePlaceholder());
        this.registerPlaceholder("lives_color", new LivesColorPlaceholder());
        this.registerPlaceholder("revealed_data", new RevealedDataPlaceholder());
    }

    private IPlaceholder getPlaceholder(final String placeholderStr) {
        final String placeholderLower = placeholderStr.toLowerCase();
        final Map.Entry<PlaceholderMetaData, IPlaceholder> placeholderEntry = this.placeholdersMap.entrySet().stream()
                .filter(entry -> placeholderLower.equals(entry.getKey().name()) || Arrays.stream(entry.getKey().aliases()).anyMatch(alias -> placeholderLower.equals(alias)))
                .findFirst()
                .orElse(null);

        if (placeholderEntry == null)
            return null;

        return placeholderEntry.getValue();
    }

    @Override
    public @NotNull String getIdentifier() {
        return this.plugin.getName().toLowerCase();
    }

    @Override
    public @NotNull String getAuthor() {
        return this.plugin.getDescription().getAuthors().get(0);
    }

    @Override
    public @NotNull String getVersion() {
        return this.plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer offlinePlayer, final String placeholderStr) {
        if (offlinePlayer == null)
            return "must request for valid player";

        if (!offlinePlayer.isOnline())
            return "offline player requested";

        Player player = offlinePlayer.getPlayer();
        PlayerData playerData = plugin.getPlayerData().get(player.getUniqueId());
        IPlaceholder placeholder = getPlaceholder(placeholderStr);

        if (playerData == null)
            return placeholder.nullPlayerData(); // Likely the session has not started yet or they joined late

        if (placeholder != null)
            return placeholder.resolve(playerData);

        return ""; // Placeholder is null
    }
}