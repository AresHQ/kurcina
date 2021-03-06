package me.joeleoli.praxi.task;

import me.joeleoli.praxi.Praxi;
import me.joeleoli.praxi.player.PraxiPlayer;
import org.bukkit.entity.Player;

public class ExpBarCooldownTask implements Runnable {

	@Override
	public void run() {
		for (Player player : Praxi.getInstance().getServer().getOnlinePlayers()) {
			final PraxiPlayer praxiPlayer = PraxiPlayer.getByUuid(player.getUniqueId());

			if (praxiPlayer.isOnEnderpearlCooldown()) {
				int seconds = Math.round(praxiPlayer.getEnderpearlCooldown().getRemaining()) / 1_000;

				player.setLevel(seconds);
				player.setExp(praxiPlayer.getEnderpearlCooldown().getRemaining() / 15_000.0F);
			}
		}
	}

}
