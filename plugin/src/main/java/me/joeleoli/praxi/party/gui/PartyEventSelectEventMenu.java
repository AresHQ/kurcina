package me.joeleoli.praxi.party.gui;

import lombok.AllArgsConstructor;

import me.joeleoli.nucleus.menu.Button;
import me.joeleoli.nucleus.menu.Menu;
import me.joeleoli.nucleus.util.CC;
import me.joeleoli.nucleus.util.ItemBuilder;
import me.joeleoli.praxi.party.PartyEvent;
import me.joeleoli.praxi.player.PlayerData;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class PartyEventSelectEventMenu extends Menu {

    @Override
    public String getTitle(Player player) {
        return CC.BLUE + CC.BOLD + "Select an event...";
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        final Map<Integer, Button> buttons = new HashMap<>();

        buttons.put(3, new SelectEventButton(PartyEvent.FFA));
        buttons.put(5, new SelectEventButton(PartyEvent.SPLIT));

        return buttons;
    }

    @AllArgsConstructor
    private class SelectEventButton extends Button {

        private PartyEvent partyEvent;

        @Override
        public ItemStack getButtonItem(Player player) {
            return new ItemBuilder(this.partyEvent == PartyEvent.FFA ? Material.QUARTZ : Material.REDSTONE)
                    .name(CC.GREEN + CC.BOLD + this.partyEvent.getName())
                    .lore(Arrays.asList(
                            "",
                            CC.YELLOW + "Click here to select " + CC.GREEN + CC.BOLD + this.partyEvent.getName() + CC.YELLOW + "."
                    ))
                    .build();
        }

        @Override
        public void clicked(Player player, int slot, ClickType clickType, int hotbarSlot) {
            final PlayerData playerData = PlayerData.getByUuid(player.getUniqueId());

            if (playerData.getParty() == null) {
                player.sendMessage(CC.RED + "You are not in a party.");
                return;
            }

            playerData.getParty().setSelectedEvent(this.partyEvent);

            new PartyEventSelectLadderMenu().openMenu(player);
        }

    }

}
