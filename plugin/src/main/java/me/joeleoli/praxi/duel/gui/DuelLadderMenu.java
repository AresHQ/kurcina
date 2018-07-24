package me.joeleoli.praxi.duel.gui;

import lombok.AllArgsConstructor;

import me.joeleoli.commons.menu.Button;
import me.joeleoli.commons.menu.Menu;

import me.joeleoli.praxi.config.Config;
import me.joeleoli.praxi.config.ConfigItem;
import me.joeleoli.praxi.config.ConfigKey;
import me.joeleoli.praxi.ladder.Ladder;
import me.joeleoli.praxi.player.PlayerData;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DuelLadderMenu extends Menu {

    @Override
    public String getTitle(Player player) {
        PlayerData playerData = PlayerData.getByUuid(player.getUniqueId());
        return Config.translatePlayerAndTarget(Config.getString(ConfigKey.MENU_DUEL_LADDER_TITLE), player, playerData.getDuelProcedure().getTarget());
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();

        for (Ladder ladder : Ladder.getLadders()) {
            if (ladder.isEnabled()) {
                buttons.put(buttons.size(), new SelectLadderButton(ladder));
            }
        }

        return buttons;
    }

    @Override
    public void onClose(Player player) {
        if (!this.isClosedByMenu()) {
            PlayerData playerData = PlayerData.getByUuid(player.getUniqueId());

            playerData.setDuelProcedure(null);
        }
    }

    @AllArgsConstructor
    private class SelectLadderButton extends Button {

        private Ladder ladder;

        @Override
        public ItemStack getButtonItem(Player player) {
            ConfigItem configItem = Config.getConfigItem(ConfigKey.MENU_DUEL_LADDER_SELECT_BUTTON);
            ItemStack itemStack = this.ladder.getDisplayIcon();
            ItemMeta itemMeta = itemStack.getItemMeta();
            List<String> lore = new ArrayList<>();

            configItem.getLore().forEach(line -> {
                lore.add(Config.translateLadder(line, this.ladder));
            });

            itemMeta.setDisplayName(Config.translateLadder(configItem.getName(), this.ladder));
            itemMeta.setLore(lore);
            itemStack.setItemMeta(itemMeta);

            return itemStack;
        }

        @Override
        public void clicked(Player player, int i, ClickType clickType, int hb) {
            PlayerData playerData = PlayerData.getByUuid(player.getUniqueId());

            // Update duel procedure
            playerData.getDuelProcedure().setLadder(this.ladder);

            // Set closed by menu
            Menu.currentlyOpenedMenus.get(player.getName()).setClosedByMenu(true);

            // Force close inventory
            player.closeInventory();

            // Open arena selection menu
            new DuelArenaMenu().openMenu(player);
        }

    }

}
