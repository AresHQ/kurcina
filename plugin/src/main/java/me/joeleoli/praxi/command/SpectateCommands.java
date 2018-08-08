package me.joeleoli.praxi.command;

import me.joeleoli.nucleus.Nucleus;
import me.joeleoli.nucleus.command.Command;
import me.joeleoli.nucleus.command.param.Parameter;
import me.joeleoli.nucleus.util.CC;
import me.joeleoli.praxi.player.PlayerState;
import me.joeleoli.praxi.player.PlayerData;

import me.joeleoli.praxi.player.PracticeSetting;
import org.bukkit.entity.Player;

public class SpectateCommands {

    @Command(names = {"spectate", "spec"})
    public static void spectate(Player player, @Parameter(name = "target") Player target) {
        if (Nucleus.isFrozen(player)) {
            player.sendMessage(CC.RED + "You cannot spectate while frozen.");
            return;
        }

        PlayerData playerData = PlayerData.getByUuid(player.getUniqueId());
        PlayerData targetData = PlayerData.getByUuid(target.getUniqueId());

        if (playerData == null || playerData.getState() != PlayerState.IN_LOBBY) {
            player.sendMessage(CC.RED + "You must be in the lobby to spectate another player's match.");
            return;
        }

        if (playerData.getParty() != null) {
            player.sendMessage(CC.RED + "You must leave your party to spectate a match.");
            return;
        }

        if (targetData == null || targetData.getState() != PlayerState.IN_MATCH || targetData.getMatch() == null) {
            player.sendMessage(CC.RED + "That player is not in a match.");
            return;
        }

        if (!Nucleus.<Boolean>getSetting(target, PracticeSetting.RECEIVE_DUEL_REQUESTS)) {
            player.sendMessage(CC.RED + "That player is not allowing spectators.");
            return;
        }

        targetData.getMatch().addSpectator(player, target);
    }

    @Command(names = "stopspectate")
    public static void stopSpectate(Player player) {
        PlayerData playerData = PlayerData.getByUuid(player.getUniqueId());

        if (playerData == null || playerData.getState() != PlayerState.SPECTATE_MATCH || playerData.getMatch() == null) {
            player.sendMessage(CC.RED + "You are not spectating a match.");
            return;
        }

        playerData.getMatch().removeSpectator(player);
    }

}
