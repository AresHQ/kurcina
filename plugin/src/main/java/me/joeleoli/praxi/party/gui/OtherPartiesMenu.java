package me.joeleoli.praxi.party.gui;

import me.joeleoli.commons.menu.Button;
import me.joeleoli.commons.menu.pagination.PaginatedMenu;

import me.joeleoli.praxi.config.Config;
import me.joeleoli.praxi.config.ConfigKey;
import me.joeleoli.praxi.party.Party;
import me.joeleoli.praxi.party.gui.button.PartyDisplayButton;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class OtherPartiesMenu extends PaginatedMenu {

    @Override
    public String getPrePaginatedTitle(Player player) {
        return Config.getString(ConfigKey.MENU_OTHER_PARTIES_TITLE);
    }

    @Override
    public Map<Integer, Button> getAllPagesButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();

        Party.getParties().forEach(party -> buttons.put(buttons.size(), new PartyDisplayButton(party)));

        return buttons;
    }

}
