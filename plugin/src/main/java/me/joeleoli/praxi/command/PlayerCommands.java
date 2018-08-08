package me.joeleoli.praxi.command;

import me.joeleoli.nucleus.command.Command;
import me.joeleoli.nucleus.command.param.Parameter;
import me.joeleoli.nucleus.util.CC;
import me.joeleoli.nucleus.uuid.UUIDCache;
import me.joeleoli.praxi.player.PlayerData;
import me.joeleoli.praxi.player.gui.PlayerSettingsMenu;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PlayerCommands {

    @Command(names = {"settings", "options"})
    public static void settings(Player player) {
        new PlayerSettingsMenu().openMenu(player);
    }

    @Command(names = {"statistics", "stats"}, async = true)
    public static void statistics(Player player, @Parameter(name = "target") String name) {
        final UUID uuid = UUIDCache.getUuid(name);

        if (uuid == null) {
            player.sendMessage(CC.RED + "Couldn't find a player with the name " + CC.RESET + name + CC.RED + ".");
            return;
        }

        final PlayerData playerData = PlayerData.getByUuid(uuid);

        if (!playerData.isLoaded()) {
            playerData.load();
        }

        if (playerData.getName() != null) {
            if (playerData.getName().equalsIgnoreCase(name)) {
                name = playerData.getName();
            }
        }

        final List<String> messages = new ArrayList<>();

        playerData.getStatistics().getLadders().forEach((key, value) -> {
            messages.add(CC.YELLOW + key + CC.GRAY + ": " + CC.PINK + value.getElo() + " ELO");
        });

        messages.add(0, CC.GOLD + CC.BOLD + name + "'s Statistics");
        messages.add(0, CC.HORIZONTAL_SEPARATOR);
        messages.add(CC.HORIZONTAL_SEPARATOR);
        messages.forEach(player::sendMessage);
    }

}
