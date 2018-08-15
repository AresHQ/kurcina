package me.joeleoli.praxi.handler;

import me.joeleoli.praxi.match.Match;
import me.joeleoli.praxi.match.MatchState;
import me.joeleoli.praxi.player.PraxiPlayer;
import me.joeleoli.ragespigot.handler.MovementHandler;
import net.minecraft.server.v1_8_R3.PacketPlayInFlying;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class PlayerMovementHandler implements MovementHandler {

	@Override
	public void handleUpdateLocation(Player player, Location from, Location to, PacketPlayInFlying packetPlayInFlying) {
		final PraxiPlayer praxiPlayer = PraxiPlayer.getByUuid(player.getUniqueId());

		if (praxiPlayer.isInMatch()) {
			if (praxiPlayer.getMatch().getLadder().isSumo() || praxiPlayer.getMatch().getLadder().isSpleef()) {
				final Match match = praxiPlayer.getMatch();

				if (match.isFighting()) {
					if (player.getLocation().getBlock().getType() == Material.WATER ||
					    player.getLocation().getBlock().getType() == Material.STATIONARY_WATER) {
						Player killer = player.getKiller();

						if (killer == null) {
							if (match.isSoloMatch()) {
								killer = praxiPlayer.getMatch().getOpponentPlayer(player);
							}
						}

						match.handleDeath(player, killer, false);
					}
				} else if (praxiPlayer.getMatch().getState() == MatchState.STARTING) {
					Location teleportTo = null;

					if (match.isSoloMatch()) {
						if (match.getPlayerA().equals(player)) {
							teleportTo = match.getArena().getSpawn1();
						} else {
							teleportTo = match.getArena().getSpawn2();
						}
					} else if (match.isTeamMatch()) {
						if (match.getTeamA().equals(match.getTeam(player))) {
							teleportTo = match.getArena().getSpawn1();
						} else {
							teleportTo = match.getArena().getSpawn2();
						}
					}

					if (teleportTo == null) {
						return;
					}

					teleportTo.setY(to.getY());
					teleportTo.setYaw(to.getYaw());
					teleportTo.setPitch(to.getPitch());

					if (teleportTo.getBlock().getType() != Material.AIR) {
						teleportTo.add(0, 2, 0);
					}

					player.teleport(teleportTo);
				}
			}
		}
	}

	@Override
	public void handleUpdateRotation(Player player, Location from, Location to, PacketPlayInFlying packetPlayInFlying) {

	}

}
