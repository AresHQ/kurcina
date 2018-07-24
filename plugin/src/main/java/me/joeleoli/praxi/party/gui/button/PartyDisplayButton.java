package me.joeleoli.praxi.party.gui.button;

import lombok.AllArgsConstructor;

import me.joeleoli.commons.menu.Button;
import me.joeleoli.commons.util.PlayerUtil;

import me.joeleoli.praxi.config.Config;
import me.joeleoli.praxi.config.ConfigItem;
import me.joeleoli.praxi.config.ConfigKey;
import me.joeleoli.praxi.party.Party;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class PartyDisplayButton extends Button {

    private Party party;

    @Override
    public ItemStack getButtonItem(Player player) {
        ConfigItem configItem = Config.getConfigItem(ConfigKey.MENU_OTHER_PARTIES_PARTY_DISPLAY_BUTTON);
        ItemStack itemStack = new ItemStack(configItem.getMaterial(), 1, configItem.getDurability());
        ItemMeta itemMeta = itemStack.getItemMeta();
        List<String> lore = new ArrayList<>();

        this.party.getPlayers().forEach(other -> {
            configItem.getLore().forEach(line -> {
                lore.add(Config.translatePlayerAndTarget(line, other, null));
            });
        });

        itemMeta.setDisplayName(Config.translateParty(configItem.getName(), this.party));
        itemMeta.setLore(lore);

        itemStack.setItemMeta(itemMeta);

        return itemStack;
    }

}
