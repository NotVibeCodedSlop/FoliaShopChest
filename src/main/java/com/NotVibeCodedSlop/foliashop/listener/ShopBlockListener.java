package com.NotVibeCodedSlop.foliashop.listener;

import com.NotVibeCodedSlop.foliashop.FoliaChestShop;
import com.NotVibeCodedSlop.foliashop.gui.CustomerTradeGUI;
import com.NotVibeCodedSlop.foliashop.gui.OwnerConfigGUI;
import com.NotVibeCodedSlop.foliashop.model.BlockPos;
import com.NotVibeCodedSlop.foliashop.model.Shop;
import com.NotVibeCodedSlop.foliashop.util.SoundUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.UUID;

public class ShopBlockListener implements Listener {

    private final FoliaChestShop plugin;
    private static final BlockFace[] ADJACENT_FACES = {BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST};

    public ShopBlockListener(FoliaChestShop plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlockPlaced();
        Player player = event.getPlayer();

        if (block.getType() == Material.CHEST || block.getType() == Material.TRAPPED_CHEST) {
            for (BlockFace face : ADJACENT_FACES) {
                Block relative = block.getRelative(face);
                if (relative.getType() == block.getType()) {
                    UUID neighborOwner = plugin.getShopManager().getOwnerOrPlacer(relative);
                    if (neighborOwner != null && !neighborOwner.equals(player.getUniqueId()) && !player.hasPermission("foliashop.admin")) {
                        event.setCancelled(true);
                        player.sendMessage(Component.text("You cannot place a chest next to someone else's shop!", NamedTextColor.RED));
                        SoundUtil.playWarning(player);
                        return;
                    }
                }
            }
            plugin.getShopManager().registerChestPlacement(block, player.getUniqueId());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getType() == Material.CHEST || block.getType() == Material.TRAPPED_CHEST) {
            BlockPos primary = plugin.getShopManager().getPrimaryPos(block);
            Shop shop = plugin.getShopManager().getShop(primary);
            if (shop != null) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(Component.text("Active shops cannot be broken directly. Delete via /shopconfig -> Delete Shop!", NamedTextColor.RED));
                SoundUtil.playWarning(event.getPlayer());
            } else {
                plugin.getShopManager().clearChestOwner(block);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        event.blockList().removeIf(block -> plugin.getShopManager().getShop(plugin.getShopManager().getPrimaryPos(block)) != null);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        event.blockList().removeIf(block -> plugin.getShopManager().getShop(plugin.getShopManager().getPrimaryPos(block)) != null);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        for (Block block : event.getBlocks()) {
            if (plugin.getShopManager().getShop(plugin.getShopManager().getPrimaryPos(block)) != null) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        for (Block block : event.getBlocks()) {
            if (plugin.getShopManager().getShop(plugin.getShopManager().getPrimaryPos(block)) != null) {
                event.setCancelled(true);
                return;
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onHopperMove(InventoryMoveItemEvent event) {
        if (event.getSource().getLocation() != null) {
            Block block = event.getSource().getLocation().getBlock();
            if (plugin.getShopManager().getShop(plugin.getShopManager().getPrimaryPos(block)) != null) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block block = event.getClickedBlock();
        if (block == null) return;

        if (block.getType() == Material.CHEST || block.getType() == Material.TRAPPED_CHEST) {
            Player player = event.getPlayer();
            BlockPos primary = plugin.getShopManager().getPrimaryPos(block);
            Shop shop = plugin.getShopManager().getShop(primary);

            if (shop != null) {
                event.setCancelled(true);
                if (player.isSneaking() && (player.getUniqueId().equals(shop.getOwnerUuid()) || player.hasPermission("foliashop.admin"))) {
                    SoundUtil.playClick(player);
                    player.getScheduler().execute(plugin, () -> OwnerConfigGUI.open(player, shop), null, 1L);
                    return;
                }

                if (!player.hasPermission("foliashop.use")) {
                    player.sendMessage(Component.text("You do not have permission to use shops.", NamedTextColor.RED));
                    return;
                }
                SoundUtil.playClick(player);
                player.getScheduler().execute(plugin, () -> CustomerTradeGUI.open(player, shop), null, 1L);
            }
        }
    }
}
