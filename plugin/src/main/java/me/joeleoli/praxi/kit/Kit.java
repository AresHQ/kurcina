package me.joeleoli.praxi.kit;

import lombok.Data;

import org.bukkit.inventory.ItemStack;

@Data
public class Kit {

    private ItemStack[] armor;
    private ItemStack[] contents;

    public Kit() {
        this.armor = new ItemStack[4];
        this.contents = new ItemStack[36];
    }

    public Kit(ItemStack[] armor, ItemStack[] contents) {
        this.armor = armor;
        this.contents = contents;
    }

}
