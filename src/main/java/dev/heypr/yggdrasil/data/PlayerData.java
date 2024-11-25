package dev.heypr.yggdrasil.data;

import dev.heypr.yggdrasil.Yggdrasil;
import dev.heypr.yggdrasil.misc.ColorManager;
import dev.heypr.yggdrasil.misc.discord.Bot;
import dev.heypr.yggdrasil.misc.discord.BotUtils;
import dev.heypr.yggdrasil.misc.discord.command.CommandManager;
import dev.heypr.yggdrasil.misc.discord.command.impl.LinkCommand;
import dev.heypr.yggdrasil.misc.discord.listeners.EventListener;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
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

        this.updateColors();
    }

    public UUID getUuid() {
        return uuid;
    }

    public int getLives() {
        return lives;
    }

    public void addLives(int amount) {
        this.lives += amount;
        this.updateColors();
    }

    public void decreaseLives(int amount) {
        this.lives -= amount;
        this.updateColors();
    }

    private void updateSkin() {
        final Player player = this.getPlayer();

        if (player == null || !player.isOnline())
            return;

        final File skinFile = ColorManager.getSkinFile(Yggdrasil.plugin, player, this.lives);

        if (skinFile == null)
            return;

        Yggdrasil.plugin.skinManager.skin(player, skinFile);

        Yggdrasil.plugin.getScheduler().runTaskLater(Yggdrasil.plugin, () -> { // Set their lives to display again (setting skin messes it up)
            ColorManager.setTabListName(Yggdrasil.plugin, player, this.lives);
        }, 20L);
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

    public void updateColors() {
        this.saveLives();
        this.updateSkin();

        if (Yggdrasil.plugin.getBot() != null)
            this.updateDiscordColor();
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

    public static int retrieveLivesOrDefault(final UUID uuid, final int defaultLives) {
        final int lives = retrieveLives(uuid);

        if (lives <= -1)
            return defaultLives;

        return lives;
    }
}
