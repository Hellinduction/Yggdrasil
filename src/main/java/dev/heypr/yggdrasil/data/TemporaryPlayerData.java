package dev.heypr.yggdrasil.data;

import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import java.util.*;

public final class TemporaryPlayerData {
    private static final Map<UUID, TemporaryPlayerData> DATA_MAP = new HashMap<>();

    private final UUID uniqueId = UUID.randomUUID();
    private final UUID uuid;
    private final String username;

    private Collection<PotionEffect> effects = Collections.emptyList();

    public TemporaryPlayerData(final Player player) {
        this(player.getUniqueId(), player.getName());
    }

    public TemporaryPlayerData(final UUID uuid, final String username) {
        this.uuid = uuid;
        this.username = username;
    }

    public UUID getUniqueId() {
        return this.uniqueId;
    }

    public UUID getUuid() {
        return this.uuid;
    }

    public String getUsername() {
        return this.username;
    }

    public Collection<PotionEffect> getEffects() {
        return this.effects;
    }

    public void setEffects(final Collection<PotionEffect> effects) {
        this.effects = effects;
    }

    /**
     * Get or create a TemporaryPlayerData object
     * @param player
     * @return
     */
    public static TemporaryPlayerData get(final Player player) {
        return DATA_MAP.computeIfAbsent(player.getUniqueId(), uuid -> new TemporaryPlayerData(player));
    }
}