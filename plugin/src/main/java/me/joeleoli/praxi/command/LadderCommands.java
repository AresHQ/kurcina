package me.joeleoli.praxi.command;

import me.joeleoli.nucleus.command.Command;
import me.joeleoli.nucleus.command.param.Parameter;
import me.joeleoli.nucleus.util.CC;

import me.joeleoli.praxi.ladder.Ladder;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LadderCommands {

    @Command(names = "ladder enable", permissionNode = "praxi.ladder")
    public static void enable(CommandSender sender, @Parameter(name = "ladder") Ladder ladder) {
        ladder.setEnabled(true);
        sender.sendMessage(CC.GREEN + "You enabled the " + ladder.getDisplayName() + CC.GREEN + " ladder.");
    }

    @Command(names = "ladder disable", permissionNode = "praxi.ladder")
    public static void disable(CommandSender sender, @Parameter(name = "ladder") Ladder ladder) {
        ladder.setEnabled(false);
        sender.sendMessage(CC.GREEN + "You disabled the " + ladder.getDisplayName() + CC.GREEN + " ladder.");
    }

    @Command(names = "ladder sethitdelay", permissionNode = "praxi.ladder")
    public static void setHitDelay(CommandSender sender, @Parameter(name = "ladder") Ladder ladder, @Parameter(name = "hitdelay") int hitDelay) {
        if (hitDelay < 0 || hitDelay > 20) {
            sender.sendMessage(CC.RED + "The hit delay must be in the range of 0-20.");
            return;
        }

        ladder.setHitDelay(hitDelay);
        sender.sendMessage(CC.GREEN + "You set the hit delay of " + ladder.getDisplayName() + CC.GREEN + " to: " + CC.RESET + ladder.getHitDelay());
    }

    @Command(names = "ladder list", permissionNode = "praxi.ladder")
    public static void list(CommandSender sender) {
        sender.sendMessage(CC.GOLD + "Ladders:");

        Ladder.getLadders().forEach(ladder -> {
            sender.sendMessage(CC.GRAY + " - " + ladder.getDisplayName());
        });
    }

    @Command(names = "ladder create", permissionNode = "praxi.ladder")
    public static void create(Player player, @Parameter(name = "name") String name) {
        Ladder ladder = Ladder.getByName(name);

        if (ladder != null) {
            player.sendMessage(CC.RED + "A ladder with that name already exists.");
            return;
        }

        ladder = new Ladder(name);
        ladder.save();

        player.sendMessage(CC.GREEN + "Created a new ladder named " + CC.AQUA + ladder.getName() + CC.GREEN + ".");
    }

    @Command(names = "ladder setkit", permissionNode = "praxi.ladder")
    public static void setKit(Player player, @Parameter(name = "ladder") Ladder ladder) {
        ladder.getDefaultKit().setArmor(player.getInventory().getArmorContents());
        ladder.getDefaultKit().setContents(player.getInventory().getContents());
        ladder.save();

        player.sendMessage(CC.GREEN + "Updated " + CC.AQUA + ladder.getName() + CC.GREEN + "'s default kit.");
    }

    @Command(names = "ladder loadkit", permissionNode = "praxi.ladder")
    public static void loadKit(Player player, @Parameter(name = "ladder") Ladder ladder) {
        player.getInventory().setArmorContents(ladder.getDefaultKit().getArmor());
        player.getInventory().setContents(ladder.getDefaultKit().getContents());
        player.updateInventory();
        player.sendMessage(CC.GREEN + "Loaded " + CC.AQUA + ladder.getName() + CC.GREEN + "'s default kit.");
    }

    @Command(names = "ladder setdisplayname", permissionNode = "praxi.ladder")
    public static void setDisplayName(CommandSender sender, @Parameter(name = "ladder") Ladder ladder, @Parameter(name = "displayName") String displayName) {
        ladder.setDisplayName(CC.translate(displayName));
        sender.sendMessage(CC.GREEN + "You set " + CC.AQUA + ladder.getName() + "'s display name " + CC.GREEN + "to: " + CC.AQUA + CC.translate(displayName));
    }

}
