package me.joeleoli.praxi.board.processor;

import me.joeleoli.commons.util.DateUtil;

import me.joeleoli.praxi.board.BoardProcessor;
import me.joeleoli.praxi.match.MatchPlayer;
import me.joeleoli.praxi.script.Replaceable;
import me.joeleoli.praxi.script.ScriptContext;
import me.joeleoli.praxi.match.Match;
import me.joeleoli.praxi.player.PlayerData;
import me.joeleoli.praxi.script.wrapper.PlayerInfoWrapper;
import me.joeleoli.praxi.team.Team;

import java.util.ArrayList;
import java.util.List;

public class MatchBoardProcessor implements BoardProcessor {

    @Override
    public boolean canProcess(PlayerData playerData) {
        return playerData.isInMatch();
    }

    @Override
    public List<String> process(PlayerData playerData, List<String> lines) {
        final Match match = playerData.getMatch();
        final Team<MatchPlayer> opponent = match.getOpponent(playerData.toPlayer());
        final ScriptContext context = new ScriptContext(lines);

        context.addCondition("starting", match.isStarting());
        context.addCondition("fighting", match.isFighting());
        context.addCondition("ending", match.isEnding());
        context.addCondition("ranked", match.isRanked());
        context.addCondition("unranked", !match.isRanked());
        context.addCondition("solo", match.isSolo());
        context.addCondition("party", !match.isSolo());
        context.addCondition("enderpearl", playerData.isOnEnderpearlCooldown());
        context.addVariable("opponent_name", opponent.getLeader().getName());
        context.addVariable("opponents_alive_count", opponent.getAliveCount());
        context.addVariable("opponents_dead_count", opponent.getDeadCount());
        context.addVariable("opponents_count", opponent.getTeamPlayers().size());

        if (!match.isSolo()) {
            context.setForLoopEntries(new ArrayList<>());

            for (MatchPlayer opponentPlayer : opponent.getTeamPlayers()) {
                context.getForLoopEntries().add(new PlayerInfoWrapper(opponentPlayer));
            }
        }

        if (match.isFighting()) {
            context.addVariable("match_time", DateUtil.formatGameTime(match.getElapsedDuration()));
        }

        if (playerData.getEnderpearlCooldown() != null) {
            context.addVariable("enderpearl_time", DateUtil.formatGameSeconds(playerData.getEnderpearlCooldown().getRemaining()));
        }

        return context.buildMultipleLines();
    }

}
