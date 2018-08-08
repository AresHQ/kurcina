package me.joeleoli.praxi.kit.editor.gui;

import lombok.AllArgsConstructor;

import me.joeleoli.nucleus.menu.Button;
import me.joeleoli.nucleus.menu.Menu;
import me.joeleoli.nucleus.util.CC;
import me.joeleoli.nucleus.util.ItemBuilder;
import me.joeleoli.praxi.kit.NamedKit;
import me.joeleoli.praxi.kit.editor.gui.button.*;
import me.joeleoli.praxi.ladder.Ladder;
import me.joeleoli.praxi.player.PlayerData;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

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
        return CC.GOLD + "Viewing " + this.ladder.getName() + " kits";
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        final Map<Integer, Button> buttons = new HashMap<>();
        final PlayerData playerData = PlayerData.getByUuid(player.getUniqueId());
        NamedKit[] kits = playerData.getKits(this.ladder);

        if (kits == null) {
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
            return new ItemBuilder(Material.STAINED_CLAY)
                    .name(CC.RED + CC.BOLD + "Delete")
                    .durability(14)
                    .lore(Arrays.asList(
                            "",
                            CC.RED + "Click to delete this kit.",
                            CC.RED + "You will " + CC.BOLD + "NOT" + CC.RED + " be able to",
                            CC.RED + "recover this kit."
                    ))
                    .build();
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
            return new ItemBuilder(Material.IRON_SWORD)
                    .name(CC.GREEN + CC.BOLD + "Create Kit")
                    .build();
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
            return new ItemBuilder(Material.SIGN)
                    .name(CC.YELLOW + CC.BOLD + "Rename")
                    .lore(Arrays.asList(
                            "",
                            CC.YELLOW + "Click to rename this kit."
                    ))
                    .build();
        }

        @Override
        public void clicked(Player player, int slot, ClickType clickType, int hotbarSlot) {
            Menu.currentlyOpenedMenus.get(player.getName()).setClosedByMenu(true);

            PlayerData playerData = PlayerData.getByUuid(player.getUniqueId());

            playerData.getKitEditor().setActive(true);
            playerData.getKitEditor().setRename(true);
            playerData.getKitEditor().setSelectedKit(this.kit);
            player.closeInventory();
            player.sendMessage(CC.YELLOW + "Renaming " + CC.BOLD + this.kit.getName() + CC.YELLOW + "... " + CC.GREEN + "Enter the new name now.");
        }

    }

    @AllArgsConstructor
    private class LoadKitButton extends Button {

        private int index;

        @Override
        public ItemStack getButtonItem(Player player) {
            return new ItemBuilder(Material.BOOK)
                    .name(CC.GREEN + CC.BOLD + "Load/Edit")
                    .lore(Arrays.asList(
                            "",
                            CC.YELLOW + "Click to edit this kit."
                    ))
                    .build();
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
            return new ItemBuilder(Material.BOOK)
                    .name(CC.GREEN + CC.BOLD + this.kit.getName())
                    .build();
        }

    }

}
