package com.NotVibeCodedSlop.foliashop.model;

import org.bukkit.Material;
import java.util.*;

public class CurrencyRegistry {
    private static final List<Material> ALLOWED_CURRENCIES = new ArrayList<>();

    static {
        ALLOWED_CURRENCIES.addAll(List.of(
                Material.IRON_NUGGET, Material.GOLD_NUGGET,
                Material.RAW_IRON, Material.RAW_COPPER, Material.RAW_GOLD,
                Material.COAL, Material.COPPER_INGOT, Material.IRON_INGOT,
                Material.GOLD_INGOT, Material.REDSTONE, Material.LAPIS_LAZULI,
                Material.DIAMOND, Material.EMERALD, Material.NETHERITE_INGOT,
                Material.QUARTZ, Material.AMETHYST_SHARD,
                Material.COAL_BLOCK, Material.RAW_COPPER_BLOCK, Material.COPPER_BLOCK,
                Material.RAW_IRON_BLOCK, Material.IRON_BLOCK, Material.RAW_GOLD_BLOCK,
                Material.GOLD_BLOCK, Material.REDSTONE_BLOCK, Material.LAPIS_BLOCK,
                Material.DIAMOND_BLOCK, Material.EMERALD_BLOCK, Material.NETHERITE_BLOCK,
                Material.QUARTZ_BLOCK, Material.AMETHYST_BLOCK
        ));
    }

    public static List<Material> getAllowedCurrencies() {
        return Collections.unmodifiableList(ALLOWED_CURRENCIES);
    }

    public static boolean isAllowed(Material material) {
        return ALLOWED_CURRENCIES.contains(material);
    }
}
