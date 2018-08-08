package me.joeleoli.praxi.duel;

import lombok.Data;

import me.joeleoli.nucleus.chat.ChatComponentBuilder;
import me.joeleoli.nucleus.util.CC;
import me.joeleoli.praxi.arena.Arena;
import me.joeleoli.praxi.ladder.Ladder;
import me.joeleoli.praxi.player.PlayerData;

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

        final PlayerData senderData = PlayerData.getByUuid(this.sender.getUniqueId());

        senderData.setDuelProcedure(null);
        senderData.getSentDuelRequests().put(this.target.getUniqueId(), request);

        this.sender.sendMessage(CC.YELLOW + "You sent a duel request to " + this.target.getDisplayName() + CC.YELLOW + " on " + CC.AQUA + this.arena.getName() + CC.YELLOW + ".");

        final HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentBuilder(CC.GRAY + "Click to accept this duel invite.").create());
        final ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/duel accept " + this.sender.getName());

        this.target.sendMessage(this.sender.getDisplayName() + CC.YELLOW + " has sent you a " + CC.RESET + this.ladder.getDisplayName() + CC.YELLOW + " duel on " + CC.AQUA + this.arena.getName() + CC.YELLOW + ".");
        this.target.sendMessage(new ChatComponentBuilder("").parse("&6Click here or type &b/duel accept " + this.sender.getName() + " &6to accept the invite.").attachToEachPart(clickEvent).attachToEachPart(hoverEvent).create());
    }

}
