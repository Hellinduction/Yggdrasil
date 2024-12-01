package dev.heypr.yggdrasil.misc.discord.listeners;

import dev.heypr.yggdrasil.Yggdrasil;
import dev.heypr.yggdrasil.misc.ColorManager;
import dev.heypr.yggdrasil.misc.discord.command.CommandManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class EventListener extends ListenerAdapter {
    public static Guild guild;

    private void createRole(final Guild guild, final ColorManager.Colors colors, final Consumer<Role> roleConsumer) {
        final ConfigurationSection rolesSection = getRoles(guild.getId());
        final String name = colors.name().toLowerCase();
        final boolean exists = rolesSection != null && rolesSection.contains(name);

        if (!exists) {
            guild.createRole()
                    .setColor(colors.getColor())
                    .setName(name)
                    .queue(roleConsumer::accept);
        }
    }

    public static ConfigurationSection getRoles(final String guildId) {
        final String name = String.format("discord.guilds.%s.roles", guildId);

        if (!Yggdrasil.plugin.getConfig().contains(name))
            return null;

        final ConfigurationSection section = Yggdrasil.plugin.getConfig().getConfigurationSection(name);
        return section;
    }

    @Override
    public void onGuildReady(final GuildReadyEvent e) {
        final Guild guild = e.getGuild();
        final long id = guild.getIdLong();
        ConfigurationSection section = Yggdrasil.plugin.getConfig().getConfigurationSection("discord.guilds");

        EventListener.guild = guild;

        boolean registered = false;

        if (!section.contains(String.valueOf(id)))
            section = section.createSection(String.valueOf(id));
        else {
            section = section.getConfigurationSection(String.valueOf(id));
            registered = section.contains("registered") && section.getBoolean("registered");
        }

        if (!registered) {
            this.createSlashCommands(e.getGuild());
            section.set("registered", true);
            Yggdrasil.plugin.saveConfig();
        }

        if (!section.contains("roles"))
            section = section.createSection("roles");
        else
            section = section.getConfigurationSection("roles");

        final ConfigurationSection finalSection = section;

        for (final ColorManager.Colors colors : ColorManager.Colors.values()) {
            final String roleName = colors.name().toLowerCase();

            if (!finalSection.contains(roleName) && !guild.getRolesByName(roleName, false).isEmpty()) {
                final Role role = guild.getRolesByName(roleName, false).get(0);
                finalSection.set(roleName, role.getId());
                Yggdrasil.plugin.saveConfig();
                continue;
            }

            this.createRole(guild, colors, role -> Yggdrasil.plugin.getScheduler().runTask(Yggdrasil.plugin, () -> {
                finalSection.set(roleName, role.getId());
                Yggdrasil.plugin.saveConfig();
            }));
        }
    }

    private void createSlashCommands(final Guild guild) {
        final List<CommandData> data = new ArrayList<>();
        CommandManager.getCommands().forEach(command -> data.add(command.getData()));

        guild.updateCommands().addCommands(data).queue(commands -> Yggdrasil.plugin.getLogger().info(String.format("Created discord slash commands for guild %s.", guild.getId())));
    }

    @Override
    public void onSlashCommandInteraction(final SlashCommandInteractionEvent e) {
        this.handleCommand(e);
    }

    private void handleCommand(final SlashCommandInteractionEvent e) {
        final String command = e.getName();
        final List<OptionMapping> args = e.getOptions();

        CommandManager.handleCommand(e, command, args);
    }
}