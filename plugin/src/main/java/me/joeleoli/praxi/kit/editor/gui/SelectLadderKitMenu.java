package me.joeleoli.praxi.kit.editor.gui;

import lombok.AllArgsConstructor;

import me.joeleoli.nucleus.menu.Button;
import me.joeleoli.nucleus.menu.Menu;

import me.joeleoli.nucleus.util.CC;
import me.joeleoli.nucleus.util.ItemBuilder;
import me.joeleoli.praxi.ladder.Ladder;
import me.joeleoli.praxi.player.PlayerData;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class SelectLadderKitMenu extends Menu {

    @Override
    public String getTitle(Player player) {
        return CC.GOLD + CC.BOLD + "Select a ladder...";
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
            return new ItemBuilder(this.ladder.getDisplayIcon())
                    .name(this.ladder.getDisplayName())
                    .lore(Arrays.asList(
                            "",
                            CC.YELLOW + "Click to select " + CC.BOLD + this.ladder.getName() + CC.YELLOW + "."
                    ))
                    .build();
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
