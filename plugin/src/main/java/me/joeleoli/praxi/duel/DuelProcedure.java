package me.joeleoli.praxi.duel;

import lombok.Data;

import me.joeleoli.nucleus.chat.ChatComponentBuilder;
import me.joeleoli.nucleus.util.Style;
import me.joeleoli.praxi.arena.Arena;
import me.joeleoli.praxi.ladder.Ladder;
import me.joeleoli.praxi.player.PraxiPlayer;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;

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

        final DuelRequest request = new DuelRequest(this.sender.getUniqueId());

        request.setLadder(this.ladder);
        request.setArena(this.arena);

        final PraxiPlayer senderData = PraxiPlayer.getByUuid(this.sender.getUniqueId());

        senderData.setDuelProcedure(null);
        senderData.getSentDuelRequests().put(this.target.getUniqueId(), request);

        this.sender.sendMessage(Style.YELLOW + "You sent a duel request to " + this.target.getDisplayName() + Style.YELLOW + " on " + Style.AQUA + this.arena.getName() + Style.YELLOW + ".");

        final HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentBuilder(Style.GRAY + "Click to aStyle.pt this duel invite.").create());
        final ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/duel aStyle.pt " + this.sender.getName());

        this.target.sendMessage(this.sender.getDisplayName() + Style.YELLOW + " has sent you a " + Style.RESET + this.ladder.getDisplayName() + Style.YELLOW + " duel on " + Style.AQUA + this.arena.getName() + Style.YELLOW + ".");
        this.target.sendMessage(new ChatComponentBuilder("").parse("&6Click here or type &b/duel aStyle.pt " + this.sender.getName() + " &6to aStyle.pt the invite.").attachToEachPart(clickEvent).attachToEachPart(hoverEvent).create());
    }

}
