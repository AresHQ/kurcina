package me.joeleoli.praxi.duel;

import lombok.Data;

import me.joeleoli.commons.chat.ChatComponentBuilder;
import me.joeleoli.commons.chat.ChatComponentExtras;
import me.joeleoli.commons.util.CC;

import me.joeleoli.praxi.arena.Arena;
import me.joeleoli.praxi.config.Config;
import me.joeleoli.praxi.config.ConfigKey;
import me.joeleoli.praxi.ladder.Ladder;
import me.joeleoli.praxi.player.PlayerData;
import me.joeleoli.praxi.script.ScriptContext;
import me.joeleoli.praxi.script.wrapper.PlayerWrapper;

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
        final PlayerData targetData = PlayerData.getByUuid(this.target.getUniqueId());

        final HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentBuilder(CC.GRAY + "Click to accept this duel invite.").create());
        final ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/duel " + this.sender.getName());

        senderData.setDuelProcedure(null);
        targetData.getDuelRequests().add(request);

        final ScriptContext context = new ScriptContext(Config.getStringList(ConfigKey.DUEL_SUCCESS));

        context.convert(this.arena);
        context.convert(this.ladder);

        context.addComponentExtras("accept_clickable", new ChatComponentExtras(hoverEvent, clickEvent));
        context.getReplaceables().add(new PlayerWrapper(this.sender));
        context.getReplaceables().add(new PlayerWrapper(this.target, "target"));

        context.buildComponents().forEach(this.sender::sendMessage);

        context.setLines(Config.getStringList(ConfigKey.DUEL_RECEIVER));
        context.getReplaceables().clear();
        context.getReplaceables().add(new PlayerWrapper(this.target));
        context.getReplaceables().add(new PlayerWrapper(this.sender, "target"));

        context.buildComponents().forEach(this.target::sendMessage);
    }

}
