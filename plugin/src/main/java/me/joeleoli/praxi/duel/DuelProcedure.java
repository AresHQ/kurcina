package me.joeleoli.praxi.duel;

import lombok.Data;
import me.joeleoli.nucleus.chat.ChatComponentBuilder;
import me.joeleoli.nucleus.util.Style;
import me.joeleoli.praxi.arena.Arena;
import me.joeleoli.praxi.ladder.Ladder;
import me.joeleoli.praxi.player.PraxiPlayer;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.entity.Player;

@Data
public class DuelProcedure {

	private Player sender;
	private Player target;
	private Ladder ladder;
	private Arena arena;

	public void send() {
		if (!this.sender.isOnline() || !this.target.isOnline()) {
			return;
		}

		final String ladderContext = StringUtils.startsWithIgnoreCase(this.ladder.getName(), "u") ? "an " : "a ";
		final DuelRequest request = new DuelRequest(this.sender.getUniqueId());

		request.setLadder(this.ladder);
		request.setArena(this.arena);

		final PraxiPlayer senderData = PraxiPlayer.getByUuid(this.sender.getUniqueId());

		senderData.setDuelProcedure(null);
		senderData.getSentDuelRequests().put(this.target.getUniqueId(), request);

		this.sender.sendMessage(
				Style.YELLOW + "You sent a duel request to " + Style.PINK + this.target.getName() + Style.YELLOW +
				" on " + Style.PINK + this.arena.getName() + Style.YELLOW + ".");

		final HoverEvent hoverEvent = new HoverEvent(
				HoverEvent.Action.SHOW_TEXT,
				new ChatComponentBuilder(Style.YELLOW + "Click to accept this duel invite.").create()
		);
		final ClickEvent clickEvent =
				new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/duel accept " + this.sender.getName());

		this.target.sendMessage(
				Style.PINK + this.sender.getName() + Style.YELLOW + " has sent you " + ladderContext + Style.PINK +
				this.ladder.getName() + Style.YELLOW + " duel on " + Style.PINK + this.arena.getName() + Style.YELLOW +
				".");
		this.target.sendMessage(new ChatComponentBuilder("")
				.parse("&6Click here or type &b/duel accept " + this.sender.getName() + " &6to accept the invite.")
				.attachToEachPart(clickEvent).attachToEachPart(hoverEvent).create());
	}

}
