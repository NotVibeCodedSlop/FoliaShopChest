package com.NotVibeCodedSlop.foliashop.gui;

import com.NotVibeCodedSlop.foliashop.model.Shop;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class CustomHolders {
    public static class OwnerConfigHolder implements InventoryHolder {
        private final Shop shop;
        public OwnerConfigHolder(Shop shop) { this.shop = shop; }
        public Shop getShop() { return shop; }
        @Override public @NotNull Inventory getInventory() { return null; }
    }

    public static class ProductSelectHolder implements InventoryHolder {
        private final Shop shop;
        public ProductSelectHolder(Shop shop) { this.shop = shop; }
        public Shop getShop() { return shop; }
        @Override public @NotNull Inventory getInventory() { return null; }
    }

    public static class CurrencySelectHolder implements InventoryHolder {
        private final Shop shop;
        private final int page;
        public CurrencySelectHolder(Shop shop, int page) { this.shop = shop; this.page = page; }
        public Shop getShop() { return shop; }
        public int getPage() { return page; }
        @Override public @NotNull Inventory getInventory() { return null; }
    }

    public static class StockManageHolder implements InventoryHolder {
        private final Shop shop;
        public StockManageHolder(Shop shop) { this.shop = shop; }
        public Shop getShop() { return shop; }
        @Override public @NotNull Inventory getInventory() { return null; }
    }

    public static class RevenueHolder implements InventoryHolder {
        private final Shop shop;
        public RevenueHolder(Shop shop) { this.shop = shop; }
        public Shop getShop() { return shop; }
        @Override public @NotNull Inventory getInventory() { return null; }
    }

    public static class CustomerTradeHolder implements InventoryHolder {
        private final Shop shop;
        public CustomerTradeHolder(Shop shop) { this.shop = shop; }
        public Shop getShop() { return shop; }
        @Override public @NotNull Inventory getInventory() { return null; }
    }
}
