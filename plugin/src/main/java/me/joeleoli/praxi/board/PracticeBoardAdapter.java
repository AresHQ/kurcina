package me.joeleoli.praxi.board;

import me.joeleoli.nucleus.NucleusAPI;
import me.joeleoli.nucleus.board.Board;
import me.joeleoli.nucleus.board.BoardAdapter;
import me.joeleoli.nucleus.util.Style;
import me.joeleoli.nucleus.util.TimeUtil;

import me.joeleoli.praxi.Praxi;
import me.joeleoli.praxi.match.Match;
import me.joeleoli.praxi.player.PraxiPlayer;
import me.joeleoli.praxi.player.PlayerState;
import me.joeleoli.praxi.player.PracticeSetting;
import me.joeleoli.praxi.queue.Queue;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;

import java.util.*;

public class PracticeBoardAdapter implements BoardAdapter {

    @Override
    public String getTitle(Player player) {
        return Style.PINK + Style.BOLD + "MineXD   ";
    }

    @Override
    public List<String> getScoreboard(Player player, Board board) {
        final PraxiPlayer praxiPlayer = PraxiPlayer.getByUuid(player.getUniqueId());

        if (!NucleusAPI.<Boolean>getSetting(player, PracticeSetting.SHOW_SCOREBOARD)) {
            return null;
        }

        final List<String> toReturn = new ArrayList<>();

        if (praxiPlayer.getState() == PlayerState.IN_LOBBY) {
            toReturn.add("Online: " + Style.PINK + Bukkit.getOnlinePlayers().size());
            toReturn.add("Fighting: " + Style.PINK + Praxi.getInstance().getFightingCount());
            toReturn.add("Queueing: " + Style.PINK + Praxi.getInstance().getQueueingCount());

            if (praxiPlayer.getParty() != null) {
                toReturn.add("Your Party: " + Style.PINK + praxiPlayer.getParty().getTeamPlayers().size());
            }
        } else if (praxiPlayer.isInQueue()) {
            final Queue queue = praxiPlayer.getQueuePlayer().getQueue();

            toReturn.add("Queue:");
            toReturn.add(" " + Style.PINK + (queue.isRanked() ? "Ranked" : "Unranked") + " " + queue.getLadder().getName());
            toReturn.add("Time:");
            toReturn.add(" " + Style.PINK + TimeUtil.millisToTimer(praxiPlayer.getQueuePlayer().getPassed()));

            if (queue.isRanked()) {
                toReturn.add("Range:");
                toReturn.add(" " + Style.PINK + praxiPlayer.getQueuePlayer().getMinRange() + " -> " + praxiPlayer.getQueuePlayer().getMaxRange());
            }
        } else if (praxiPlayer.isInMatch()) {
            final Match match = praxiPlayer.getMatch();

            if (match == null) {
                return null;
            }

            if (match.isSoloMatch()) {
                toReturn.add("Opponent: " + Style.PINK + match.getOpponentMatchPlayer(player).getName());
                toReturn.add("Duration: " + Style.PINK + match.getDuration());

                if (match.isFighting()) {
                    toReturn.add("");
                    toReturn.add("Your Ping: " + Style.PINK + player.getPing() + "ms");
                    toReturn.add("Their Ping: " + Style.PINK + match.getOpponentPlayer(player).getPing() + "ms");
                }
            } else if (match.isTeamMatch()) {
                toReturn.add("Duration: " + Style.PINK + match.getDuration());
                toReturn.add("Opponents: " + Style.PINK + match.getOpponentsLeft(player) + "/" + match.getOpponentTeam(player).getTeamPlayers().size());

                if (match.getTeam(player).getTeamPlayers().size() >= 8) {
                    toReturn.add("Your Team: " + Style.PINK + match.getTeam(player).getTeamPlayers().size());
                } else {
                    toReturn.add("");
                    toReturn.add("Your Team:");

                    match.getTeam(player).getTeamPlayers().forEach(teamPlayer -> {
                        toReturn.add(" " + (teamPlayer.isDisconnected() || !teamPlayer.isAlive() ? Style.STRIKE_THROUGH : "") + teamPlayer.getName());
                    });
                }
            }
        } else if (praxiPlayer.isSpectating()) {
            final Match match = praxiPlayer.getMatch();

            if (match == null) {
                return null;
            }

            toReturn.add("Ladder: " + Style.PINK + match.getLadder().getName());
            toReturn.add("Duration: " + Style.PINK + match.getDuration());
            toReturn.add("");

            if (match.isSoloMatch()) {
                toReturn.add(" " + match.getPlayerA().getName());
                toReturn.add(" " + match.getPlayerB().getName());
            } else {
                toReturn.add(" " + match.getTeamA().getLeader().getName() + "'s Team");
                toReturn.add(" " + match.getTeamA().getLeader().getName() + "'s Team");
            }
        }

        toReturn.add(0, Style.BORDER_LINE_SCOREBOARD);
        toReturn.add("");
        toReturn.add(Style.PINK + "www.minexd.com");
        toReturn.add(Style.BORDER_LINE_SCOREBOARD);

        return toReturn;
    }

    @Override
    public long getInterval() {
        return 2L;
    }

    @Override
    public void preLoop() {}

    @Override
    public void onScoreboardCreate(Player player, Scoreboard scoreboard) {}

}
