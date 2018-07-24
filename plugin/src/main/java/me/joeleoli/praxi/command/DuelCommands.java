package me.joeleoli.praxi.command;

import me.joeleoli.commons.command.Command;
import me.joeleoli.commons.command.param.Parameter;

import me.joeleoli.praxi.arena.Arena;
import me.joeleoli.praxi.duel.DuelRequest;
import me.joeleoli.praxi.match.MatchPlayer;
import me.joeleoli.praxi.match.MatchType;
import me.joeleoli.praxi.player.PlayerState;
import me.joeleoli.praxi.config.Config;
import me.joeleoli.praxi.config.ConfigKey;
import me.joeleoli.praxi.duel.DuelProcedure;
import me.joeleoli.praxi.duel.gui.DuelLadderMenu;
import me.joeleoli.praxi.match.Match;
import me.joeleoli.praxi.player.PlayerData;
import me.joeleoli.praxi.team.Team;

import org.bukkit.entity.Player;

import java.util.Iterator;

public class DuelCommands {

    @Command(names = "duel")
    public static void duel(Player player, @Parameter(name = "target") Player target) {
        if (player.getUniqueId().equals(target.getUniqueId())) {
            player.sendMessage(Config.getString(ConfigKey.DUEL_INVITE_SELF));
            return;
        }

        PlayerData playerData = PlayerData.getByUuid(player.getUniqueId());

        if (playerData.isInMatch() || playerData.isInQueue()) {
            player.sendMessage(Config.getString(ConfigKey.DUEL_REJECTED_STATE));
            return;
        }

        PlayerData targetData = PlayerData.getByUuid(target.getUniqueId());

        if (!targetData.getPlayerSettings().isReceiveDuelRequests()) {
            player.sendMessage(Config.translatePlayerAndTarget(Config.getString(ConfigKey.DUEL_REJECTED_TARGET_DISABLED), player, target));
            return;
        }

        for (DuelRequest duelRequest : targetData.getDuelRequests()) {
            if (duelRequest.getSender().equals(player.getUniqueId())) {
                player.sendMessage(Config.translatePlayerAndTarget(Config.getString(ConfigKey.DUEL_ALREADY_SENT), player, target));
                return;
            }
        }

        DuelProcedure procedure = new DuelProcedure();

        procedure.setSender(player);
        procedure.setTarget(target);

        playerData.setDuelProcedure(procedure);

        new DuelLadderMenu().openMenu(player);
    }

    @Command(names = "duel accept")
    public static void accept(Player player, @Parameter(name = "target") Player target) {
        PlayerData playerData = PlayerData.getByUuid(player.getUniqueId());
        Iterator<DuelRequest> requestIterator = playerData.getDuelRequests().iterator();

        while (requestIterator.hasNext()) {
            DuelRequest request = requestIterator.next();

            if (request.getSender().equals(target.getUniqueId())) {
                if (System.currentTimeMillis() - request.getTimestamp() >= 30_000) {
                    requestIterator.remove();
                    player.sendMessage(Config.translatePlayerAndTarget(Config.getString(ConfigKey.DUEL_INVITE_EXPIRED), player, target));
                } else {
                    PlayerData targetData = PlayerData.getByUuid(target.getUniqueId());

                    if (playerData.isInMatch() || playerData.isInQueue()) {
                        player.sendMessage(Config.translatePlayerAndTarget(Config.getString(ConfigKey.DUEL_REJECTED_STATE), player, target));
                        return;
                    }

                    if (targetData.isInMatch() || targetData.isInQueue()) {
                        player.sendMessage(Config.translatePlayerAndTarget(Config.getString(ConfigKey.DUEL_REJECTED_TARGET_STATE), player, target));
                        return;
                    }

                    Arena arena = request.getArena();

                    if (arena.isActive()) {
                        for (Arena other : Arena.getArenas()) {
                            if (!other.isActive() && other.isSetup() && other.getType() == arena.getType()) {
                                arena = other;
                            }
                        }
                    }

                    if (arena.isActive()) {
                        return;
                    }

                    // Update arena
                    arena.setActive(true);

                    // Create match
                    Match match = new Match(MatchType.ONE_VS_ONE, request.getLadder(), arena, false);

                    match.setTeamA(new Team<>(new MatchPlayer(player)));
                    match.setTeamB(new Team<>(new MatchPlayer(player)));

                    // Update player's states
                    playerData.setState(PlayerState.IN_FIGHT);
                    playerData.setQueuePlayer(null);
                    playerData.setMatch(match);

                    targetData.setState(PlayerState.IN_FIGHT);
                    targetData.setQueuePlayer(null);
                    targetData.setMatch(match);

                    // Start match
                    match.start();
                }

                return;
            }
        }

        player.sendMessage(Config.translatePlayerAndTarget(Config.getString(ConfigKey.DUEL_NOT_INVITED), player, target));
    }

}
