package com.NotVibeCodedSlop.foliashop.command;

import com.NotVibeCodedSlop.foliashop.FoliaChestShop;
import com.NotVibeCodedSlop.foliashop.manager.ShopManager;
import com.NotVibeCodedSlop.foliashop.model.Shop;
import com.NotVibeCodedSlop.foliashop.util.SoundUtil;
import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ShopConfirmCommand implements BasicCommand {

    private final FoliaChestShop plugin;

    public ShopConfirmCommand(FoliaChestShop plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(@NotNull CommandSourceStack source, @NotNull String[] args) {
        CommandSender sender = source.getSender();
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("This command can only be executed by players.", NamedTextColor.RED));
            return;
        }

        if (args.length < 1) {
            player.sendMessage(Component.text("Usage: /shopconfirm <0-9>", NamedTextColor.RED));
            return;
        }

        int code;
        try {
            code = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            player.sendMessage(Component.text("Invalid code! Must be a single digit number (0-9).", NamedTextColor.RED));
            return;
        }

        ShopManager.DeletionRequest req = plugin.getShopManager().getPendingDeletion(player.getUniqueId());
        if (req == null) {
            player.sendMessage(Component.text("You have no pending shop deletions or the confirmation expired.", NamedTextColor.RED));
            SoundUtil.playWarning(player);
            return;
        }

        if (req.code() != code) {
            player.sendMessage(Component.text("Incorrect confirmation code!", NamedTextColor.RED));
            SoundUtil.playWarning(player);
            return;
        }

        Shop shop = req.shop();
        Location loc = shop.getLocation();
        plugin.getShopManager().clearPendingDeletion(player.getUniqueId());

        if (loc != null) {
            Bukkit.getRegionScheduler().execute(plugin, loc, () -> {
                synchronized (shop) {
                    for (ItemStack item : shop.getStock()) {
                        if (item != null) loc.getWorld().dropItemNaturally(loc, item);
                    }
                    for (ItemStack rev : shop.getRevenue()) {
                        if (rev != null) loc.getWorld().dropItemNaturally(loc, rev);
                    }
                    shop.getStock().clear();
                    shop.getRevenue().clear();
                }

                plugin.getShopManager().clearChestOwner(loc.getBlock());
                plugin.getShopManager().removeShop(shop.getPos());

                player.getScheduler().execute(plugin, () -> {
                    SoundUtil.playDelete(player);
                    player.sendMessage(Component.text("Shop successfully deleted and all items refunded!", NamedTextColor.GREEN));
                }, null, 1L);
            });
        }
    }

    @Override
    public @Nullable String permission() {
        return "foliashop.delete";
    }
}
