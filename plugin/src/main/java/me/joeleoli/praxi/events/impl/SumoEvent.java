package me.joeleoli.praxi.events.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;
import me.joeleoli.nucleus.player.PlayerInfo;
import me.joeleoli.nucleus.util.PlayerUtil;
import me.joeleoli.nucleus.util.TimeUtil;
import me.joeleoli.praxi.Praxi;
import me.joeleoli.praxi.events.Event;
import me.joeleoli.praxi.events.EventPlayer;
import me.joeleoli.praxi.events.EventPlayerState;
import me.joeleoli.praxi.events.EventState;
import me.joeleoli.praxi.events.task.EventRoundStartTask;
import org.bukkit.entity.Player;

@Getter
public class SumoEvent extends Event {

	private Map<UUID, Integer> roundWins = new HashMap<>();
	private EventPlayer roundPlayerA;
	private EventPlayer roundPlayerB;
	@Setter
	private long roundStart;

	public SumoEvent(Player player) {
		super("Sumo", new PlayerInfo(player), 64);
	}

	@Override
	public boolean isSumo() {
		return true;
	}

	@Override
	public boolean isCorners() {
		return false;
	}

	@Override
	public void onJoin(Player player) {
		this.roundWins.put(player.getUniqueId(), 0);
	}

	@Override
	public void onLeave(Player player) {
		this.roundWins.remove(player.getUniqueId());
	}

	@Override
	public void onRound() {
		this.setState(EventState.ROUND_STARTING);
		this.setEventTask(new EventRoundStartTask(this));

		this.roundPlayerA = this.findRoundPlayer();
		this.roundPlayerB = this.findRoundPlayer();

		final Player playerA = this.roundPlayerA.toPlayer();
		final Player playerB = this.roundPlayerB.toPlayer();

		PlayerUtil.reset(playerA);
		PlayerUtil.reset(playerB);

		PlayerUtil.denyMovement(playerA);
		PlayerUtil.denyMovement(playerB);

		playerA.teleport(Praxi.getInstance().getEventManager().getSumoSpawn1());
		playerB.teleport(Praxi.getInstance().getEventManager().getSumoSpawn2());
	}

	@Override
	public String getRoundDuration() {
		if (this.getState() == EventState.ROUND_STARTING) {
			return "00:00";
		} else if (this.getState() == EventState.ROUND_FIGHTING) {
			return TimeUtil.millisToTimer(System.currentTimeMillis() - this.roundStart);
		} else {
			return "Ending";
		}
	}

	private EventPlayer findRoundPlayer() {
		EventPlayer eventPlayer = null;

		for (EventPlayer check : this.getEventPlayers().values()) {
			if (check.getState() == EventPlayerState.WAITING) {
				int roundWins = this.roundWins.get(check.getUuid());

				if (eventPlayer == null) {
					eventPlayer = check;
				}

				if (roundWins == 0) {
					break;
				}

				if (roundWins <= this.roundWins.get(eventPlayer.getUuid())) {
					if (!check.getUuid().equals(this.roundPlayerA.getUuid()) && !check.getUuid().equals(this.roundPlayerB.getUuid())) {
						eventPlayer = check;
					}
				}
			}
		}

		if (eventPlayer == null) {
			throw new RuntimeException("Could not find a new round player");
		}

		return eventPlayer;
	}

}
