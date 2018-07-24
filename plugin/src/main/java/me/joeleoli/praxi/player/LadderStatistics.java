package me.joeleoli.praxi.player;

import lombok.Data;

@Data
public class LadderStatistics {

    private int elo = 1000;
    private int won, lost;

}
