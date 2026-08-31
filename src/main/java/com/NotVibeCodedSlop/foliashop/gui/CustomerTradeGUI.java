package com.NotVibeCodedSlop.foliashop.gui;

import com.NotVibeCodedSlop.foliashop.model.Shop;
import com.NotVibeCodedSlop.foliashop.util.SoundUtil;
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

public class CustomerTradeGUI {

    public static final int INPUT_SLOT = 19;
    public static final int CONFIG_SHORTCUT_SLOT = 8;
    public static final int[] OUTPUT_SLOTS = {23, 24, 25, 32, 33, 34};

    public static void open(Player player, Shop shop) {
        Inventory inv = Bukkit.createInventory(new CustomHolders.CustomerTradeHolder(shop), 45,
                Component.text("Shop Trade", NamedTextColor.DARK_PURPLE));

        fillDecorations(inv, shop, player);
        player.openInventory(inv);

        if (shop.getProductItem() == null || shop.getTotalStockCount() < shop.getProductAmount() || !shop.canAcceptRevenue(shop.getCostAmount())) {
            SoundUtil.playVillagerNo(player);
        }

        player.sendMessage(Component.text("Put ", NamedTextColor.GRAY)
                .append(Component.text(shop.getCostAmount() + "x " + shop.getCurrency().name(), NamedTextColor.YELLOW))
                .append(Component.text(" into the payment slot to unlock items.", NamedTextColor.GRAY)));
    }

    public static void updateTradingSlots(Inventory inv, Shop shop) {
        ItemStack input = inv.getItem(INPUT_SLOT);
        int inputAmount = (input != null && input.getType() == shop.getCurrency()) ? input.getAmount() : 0;

        int cost = shop.getCostAmount();
        int batchesAffordable = cost > 0 ? inputAmount / cost : 0;

        int stockAvailable = shop.getTotalStockCount();
        int productPerBatch = shop.getProductAmount();
        int batchesStocked = (productPerBatch > 0 && shop.getProductItem() != null) ? stockAvailable / productPerBatch : 0;

        int batchesToDisplay = Math.min(batchesAffordable, batchesStocked);
        boolean vaultHasSpace = shop.canAcceptRevenue(cost);

        for (int i = 0; i < OUTPUT_SLOTS.length; i++) {
            int slot = OUTPUT_SLOTS[i];
            if (!vaultHasSpace && i == 0) {
                ItemStack barrier = new ItemStack(Material.BARRIER);
                ItemMeta bMeta = barrier.getItemMeta();
                bMeta.displayName(Component.text("REVENUE VAULT FULL", NamedTextColor.RED).decoration(TextDecoration.BOLD, true));
                bMeta.lore(List.of(Component.text("Owner must collect earnings before sales continue.", NamedTextColor.GRAY)));
                barrier.setItemMeta(bMeta);
                inv.setItem(slot, barrier);
            } else if (vaultHasSpace && i < batchesToDisplay) {
                ItemStack product = shop.getProductItem();
                if (product != null) {
                    ItemStack display = product.clone();
                    display.setAmount(productPerBatch);
                    ItemMeta meta = display.getItemMeta();
                    meta.lore(List.of(
                            Component.text("Cost: " + cost + "x " + shop.getCurrency().name(), NamedTextColor.GOLD),
                            Component.text("Click to take this batch!", NamedTextColor.GREEN)
                    ));
                    display.setItemMeta(meta);
                    inv.setItem(slot, display);
                }
            } else {
                inv.setItem(slot, null);
            }
        }
    }

    private static void fillDecorations(Inventory inv, Shop shop, Player player) {
        ItemStack glass = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = glass.getItemMeta();
        meta.displayName(Component.empty());
        glass.setItemMeta(meta);
        for (int i = 0; i < inv.getSize(); i++) inv.setItem(i, glass);

        inv.setItem(INPUT_SLOT, null);

        ItemStack arrow = new ItemStack(Material.ARROW);
        ItemMeta aMeta = arrow.getItemMeta();
        aMeta.displayName(Component.text("➔ Trade Rate ➔", NamedTextColor.GOLD).decoration(TextDecoration.BOLD, true));
        aMeta.lore(List.of(
                Component.text("Cost: " + shop.getCostAmount() + "x " + shop.getCurrency().name(), NamedTextColor.YELLOW),
                Component.text("Gives: " + shop.getProductAmount() + "x " + (shop.getProductItem() != null ? shop.getProductItem().getType().name() : "None"), NamedTextColor.GREEN),
                Component.text("In Stock: " + shop.getTotalStockCount(), NamedTextColor.GRAY)
        ));
        arrow.setItemMeta(aMeta);
        inv.setItem(21, arrow);

        if (player.getUniqueId().equals(shop.getOwnerUuid()) || player.hasPermission("foliashop.admin")) {
            ItemStack configBtn = new ItemStack(Material.NETHER_STAR);
            ItemMeta cMeta = configBtn.getItemMeta();
            cMeta.displayName(Component.text("⚙ Owner Settings", NamedTextColor.GOLD).decoration(TextDecoration.BOLD, true));
            cMeta.lore(List.of(Component.text("Click to open shop management", NamedTextColor.YELLOW)));
            configBtn.setItemMeta(cMeta);
            inv.setItem(CONFIG_SHORTCUT_SLOT, configBtn);
        }
    }
}
