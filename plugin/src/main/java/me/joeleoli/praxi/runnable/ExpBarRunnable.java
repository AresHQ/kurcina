package me.joeleoli.praxi.runnable;

import me.joeleoli.praxi.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ExpBarRunnable implements Runnable {

    @Override
    public void run() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerData playerData = PlayerData.getByUuid(player.getUniqueId());

            if (playerData == null) {
                continue;
            }

            if (playerData.getEnderpearlCooldown() != null) {
                int seconds = Math.round(playerData.getEnderpearlCooldown().getRemaining()) / 1_000;

                player.setLevel(seconds);
                player.setExp(playerData.getEnderpearlCooldown().getRemaining() / 15_000.0F);
            }
        }
    }

}
