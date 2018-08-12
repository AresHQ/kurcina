package me.joeleoli.praxi.runnable;

import me.joeleoli.praxi.player.PraxiPlayer;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ExpBarRunnable implements Runnable {

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            PraxiPlayer praxiPlayer = PraxiPlayer.getByUuid(player.getUniqueId());

            if (praxiPlayer == null) {
                continue;
            }

            if (praxiPlayer.isOnEnderpearlCooldown()) {
                int seconds = Math.round(praxiPlayer.getEnderpearlCooldown().getRemaining()) / 1_000;

                player.setLevel(seconds);
                player.setExp(praxiPlayer.getEnderpearlCooldown().getRemaining() / 15_000.0F);
            }
        }
    }

}
