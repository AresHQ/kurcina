package me.joeleoli.praxi.event;

import me.joeleoli.nucleus.player.PlayerInfo;

import org.bukkit.entity.Player;

import java.util.List;

public interface Event {

    String getName();

    EventState getState();

    PlayerInfo getHost();

    void start();

    void end();

    void handleRound();

    List<Player> getPlayers();

    int getPlayerCount();

    int getMaxPlayers();

    void handleJoin(Player player);

    void handleLeave(Player player);

    String getRoundDuration();

    EventPlayer getRoundPlayerA();

    EventPlayer getRoundPlayerB();

    void broadcast();

}
