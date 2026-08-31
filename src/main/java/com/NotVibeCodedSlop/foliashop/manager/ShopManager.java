package com.NotVibeCodedSlop.foliashop.manager;

import com.NotVibeCodedSlop.foliashop.FoliaChestShop;
import com.NotVibeCodedSlop.foliashop.model.BlockPos;
import com.NotVibeCodedSlop.foliashop.model.Shop;
import com.NotVibeCodedSlop.foliashop.util.ItemSerializer;
import com.google.gson.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.TileState;
import org.bukkit.block.data.type.Chest;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ShopManager {

    public record DeletionRequest(Shop shop, int code, long expiryTimeMillis) {}

    private final FoliaChestShop plugin;
    private final NamespacedKey ownerKey;
    private final Map<BlockPos, Shop> shopCache = new ConcurrentHashMap<>();
    private final Map<BlockPos, UUID> chestPlacers = new ConcurrentHashMap<>();
    private final Map<UUID, DeletionRequest> pendingDeletions = new ConcurrentHashMap<>();
    private final File dataFile;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public ShopManager(FoliaChestShop plugin) {
        this.plugin = plugin;
        this.ownerKey = new NamespacedKey(plugin, "chest_owner");
        this.dataFile = new File(plugin.getDataFolder(), "shops.json");
        loadAll();
    }

    public List<BlockPos> getAllChestBlocks(Block block) {
        List<BlockPos> list = new ArrayList<>();
        list.add(BlockPos.of(block));

        if (block.getBlockData() instanceof Chest chestData) {
            if (chestData.getType() != Chest.Type.SINGLE) {
                for (BlockFace face : new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST}) {
                    Block rel = block.getRelative(face);
                    if (rel.getType() == block.getType() && rel.getBlockData() instanceof Chest relData) {
                        if (relData.getFacing() == chestData.getFacing() && relData.getType() != Chest.Type.SINGLE && relData.getType() != chestData.getType()) {
                            list.add(BlockPos.of(rel));
                            break;
                        }
                    }
                }
            }
        }
        return list;
    }

    public BlockPos getPrimaryPos(Block block) {
        List<BlockPos> all = getAllChestBlocks(block);
        if (all.size() == 1) return all.get(0);
        all.sort((a, b) -> {
            int cx = Integer.compare(a.x(), b.x());
            if (cx != 0) return cx;
            int cy = Integer.compare(a.y(), b.y());
            if (cy != 0) return cy;
            return Integer.compare(a.z(), b.z());
        });
        return all.get(0);
    }

    public void registerChestPlacement(Block block, UUID placerUuid) {
        for (BlockPos pos : getAllChestBlocks(block)) {
            chestPlacers.put(pos, placerUuid);
        }
        BlockPos primary = getPrimaryPos(block);
        chestPlacers.put(primary, placerUuid);

        Bukkit.getRegionScheduler().execute(plugin, block.getLocation(), () -> {
            if (block.getState() instanceof TileState tileState) {
                PersistentDataContainer pdc = tileState.getPersistentDataContainer();
                pdc.set(ownerKey, PersistentDataType.STRING, placerUuid.toString());
                tileState.update(true);
            }
        });
        saveAllAsync();
    }

    public void clearChestOwner(Block block) {
        for (BlockPos pos : getAllChestBlocks(block)) {
            chestPlacers.remove(pos);
            shopCache.remove(pos);
        }
        BlockPos primary = getPrimaryPos(block);
        chestPlacers.remove(primary);
        shopCache.remove(primary);

        if (block.getState() instanceof TileState tileState) {
            PersistentDataContainer pdc = tileState.getPersistentDataContainer();
            pdc.remove(ownerKey);
            tileState.update(true);
        }
        saveAllAsync();
    }

    public UUID getOwnerOrPlacer(Block block) {
        BlockPos primary = getPrimaryPos(block);
        BlockPos current = BlockPos.of(block);

        Shop shop = shopCache.get(primary);
        if (shop != null) return shop.getOwnerUuid();

        shop = shopCache.get(current);
        if (shop != null) return shop.getOwnerUuid();

        UUID placer = chestPlacers.get(primary);
        if (placer != null) return placer;

        placer = chestPlacers.get(current);
        if (placer != null) return placer;

        for (BlockPos pos : getAllChestBlocks(block)) {
            UUID p = chestPlacers.get(pos);
            if (p != null) return p;
        }

        if (block.getState() instanceof TileState tileState) {
            String uuidStr = tileState.getPersistentDataContainer().get(ownerKey, PersistentDataType.STRING);
            if (uuidStr != null) {
                try {
                    return UUID.fromString(uuidStr);
                } catch (IllegalArgumentException ignored) {}
            }
        }
        return null;
    }

    public Shop getOrCreateShop(BlockPos primaryPos, UUID ownerUuid) {
        return shopCache.computeIfAbsent(primaryPos, p -> {
            Shop s = new Shop(p, ownerUuid);
            chestPlacers.put(p, ownerUuid);
            saveAllAsync();
            return s;
        });
    }

    public Shop getShop(BlockPos pos) {
        return shopCache.get(pos);
    }

    public void removeShop(BlockPos pos) {
        shopCache.remove(pos);
        chestPlacers.remove(pos);
        saveAllAsync();
    }

    public void setPendingDeletion(UUID playerUuid, Shop shop, int code) {
        pendingDeletions.put(playerUuid, new DeletionRequest(shop, code, System.currentTimeMillis() + 30000));
    }

    public DeletionRequest getPendingDeletion(UUID playerUuid) {
        DeletionRequest req = pendingDeletions.get(playerUuid);
        if (req != null && System.currentTimeMillis() > req.expiryTimeMillis()) {
            pendingDeletions.remove(playerUuid);
            return null;
        }
        return req;
    }

    public void clearPendingDeletion(UUID playerUuid) {
        pendingDeletions.remove(playerUuid);
    }

    public void saveAllAsync() {
        Bukkit.getAsyncScheduler().runNow(plugin, task -> saveAllSync());
    }

    public synchronized void saveAllSync() {
        try {
            if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();
            JsonObject root = new JsonObject();
            JsonArray shopsArray = new JsonArray();
            JsonArray placersArray = new JsonArray();

            for (Shop shop : shopCache.values()) {
                JsonObject obj = new JsonObject();
                BlockPos p = shop.getPos();
                obj.addProperty("pos", p.toString());
                obj.addProperty("owner", shop.getOwnerUuid().toString());
                obj.addProperty("currency", shop.getCurrency().name());
                obj.addProperty("costAmount", shop.getCostAmount());
                obj.addProperty("productAmount", shop.getProductAmount());
                obj.addProperty("productItem", ItemSerializer.toBase64(shop.getProductItem()));

                JsonArray stockArray = new JsonArray();
                synchronized (shop) {
                    for (ItemStack item : shop.getStock()) {
                        if (item != null) stockArray.add(ItemSerializer.toBase64(item));
                    }
                }
                obj.add("stock", stockArray);

                JsonArray revArray = new JsonArray();
                synchronized (shop) {
                    for (ItemStack item : shop.getRevenue()) {
                        if (item != null) revArray.add(ItemSerializer.toBase64(item));
                    }
                }
                obj.add("revenue", revArray);

                shopsArray.add(obj);
            }

            for (Map.Entry<BlockPos, UUID> entry : chestPlacers.entrySet()) {
                JsonObject pObj = new JsonObject();
                pObj.addProperty("pos", entry.getKey().toString());
                pObj.addProperty("placer", entry.getValue().toString());
                placersArray.add(pObj);
            }

            root.add("shops", shopsArray);
            root.add("placers", placersArray);

            try (FileWriter writer = new FileWriter(dataFile)) {
                gson.toJson(root, writer);
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to save shops.json: " + e.getMessage());
        }
    }

    public synchronized void loadAll() {
        if (!dataFile.exists()) return;
        try (FileReader reader = new FileReader(dataFile)) {
            JsonElement parsed = JsonParser.parseReader(reader);
            if (parsed == null || !parsed.isJsonObject()) return;
            JsonObject root = parsed.getAsJsonObject();

            if (root.has("placers")) {
                for (JsonElement pEl : root.getAsJsonArray("placers")) {
                    JsonObject pObj = pEl.getAsJsonObject();
                    BlockPos pos = BlockPos.fromString(pObj.get("pos").getAsString());
                    chestPlacers.put(pos, UUID.fromString(pObj.get("placer").getAsString()));
                }
            }

            if (root.has("shops")) {
                for (JsonElement el : root.getAsJsonArray("shops")) {
                    JsonObject obj = el.getAsJsonObject();
                    BlockPos pos = BlockPos.fromString(obj.get("pos").getAsString());
                    UUID ownerUuid = UUID.fromString(obj.get("owner").getAsString());
                    Shop shop = new Shop(pos, ownerUuid);

                    if (obj.has("currency")) shop.setCurrency(Material.matchMaterial(obj.get("currency").getAsString()));
                    if (obj.has("costAmount")) shop.setCostAmount(obj.get("costAmount").getAsInt());
                    if (obj.has("productAmount")) shop.setProductAmount(obj.get("productAmount").getAsInt());
                    if (obj.has("productItem")) shop.setProductItem(ItemSerializer.fromBase64(obj.get("productItem").getAsString()));

                    if (obj.has("stock")) {
                        for (JsonElement sEl : obj.getAsJsonArray("stock")) {
                            ItemStack it = ItemSerializer.fromBase64(sEl.getAsString());
                            if (it != null) shop.getStock().add(it);
                        }
                    }
                    if (obj.has("revenue")) {
                        for (JsonElement rEl : obj.getAsJsonArray("revenue")) {
                            ItemStack it = ItemSerializer.fromBase64(rEl.getAsString());
                            if (it != null) shop.getRevenue().add(it);
                        }
                    }

                    shopCache.put(pos, shop);
                    chestPlacers.put(pos, ownerUuid);
                }
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to load shops.json: " + e.getMessage());
        }
    }
}
