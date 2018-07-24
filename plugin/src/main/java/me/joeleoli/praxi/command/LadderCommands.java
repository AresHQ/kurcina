package me.joeleoli.praxi.command;

import me.joeleoli.commons.command.Command;
import me.joeleoli.commons.command.param.Parameter;
import me.joeleoli.commons.util.CC;

import me.joeleoli.praxi.ladder.Ladder;

import org.bukkit.entity.Player;

public class LadderCommands {

    @Command(names = "ladder create", permissionNode = "praxi.ladder.create")
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

    @Command(names = "ladder setkit", permissionNode = "praxi.ladder.setkit")
    public static void setKit(Player player, @Parameter(name = "ladder") Ladder ladder) {
        ladder.getDefaultKit().setArmor(player.getInventory().getArmorContents());
        ladder.getDefaultKit().setContents(player.getInventory().getContents());
        ladder.save();

        player.sendMessage(CC.GREEN + "Updated " + CC.AQUA + ladder.getName() + CC.GREEN + "'s default kit.");
    }

    @Command(names = "ladder loadkit", permissionNode = "praxi.ladder.loadkit")
    public static void loadKit(Player player, @Parameter(name = "ladder") Ladder ladder) {
        player.getInventory().setArmorContents(ladder.getDefaultKit().getArmor());
        player.getInventory().setContents(ladder.getDefaultKit().getContents());
        player.updateInventory();
        player.sendMessage(CC.GREEN + "Loaded " + CC.AQUA + ladder.getName() + CC.GREEN + "'s default kit.");
    }

}
