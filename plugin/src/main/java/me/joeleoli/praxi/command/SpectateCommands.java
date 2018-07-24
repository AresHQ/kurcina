package me.joeleoli.praxi.command;

import me.joeleoli.commons.command.Command;
import me.joeleoli.commons.command.param.Parameter;

import me.joeleoli.praxi.config.Config;
import me.joeleoli.praxi.config.ConfigKey;
import me.joeleoli.praxi.player.PlayerState;
import me.joeleoli.praxi.player.PlayerData;

import org.bukkit.entity.Player;

public class SpectateCommands {

    @Command(names = {"spectate", "spec"})
    public static void spectate(Player player, @Parameter(name = "target") Player target) {
        PlayerData playerData = PlayerData.getByUuid(player.getUniqueId());
        PlayerData targetData = PlayerData.getByUuid(target.getUniqueId());

        if (playerData == null || playerData.getState() != PlayerState.IN_LOBBY) {
            player.sendMessage(Config.getString(ConfigKey.SPECTATE_JOIN_REJECTED_STATE, player, target));
            return;
        }

        if (targetData == null || targetData.getState() != PlayerState.IN_FIGHT || targetData.getMatch() == null) {
            player.sendMessage(Config.getString(ConfigKey.SPECTATE_JOIN_REJECTED_TARGET_STATE, player, target));
            return;
        }

        if (!targetData.getPlayerSettings().isAllowSpectators()) {
            player.sendMessage(Config.getString(ConfigKey.SPECTATE_JOIN_REJECTED_TARGET_DISABLED, player, target));
            return;
        }

        targetData.getMatch().addSpectator(player, target);
    }

    @Command(names = "stopspectate")
    public static void stopSpectate(Player player) {
        PlayerData playerData = PlayerData.getByUuid(player.getUniqueId());

        if (playerData == null || playerData.getState() != PlayerState.SPECTATE_MATCH || playerData.getMatch() == null) {
            player.sendMessage(Config.getString(ConfigKey.SPECTATE_QUIT_REJECTED_STATE, player, null));
            return;
        }

        playerData.getMatch().removeSpectator(player);
    }

}
