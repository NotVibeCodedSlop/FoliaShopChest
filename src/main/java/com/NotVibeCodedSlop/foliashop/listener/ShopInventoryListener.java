package com.NotVibeCodedSlop.foliashop.listener;

import com.NotVibeCodedSlop.foliashop.FoliaChestShop;
import com.NotVibeCodedSlop.foliashop.gui.*;
import com.NotVibeCodedSlop.foliashop.model.CurrencyRegistry;
import com.NotVibeCodedSlop.foliashop.model.Shop;
import com.NotVibeCodedSlop.foliashop.util.SoundUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class ShopInventoryListener implements Listener {

    private final FoliaChestShop plugin;

    public ShopInventoryListener(FoliaChestShop plugin) {
        this.plugin = plugin;
    }

    private boolean checkOwnerAccess(Player player, Shop shop) {
        if (!player.getUniqueId().equals(shop.getOwnerUuid()) && !player.hasPermission("foliashop.admin")) {
            player.closeInventory();
            SoundUtil.playWarning(player);
            player.sendMessage(Component.text("You are not the owner of this shop!", NamedTextColor.RED));
            return false;
        }
        return true;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        Inventory inv = event.getInventory();

        if (inv.getHolder() instanceof CustomHolders.OwnerConfigHolder holder) {
            event.setCancelled(true);
            Shop shop = holder.getShop();
            if (!checkOwnerAccess(player, shop)) return;

            int slot = event.getRawSlot();

            if (slot == 10) {
                SoundUtil.playClick(player);
                CurrencySelectGUI.open(player, shop, 0);
            } else if (slot == 12) {
                SoundUtil.playValueDown(player);
                shop.setCostAmount(shop.getCostAmount() - 1);
                plugin.getShopManager().saveAllAsync();
                OwnerConfigGUI.open(player, shop);
            } else if (slot == 14) {
                SoundUtil.playValueUp(player);
                shop.setCostAmount(shop.getCostAmount() + 1);
                plugin.getShopManager().saveAllAsync();
                OwnerConfigGUI.open(player, shop);
            } else if (slot == 19) {
                SoundUtil.playClick(player);
                ProductSelectGUI.open(player, shop);
            } else if (slot == 21) {
                SoundUtil.playValueDown(player);
                shop.setProductAmount(shop.getProductAmount() - 1);
                plugin.getShopManager().saveAllAsync();
                OwnerConfigGUI.open(player, shop);
            } else if (slot == 23) {
                SoundUtil.playValueUp(player);
                shop.setProductAmount(shop.getProductAmount() + 1);
                plugin.getShopManager().saveAllAsync();
                OwnerConfigGUI.open(player, shop);
            } else if (slot == 29) {
                SoundUtil.playClick(player);
                StockManageGUI.open(player, shop);
            } else if (slot == 31) {
                SoundUtil.playSelect(player);
                RevenueGUI.open(player, shop);
            } else if (slot == 33) {
                SoundUtil.playWarning(player);
                int code = ThreadLocalRandom.current().nextInt(10);
                plugin.getShopManager().setPendingDeletion(player.getUniqueId(), shop, code);
                player.closeInventory();

                player.sendMessage(Component.text("---------------------------------------------", NamedTextColor.RED));
                player.sendMessage(Component.text("ARE YOU SURE YOU WANT TO DELETE THIS SHOP?", NamedTextColor.RED).decoration(TextDecoration.BOLD, true));
                player.sendMessage(Component.text("Type ", NamedTextColor.GRAY)
                        .append(Component.text("/shopconfirm " + code, NamedTextColor.GOLD).decoration(TextDecoration.BOLD, true))
                        .append(Component.text(" to confirm deletion within 30s.", NamedTextColor.GRAY)));
                player.sendMessage(Component.text("---------------------------------------------", NamedTextColor.RED));
            }
            return;
        }

        if (inv.getHolder() instanceof CustomHolders.ProductSelectHolder holder) {
            Shop shop = holder.getShop();
            if (!checkOwnerAccess(player, shop)) return;

            int rawSlot = event.getRawSlot();

            if (rawSlot == 22) {
                SoundUtil.playClick(player);
                OwnerConfigGUI.open(player, shop);
                return;
            }

            if (rawSlot >= inv.getSize()) {
                event.setCancelled(true);
                ItemStack clicked = event.getCurrentItem();
                if (clicked != null && clicked.getType() != Material.AIR) {
                    shop.setProductItem(clicked);
                    plugin.getShopManager().saveAllAsync();
                    SoundUtil.playSuccess(player);
                    player.sendMessage(Component.text("Product set to: ", NamedTextColor.GREEN)
                            .append(Component.text(clicked.getType().name(), NamedTextColor.YELLOW)));
                    OwnerConfigGUI.open(player, shop);
                }
                return;
            }
            event.setCancelled(true);
            return;
        }

        if (inv.getHolder() instanceof CustomHolders.CurrencySelectHolder holder) {
            event.setCancelled(true);
            Shop shop = holder.getShop();
            if (!checkOwnerAccess(player, shop)) return;

            int slot = event.getRawSlot();

            if (slot == 49) {
                SoundUtil.playClick(player);
                OwnerConfigGUI.open(player, shop);
                return;
            }

            ItemStack clicked = event.getCurrentItem();
            if (clicked != null && CurrencyRegistry.isAllowed(clicked.getType())) {
                shop.setCurrency(clicked.getType());
                plugin.getShopManager().saveAllAsync();
                SoundUtil.playSelect(player);
                player.sendMessage(Component.text("Currency set to: " + clicked.getType().name(), NamedTextColor.GREEN));
                OwnerConfigGUI.open(player, shop);
            }
            return;
        }

        if (inv.getHolder() instanceof CustomHolders.StockManageHolder holder) {
            Shop shop = holder.getShop();
            if (!checkOwnerAccess(player, shop)) {
                event.setCancelled(true);
                return;
            }

            int slot = event.getRawSlot();
            if (slot >= 45 && slot < 54) {
                event.setCancelled(true);
                if (slot == StockManageGUI.BACK_SLOT) {
                    SoundUtil.playClick(player);
                    saveStockFromInventory(inv, shop);
                    OwnerConfigGUI.open(player, shop);
                }
                return;
            }
            return;
        }

        if (inv.getHolder() instanceof CustomHolders.RevenueHolder holder) {
            Shop shop = holder.getShop();
            if (!checkOwnerAccess(player, shop)) {
                event.setCancelled(true);
                return;
            }

            int slot = event.getRawSlot();
            if (slot >= 45 && slot < 54) {
                event.setCancelled(true);
                if (slot == RevenueGUI.BACK_SLOT) {
                    SoundUtil.playClick(player);
                    saveRevenueFromInventory(inv, shop);
                    OwnerConfigGUI.open(player, shop);
                } else if (slot == RevenueGUI.WITHDRAW_ALL_SLOT) {
                    synchronized (shop) {
                        List<ItemStack> toWithdraw = new ArrayList<>(shop.getRevenue());
                        shop.getRevenue().clear();
                        for (int i = 0; i < 45; i++) inv.setItem(i, null);
                        for (ItemStack item : toWithdraw) {
                            if (item != null) {
                                player.getInventory().addItem(item).values().forEach(rem ->
                                        player.getWorld().dropItemNaturally(player.getLocation(), rem)
                                );
                            }
                        }
                    }
                    plugin.getShopManager().saveAllAsync();
                    SoundUtil.playWithdraw(player);
                    player.sendMessage(Component.text("All revenue withdrawn successfully!", NamedTextColor.GREEN));
                    RevenueGUI.open(player, shop);
                }
                return;
            }
            return;
        }

        if (inv.getHolder() instanceof CustomHolders.CustomerTradeHolder holder) {
            Shop shop = holder.getShop();
            int rawSlot = event.getRawSlot();

            if (rawSlot == CustomerTradeGUI.CONFIG_SHORTCUT_SLOT) {
                event.setCancelled(true);
                if (player.getUniqueId().equals(shop.getOwnerUuid()) || player.hasPermission("foliashop.admin")) {
                    SoundUtil.playClick(player);
                    OwnerConfigGUI.open(player, shop);
                }
                return;
            }

            if (rawSlot == CustomerTradeGUI.INPUT_SLOT || rawSlot >= inv.getSize()) {
                player.getScheduler().execute(plugin, () -> CustomerTradeGUI.updateTradingSlots(inv, shop), null, 1L);
                return;
            }

            boolean isOutputSlot = Arrays.stream(CustomerTradeGUI.OUTPUT_SLOTS).anyMatch(s -> s == rawSlot);
            if (isOutputSlot) {
                event.setCancelled(true);
                ItemStack outputItem = inv.getItem(rawSlot);
                if (outputItem == null || outputItem.getType() == Material.AIR) {
                    SoundUtil.playVillagerNo(player);
                    return;
                }

                if (outputItem.getType() == Material.BARRIER) {
                    SoundUtil.playVillagerNo(player);
                    player.sendMessage(Component.text("Shop Revenue Vault is full! Purchase cancelled.", NamedTextColor.RED));
                    return;
                }

                synchronized (shop) {
                    ItemStack input = inv.getItem(CustomerTradeGUI.INPUT_SLOT);
                    if (input != null && input.getType() == shop.getCurrency() && input.getAmount() >= shop.getCostAmount()) {
                        if (shop.executePurchase(1)) {
                            int newAmount = input.getAmount() - shop.getCostAmount();
                            if (newAmount <= 0) {
                                inv.setItem(CustomerTradeGUI.INPUT_SLOT, null);
                            } else {
                                input.setAmount(newAmount);
                            }

                            ItemStack reward = shop.getProductItem();
                            reward.setAmount(shop.getProductAmount());
                            player.getInventory().addItem(reward).values().forEach(remaining ->
                                player.getWorld().dropItemNaturally(player.getLocation(), remaining)
                            );

                            plugin.getShopManager().saveAllAsync();
                            SoundUtil.playPurchaseSuccess(player);
                            player.sendMessage(Component.text("Purchase successful!", NamedTextColor.GREEN));

                            if (shop.getTotalStockCount() < shop.getProductAmount()) {
                                triggerOutOfStockAlarm(shop);
                            }
                        } else {
                            SoundUtil.playVillagerNo(player);
                            player.sendMessage(Component.text("Shop cannot complete transaction (Stock depleted or Vault Full)!", NamedTextColor.RED));
                        }
                    } else {
                        SoundUtil.playVillagerNo(player);
                    }
                }
                CustomerTradeGUI.updateTradingSlots(inv, shop);
                return;
            }

            event.setCancelled(true);
        }
    }

    private void saveStockFromInventory(Inventory inv, Shop shop) {
        synchronized (shop) {
            shop.getStock().clear();
            for (int i = 0; i < 45; i++) {
                ItemStack item = inv.getItem(i);
                if (item != null && item.getType() != Material.AIR) {
                    shop.getStock().add(item);
                }
            }
        }
        plugin.getShopManager().saveAllAsync();
    }

    private void saveRevenueFromInventory(Inventory inv, Shop shop) {
        synchronized (shop) {
            shop.getRevenue().clear();
            for (int i = 0; i < 45; i++) {
                ItemStack item = inv.getItem(i);
                if (item != null && item.getType() != Material.AIR) {
                    shop.getRevenue().add(item);
                }
            }
        }
        plugin.getShopManager().saveAllAsync();
    }

    private void triggerOutOfStockAlarm(Shop shop) {
        Player owner = Bukkit.getPlayer(shop.getOwnerUuid());
        if (owner != null && owner.isOnline()) {
            owner.getScheduler().execute(plugin, () -> {
                SoundUtil.playAlarm(owner);
                Location loc = shop.getLocation();
                if (loc != null) {
                    owner.showTitle(Title.title(
                            Component.text("OUT OF STOCK!", NamedTextColor.RED).decoration(TextDecoration.BOLD, true),
                            Component.text("Your shop at [" + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + "] is empty!", NamedTextColor.YELLOW),
                            Title.Times.times(Duration.ofMillis(200), Duration.ofSeconds(3), Duration.ofMillis(500))
                    ));
                    owner.sendMessage(Component.text("[Shop Alert] ", NamedTextColor.RED).decoration(TextDecoration.BOLD, true)
                            .append(Component.text("Your shop at " + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ() + " has run out of stock!", NamedTextColor.YELLOW)));
                }
            }, null, 1L);
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getInventory().getHolder() instanceof CustomHolders.CustomerTradeHolder holder) {
            if (event.getRawSlots().contains(CustomerTradeGUI.INPUT_SLOT)) {
                Player player = (Player) event.getWhoClicked();
                player.getScheduler().execute(plugin, () -> CustomerTradeGUI.updateTradingSlots(event.getInventory(), holder.getShop()), null, 1L);
            } else if (event.getRawSlots().stream().anyMatch(slot -> slot < event.getInventory().getSize())) {
                event.setCancelled(true);
            }
        } else if (event.getInventory().getHolder() instanceof CustomHolders.StockManageHolder ||
                   event.getInventory().getHolder() instanceof CustomHolders.RevenueHolder) {
            if (event.getRawSlots().stream().anyMatch(slot -> slot >= 45 && slot < 54)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        Inventory inv = event.getInventory();
        Player player = (Player) event.getPlayer();

        if (inv.getHolder() instanceof CustomHolders.StockManageHolder holder) {
            saveStockFromInventory(inv, holder.getShop());
        }

        if (inv.getHolder() instanceof CustomHolders.RevenueHolder holder) {
            saveRevenueFromInventory(inv, holder.getShop());
        }

        if (inv.getHolder() instanceof CustomHolders.CustomerTradeHolder) {
            ItemStack unconsumed = inv.getItem(CustomerTradeGUI.INPUT_SLOT);
            if (unconsumed != null && unconsumed.getType() != Material.AIR) {
                inv.setItem(CustomerTradeGUI.INPUT_SLOT, null);
                player.getInventory().addItem(unconsumed).values().forEach(remaining ->
                    player.getWorld().dropItemNaturally(player.getLocation(), remaining)
                );
            }
        }
    }
}
