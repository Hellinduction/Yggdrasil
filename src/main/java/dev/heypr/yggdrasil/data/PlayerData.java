package dev.heypr.yggdrasil.data;

public class PlayerData {

    private int lives;
    private boolean isBoogeyman;

    public PlayerData(int lives) {
        this.lives = lives;
        this.isBoogeyman = false;
    }

    public int getLives() {
        return lives;
    }

    public void addLives(int amount) {
        this.lives += amount;
    }

    public void decreaseLives(int amount) {
        this.lives -= amount;
    }

    public boolean isBoogeyman() {
        return isBoogeyman;
    }

    public void setBoogeyman(boolean boogeyman) {
        this.isBoogeyman = boogeyman;
    }
}
