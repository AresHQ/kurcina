package me.joeleoli.praxi.duel.gui;

import lombok.AllArgsConstructor;

import me.joeleoli.commons.menu.Button;
import me.joeleoli.commons.menu.Menu;

import me.joeleoli.praxi.arena.Arena;
import me.joeleoli.praxi.arena.ArenaType;
import me.joeleoli.praxi.config.Config;
import me.joeleoli.praxi.config.ConfigItem;
import me.joeleoli.praxi.config.ConfigKey;
import me.joeleoli.praxi.player.PlayerData;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DuelArenaMenu extends Menu {

    @Override
    public String getTitle(Player player) {
        PlayerData playerData = PlayerData.getByUuid(player.getUniqueId());
        return Config.translatePlayerAndTarget(Config.getString(ConfigKey.MENU_DUEL_ARENA_TITLE), player, playerData.getDuelProcedure().getTarget());
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        PlayerData playerData = PlayerData.getByUuid(player.getUniqueId());

        Map<Integer, Button> buttons = new HashMap<>();

        for (Arena arena : Arena.getArenas()) {
            if (arena.isSetup() && arena.getType() != ArenaType.DUPLICATE && !arena.isActive()) {
                if (arena.getType() == (playerData.getDuelProcedure().getLadder().isBuild() ? ArenaType.STANDALONE : ArenaType.SHARED)) {
                    buttons.put(buttons.size(), new SelectArenaButton(arena));
                }
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
    private class SelectArenaButton extends Button {

        private Arena arena;

        @Override
        public ItemStack getButtonItem(Player player) {
            ConfigItem configItem = Config.getConfigItem(ConfigKey.MENU_DUEL_ARENA_SELECT_BUTTON);
            ItemStack itemStack = new ItemStack(configItem.getMaterial(), 1, configItem.getDurability());
            ItemMeta itemMeta = itemStack.getItemMeta();
            List<String> lore = new ArrayList<>();

            configItem.getLore().forEach(line -> {
                lore.add(Config.translateArena(line, this.arena));
            });

            itemMeta.setDisplayName(Config.translateArena(configItem.getName(), this.arena));
            itemMeta.setLore(lore);
            itemStack.setItemMeta(itemMeta);

            return itemStack;
        }

        @Override
        public void clicked(Player player, int i, ClickType clickType, int hb) {
            PlayerData playerData = PlayerData.getByUuid(player.getUniqueId());

            // Update and send the procedure
            playerData.getDuelProcedure().setArena(this.arena);
            playerData.getDuelProcedure().send();

            // Set closed by menu
            Menu.currentlyOpenedMenus.get(player.getName()).setClosedByMenu(true);

            // Force close inventory
            player.closeInventory();
        }

    }

}
