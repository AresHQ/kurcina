package me.joeleoli.praxi.command;

import me.joeleoli.nucleus.Nucleus;
import me.joeleoli.nucleus.command.Command;
import me.joeleoli.nucleus.command.param.Parameter;
import me.joeleoli.nucleus.util.CC;
import me.joeleoli.praxi.arena.Arena;
import me.joeleoli.praxi.duel.DuelRequest;
import me.joeleoli.praxi.match.MatchPlayer;
import me.joeleoli.praxi.match.impl.SoloMatch;
import me.joeleoli.praxi.player.PlayerState;
import me.joeleoli.praxi.duel.DuelProcedure;
import me.joeleoli.praxi.duel.gui.DuelSelectLadderMenu;
import me.joeleoli.praxi.match.Match;
import me.joeleoli.praxi.player.PlayerData;
import me.joeleoli.praxi.player.PracticeSetting;

import org.bukkit.entity.Player;

public class DuelCommands {

    @Command(names = "duel")
    public static void duel(Player player, @Parameter(name = "target") Player target) {
        if (Nucleus.isFrozen(player)) {
            player.sendMessage(CC.RED + "You cannot duel while frozen.");
            return;
        }

        if (Nucleus.isFrozen(target)) {
            player.sendMessage(CC.RED + "You cannot duel a frozen player.");
            return;
        }

        if (player.getUniqueId().equals(target.getUniqueId())) {
            player.sendMessage(CC.RED + "You cannot duel yourself.");
            return;
        }

        final PlayerData playerData = PlayerData.getByUuid(player.getUniqueId());
        final PlayerData targetData = PlayerData.getByUuid(target.getUniqueId());

        if (playerData.getParty() != null) {
            player.sendMessage(CC.RED + "You cannot duel whilst in a party.");
            return;
        }

        if (playerData.isInMatch() || playerData.isInQueue()) {
            player.sendMessage(CC.RED + "You must be in the lobby to send a duel request.");
            return;
        }

        if (targetData.isInMatch() || targetData.isInQueue() || targetData.getParty() != null) {
            player.sendMessage(Nucleus.getColoredName(target) + CC.RED + " is currently busy.");
            return;
        }

        if (!Nucleus.<Boolean>getSetting(target, PracticeSetting.RECEIVE_DUEL_REQUESTS)) {
            player.sendMessage(CC.RED + "That player is not accepting duel requests at the moment.");
            return;
        }

        if (!playerData.canSendDuelRequest(player)) {
            player.sendMessage(CC.RED + "You have already sent that player a duel request.");
            return;
        }

        DuelProcedure procedure = new DuelProcedure();

        procedure.setSender(player);
        procedure.setTarget(target);

        playerData.setDuelProcedure(procedure);

        new DuelSelectLadderMenu().openMenu(player);
    }

    @Command(names = "duel accept")
    public static void accept(Player player, @Parameter(name = "target") Player target) {
        if (Nucleus.isFrozen(player)) {
            player.sendMessage(CC.RED + "You cannot duel while frozen.");
            return;
        }

        if (Nucleus.isFrozen(target)) {
            player.sendMessage(CC.RED + "You cannot duel a frozen player.");
            return;
        }

        final PlayerData playerData = PlayerData.getByUuid(player.getUniqueId());
        final PlayerData targetData = PlayerData.getByUuid(target.getUniqueId());

        if (!targetData.isPendingDuelRequest(player)) {
            player.sendMessage(CC.RED + "You do not have a pending duel request from " + Nucleus.getColoredName(target) + CC.RED + ".");
            return;
        }

        if (playerData.isInMatch() || playerData.isInQueue()) {
            player.sendMessage(CC.RED + "You must be in the lobby to accept a duel.");
            return;
        }

        if (targetData.isInMatch() || targetData.isInQueue()) {
            player.sendMessage(CC.RED + "That player is no longer available.");
            return;
        }

        final DuelRequest request = targetData.getSentDuelRequests().get(player.getUniqueId());

        Arena arena = request.getArena();

        if (arena.isActive()) {
            player.sendMessage(CC.RED + "Tried to start a match but there are no available arenas.");
            return;
        }

        // Update arena
        arena.setActive(true);

        // Create match
        Match match = new SoloMatch(new MatchPlayer(player), new MatchPlayer(target), request.getLadder(), arena, false);

        // Update player's states
        playerData.setState(PlayerState.IN_MATCH);
        playerData.setMatch(match);

        targetData.setState(PlayerState.IN_MATCH);
        targetData.setMatch(match);

        // Start match
        match.handleStart();
    }

}
