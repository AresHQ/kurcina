package me.joeleoli.praxi.command;

import me.joeleoli.commons.command.Command;
import me.joeleoli.commons.command.param.Parameter;
import me.joeleoli.commons.util.CC;

import me.joeleoli.praxi.match.MatchSnapshot;
import me.joeleoli.praxi.match.gui.MatchDetailsMenu;

import org.bukkit.entity.Player;

import java.util.UUID;

public class MatchCommands {

    @Command(names = {"viewinventory", "viewinv"})
    public static void viewInventory(Player player, @Parameter(name = "id") String id) {
        MatchSnapshot cachedInventory;

        try {
            cachedInventory = MatchSnapshot.getByUuid(UUID.fromString(id));
        } catch (Exception e) {
            cachedInventory = MatchSnapshot.getByName(id);
        }

        if (cachedInventory == null) {
            player.sendMessage(CC.RED + "Couldn't find an inventory for that ID.");
            return;
        }

        new MatchDetailsMenu(cachedInventory).openMenu(player);
    }

}