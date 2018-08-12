package me.joeleoli.praxi.events.impl;

import lombok.Getter;

import me.joeleoli.nucleus.NucleusAPI;
import me.joeleoli.nucleus.player.PlayerInfo;
import me.joeleoli.nucleus.util.Style;
import me.joeleoli.nucleus.util.TimeUtil;

import me.joeleoli.praxi.events.Event;
import me.joeleoli.praxi.events.EventPlayer;
import me.joeleoli.praxi.events.EventState;

import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Getter
public class SumoEvent extends Event {

    private Map<UUID, Integer> roundWins = new HashMap<>();
    private EventPlayer roundPlayerA;
    private EventPlayer roundPlayerB;
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
    public void onRound() {
        if (this.roundPlayerA == null || this.roundPlayerB == null) {
            // find new round players

        }
    }

    @Override
    public void onJoin(Player player) {
        this.getEventPlayers().put(player.getUniqueId(), new EventPlayer(player));

        for (Player other : this.getPlayers()) {
            other.sendMessage(Style.GOLD + Style.BOLD + "[Event] " + NucleusAPI.getColoredName(player) + Style.YELLOW + " has joined the events! " + Style.GRAY + "( " + this.getPlayerCount() + "/" + this.getMaxPlayers() + ")");
        }
    }

    @Override
    public void onLeave(Player player) {
        this.getEventPlayers().remove(player.getUniqueId());

        for (Player other : this.getPlayers()) {
            other.sendMessage(Style.GOLD + Style.BOLD + "[Event] " + NucleusAPI.getColoredName(player) + Style.YELLOW + " has left the events! " + Style.GRAY + "( " + this.getPlayerCount() + "/" + this.getMaxPlayers() + ")");
        }
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

    @Override
    public EventPlayer getRoundPlayerA() {
        return this.roundPlayerA;
    }

    @Override
    public EventPlayer getRoundPlayerB() {
        return this.roundPlayerB;
    }

}
