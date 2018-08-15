package me.joeleoli.praxi.match;

import me.joeleoli.fairfight.FairFight;
import me.joeleoli.nucleus.util.Style;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class MatchStartTask extends BukkitRunnable {

	private Match match;
	private int tick;

	public MatchStartTask(Match match) {
		this.match = match;
	}

	@Override
	public void run() {
		int seconds = 5 - this.tick;

		if (this.match.isEnding()) {
			this.cancel();
			return;
		}

		if (this.match.getLadder().isSumo()) {
			if (seconds == 2) {
				if (this.match.isSoloMatch()) {
					final Player playerA = this.match.getPlayerA();
					final Player playerB = this.match.getPlayerB();

					if (playerA != null) {
						FairFight.getInstance().getPlayerDataManager().getPlayerData(playerA).setAllowTeleport(false);

						playerA.setWalkSpeed(0.2F);
					}

					if (playerB != null) {
						FairFight.getInstance().getPlayerDataManager().getPlayerData(playerB).setAllowTeleport(false);

						playerB.setWalkSpeed(0.2F);
					}
				} else if (this.match.isTeamMatch()) {
					this.match.getMatchPlayers().forEach(matchPlayer -> {
						if (!matchPlayer.isDisconnected()) {
							final Player player = matchPlayer.toPlayer();

							if (player != null) {
								FairFight.getInstance().getPlayerDataManager().getPlayerData(player)
								         .setAllowTeleport(false);

								player.setWalkSpeed(0.2F);
							}
						}
					});
				}

				this.match.setState(MatchState.FIGHTING);
				this.match.setStartTimestamp(System.currentTimeMillis());
				this.match.broadcast(Style.YELLOW + "The round has started!");
				this.match.broadcast(Sound.NOTE_BASS);
				this.cancel();
				return;
			}

			this.match.broadcast(Style.YELLOW + "The round will start in " + Style.PINK + (seconds - 2) + " second" +
			                     (seconds - 2 == 1 ? "" : "s") + Style.YELLOW + "...");
			this.match.broadcast(Sound.NOTE_PLING);
		} else {
			if (seconds == 0) {
				this.match.setState(MatchState.FIGHTING);
				this.match.setStartTimestamp(System.currentTimeMillis());
				this.match.broadcast(Style.YELLOW + "The match has started!");
				this.match.broadcast(Sound.NOTE_BASS);
				this.cancel();
				return;
			}

			this.match.broadcast(Style.YELLOW + "The match will start in " + Style.PINK + seconds + " second" +
			                     (seconds == 1 ? "" : "s") + Style.YELLOW + "...");
			this.match.broadcast(Sound.NOTE_PLING);
		}

		this.tick++;
	}

}
