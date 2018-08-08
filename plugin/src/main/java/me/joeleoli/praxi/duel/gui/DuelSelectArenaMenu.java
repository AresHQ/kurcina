package me.joeleoli.praxi.duel.gui;

import lombok.AllArgsConstructor;

import me.joeleoli.nucleus.menu.Button;
import me.joeleoli.nucleus.menu.Menu;
import me.joeleoli.nucleus.util.CC;
import me.joeleoli.nucleus.util.ItemBuilder;

import me.joeleoli.praxi.arena.Arena;
import me.joeleoli.praxi.arena.ArenaType;
import me.joeleoli.praxi.player.PlayerData;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class DuelSelectArenaMenu extends Menu {

    @Override
    public String getTitle(Player player) {
        return CC.BLUE + CC.BOLD + "Select an arena...";
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        final PlayerData playerData = PlayerData.getByUuid(player.getUniqueId());

        Map<Integer, Button> buttons = new HashMap<>();

        for (Arena arena : Arena.getArenas()) {
            if (!arena.isSetup()) {
                continue;
            }

            if (!arena.getLadders().contains(playerData.getDuelProcedure().getLadder().getName())) {
                continue;
            }

            if (playerData.getDuelProcedure().getLadder().isBuild() && arena.getType() == ArenaType.SHARED) {
                continue;
            }

            if (playerData.getDuelProcedure().getLadder().isBuild() && arena.getType() != ArenaType.STANDALONE) {
                continue;
            }

            if (playerData.getDuelProcedure().getLadder().isBuild() && arena.isActive()) {
                continue;
            }

            buttons.put(buttons.size(), new SelectArenaButton(arena));
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
            return new ItemBuilder(Material.PAPER).name(CC.GREEN + CC.BOLD + this.arena.getName()).build();
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
