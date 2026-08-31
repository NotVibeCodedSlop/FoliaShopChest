package com.NotVibeCodedSlop.foliashop.command;

import com.NotVibeCodedSlop.foliashop.FoliaChestShop;
import com.NotVibeCodedSlop.foliashop.gui.OwnerConfigGUI;
import com.NotVibeCodedSlop.foliashop.model.BlockPos;
import com.NotVibeCodedSlop.foliashop.model.Shop;
import com.NotVibeCodedSlop.foliashop.util.SoundUtil;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class ShopConfigCommand implements BasicCommand {

    private final FoliaChestShop plugin;

    public ShopConfigCommand(FoliaChestShop plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(@NotNull CommandSourceStack source, @NotNull String[] args) {
        CommandSender sender = source.getSender();
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("This command can only be executed by players.", NamedTextColor.RED));
            return;
        }

        if (!player.hasPermission("foliashop.create")) {
            player.sendMessage(Component.text("You do not have permission to configure shops.", NamedTextColor.RED));
            return;
        }

        Block target = player.getTargetBlockExact(5);
        if (target == null || (target.getType() != Material.CHEST && target.getType() != Material.TRAPPED_CHEST)) {
            player.sendMessage(Component.text("You must look directly at a chest to configure it!", NamedTextColor.RED));
            SoundUtil.playWarning(player);
            return;
        }

        Bukkit.getRegionScheduler().execute(plugin, target.getLocation(), () -> {
            BlockPos primaryPos = plugin.getShopManager().getPrimaryPos(target);
            UUID ownerUuid = plugin.getShopManager().getOwnerOrPlacer(target);

            if (ownerUuid != null && !ownerUuid.equals(player.getUniqueId()) && !player.hasPermission("foliashop.admin")) {
                player.sendMessage(Component.text("You cannot configure a chest placed by another player!", NamedTextColor.RED));
                SoundUtil.playWarning(player);
                return;
            }

            if (ownerUuid == null) {
                ownerUuid = player.getUniqueId();
                plugin.getShopManager().registerChestPlacement(target, ownerUuid);
            }

            Shop shop = plugin.getShopManager().getOrCreateShop(primaryPos, ownerUuid);
            player.getScheduler().execute(plugin, () -> {
                SoundUtil.playClick(player);
                OwnerConfigGUI.open(player, shop);
            }, null, 1L);
        });
    }

    @Override
    public @Nullable String permission() {
        return "foliashop.create";
    }
}
