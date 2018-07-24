package me.joeleoli.praxi.kit.editor.gui;

import lombok.AllArgsConstructor;

import me.joeleoli.commons.menu.Button;
import me.joeleoli.commons.menu.Menu;
import me.joeleoli.commons.menu.buttons.DisplayButton;
import me.joeleoli.commons.util.ItemUtil;
import me.joeleoli.commons.util.PlayerUtil;
import me.joeleoli.commons.util.TaskUtil;

import me.joeleoli.praxi.config.ConfigItem;
import me.joeleoli.praxi.kit.NamedKit;
import me.joeleoli.praxi.config.Config;
import me.joeleoli.praxi.config.ConfigKey;
import me.joeleoli.praxi.player.PlayerData;
import me.joeleoli.praxi.script.ScriptContext;
import me.joeleoli.praxi.script.wrapper.PlayerWrapper;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class KitEditorMenu extends Menu {

    private static final int[] ITEM_POSITIONS = new int[]{20, 21, 22, 23, 24, 25, 26, 29, 30, 31, 32, 33, 34, 35, 38, 39, 40, 41, 42, 43, 44, 47, 48, 49, 50, 51, 52, 53};
    private static final int[] BORDER_POSITIONS = new int[]{1, 9, 10, 11, 12, 13, 14, 15, 16, 17, 19, 28, 37, 46};
    private static final Button BORDER_BUTTON = Button.placeholder(Material.COAL_BLOCK, (byte) 0, " ");

    public KitEditorMenu() {
        this.setUpdateAfterClick(false);
    }

    @Override
    public String getTitle(Player player) {
        PlayerData playerData = PlayerData.getByUuid(player.getUniqueId());
        return Config.translateKit(Config.getString(ConfigKey.MENU_KIT_EDITOR_TITLE), playerData.getKitEditor().getSelectedKit());
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        PlayerData playerData = PlayerData.getByUuid(player.getUniqueId());
        NamedKit kit = playerData.getKitEditor().getSelectedKit();
        Map<Integer, Button> buttons = new HashMap<>();

        for (int border : BORDER_POSITIONS) {
            buttons.put(border, BORDER_BUTTON);
        }

        buttons.put(0, new CurrentKitButton());
        buttons.put(2, new SaveButton());
        buttons.put(6, new LoadDefaultKitButton());
        buttons.put(7, new ClearInventoryButton());
        buttons.put(8, new CancelButton());
        buttons.put(18, new ArmorDisplayButton(kit.getArmor()[3]));
        buttons.put(27, new ArmorDisplayButton(kit.getArmor()[2]));
        buttons.put(36, new ArmorDisplayButton(kit.getArmor()[1]));
        buttons.put(45, new ArmorDisplayButton(kit.getArmor()[0]));

        List<ItemStack> items = playerData.getKitEditor().getSelectedLadder().getKitEditorItems();

        for (int i = 20; i < (playerData.getKitEditor().getSelectedLadder().getKitEditorItems().size() + 20); i++) {
            buttons.put(ITEM_POSITIONS[i - 20], new InfiniteItemButton(items.get(i - 20)));
        }

        return buttons;
    }

    @Override
    public void onOpen(Player player) {
        if (!this.isClosedByMenu()) {
            PlayerUtil.reset(player);

            PlayerData playerData = PlayerData.getByUuid(player.getUniqueId());
            playerData.getKitEditor().setActive(true);

            if (playerData.getKitEditor().getSelectedKit() != null) {
                player.getInventory().setContents(playerData.getKitEditor().getSelectedKit().getContents());
            }

            player.updateInventory();
        }
    }

    @Override
    public void onClose(Player player) {
        PlayerData playerData = PlayerData.getByUuid(player.getUniqueId());
        playerData.getKitEditor().setActive(false);

        if (!playerData.isInMatch()) {
            TaskUtil.runLater(playerData::loadLayout, 1L);
        }
    }

    @AllArgsConstructor
    private class ArmorDisplayButton extends Button {

        private ItemStack itemStack;

        @Override
        public ItemStack getButtonItem(Player player) {
            if (this.itemStack == null || this.itemStack.getType() == Material.AIR) {
                return new ItemStack(Material.AIR);
            }

            final ConfigItem configItem = Config.getConfigItem(ConfigKey.MENU_KIT_EDITOR_ARMOR_DISPLAY_BUTTON);
            final ItemStack itemStack = this.itemStack;
            final ItemMeta itemMeta = itemStack.getItemMeta();
            final PlayerData playerData = PlayerData.getByUuid(player.getUniqueId());
            final ScriptContext context = new ScriptContext(configItem.getName());

            context.addVariable("item_name", ItemUtil.getName(this.itemStack));
            context.getReplaceables().add(new PlayerWrapper(player));
            context.getReplaceables().add(playerData.getKitEditor().getSelectedLadder());
            context.getReplaceables().add(playerData.getKitEditor().getSelectedKit());

            itemMeta.setDisplayName(context.buildSingleLine());

            context.setLines(configItem.getLore());

            itemMeta.setLore(context.buildMultipleLines());
            itemStack.setItemMeta(itemMeta);

            return itemStack;
        }

    }

    @AllArgsConstructor
    private class CurrentKitButton extends Button {

        @Override
        public ItemStack getButtonItem(Player player) {
            final ConfigItem configItem = Config.getConfigItem(ConfigKey.MENU_KIT_EDITOR_CURRENT_KIT_BUTTON);
            final ItemStack itemStack = new ItemStack(configItem.getMaterial(), 1, configItem.getDurability());
            final ItemMeta itemMeta = itemStack.getItemMeta();
            final PlayerData playerData = PlayerData.getByUuid(player.getUniqueId());
            final ScriptContext context = new ScriptContext(configItem.getName());

            context.getReplaceables().add(new PlayerWrapper(player));
            context.getReplaceables().add(playerData.getKitEditor().getSelectedLadder());
            context.getReplaceables().add(playerData.getKitEditor().getSelectedKit());

            itemMeta.setDisplayName(context.buildSingleLine());

            context.setLines(configItem.getLore());

            itemMeta.setLore(context.buildMultipleLines());
            itemStack.setItemMeta(itemMeta);

            return itemStack;
        }

    }

    @AllArgsConstructor
    private class ClearInventoryButton extends Button {

        @Override
        public ItemStack getButtonItem(Player player) {
            final ConfigItem configItem = Config.getConfigItem(ConfigKey.MENU_KIT_EDITOR_CLEAR_INVENTORY_BUTTON);
            final ItemStack itemStack = new ItemStack(configItem.getMaterial(), 1, configItem.getDurability());
            final ItemMeta itemMeta = itemStack.getItemMeta();
            final PlayerData playerData = PlayerData.getByUuid(player.getUniqueId());
            final ScriptContext context = new ScriptContext(configItem.getName());

            context.getReplaceables().add(new PlayerWrapper(player));
            context.getReplaceables().add(playerData.getKitEditor().getSelectedLadder());
            context.getReplaceables().add(playerData.getKitEditor().getSelectedKit());

            itemMeta.setDisplayName(context.buildSingleLine());

            context.setLines(configItem.getLore());

            itemMeta.setLore(context.buildMultipleLines());
            itemStack.setItemMeta(itemMeta);

            return itemStack;
        }

        @Override
        public void clicked(Player player, int i, ClickType clickType, int hb) {
            Button.playNeutral(player);
            player.getInventory().setContents(new ItemStack[36]);
            player.updateInventory();
        }

        @Override
        public boolean shouldUpdate(Player player, int i, ClickType clickType) {
            return true;
        }

    }

    @AllArgsConstructor
    private class LoadDefaultKitButton extends Button {

        @Override
        public ItemStack getButtonItem(Player player) {
            final ConfigItem configItem = Config.getConfigItem(ConfigKey.MENU_KIT_EDITOR_LOAD_DEFAULT_BUTTON);
            final ItemStack itemStack = new ItemStack(configItem.getMaterial(), 1, configItem.getDurability());
            final ItemMeta itemMeta = itemStack.getItemMeta();
            final PlayerData playerData = PlayerData.getByUuid(player.getUniqueId());
            final ScriptContext context = new ScriptContext(configItem.getName());

            context.getReplaceables().add(new PlayerWrapper(player));
            context.getReplaceables().add(playerData.getKitEditor().getSelectedLadder());
            context.getReplaceables().add(playerData.getKitEditor().getSelectedKit());

            itemMeta.setDisplayName(context.buildSingleLine());

            context.setLines(configItem.getLore());

            itemMeta.setLore(context.buildMultipleLines());
            itemStack.setItemMeta(itemMeta);

            return itemStack;
        }

        @Override
        public void clicked(Player player, int i, ClickType clickType, int hb) {
            Button.playNeutral(player);

            PlayerData playerData = PlayerData.getByUuid(player.getUniqueId());

            player.getInventory().setContents(playerData.getKitEditor().getSelectedLadder().getDefaultKit().getContents());
            player.updateInventory();
        }

        @Override
        public boolean shouldUpdate(Player player, int i, ClickType clickType) {
            return true;
        }

    }

    @AllArgsConstructor
    private class SaveButton extends Button {

        @Override
        public ItemStack getButtonItem(Player player) {
            final ConfigItem configItem = Config.getConfigItem(ConfigKey.MENU_KIT_EDITOR_SAVE_BUTTON);
            final ItemStack itemStack = new ItemStack(configItem.getMaterial(), 1, configItem.getDurability());
            final ItemMeta itemMeta = itemStack.getItemMeta();
            final PlayerData playerData = PlayerData.getByUuid(player.getUniqueId());
            final ScriptContext context = new ScriptContext(configItem.getName());

            context.getReplaceables().add(new PlayerWrapper(player));
            context.getReplaceables().add(playerData.getKitEditor().getSelectedLadder());
            context.getReplaceables().add(playerData.getKitEditor().getSelectedKit());

            itemMeta.setDisplayName(context.buildSingleLine());

            context.setLines(configItem.getLore());

            itemMeta.setLore(context.buildMultipleLines());
            itemStack.setItemMeta(itemMeta);

            return itemStack;
        }

        @Override
        public void clicked(Player player, int i, ClickType clickType, int hb) {
            Button.playNeutral(player);
            player.closeInventory();

            PlayerData playerData = PlayerData.getByUuid(player.getUniqueId());

            if (playerData.getKitEditor().getSelectedKit() != null) {
                playerData.getKitEditor().getSelectedKit().setContents(player.getInventory().getContents());
            }

            playerData.loadLayout();

            new KitManagementMenu(playerData.getKitEditor().getSelectedLadder()).openMenu(player);
        }

    }

    @AllArgsConstructor
    private class CancelButton extends Button {

        @Override
        public ItemStack getButtonItem(Player player) {
            final ConfigItem configItem = Config.getConfigItem(ConfigKey.MENU_KIT_EDITOR_CANCEL_BUTTON);
            final ItemStack itemStack = new ItemStack(configItem.getMaterial(), 1, configItem.getDurability());
            final ItemMeta itemMeta = itemStack.getItemMeta();
            final PlayerData playerData = PlayerData.getByUuid(player.getUniqueId());
            final ScriptContext context = new ScriptContext(configItem.getName());

            context.getReplaceables().add(new PlayerWrapper(player));
            context.getReplaceables().add(playerData.getKitEditor().getSelectedLadder());
            context.getReplaceables().add(playerData.getKitEditor().getSelectedKit());

            itemMeta.setDisplayName(context.buildSingleLine());

            context.setLines(configItem.getLore());

            itemMeta.setLore(context.buildMultipleLines());
            itemStack.setItemMeta(itemMeta);

            return itemStack;
        }

        @Override
        public void clicked(Player player, int i, ClickType clickType, int hb) {
            Button.playNeutral(player);

            PlayerData playerData = PlayerData.getByUuid(player.getUniqueId());

            if (playerData.getKitEditor().getSelectedLadder() != null) {
                new KitManagementMenu(playerData.getKitEditor().getSelectedLadder()).openMenu(player);
            }
        }

    }

    private class InfiniteItemButton extends DisplayButton {

        InfiniteItemButton(ItemStack itemStack) {
            super(itemStack, false);
        }

        @Override
        public void clicked(Player player, int i, ClickType clickType, int hb) {
            final Inventory inventory = player.getOpenInventory().getTopInventory();
            final ItemStack itemStack = inventory.getItem(i);

            inventory.setItem(i, itemStack);

            player.setItemOnCursor(itemStack);
            player.updateInventory();
        }

    }

}
