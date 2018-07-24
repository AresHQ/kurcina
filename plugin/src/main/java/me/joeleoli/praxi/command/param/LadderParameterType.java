package me.joeleoli.praxi.command.param;

import me.joeleoli.commons.command.param.ParameterType;
import me.joeleoli.commons.util.CC;

import me.joeleoli.praxi.ladder.Ladder;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class LadderParameterType implements ParameterType<Ladder> {

    public Ladder transform(CommandSender sender, String source) {
        Ladder ladder = Ladder.getByName(source);

        if (ladder == null) {
            sender.sendMessage(CC.RED + "That is not a valid ladder type.");
            return null;
        }

        return ladder;
    }

    public List<String> tabComplete(Player sender, Set<String> flags, String source) {
        List<String> completions = new ArrayList<>();

        for (Ladder ladder : Ladder.getLadders()) {
            completions.add(ladder.getName());
        }

        return completions;
    }

}