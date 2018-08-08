package me.joeleoli.praxi.command;

import me.joeleoli.nucleus.command.Command;
import me.joeleoli.nucleus.command.CommandHelp;
import me.joeleoli.nucleus.command.param.Parameter;
import me.joeleoli.nucleus.util.CC;
import me.joeleoli.nucleus.util.TaskUtil;
import me.joeleoli.nucleus.uuid.UUIDCache;
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

    @Command(names = "resetelo", permissionNode = "prax.admin.resetelo")
    public static void resetElo(CommandSender sender, @Parameter(name = "target") String targetName) {
        UUID uuid;

        try {
            uuid = UUID.fromString(targetName);
        } catch (Exception e) {
            uuid = UUIDCache.getUuid(targetName);
        }

        if (uuid == null) {
            sender.sendMessage(CC.RED + "Couldn't find a player with the name " + CC.RESET + targetName + CC.RED + ". Have they joined the network?");
            return;
        }

        PlayerData playerData = PlayerData.getByUuid(uuid);

        if (playerData.isLoaded()) {
            playerData.getStatistics().getLadders().values().forEach(stats -> {
                stats.setElo(1000);
            });

            playerData.save();
        } else {
            TaskUtil.runAsync(() -> {
                playerData.load();

                playerData.getStatistics().getLadders().values().forEach(stats -> {
                    stats.setElo(1000);
                });

                playerData.save();
            });
        }

        sender.sendMessage(CC.GREEN + "You reset " + targetName + "'s elo.");
    }

}
