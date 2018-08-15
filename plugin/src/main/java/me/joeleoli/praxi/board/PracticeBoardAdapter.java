package me.joeleoli.praxi.board;

import java.util.ArrayList;
import java.util.List;
import me.joeleoli.nucleus.NucleusAPI;
import me.joeleoli.nucleus.board.Board;
import me.joeleoli.nucleus.board.BoardAdapter;
import me.joeleoli.nucleus.util.Style;
import me.joeleoli.nucleus.util.TimeUtil;
import me.joeleoli.praxi.Praxi;
import me.joeleoli.praxi.match.Match;
import me.joeleoli.praxi.player.PlayerState;
import me.joeleoli.praxi.player.PracticeSetting;
import me.joeleoli.praxi.player.PraxiPlayer;
import me.joeleoli.praxi.queue.Queue;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;

public class PracticeBoardAdapter implements BoardAdapter {

	@Override
	public String getTitle(Player player) {
		return Style.SECONDARY + Style.BOLD + "MineXD   ";
	}

	@Override
	public List<String> getScoreboard(Player player, Board board) {
		final PraxiPlayer praxiPlayer = PraxiPlayer.getByUuid(player.getUniqueId());

		if (!NucleusAPI.<Boolean>getSetting(player, PracticeSetting.SHOW_SCOREBOARD)) {
			return null;
		}

		final List<String> toReturn = new ArrayList<>();

		if (praxiPlayer.getState() == PlayerState.IN_LOBBY) {
			toReturn.add("Online: " + Style.SECONDARY + Bukkit.getOnlinePlayers().size());
			toReturn.add("Fighting: " + Style.SECONDARY + Praxi.getInstance().getFightingCount());
			toReturn.add("Queueing: " + Style.SECONDARY + Praxi.getInstance().getQueueingCount());

			if (praxiPlayer.getParty() != null) {
				toReturn.add("Your Party: " + Style.SECONDARY + praxiPlayer.getParty().getTeamPlayers().size());
			}
		} else if (praxiPlayer.isInQueue()) {
			final Queue queue = praxiPlayer.getQueuePlayer().getQueue();

			toReturn.add("Queue:");
			toReturn.add(" " + Style.SECONDARY + (queue.isRanked() ? "Ranked" : "Unranked") + " " +
			             queue.getLadder().getName());
			toReturn.add("Time:");
			toReturn.add(" " + Style.SECONDARY + TimeUtil.millisToTimer(praxiPlayer.getQueuePlayer().getPassed()));

			if (queue.isRanked()) {
				toReturn.add("Range:");
				toReturn.add(" " + Style.SECONDARY + praxiPlayer.getQueuePlayer().getMinRange() + " -> " +
				             praxiPlayer.getQueuePlayer().getMaxRange());
			}
		} else if (praxiPlayer.isInMatch()) {
			final Match match = praxiPlayer.getMatch();

			if (match == null) {
				return null;
			}

			if (match.isSoloMatch()) {
				toReturn.add("Opponent: " + Style.SECONDARY + match.getOpponentMatchPlayer(player).getName());
				toReturn.add("Duration: " + Style.SECONDARY + match.getDuration());

				if (match.isFighting()) {
					toReturn.add("");
					toReturn.add("Your Ping: " + Style.SECONDARY + player.getPing() + "ms");
					toReturn.add("Their Ping: " + Style.SECONDARY + match.getOpponentPlayer(player).getPing() + "ms");
				}
			} else if (match.isTeamMatch()) {
				toReturn.add("Duration: " + Style.SECONDARY + match.getDuration());
				toReturn.add("Opponents: " + Style.SECONDARY + match.getOpponentsLeft(player) + "/" +
				             match.getOpponentTeam(player).getTeamPlayers().size());

				if (match.getTeam(player).getTeamPlayers().size() >= 8) {
					toReturn.add("Your Team: " + Style.SECONDARY + match.getTeam(player).getTeamPlayers().size());
				} else {
					toReturn.add("");
					toReturn.add("Your Team:");

					match.getTeam(player).getTeamPlayers().forEach(teamPlayer -> {
						toReturn.add(" " + (teamPlayer.isDisconnected() || !teamPlayer.isAlive() ? Style.STRIKE_THROUGH
								: "") + teamPlayer.getName());
					});
				}
			}
		} else if (praxiPlayer.isSpectating()) {
			final Match match = praxiPlayer.getMatch();

			if (match == null) {
				return null;
			}

			toReturn.add("Ladder: " + Style.SECONDARY + match.getLadder().getName());
			toReturn.add("Duration: " + Style.SECONDARY + match.getDuration());
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
		toReturn.add(Style.SECONDARY + "minexd.com");
		toReturn.add(Style.BORDER_LINE_SCOREBOARD);

		return toReturn;
	}

	@Override
	public long getInterval() {
		return 2L;
	}

	@Override
	public void preLoop() {
	}

	@Override
	public void onScoreboardCreate(Player player, Scoreboard scoreboard) {
	}

}
