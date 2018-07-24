package me.joeleoli.praxi.runnable;

import me.joeleoli.praxi.player.PlayerData;

public class SaveRunnable implements Runnable {

    @Override
    public void run() {
        for (PlayerData playerData : PlayerData.getPlayers().values()) {
            playerData.save();
        }
    }

}
