package dev.heypr.yggdrasil.data;

import dev.heypr.yggdrasil.Yggdrasil;
import dev.heypr.yggdrasil.misc.Colors;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.UUID;

public class PlayerData {

    private UUID uuid;
    private int lives;
    private boolean isBoogeyman;

    public PlayerData(UUID uuid, int lives) {
        this.uuid = uuid;
        this.lives = lives;
        this.isBoogeyman = false;

        this.updateSkin();
    }

    public UUID getUuid() {
        return uuid;
    }

    public int getLives() {
        return lives;
    }

    public void addLives(int amount) {
        this.lives += amount;
        this.updateSkin();
    }

    public void decreaseLives(int amount) {
        this.lives -= amount;
        this.updateSkin();
    }

    private void updateSkin() {
        final Player player = this.getPlayer();

        if (player == null || !player.isOnline())
            return;

        final File skinFile = Colors.getSkinFile(Yggdrasil.plugin, player, this.lives);

        if (skinFile == null)
            return;

        Yggdrasil.plugin.skinsManager.skin(player, skinFile);
    }

    public boolean isBoogeyman() {
        return isBoogeyman;
    }

    public void setBoogeyman(boolean boogeyman) {
        this.isBoogeyman = boogeyman;
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(this.uuid);
    }
}
