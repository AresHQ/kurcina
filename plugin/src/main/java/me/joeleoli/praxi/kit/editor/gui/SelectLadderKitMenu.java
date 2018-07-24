package me.joeleoli.praxi.kit.editor.gui;

import lombok.AllArgsConstructor;

import me.joeleoli.commons.menu.Button;
import me.joeleoli.commons.menu.Menu;

import me.joeleoli.praxi.config.Config;
import me.joeleoli.praxi.config.ConfigItem;
import me.joeleoli.praxi.config.ConfigKey;
import me.joeleoli.praxi.ladder.Ladder;
import me.joeleoli.praxi.player.PlayerData;
import me.joeleoli.praxi.script.ScriptContext;

import me.joeleoli.praxi.script.wrapper.PlayerWrapper;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;

public class SelectLadderKitMenu extends Menu {

    @Override
    public String getTitle(Player player) {
        return Config.getString(ConfigKey.MENU_SELECT_LADDER_KIT_TITLE);
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();

        Ladder.getLadders().forEach(ladder -> {
            if (ladder.isEnabled()) {
                buttons.put(buttons.size(), new LadderKitDisplayButton(ladder));
            }
        });

        return buttons;
    }

    @AllArgsConstructor
    private class LadderKitDisplayButton extends Button {

        private Ladder ladder;

        @Override
        public ItemStack getButtonItem(Player player) {
            final ConfigItem configItem = Config.getConfigItem(ConfigKey.MENU_SELECT_LADDER_KIT_DISPLAY_BUTTON);
            final ItemStack itemStack = this.ladder.getDisplayIcon();
            final ItemMeta itemMeta = itemStack.getItemMeta();
            final ScriptContext context = new ScriptContext(configItem.getName());

            context.getReplaceables().add(new PlayerWrapper(player));
            context.getReplaceables().add(this.ladder);
            context.buildComponents();

            itemMeta.setDisplayName(context.buildSingleLine());

            context.setLines(configItem.getLore());
            context.buildComponents();

            itemMeta.setLore(context.getLines());
            itemStack.setItemMeta(itemMeta);

            return itemStack;
        }

        @Override
        public void clicked(Player player, int slot, ClickType clickType, int hotbarSlot) {
            player.closeInventory();

            PlayerData playerData = PlayerData.getByUuid(player.getUniqueId());

            playerData.getKitEditor().setSelectedLadder(this.ladder);
            playerData.getKitEditor().setPreviousState(playerData.getState());

            new KitManagementMenu(this.ladder).openMenu(player);
        }

    }
}
