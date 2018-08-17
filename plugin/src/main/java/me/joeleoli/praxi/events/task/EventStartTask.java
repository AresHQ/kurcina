package me.joeleoli.praxi.events.task;

import me.joeleoli.nucleus.cooldown.Cooldown;
import me.joeleoli.praxi.events.Event;
import me.joeleoli.praxi.events.EventState;
import me.joeleoli.praxi.events.EventTask;

public class EventStartTask extends EventTask {

	public EventStartTask(Event event) {
		super(event, EventState.WAITING);
	}

	@Override
	public void onRun() {
		if (this.getEvent().getPlayers().size() == this.getEvent().getMaxPlayers() || (this.getTicks() >= 30 && this.getEvent().getPlayers().size() >= 2)) {
			if (this.getEvent().getCooldown() == null) {
				this.getEvent().setCooldown(new Cooldown(11_000));
			} else {
				if (this.getEvent().getCooldown().hasExpired()) {
					this.getEvent().setState(EventState.ROUND_STARTING);
					this.getEvent().onRound();
					this.getEvent().setEventTask(new EventRoundStartTask(this.getEvent()));
				}
			}
		}

		if (this.getTicks() % 10 == 0) {
			this.getEvent().announce();
		}
	}

}
