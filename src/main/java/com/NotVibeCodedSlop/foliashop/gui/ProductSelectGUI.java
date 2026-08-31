package com.NotVibeCodedSlop.foliashop.gui;

import com.NotVibeCodedSlop.foliashop.model.Shop;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class ProductSelectGUI {

    public static void open(Player player, Shop shop) {
        Inventory inv = Bukkit.createInventory(new CustomHolders.ProductSelectHolder(shop), 27,
                Component.text("Click an Item in Your Inventory", NamedTextColor.DARK_AQUA));

        ItemStack filler = new ItemStack(Material.CYAN_STAINED_GLASS_PANE);
        ItemMeta meta = filler.getItemMeta();
        meta.displayName(Component.empty());
        filler.setItemMeta(meta);
        for (int i = 0; i < 27; i++) inv.setItem(i, filler);

        ItemStack info = new ItemStack(Material.ITEM_FRAME);
        ItemMeta iMeta = info.getItemMeta();
        iMeta.displayName(Component.text("Select Product to Sell", NamedTextColor.YELLOW).decoration(TextDecoration.BOLD, true));
        iMeta.lore(List.of(
                Component.text("Click any item in your player inventory below", NamedTextColor.WHITE),
                Component.text("to set it as this shop's product.", NamedTextColor.GRAY)
        ));
        info.setItemMeta(iMeta);
        inv.setItem(13, info);

        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta bMeta = back.getItemMeta();
        bMeta.displayName(Component.text("Back", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
        back.setItemMeta(bMeta);
        inv.setItem(22, back);

        player.openInventory(inv);
    }
}
