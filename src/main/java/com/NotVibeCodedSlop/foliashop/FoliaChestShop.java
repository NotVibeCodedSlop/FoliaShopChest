package com.NotVibeCodedSlop.foliashop;

import com.NotVibeCodedSlop.foliashop.command.ShopConfigCommand;
import com.NotVibeCodedSlop.foliashop.command.ShopConfirmCommand;
import com.NotVibeCodedSlop.foliashop.listener.ShopBlockListener;
import com.NotVibeCodedSlop.foliashop.listener.ShopInventoryListener;
import com.NotVibeCodedSlop.foliashop.manager.ShopManager;
import org.bukkit.plugin.java.JavaPlugin;

public class FoliaChestShop extends JavaPlugin {

    private ShopManager shopManager;

    @Override
    public void onEnable() {
        this.shopManager = new ShopManager(this);

        getServer().getPluginManager().registerEvents(new ShopBlockListener(this), this);
        getServer().getPluginManager().registerEvents(new ShopInventoryListener(this), this);

        registerCommand("shopconfig", new ShopConfigCommand(this));
        registerCommand("shopconfirm", new ShopConfirmCommand(this));

        getLogger().info("FoliaChestShop enabled successfully on Folia (SMP Protected)!");
    }

    @Override
    public void onDisable() {
        if (shopManager != null) {
            shopManager.saveAllSync();
        }
        getLogger().info("FoliaChestShop disabled and data saved.");
    }

    public ShopManager getShopManager() {
        return shopManager;
    }
}
