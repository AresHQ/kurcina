package me.joeleoli.praxi.events;

import lombok.Getter;
import lombok.Setter;

import me.joeleoli.nucleus.player.PlayerInfo;

import org.bukkit.entity.Player;

@Getter
@Setter
public class EventPlayer extends PlayerInfo {

    public EventPlayer(Player player) {
        super(player.getUniqueId(), player.getName());
    }

    private EventPlayerState state = EventPlayerState.WAITING;

}
