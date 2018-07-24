package me.joeleoli.praxi.kit.editor.gui;

import lombok.AllArgsConstructor;

import me.joeleoli.commons.menu.Button;
import me.joeleoli.commons.menu.Menu;

import me.joeleoli.praxi.config.ConfigItem;
import me.joeleoli.praxi.kit.NamedKit;
import me.joeleoli.praxi.config.Config;
import me.joeleoli.praxi.config.ConfigKey;
import me.joeleoli.praxi.kit.editor.gui.button.*;
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

public class KitManagementMenu extends Menu {

    private static final Button PLACEHOLDER = Button.placeholder(Material.STAINED_GLASS_PANE, (byte) 7, " ");

    private Ladder ladder;

    public KitManagementMenu(Ladder ladder) {
        this.ladder = ladder;

        this.setPlaceholder(true);
        this.setUpdateAfterClick(false);
    }

    @Override
    public String getTitle(Player player) {
        return Config.getString(ConfigKey.MENU_KIT_MANAGEMENT_TITLE, this.ladder);
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        final Map<Integer, Button> buttons = new HashMap<>();
        final PlayerData playerData = PlayerData.getByUuid(player.getUniqueId());
        NamedKit[] kits = playerData.getKits(this.ladder);

        if (kits == null) {
            System.out.println("KITS IS NULL");
            return buttons;
        }

        int startPos = -1;

        for (int i = 0; i < 4; i++) {
            NamedKit kit = kits[i];
            startPos += 2;

            buttons.put(startPos, kit == null ? new CreateKitButton(i) : new KitDisplayButton(kit));
            buttons.put(startPos + 18, new LoadKitButton(i));
            buttons.put(startPos + 27, kit == null ? PLACEHOLDER : new RenameKitButton(kit));
            buttons.put(startPos + 36, kit == null ? PLACEHOLDER : new DeleteKitButton(kit));
        }

        buttons.put(36, new BackButton(new SelectLadderKitMenu()));

        return buttons;
    }

    @Override
    public void onClose(Player player) {
        if (!this.isClosedByMenu()) {
            PlayerData playerData = PlayerData.getByUuid(player.getUniqueId());

            playerData.setState(playerData.getKitEditor().getPreviousState());
            playerData.getKitEditor().setSelectedLadder(null);
        }
    }

    @AllArgsConstructor
    private class DeleteKitButton extends Button {

        private NamedKit kit;

        @Override
        public ItemStack getButtonItem(Player player) {
            ConfigItem configItem = Config.getConfigItem(ConfigKey.MENU_KIT_MANAGEMENT_DELETE_KIT_BUTTON);
            ItemStack itemStack = new ItemStack(configItem.getMaterial(), 1, configItem.getDurability());
            ItemMeta itemMeta = itemStack.getItemMeta();

            itemMeta.setDisplayName(configItem.getName());
            itemMeta.setLore(configItem.getLore());
            itemStack.setItemMeta(itemMeta);

            return itemStack;
        }

        @Override
        public void clicked(Player player, int slot, ClickType clickType, int hotbarSlot) {
            PlayerData playerData = PlayerData.getByUuid(player.getUniqueId());

            playerData.deleteKit(playerData.getKitEditor().getSelectedLadder(), this.kit);

            new KitManagementMenu(playerData.getKitEditor().getSelectedLadder()).openMenu(player);
        }

    }

    @AllArgsConstructor
    private class CreateKitButton extends Button {

        private int index;

        @Override
        public ItemStack getButtonItem(Player player) {
            final ConfigItem configItem = Config.getConfigItem(ConfigKey.MENU_KIT_MANAGEMENT_CREATE_KIT_BUTTON);
            final ItemStack itemStack = new ItemStack(configItem.getMaterial(), 1, configItem.getDurability());
            final ItemMeta itemMeta = itemStack.getItemMeta();

            itemMeta.setDisplayName(configItem.getName());
            itemMeta.setLore(configItem.getLore());
            itemStack.setItemMeta(itemMeta);

            return itemStack;
        }

        @Override
        public void clicked(Player player, int slot, ClickType clickType, int hotbarSlot) {
            final PlayerData playerData = PlayerData.getByUuid(player.getUniqueId());
            final Ladder ladder = playerData.getKitEditor().getSelectedLadder();
            final NamedKit kit = new NamedKit("Kit " + (this.index + 1));

            kit.setArmor(ladder.getDefaultKit().getArmor());
            kit.setContents(ladder.getDefaultKit().getContents());

            playerData.replaceKit(ladder, this.index, kit);
            playerData.getKitEditor().setSelectedKit(kit);

            new KitEditorMenu().openMenu(player);
        }

    }

    @AllArgsConstructor
    private class RenameKitButton extends Button {

        private NamedKit kit;

        @Override
        public ItemStack getButtonItem(Player player) {
            ConfigItem configItem = Config.getConfigItem(ConfigKey.MENU_KIT_MANAGEMENT_RENAME_KIT_BUTTON);
            ItemStack itemStack = new ItemStack(configItem.getMaterial(), 1, configItem.getDurability());
            ItemMeta itemMeta = itemStack.getItemMeta();

            itemMeta.setDisplayName(configItem.getName());
            itemMeta.setLore(configItem.getLore());
            itemStack.setItemMeta(itemMeta);

            return itemStack;
        }

        @Override
        public void clicked(Player player, int slot, ClickType clickType, int hotbarSlot) {
            Menu.currentlyOpenedMenus.get(player.getName()).setClosedByMenu(true);

            PlayerData playerData = PlayerData.getByUuid(player.getUniqueId());

            playerData.getKitEditor().setActive(true);
            playerData.getKitEditor().setRename(true);
            playerData.getKitEditor().setSelectedKit(this.kit);
            player.closeInventory();
            player.sendMessage(Config.getString(ConfigKey.KIT_EDITOR_RENAME_KIT_START, this.kit));
        }

    }

    @AllArgsConstructor
    private class LoadKitButton extends Button {

        private int index;

        @Override
        public ItemStack getButtonItem(Player player) {
            ConfigItem configItem = Config.getConfigItem(ConfigKey.MENU_KIT_MANAGEMENT_LOAD_KIT_BUTTON);
            ItemStack itemStack = new ItemStack(configItem.getMaterial(), 1, configItem.getDurability());
            ItemMeta itemMeta = itemStack.getItemMeta();

            itemMeta.setDisplayName(configItem.getName());
            itemMeta.setLore(configItem.getLore());
            itemStack.setItemMeta(itemMeta);

            return itemStack;
        }

        @Override
        public void clicked(Player player, int slot, ClickType clickType, int hotbarSlot) {
            PlayerData playerData = PlayerData.getByUuid(player.getUniqueId());
            NamedKit kit = playerData.getKit(playerData.getKitEditor().getSelectedLadder(), this.index);

            if (kit == null) {
                kit = new NamedKit("Kit " + (this.index + 1));
                kit.setArmor(playerData.getKitEditor().getSelectedLadder().getDefaultKit().getArmor());
                kit.setContents(playerData.getKitEditor().getSelectedLadder().getDefaultKit().getContents());

                playerData.replaceKit(playerData.getKitEditor().getSelectedLadder(), this.index, kit);
            }

            playerData.getKitEditor().setSelectedKit(kit);

            new KitEditorMenu().openMenu(player);
        }

    }

    @AllArgsConstructor
    private class KitDisplayButton extends Button {

        private NamedKit kit;

        @Override
        public ItemStack getButtonItem(Player player) {
            ConfigItem configItem = Config.getConfigItem(ConfigKey.MENU_KIT_MANAGEMENT_KIT_DISPLAY_BUTTON);
            ItemStack itemStack = new ItemStack(configItem.getMaterial(), 1, configItem.getDurability());
            ItemMeta itemMeta = itemStack.getItemMeta();
            List<String> lore = new ArrayList<>();

            configItem.getLore().forEach(line -> {
                lore.add(Config.translateKit(line, this.kit));
            });

            itemMeta.setDisplayName(Config.translateKit(configItem.getName(), this.kit));
            itemMeta.setLore(lore);
            itemStack.setItemMeta(itemMeta);

            return itemStack;
        }

    }

}
