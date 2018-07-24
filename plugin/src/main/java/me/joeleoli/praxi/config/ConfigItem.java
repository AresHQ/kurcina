package me.joeleoli.praxi.config;

import lombok.Getter;

import me.joeleoli.commons.config.ConfigCursor;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

@Getter
public class ConfigItem {

    private Material material = Material.AIR;
    private short durability = 0;
    private String name;
    private List<String> lore = new ArrayList<>();
    private int amount = 1;

    public ConfigItem(ConfigCursor cursor, String path) {
        if (cursor.exists(path + ".material")) {
            this.material = Material.valueOf(cursor.getString(path + ".material"));
        }

        if (cursor.exists(path + ".durability")) {
            this.durability = (short) cursor.getInt(path + ".durability");
        }

        if (cursor.exists(path + ".name")) {
            this.name = Config.translateGlobal(cursor.getString(path + ".name"));
        }

        if (cursor.exists(path + ".lore")) {
            this.lore = Config.translateGlobal(cursor.getStringList(path + ".lore"));
        }

        if (cursor.exists(path + ".amount")) {
            this.amount = cursor.getInt(path + ".amount");
        }
    }

    public ItemStack toItemStack() {
        ItemStack itemStack = new ItemStack(this.material);
        ItemMeta itemMeta = itemStack.getItemMeta();

        if (this.name != null) {
            itemMeta.setDisplayName(Config.translateGlobal(this.name));
        }

        if (this.lore != null && !this.lore.isEmpty()) {
            List<String> lore = new ArrayList<>();

            this.lore.forEach(line -> {
                lore.add(Config.translateGlobal(line));
            });

            itemMeta.setLore(lore);
        }

        itemStack.setAmount(this.amount);
        itemStack.setDurability(this.durability);
        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }

}