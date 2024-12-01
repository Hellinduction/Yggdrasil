package dev.heypr.yggdrasil.commands;

import dev.heypr.yggdrasil.Yggdrasil;
import dev.heypr.yggdrasil.data.PlayerData;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class CommandWrapper implements CommandExecutor {
    private final CommandExecutor executor;
    private final boolean requireActiveSession;
    private final boolean playerOnly;
    private final boolean requireConfirmation;

    public CommandWrapper(final CommandExecutor executor, final boolean requireActiveSession, final boolean playerOnly, final boolean requireConfirmation) {
        this.executor = executor;
        this.requireActiveSession = requireActiveSession;
        this.playerOnly = playerOnly;
        this.requireConfirmation = requireConfirmation;
    }

    public CommandWrapper(final CommandExecutor executor, final boolean requireActiveSession, final boolean playerOnly) {
        this(executor, requireActiveSession, playerOnly, false);
    }

    public CommandWrapper(final CommandExecutor executor, final boolean requireActiveSession) {
        this(executor, requireActiveSession, false);
    }

    public CommandWrapper(final CommandExecutor executor) {
        this(executor, false);
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (this.playerOnly && !(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "You must be a player to use this command.");
            return true;
        }

        if (this.requireActiveSession && !Yggdrasil.plugin.isSessionRunning) {
            sender.sendMessage(ChatColor.RED + "The session must be started in order to use this command.");
            return true;
        }

        if (this.requireConfirmation && !(args.length > 0 && "confirm".equalsIgnoreCase(args[0]))) {
            sender.sendMessage(ChatColor.RED + String.format("You must type '/%s confirm' in order to execute this command.", label));
            return true;
        }

        try {
            return this.executor.onCommand(sender, command, label, args);
        } catch (final NumberFormatException exception) {
            sender.sendMessage(ChatColor.RED +  "You did not provide a valid integer.");
        } catch (final NullPointerException exception) {
            final String message = exception.getMessage();

            if (message.contains(PlayerData.class.getName()) && message.contains("java.util.Map.get(Object)"))
                sender.sendMessage(ChatColor.RED + (args.length == 0 ? "Your player data could not be found." : "The player data for that player could not be found."));
            else
                exception.printStackTrace();
        }

        return false;
    }

    public CommandExecutor getExecutor() {
        return this.executor;
    }

    public boolean isRequireActiveSession() {
        return this.requireActiveSession;
    }
}
