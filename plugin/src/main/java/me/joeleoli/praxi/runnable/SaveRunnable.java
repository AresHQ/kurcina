package me.joeleoli.praxi.runnable;

import me.joeleoli.praxi.player.PraxiPlayer;

public class SaveRunnable implements Runnable {

    @Override
    public void run() {
        for (PraxiPlayer praxiPlayer : PraxiPlayer.getPlayers().values()) {
            praxiPlayer.save();
        }
    }

}
