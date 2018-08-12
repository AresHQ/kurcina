package me.joeleoli.praxi.command;

import me.joeleoli.nucleus.NucleusAPI;
import me.joeleoli.nucleus.command.Command;
import me.joeleoli.nucleus.command.param.Parameter;
import me.joeleoli.nucleus.util.Style;

import me.joeleoli.praxi.player.PlayerState;
import me.joeleoli.praxi.player.PraxiPlayer;
import me.joeleoli.praxi.player.PracticeSetting;

import org.bukkit.entity.Player;

public class SpectateCommands {

    @Command(names = {"spectate", "spec"})
    public static void spectate(Player player, @Parameter(name = "target") Player target) {
        if (NucleusAPI.isFrozen(player)) {
            player.sendMessage(Style.RED + "You cannot spectate while frozen.");
            return;
        }

        PraxiPlayer praxiPlayer = PraxiPlayer.getByUuid(player.getUniqueId());
        PraxiPlayer targetData = PraxiPlayer.getByUuid(target.getUniqueId());

        if (praxiPlayer == null || praxiPlayer.getState() != PlayerState.IN_LOBBY) {
            player.sendMessage(Style.RED + "You must be in the lobby to spectate another player's match.");
            return;
        }

        if (praxiPlayer.getParty() != null) {
            player.sendMessage(Style.RED + "You must leave your party to spectate a match.");
            return;
        }

        if (targetData == null || targetData.getState() != PlayerState.IN_MATCH || targetData.getMatch() == null) {
            player.sendMessage(Style.RED + "That player is not in a match.");
            return;
        }

        if (!NucleusAPI.<Boolean>getSetting(target, PracticeSetting.RECEIVE_DUEL_REQUESTS)) {
            player.sendMessage(Style.RED + "That player is not allowing spectators.");
            return;
        }

        targetData.getMatch().addSpectator(player, target);
    }

    @Command(names = "stopspectate")
    public static void stopSpectate(Player player) {
        PraxiPlayer praxiPlayer = PraxiPlayer.getByUuid(player.getUniqueId());

        if (praxiPlayer == null || praxiPlayer.getState() != PlayerState.SPECTATE_MATCH || praxiPlayer.getMatch() == null) {
            player.sendMessage(Style.RED + "You are not spectating a match.");
            return;
        }

        praxiPlayer.getMatch().removeSpectator(player);
    }

}
