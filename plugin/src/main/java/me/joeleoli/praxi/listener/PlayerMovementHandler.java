package me.joeleoli.praxi.listener;

import me.joeleoli.nucleus.Nucleus;

import me.joeleoli.praxi.match.Match;
import me.joeleoli.praxi.match.MatchState;
import me.joeleoli.praxi.player.PlayerData;

import me.joeleoli.ragespigot.handler.MovementHandler;

import net.minecraft.server.v1_8_R3.PacketPlayInFlying;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class PlayerMovementHandler implements MovementHandler {

    @Override
    public void handleUpdateLocation(Player player, Location from, Location to, PacketPlayInFlying packetPlayInFlying) {
        final PlayerData playerData = PlayerData.getByUuid(player.getUniqueId());

        if (Nucleus.isFrozen(player)) {
            final Location teleportTo = from.clone();

            teleportTo.setY(to.getY());
            teleportTo.setYaw(to.getYaw());
            teleportTo.setPitch(to.getPitch());
            player.teleport(teleportTo);
            return;
        }

        if (playerData.isInMatch()) {
            if (playerData.getMatch().getLadder().isSumo() || playerData.getMatch().getLadder().isSpleef()) {
                final Match match = playerData.getMatch();

                if (match.isFighting()) {
                    if (player.getLocation().getBlock().getType() == Material.WATER || player.getLocation().getBlock().getType() == Material.STATIONARY_WATER) {
                        Player killer = player.getKiller();

                        if (killer == null) {
                            if (match.isSoloMatch()) {
                                killer = playerData.getMatch().getOpponentPlayer(player);
                            }
                        }

                        match.handleDeath(player, killer, false);
                    }
                } else if (playerData.getMatch().getState() == MatchState.STARTING) {
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
