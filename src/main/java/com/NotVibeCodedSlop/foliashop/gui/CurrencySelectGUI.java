package com.NotVibeCodedSlop.foliashop.gui;

import com.NotVibeCodedSlop.foliashop.model.CurrencyRegistry;
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

public class CurrencySelectGUI {

    public static void open(Player player, Shop shop, int page) {
        Inventory inv = Bukkit.createInventory(new CustomHolders.CurrencySelectHolder(shop, page), 54,
                Component.text("Select Currency", NamedTextColor.DARK_BLUE));

        List<Material> allCurrencies = CurrencyRegistry.getAllowedCurrencies();
        int pageSize = 45;
        int start = page * pageSize;
        int end = Math.min(start + pageSize, allCurrencies.size());

        for (int i = start; i < end; i++) {
            Material mat = allCurrencies.get(i);
            ItemStack item = new ItemStack(mat);
            ItemMeta meta = item.getItemMeta();
            meta.displayName(Component.text(mat.name(), NamedTextColor.YELLOW).decoration(TextDecoration.ITALIC, false));
            meta.lore(List.of(Component.text("Click to choose as currency", NamedTextColor.GRAY)));
            item.setItemMeta(meta);
            inv.setItem(i - start, item);
        }

        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta bMeta = back.getItemMeta();
        bMeta.displayName(Component.text("Back", NamedTextColor.RED).decoration(TextDecoration.ITALIC, false));
        back.setItemMeta(bMeta);
        inv.setItem(49, back);

        player.openInventory(inv);
    }
}
