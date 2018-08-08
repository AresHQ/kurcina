package me.joeleoli.praxi.party.gui;

import me.joeleoli.nucleus.menu.Button;
import me.joeleoli.nucleus.menu.pagination.PaginatedMenu;
import me.joeleoli.nucleus.util.CC;
import me.joeleoli.praxi.party.Party;
import me.joeleoli.praxi.party.gui.button.PartyDisplayButton;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class OtherPartiesMenu extends PaginatedMenu {

    @Override
    public String getPrePaginatedTitle(Player player) {
        return CC.GOLD + "Other Parties";
    }

    @Override
    public Map<Integer, Button> getAllPagesButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();

        Party.getParties().forEach(party -> {
            buttons.put(buttons.size(), new PartyDisplayButton(party));
        });

        return buttons;
    }

}
