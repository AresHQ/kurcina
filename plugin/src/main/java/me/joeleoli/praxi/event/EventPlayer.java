package me.joeleoli.praxi.event;

import lombok.Getter;

import me.joeleoli.nucleus.player.PlayerInfo;

import org.bukkit.entity.Player;

@Getter
public class EventPlayer extends PlayerInfo {

    public EventPlayer(Player player) {
        super(player.getUniqueId(), player.getName());
    }

    private EventPlayerState state = EventPlayerState.WAITING;

}
