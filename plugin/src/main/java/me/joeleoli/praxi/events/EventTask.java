package me.joeleoli.praxi.events;

import me.joeleoli.praxi.Praxi;
import org.bukkit.scheduler.BukkitRunnable;

public class EventTask extends BukkitRunnable {

	private int ticks;
	private Event event;

	public EventTask(Event event) {
		this.event = event;
	}

	@Override
	public void run() {
		this.ticks++;

		if (Praxi.getInstance().getEventManager().getActiveEvent() == null ||
		    !Praxi.getInstance().getEventManager().getActiveEvent().equals(this.event)) {
			this.cancel();
			return;
		}

		if (this.event.getState() == EventState.WAITING) {
			if (this.event.getPlayers().size() == this.event.getMaxPlayers() ||
			    (this.event.getCooldown().hasExpired() && this.event.getPlayers().size() >= 2)) {
				this.event.onRound();
			}

			if (!this.event.getCooldown().hasExpired() && this.ticks % 5 == 0) {
				this.event.announce();
			}
		}
	}

}
