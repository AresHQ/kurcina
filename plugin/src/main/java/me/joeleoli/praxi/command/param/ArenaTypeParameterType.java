package me.joeleoli.praxi.command.param;

import me.joeleoli.commons.command.param.ParameterType;
import me.joeleoli.commons.util.CC;
import me.joeleoli.praxi.arena.ArenaType;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ArenaTypeParameterType implements ParameterType<ArenaType> {

    public ArenaType transform(CommandSender sender, String source) {
        ArenaType type;

        try {
            type = ArenaType.valueOf(source);
        } catch (Exception e) {
            sender.sendMessage(CC.RED + "That is not a valid arena type.");
            return null;
        }

        return type;
    }

    public List<String> tabComplete(Player sender, Set<String> flags, String source) {
        List<String> completions = new ArrayList<>();

        for (ArenaType type : ArenaType.values()) {
            completions.add(type.name());
        }

        return completions;
    }

}