package com.NotVibeCodedSlop.foliashop.model;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class Shop {

    private final BlockPos pos;
    private final UUID ownerUuid;
    private Material currency = Material.DIAMOND;
    private int costAmount = 5;
    private ItemStack productItem = null;
    private int productAmount = 1;

    private final List<ItemStack> stock = new ArrayList<>();
    private final List<ItemStack> revenue = new ArrayList<>();

    public Shop(BlockPos pos, UUID ownerUuid) {
        this.pos = pos;
        this.ownerUuid = ownerUuid;
    }

    public BlockPos getPos() { return pos; }
    public Location getLocation() { return pos.toLocation(); }
    public UUID getOwnerUuid() { return ownerUuid; }

    public synchronized Material getCurrency() { return currency; }
    public synchronized void setCurrency(Material currency) { this.currency = currency; }

    public synchronized int getCostAmount() { return costAmount; }
    public synchronized void setCostAmount(int costAmount) { this.costAmount = Math.max(1, costAmount); }

    public synchronized ItemStack getProductItem() { return productItem != null ? productItem.clone() : null; }
    public synchronized void setProductItem(ItemStack productItem) {
        if (productItem != null) {
            ItemStack single = productItem.clone();
            single.setAmount(1);
            this.productItem = single;
        } else {
            this.productItem = null;
        }
    }

    public synchronized int getProductAmount() { return productAmount; }
    public synchronized void setProductAmount(int productAmount) { this.productAmount = Math.max(1, productAmount); }

    public synchronized List<ItemStack> getStock() { return stock; }
    public synchronized List<ItemStack> getRevenue() { return revenue; }

    public synchronized int getTotalStockCount() {
        if (productItem == null) return 0;
        int count = 0;
        for (ItemStack item : stock) {
            if (item != null && item.isSimilar(productItem)) {
                count += item.getAmount();
            }
        }
        return count;
    }

    public synchronized int getTotalRevenueCount() {
        int count = 0;
        for (ItemStack item : revenue) {
            if (item != null && item.getType() == currency) {
                count += item.getAmount();
            }
        }
        return count;
    }

    public synchronized boolean canAcceptRevenue(int amount) {
        int remaining = amount;
        int maxStack = currency.getMaxStackSize();
        int usedSlots = revenue.size();

        for (ItemStack rev : revenue) {
            if (rev != null && rev.getType() == currency && rev.getAmount() < maxStack) {
                int space = maxStack - rev.getAmount();
                remaining -= Math.min(space, remaining);
                if (remaining <= 0) return true;
            }
        }

        int newSlotsNeeded = (int) Math.ceil((double) remaining / maxStack);
        return (usedSlots + newSlotsNeeded) <= 45;
    }

    public synchronized boolean executePurchase(int batches) {
        if (productItem == null) return false;
        int totalProductNeeded = batches * productAmount;
        if (getTotalStockCount() < totalProductNeeded) return false;

        int totalEarned = batches * costAmount;
        if (!canAcceptRevenue(totalEarned)) return false;

        int remainingToDeduct = totalProductNeeded;
        Iterator<ItemStack> it = stock.iterator();
        while (it.hasNext() && remainingToDeduct > 0) {
            ItemStack stack = it.next();
            if (stack != null && stack.isSimilar(productItem)) {
                if (stack.getAmount() <= remainingToDeduct) {
                    remainingToDeduct -= stack.getAmount();
                    it.remove();
                } else {
                    stack.setAmount(stack.getAmount() - remainingToDeduct);
                    remainingToDeduct = 0;
                }
            }
        }

        addRevenueCurrency(totalEarned);
        return true;
    }

    private synchronized void addRevenueCurrency(int amount) {
        int remaining = amount;
        int maxStack = currency.getMaxStackSize();
        for (ItemStack rev : revenue) {
            if (rev != null && rev.getType() == currency && rev.getAmount() < maxStack) {
                int space = maxStack - rev.getAmount();
                int toAdd = Math.min(space, remaining);
                rev.setAmount(rev.getAmount() + toAdd);
                remaining -= toAdd;
                if (remaining <= 0) break;
            }
        }
        while (remaining > 0 && revenue.size() < 45) {
            int toAdd = Math.min(remaining, maxStack);
            revenue.add(new ItemStack(currency, toAdd));
            remaining -= toAdd;
        }
    }
}
