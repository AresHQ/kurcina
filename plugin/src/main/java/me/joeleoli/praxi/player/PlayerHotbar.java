package me.joeleoli.praxi.player;

import me.joeleoli.commons.config.ConfigCursor;

import me.joeleoli.praxi.hotbar.HotbarItem;
import me.joeleoli.praxi.hotbar.HotbarLayout;
import me.joeleoli.praxi.config.Config;
import me.joeleoli.praxi.Praxi;

import lombok.Getter;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerHotbar {

    @Getter
    private static Map<HotbarItem, ItemStack> items = new HashMap<>();
    private static Map<HotbarLayout, HotbarItem[]> layouts = new HashMap<>();

    // Utility class - cannot be instantiated
    private PlayerHotbar() {}

    public static void init() {
        ConfigCursor cursor = new ConfigCursor(Praxi.getInstance().getMainConfig(), "hotbar");

        for (String key : cursor.getKeys("items")) {
            cursor.setPath("hotbar.items." + key);

            try {
                HotbarItem hotbarItem = HotbarItem.valueOf(key);
                ItemStack itemStack = new ItemStack(Material.valueOf(cursor.getString("material")));
                ItemMeta itemMeta = itemStack.getItemMeta();

                if (cursor.exists("lore")) {
                    List<String> lore = new ArrayList<>();

                    cursor.getStringList("lore").forEach(line -> lore.add(Config.translateGlobal(line)));

                    itemMeta.setLore(lore);
                }

                itemMeta.setDisplayName(Config.translateGlobal(cursor.getString("name")));
                itemStack.setDurability((short) cursor.getInt("durability"));
                itemStack.setItemMeta(itemMeta);

                items.put(hotbarItem, itemStack);
            } catch (Exception e) {
                e.printStackTrace();
                Praxi.getInstance().getLogger().severe("Failed to load hotbar item `" + key + "`.");
            }
        }

        cursor.setPath("hotbar");

        for (String key : cursor.getKeys("layouts")) {
            try {
                HotbarLayout hotbarLayout = HotbarLayout.valueOf(key);
                HotbarItem[] hotbarItems = new HotbarItem[9];

                int i = 0;

                for (String itemName : cursor.getStringList("layouts." + key)) {
                    if (itemName == null || itemName.equals("")) {
                        hotbarItems[i++] = null;
                        continue;
                    }

                    HotbarItem hotbarItem = HotbarItem.valueOf(itemName);

                    hotbarItems[i++] = hotbarItem;
                }

                layouts.put(hotbarLayout, hotbarItems);
            } catch (Exception e) {
                e.printStackTrace();
                Praxi.getInstance().getLogger().severe("Failed to load hotbar layout `" + key + "`.");
            }
        }
    }

    public static ItemStack[] getLayout(HotbarLayout layout) {
        HotbarItem[] hotbarItems = layouts.get(layout);

        if (hotbarItems == null) {
            return new ItemStack[9];
        }

        ItemStack[] toReturn = new ItemStack[9];
        int i = 0;

        for (HotbarItem hotbarItem : hotbarItems) {
            toReturn[i++] = items.get(hotbarItem);
        }

        return toReturn;
    }

    public static HotbarItem fromItemStack(ItemStack itemStack) {
        for (Map.Entry<HotbarItem, ItemStack> entry : PlayerHotbar.getItems().entrySet()) {
            if (entry.getValue() != null && entry.getValue().equals(itemStack)) {
                return entry.getKey();
            }
        }

        return null;
    }

}
