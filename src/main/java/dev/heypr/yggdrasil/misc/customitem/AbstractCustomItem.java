package dev.heypr.yggdrasil.misc.customitem;

import de.tr7zw.changeme.nbtapi.NBTItem;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractCustomItem implements ICustomItem {
    public static final String KEY = "YGGDRASIL_CUSTOM_ITEM";

    protected final Material item;
    protected final String itemName;
    protected List<String> itemLore = new ArrayList<>();
    protected boolean dropOnDeath = true;
    protected boolean allowDropping = false;

    public AbstractCustomItem(Material item, String itemName, String... loreLines) {
        this.item = item;
        this.itemName = ChatColor.translateAlternateColorCodes('&', itemName);
        for (String loreLine : loreLines)
            itemLore.add(ChatColor.translateAlternateColorCodes('&', loreLine));
    }

    @Override
    public ItemStack getItem() {
        ItemStack itemStack = new ItemStack(item);
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.setDisplayName(itemName);
        itemMeta.setLore(itemLore);
        itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        itemMeta.addItemFlags(ItemFlag.HIDE_DESTROYS);
        itemMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        itemStack.setItemMeta(itemMeta);

        itemStack = this.applyKey(itemStack);

        return itemStack;
    }

    public ItemStack applyKey(final ItemStack item) {
        final NBTItem nbtItem = new NBTItem(item);

        nbtItem.setString(KEY, toEnumName(this.getRawName()));

        return nbtItem.getItem();
    }

    @Override
    public boolean verify(final ItemStack item) {
        if (item == null || item.getType() == Material.AIR || item.getAmount() < 1)
            return false;

        final NBTItem nbtItem = new NBTItem(item);

        if (!nbtItem.hasTag(KEY))
            return false;

        final String tag = nbtItem.getString(KEY);
        return tag.equals(toEnumName(this.getRawName()));
    }

    public static String toEnumName(final String name) {
        return ChatColor.stripColor(name).toUpperCase().replace(" ", "_");
    }

    @Override
    public String getRawName() {
        return ChatColor.stripColor(itemName);
    }

    public int indexOf(final PlayerInventory inv) {
        return this.indexOf(inv, this.getItem().getType());
    }

    protected int indexOf(final PlayerInventory inv, final Material type) {
        int index;

        for (index = 0; index < inv.getSize(); index++) {
            final ItemStack item = inv.getItem(index);

            if (item == null)
                continue;

            if (item.getType() == type && this.verify(item))
                return index;
        }

        return -1;
    }

    public void setDropOnDeath(final boolean dropOnDeath) {
        this.dropOnDeath = dropOnDeath;
    }

    public boolean isDropOnDeath() {
        return this.dropOnDeath;
    }

    public void setAllowDropping(final boolean allowDropping) {
        this.allowDropping = allowDropping;
    }

    public boolean isAllowDropping() {
        return this.allowDropping;
    }
}