package me.joeleoli.praxi.events;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.Getter;
import me.joeleoli.nucleus.chat.ChatComponentBuilder;
import me.joeleoli.nucleus.cooldown.Cooldown;
import me.joeleoli.nucleus.player.PlayerInfo;
import me.joeleoli.nucleus.util.PlayerUtil;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

@Getter
public abstract class Event {

	private String name;
	private EventState state = EventState.WAITING;
	private EventTask runnable;
	private PlayerInfo host;
	private Map<UUID, EventPlayer> eventPlayers = new HashMap<>();
	private int maxPlayers;
	private Cooldown cooldown;

	public Event(String name, PlayerInfo host, int maxPlayers) {
		this.name = name;
		this.host = host;
		this.maxPlayers = maxPlayers;
	}

	public abstract boolean isSumo();

	public abstract boolean isCorners();

	public abstract void onRound();

	public abstract void onJoin(Player player);

	public abstract void onLeave(Player player);

	public abstract String getRoundDuration();

	public abstract EventPlayer getRoundPlayerA();

	public abstract EventPlayer getRoundPlayerB();

	public List<Player> getPlayers() {
		List<Player> players = new ArrayList<>();

		for (EventPlayer eventPlayer : this.eventPlayers.values()) {
			final Player player = eventPlayer.toPlayer();

			if (player != null) {
				players.add(player);
			}
		}

		return players;
	}

	public int getPlayerCount() {
		return this.eventPlayers.size();
	}

	public void handleStart() {
		this.cooldown = new Cooldown(60_000);
		this.runnable = new EventTask(this);
	}

	public void handleJoin(Player player) {
		PlayerUtil.reset(player);

		this.eventPlayers.put(player.getUniqueId(), new EventPlayer(player));

		this.onJoin(player);
	}

	public void handleDeath(Player player) {
		if (player.getUniqueId().equals(this.getRoundPlayerA().getUuid())) {

		} else if (player.getUniqueId().equals(this.getRoundPlayerB().getUuid())) {

		}
	}

	public void end() {

	}

	public boolean canEnd() {
		int eliminated = 0;

		for (EventPlayer eventPlayer : this.eventPlayers.values()) {
			if (eventPlayer.getState() == EventPlayerState.ELIMINATED) {
				eliminated++;
			}
		}

		return eliminated + 1 == this.getPlayerCount();
	}

	public Player getWinner() {
		for (EventPlayer eventPlayer : this.eventPlayers.values()) {
			if (eventPlayer.getState() != EventPlayerState.ELIMINATED) {
				return eventPlayer.toPlayer();
			}
		}

		return null;
	}

	public void announce() {
		BaseComponent[] components = new ChatComponentBuilder("")
				.parse("&6&l[Event] &r" + this.getHost().getDisplayName() +
				       " &eis hosting a &6&lSumo Event&e! &7[Click to join]")
				.attachToEachPart(
						new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentBuilder("").parse("").create()))
				.attachToEachPart(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/events join"))
				.create();

		for (Player player : Bukkit.getOnlinePlayers()) {
			player.sendMessage(components);
		}
	}

	public void broadcast(String message) {
		for (Player player : this.getPlayers()) {
			player.sendMessage(message);
		}
	}

}
