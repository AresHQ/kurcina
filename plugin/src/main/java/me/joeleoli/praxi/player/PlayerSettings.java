package me.joeleoli.praxi.player;

import lombok.Data;

@Data
public class PlayerSettings {

    private boolean receiveDuelRequests = true;
    private boolean globalChat = true;
    private boolean showScoreboard = true;
    private boolean allowSpectators = true;

}
