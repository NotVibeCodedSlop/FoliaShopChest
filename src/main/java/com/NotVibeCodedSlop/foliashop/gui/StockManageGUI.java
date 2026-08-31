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

public class StockManageGUI {

    public static final int BACK_SLOT = 49;
    public static final int INFO_SLOT = 45;

    public static void open(Player player, Shop shop) {
        Inventory inv = Bukkit.createInventory(new CustomHolders.StockManageHolder(shop), 54,
                Component.text("Deposit / Manage Stock", NamedTextColor.DARK_GREEN));

        synchronized (shop) {
            for (int i = 0; i < Math.min(45, shop.getStock().size()); i++) {
                inv.setItem(i, shop.getStock().get(i));
            }
        }

        ItemStack bar = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = bar.getItemMeta();
        meta.displayName(Component.empty());
        bar.setItemMeta(meta);
        for (int i = 45; i < 54; i++) inv.setItem(i, bar);

        ItemStack info = new ItemStack(Material.CHEST);
        ItemMeta iMeta = info.getItemMeta();
        iMeta.displayName(Component.text("Stock Storage (Max 45 Stacks)", NamedTextColor.GREEN).decoration(TextDecoration.BOLD, true));
        iMeta.lore(List.of(
                Component.text("Total units: " + shop.getTotalStockCount(), NamedTextColor.WHITE),
                Component.text("Place your product in slots 1-45 above.", NamedTextColor.GRAY)
        ));
        info.setItemMeta(iMeta);
        inv.setItem(INFO_SLOT, info);

        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta bMeta = back.getItemMeta();
        bMeta.displayName(Component.text("⬅ Back to Config", NamedTextColor.RED).decoration(TextDecoration.BOLD, true));
        back.setItemMeta(bMeta);
        inv.setItem(BACK_SLOT, back);

        player.openInventory(inv);
    }
}
