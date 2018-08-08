package me.joeleoli.praxi.duel.gui;

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

import java.util.*;

public class DuelSelectLadderMenu extends Menu {

    @Override
    public String getTitle(Player player) {
        return CC.GOLD + CC.BOLD + "Select a ladder...";
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
            return new ItemBuilder(this.ladder.getDisplayIcon())
                    .name(this.ladder.getDisplayName())
                    .lore(Arrays.asList(
                            "",
                            CC.YELLOW + "Click here to select " + CC.BOLD + this.ladder.getDisplayName() + CC.YELLOW + "."
                    ))
                    .build();
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
            new DuelSelectArenaMenu().openMenu(player);
        }

    }

}