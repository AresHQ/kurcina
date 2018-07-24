package me.joeleoli.praxi.kit.editor.gui.button;

import lombok.AllArgsConstructor;

import me.joeleoli.commons.menu.Button;
import me.joeleoli.commons.menu.Menu;

import me.joeleoli.praxi.config.Config;
import me.joeleoli.praxi.config.ConfigItem;
import me.joeleoli.praxi.config.ConfigKey;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;


@AllArgsConstructor
public class BackButton extends Button {

    private Menu back;

    @Override
    public ItemStack getButtonItem(Player player) {
        ConfigItem configItem = Config.getConfigItem(ConfigKey.MENU_KIT_MANAGEMENT_BACK_BUTTON);
        ItemStack itemStack = new ItemStack(configItem.getMaterial(), 1, configItem.getDurability());
        ItemMeta itemMeta = itemStack.getItemMeta();

        itemMeta.setDisplayName(configItem.getName());
        itemMeta.setLore(configItem.getLore());
        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }

    @Override
    public void clicked(Player player, int i, ClickType clickType, int hb) {
        Button.playNeutral(player);

        this.back.openMenu(player);
    }

}
