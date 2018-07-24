package me.joeleoli.praxi.command;

import me.joeleoli.commons.command.Command;
import me.joeleoli.commons.command.CommandHelp;
import me.joeleoli.commons.command.param.Parameter;
import me.joeleoli.commons.util.CC;
import me.joeleoli.commons.util.TaskUtil;
import me.joeleoli.commons.uuid.UUIDCache;

import me.joeleoli.praxi.config.Config;
import me.joeleoli.praxi.player.PlayerData;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ManagementCommands {

    private static final CommandHelp[] HELP = new CommandHelp[]{
            new CommandHelp("/praxi reload", "Reload the config"),
    };

    @Command(names = {"praxi", "praxi help"}, permissionNode = "praxi.admin")
    public static void help(Player player) {
        for (CommandHelp help : HELP) {
            player.sendMessage(CC.YELLOW + help.getSyntax() + CC.AQUA + " - " + CC.PINK + help.getDescription());
        }
    }

    @Command(names = "praxi reload", permissionNode = "prax.admin.reload")
    public static void reload(Player player) {
        Config.init();
        player.sendMessage(CC.GREEN + "Reloaded the Praxi configuration.");
    }

    @Command(names = "resetelo", permissionNode = "prax.admin.resetelo")
    public static void resetElo(CommandSender sender, @Parameter(name = "target") String targetName) {
        UUID uuid;

        try {
            uuid = UUID.fromString(targetName);
        } catch (Exception e) {
            uuid = UUIDCache.getUuid(targetName);
        }

        if (uuid == null) {
            sender.sendMessage(CC.RED + "Target not found.");
            return;
        }

        PlayerData playerData = PlayerData.getByUuid(uuid);

        if (playerData.isLoaded()) {
            playerData.getPlayerStatistics().getLadders().values().forEach(stats -> {
                stats.setElo(1000);
            });

            playerData.save();
        } else {
            TaskUtil.runAsync(() -> {
                playerData.load();

                playerData.getPlayerStatistics().getLadders().values().forEach(stats -> {
                    stats.setElo(1000);
                });

                playerData.save();
            });
        }

        sender.sendMessage(CC.GREEN + "You have reset " + targetName + "'s elo.");
    }

}
