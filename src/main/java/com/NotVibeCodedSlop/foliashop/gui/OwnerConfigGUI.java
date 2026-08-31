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

public class OwnerConfigGUI {

    public static void open(Player player, Shop shop) {
        Inventory inv = Bukkit.createInventory(new CustomHolders.OwnerConfigHolder(shop), 45,
                Component.text("Shop Configuration", NamedTextColor.DARK_AQUA));

        fillGlass(inv);

        ItemStack currencyItem = new ItemStack(shop.getCurrency());
        ItemMeta cMeta = currencyItem.getItemMeta();
        cMeta.displayName(Component.text("Payment Currency", NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false));
        cMeta.lore(List.of(
                Component.text("Selected: ", NamedTextColor.GRAY).append(Component.text(shop.getCurrency().name(), NamedTextColor.YELLOW)),
                Component.text("Click to choose another currency", NamedTextColor.AQUA)
        ));
        currencyItem.setItemMeta(cMeta);
        inv.setItem(10, currencyItem);

        inv.setItem(12, createButton(Material.RED_STAINED_GLASS_PANE, "-1 Price", NamedTextColor.RED));

        ItemStack priceItem = new ItemStack(Material.GOLD_NUGGET);
        ItemMeta pMeta = priceItem.getItemMeta();
        pMeta.displayName(Component.text("Cost: " + shop.getCostAmount() + "x", NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));
        priceItem.setItemMeta(pMeta);
        inv.setItem(13, priceItem);

        inv.setItem(14, createButton(Material.LIME_STAINED_GLASS_PANE, "+1 Price", NamedTextColor.GREEN));

        ItemStack currentProd = shop.getProductItem();
        ItemStack prodIcon = currentProd != null ? currentProd.clone() : new ItemStack(Material.BARRIER);
        ItemMeta prMeta = prodIcon.getItemMeta();
        if (currentProd != null) {
            prMeta.displayName(Component.text("Product: " + currentProd.getType().name(), NamedTextColor.AQUA).decoration(TextDecoration.ITALIC, false));
            prMeta.lore(List.of(Component.text("Click to change item to sell", NamedTextColor.YELLOW)));
        } else {
            prMeta.displayName(Component.text("No Product Set!", NamedTextColor.RED).decoration(TextDecoration.BOLD, true));
            prMeta.lore(List.of(Component.text("Click to choose an item from your inventory!", NamedTextColor.YELLOW)));
        }
        prodIcon.setItemMeta(prMeta);
        inv.setItem(19, prodIcon);

        inv.setItem(21, createButton(Material.RED_STAINED_GLASS_PANE, "-1 Amount", NamedTextColor.RED));

        ItemStack amountDisplay = new ItemStack(Material.PAPER);
        ItemMeta aMeta = amountDisplay.getItemMeta();
        aMeta.displayName(Component.text("Per Batch: " + shop.getProductAmount() + "x", NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, false));
        amountDisplay.setItemMeta(aMeta);
        inv.setItem(22, amountDisplay);

        inv.setItem(23, createButton(Material.LIME_STAINED_GLASS_PANE, "+1 Amount", NamedTextColor.GREEN));

        ItemStack stockItem = new ItemStack(Material.CHEST);
        ItemMeta sMeta = stockItem.getItemMeta();
        sMeta.displayName(Component.text("Manage Stock", NamedTextColor.GREEN).decoration(TextDecoration.ITALIC, false));
        sMeta.lore(List.of(
                Component.text("Available stock: " + shop.getTotalStockCount(), NamedTextColor.GRAY),
                Component.text("Click to open stock storage", NamedTextColor.AQUA)
        ));
        stockItem.setItemMeta(sMeta);
        inv.setItem(29, stockItem);

        ItemStack revItem = new ItemStack(Material.HOPPER);
        ItemMeta rMeta = revItem.getItemMeta();
        rMeta.displayName(Component.text("Revenue Vault", NamedTextColor.GOLD).decoration(TextDecoration.ITALIC, false));
        rMeta.lore(List.of(
                Component.text("Total Earned: " + shop.getTotalRevenueCount() + "x " + shop.getCurrency().name(), NamedTextColor.GRAY),
                Component.text("Click to withdraw earnings", NamedTextColor.YELLOW)
        ));
        revItem.setItemMeta(rMeta);
        inv.setItem(31, revItem);

        ItemStack delItem = new ItemStack(Material.TNT);
        ItemMeta dMeta = delItem.getItemMeta();
        dMeta.displayName(Component.text("DELETE SHOP", NamedTextColor.RED).decoration(TextDecoration.BOLD, true));
        dMeta.lore(List.of(
                Component.text("Click to request shop deletion.", NamedTextColor.GRAY)
        ));
        delItem.setItemMeta(dMeta);
        inv.setItem(33, delItem);

        player.openInventory(inv);
    }

    private static ItemStack createButton(Material mat, String name, NamedTextColor color) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(name, color).decoration(TextDecoration.ITALIC, false));
        item.setItemMeta(meta);
        return item;
    }

    private static void fillGlass(Inventory inv) {
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = filler.getItemMeta();
        meta.displayName(Component.empty());
        filler.setItemMeta(meta);
        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, filler);
        }
    }
}
