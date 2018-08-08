package me.joeleoli.praxi.board;

import me.joeleoli.nucleus.Nucleus;
import me.joeleoli.nucleus.board.Board;
import me.joeleoli.nucleus.board.BoardAdapter;
import me.joeleoli.nucleus.util.CC;
import me.joeleoli.nucleus.util.TimeUtil;

import me.joeleoli.praxi.Praxi;
import me.joeleoli.praxi.match.Match;
import me.joeleoli.praxi.player.PlayerData;
import me.joeleoli.praxi.player.PlayerState;
import me.joeleoli.praxi.player.PracticeSetting;
import me.joeleoli.praxi.queue.Queue;

import org.apache.commons.lang.StringEscapeUtils;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;

import java.util.*;

public class PracticeBoardAdapter implements BoardAdapter {

    private static final String TITLE_PREFIX = CC.GOLD + CC.BOLD;
    private static final String ONLINE = CC.YELLOW + "Online" + CC.GRAY + ": " + CC.RESET;
    private static final String IN_FIGHTS = CC.PINK + "In Fights" + CC.GRAY + ": " + CC.RESET;
    private static final String IN_QUEUES = CC.AQUA + "In Queues" + CC.GRAY + ": " + CC.RESET;

    @Override
    public String getTitle(Player player) {
        return TITLE_PREFIX + Nucleus.getInstance().getServerName() + " " + CC.GRAY + StringEscapeUtils.unescapeJava("\u2503") + " " + CC.RESET + "Practice";
    }

    @Override
    public List<String> getScoreboard(Player player, Board board) {
        final PlayerData playerData = PlayerData.getByUuid(player.getUniqueId());

        if (!Nucleus.<Boolean>getSetting(player, PracticeSetting.SHOW_SCOREBOARD)) {
            return null;
        }

        final List<String> toReturn = new ArrayList<>();

        if (playerData.getState() == PlayerState.IN_LOBBY) {
            toReturn.add(ONLINE + Bukkit.getOnlinePlayers().size());
            toReturn.add(IN_FIGHTS + Praxi.getInstance().getFightingCount());
            toReturn.add(IN_QUEUES + Praxi.getInstance().getQueueingCount());

            if (playerData.getParty() != null) {
                toReturn.add(CC.BLUE + "Your Party: " + CC.RESET + playerData.getParty().getTeamPlayers().size());
            }
        } else if (playerData.isInQueue()) {
            final Queue queue = playerData.getQueuePlayer().getQueue();

            toReturn.add(ONLINE + Bukkit.getOnlinePlayers().size());
            toReturn.add(IN_FIGHTS + Praxi.getInstance().getFightingCount());
            toReturn.add(IN_QUEUES + Praxi.getInstance().getQueueingCount());

            if (playerData.getParty() != null) {
                toReturn.add(CC.BLUE + "Your Party" + CC.GRAY + ": " + CC.RESET + playerData.getParty().getTeamPlayers().size());
            }

            toReturn.add(CC.SCOREBAORD_SEPARATOR);
            toReturn.add(CC.GOLD + "Queue" + CC.GRAY + ": " + CC.GREEN + (queue.isRanked() ? "Ranked" : "Unranked") + " " + queue.getLadder().getName());
            toReturn.add(CC.GOLD + "Time" + CC.GRAY + ": " + CC.RESET + TimeUtil.formatTime(playerData.getQueuePlayer().getPassed()));

            if (queue.isRanked()) {
                toReturn.add(CC.GOLD + "Search range" + CC.GRAY + ": " + CC.RESET + playerData.getQueuePlayer().getMinRange() + " -> " + playerData.getQueuePlayer().getMaxRange());
            }
        } else if (playerData.isInMatch()) {
            final Match match = playerData.getMatch();

            if (match == null) {
                return null;
            }

            if (match.isSoloMatch()) {
                toReturn.add(CC.RED + CC.BOLD + "Opponent" + CC.GRAY + ": " + CC.RESET + match.getOpponentMatchPlayer(player).getName());
            } else {
                toReturn.add(CC.RED + CC.BOLD + "Opponents" + CC.GRAY + ": " + CC.RESET + match.getOpponentsLeft(player) + "/" + match.getOpponentTeam(player).getTeamPlayers().size());

                if (match.getTeam(player).getTeamPlayers().size() >= 8) {
                    toReturn.add(CC.GREEN + CC.BOLD + "Your Team" + CC.GRAY + ": " + CC.RESET + match.getTeam(player).getTeamPlayers().size());
                }
            }

            toReturn.add(CC.GOLD + CC.BOLD + "Duration" + CC.GRAY + ": " + CC.RESET + (match.isStarting() ? "00:00" : (match.isEnding() ? "Ending" : TimeUtil.formatTime(match.getElapsedDuration()))));

            if (match.isSoloMatch() && match.isFighting()) {
                toReturn.add("");
                toReturn.add(CC.YELLOW + "Your Ping: " + CC.RESET + player.getPing() + "ms");
                toReturn.add(CC.YELLOW + "Their Ping: " + CC.RESET + match.getOpponentPlayer(player).getPing() + "ms");
            }

            if (match.isTeamMatch()) {
                if (match.getTeam(player).getTeamPlayers().size() < 8) {
                    toReturn.add("");
                    toReturn.add(CC.GREEN + CC.BOLD + "Your Team" + CC.GRAY + ":");

                    match.getTeam(player).getTeamPlayers().forEach(teamPlayer -> {
                        toReturn.add(" " + (teamPlayer.isDisconnected() || !teamPlayer.isAlive() ? CC.GRAY + CC.STRIKE_THROUGH : "") + teamPlayer.getName());
                    });
                }
            }
        } else if (playerData.isSpectating()) {
            final Match match = playerData.getMatch();

            if (match == null) {
                return null;
            }

            toReturn.add(CC.GOLD + CC.BOLD + "Ladder" + CC.GRAY + ": " + CC.GREEN + match.getLadder().getName());
            toReturn.add(CC.GOLD + CC.BOLD + "Duration" + CC.GRAY + ": " + CC.RESET + match.getDuration());
            toReturn.add("");

            if (match.isSoloMatch()) {
                toReturn.add(" " + CC.AQUA + match.getPlayerA().getName());
                toReturn.add(" " + CC.PINK + match.getPlayerB().getName());
            } else {
                toReturn.add(" " + CC.AQUA + match.getTeamA().getLeader().getName() + "'s Team");
                toReturn.add(" " + CC.PINK + match.getTeamA().getLeader().getName() + "'s Team");
            }
        }

        toReturn.add(0, CC.SCOREBAORD_SEPARATOR);
        toReturn.add(CC.SCOREBAORD_SEPARATOR);

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
