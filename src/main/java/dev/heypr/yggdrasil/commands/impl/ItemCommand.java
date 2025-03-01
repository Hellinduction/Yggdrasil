package dev.heypr.yggdrasil.commands.impl;

import dev.heypr.yggdrasil.Yggdrasil;
import dev.heypr.yggdrasil.misc.customitem.AbstractCustomItem;
import dev.heypr.yggdrasil.misc.customitem.ICustomItem;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class ItemCommand implements CommandExecutor, TabCompleter {
    private final Yggdrasil plugin;

    public ItemCommand(final Yggdrasil plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED + "Usage: /" + label + " <item_name> (<amount>)");
            return true;
        }

        final String itemName = args[0];
        int amount = 1;

        if (args.length > 1) {
            try {
                amount = Integer.parseInt(args[1]);
                if (amount <= 0) amount = 1;
            } catch (NumberFormatException e) {}
        }

        final ICustomItem customItem = plugin.customItemManager.getItem(itemName).orElse(null);

        if (customItem == null) {
            sender.sendMessage(ChatColor.RED + "That item does not exist.");
            return true;
        }

        final Player player = (Player) sender;
        final ItemStack itemStack = customItem.getItem();

        itemStack.setAmount(amount);
        player.getInventory().addItem(itemStack);
        player.sendMessage(ChatColor.GREEN + String.format("Gave you item '%s'.", customItem.getRawName()));
        return true;
    }

    @Override
    public List<String> onTabComplete(final CommandSender sender, final Command command, final String alias, final String[] args) {
        if (args.length == 1) {
            final String prefix = args[0].toUpperCase();
            return Yggdrasil.plugin.customItemManager.getItems().stream()
                    .map(item -> AbstractCustomItem.toEnumName(item.getRawName()))
                    .filter(name -> name.startsWith(prefix))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
}