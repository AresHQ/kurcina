package me.joeleoli.praxi.event.impl;

import lombok.Getter;

import me.joeleoli.nucleus.Nucleus;
import me.joeleoli.nucleus.chat.ChatComponentBuilder;
import me.joeleoli.nucleus.player.PlayerInfo;
import me.joeleoli.nucleus.util.CC;
import me.joeleoli.nucleus.util.TimeUtil;

import me.joeleoli.praxi.event.Event;
import me.joeleoli.praxi.event.EventPlayer;
import me.joeleoli.praxi.event.EventState;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

public class SumoEvent implements Event {

    @Getter
    private EventState state = EventState.WAITING;
    @Getter
    private PlayerInfo host;
    private Map<UUID, EventPlayer> players = new HashMap<>();
    private Map<UUID, Integer> roundWins = new HashMap<>();
    private EventPlayer roundPlayerA;
    private EventPlayer roundPlayerB;
    private long roundStart;

    public SumoEvent(PlayerInfo host) {
        this.host = host;
    }

    @Override
    public String getName() {
        return "Sumo";
    }

    @Override
    public void start() {

    }

    @Override
    public void end() {

    }

    @Override
    public void handleRound() {

    }

    @Override
    public List<Player> getPlayers() {
        final List<Player> players = new ArrayList<>();

        this.players.values().forEach(player -> {
            players.add(player.toPlayer());
        });

        return players;
    }

    @Override
    public int getPlayerCount() {
        return this.players.size();
    }

    @Override
    public int getMaxPlayers() {
        return 128;
    }

    @Override
    public String getRoundDuration() {
        return this.state == EventState.ROUND_STARTING ? "00:00" : (this.state == EventState.ROUND_FIGHTING ? TimeUtil.formatTime(System.currentTimeMillis() - this.roundStart) : "Ending");
    }

    @Override
    public EventPlayer getRoundPlayerA() {
        return this.roundPlayerA;
    }

    @Override
    public EventPlayer getRoundPlayerB() {
        return this.roundPlayerB;
    }

    @Override
    public void handleJoin(Player player) {
        this.players.put(player.getUniqueId(), new EventPlayer(player));

        for (Player other : this.getPlayers()) {
            other.sendMessage(CC.GOLD + CC.BOLD + "[Event] " + Nucleus.getColoredName(player) + CC.YELLOW + " has joined the event! " + CC.GRAY + "( " + this.players.size() + "/" + this.getMaxPlayers() + ")");
        }
    }

    @Override
    public void handleLeave(Player player) {
        this.players.remove(player.getUniqueId());
    }

    @Override
    public void broadcast() {
        BaseComponent[] components = new ChatComponentBuilder("")
                .parse("&6&l[Event] &r" + this.host.getDisplayName() + " &eis hosting a &6&lSumo Event&e! &7[Click to join]")
                .attachToEachPart(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentBuilder("").parse("").create()))
                .attachToEachPart(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/event join"))
                .create();

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(components);
        }
    }

}
