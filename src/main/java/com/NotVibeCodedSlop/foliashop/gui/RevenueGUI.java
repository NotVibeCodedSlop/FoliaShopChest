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

public class RevenueGUI {

    public static final int BACK_SLOT = 49;
    public static final int WITHDRAW_ALL_SLOT = 53;
    public static final int INFO_SLOT = 45;

    public static void open(Player player, Shop shop) {
        Inventory inv = Bukkit.createInventory(new CustomHolders.RevenueHolder(shop), 54,
                Component.text("Revenue Vault", NamedTextColor.GOLD));

        synchronized (shop) {
            for (int i = 0; i < Math.min(45, shop.getRevenue().size()); i++) {
                inv.setItem(i, shop.getRevenue().get(i));
            }
        }

        ItemStack bar = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = bar.getItemMeta();
        meta.displayName(Component.empty());
        bar.setItemMeta(meta);
        for (int i = 45; i < 54; i++) inv.setItem(i, bar);

        ItemStack info = new ItemStack(Material.GOLD_BLOCK);
        ItemMeta iMeta = info.getItemMeta();
        iMeta.displayName(Component.text("Vault Info", NamedTextColor.GOLD).decoration(TextDecoration.BOLD, true));
        iMeta.lore(List.of(
                Component.text("Total Earned: " + shop.getTotalRevenueCount() + "x " + shop.getCurrency().name(), NamedTextColor.YELLOW),
                Component.text("Vault Capacity: " + shop.getRevenue().size() + "/45 Stacks", NamedTextColor.GRAY)
        ));
        info.setItemMeta(iMeta);
        inv.setItem(INFO_SLOT, info);

        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta bMeta = back.getItemMeta();
        bMeta.displayName(Component.text("⬅ Back to Config", NamedTextColor.RED).decoration(TextDecoration.BOLD, true));
        back.setItemMeta(bMeta);
        inv.setItem(BACK_SLOT, back);

        ItemStack withdraw = new ItemStack(Material.HOPPER);
        ItemMeta wMeta = withdraw.getItemMeta();
        wMeta.displayName(Component.text("⚡ Withdraw All", NamedTextColor.GREEN).decoration(TextDecoration.BOLD, true));
        wMeta.lore(List.of(Component.text("Click to claim all currency into your inventory!", NamedTextColor.GRAY)));
        withdraw.setItemMeta(wMeta);
        inv.setItem(WITHDRAW_ALL_SLOT, withdraw);

        player.openInventory(inv);
    }
}
