package me.joeleoli.praxi.command.param;

import me.joeleoli.nucleus.command.param.ParameterType;
import me.joeleoli.nucleus.util.CC;
import me.joeleoli.praxi.arena.Arena;

import org.apache.commons.lang.StringUtils;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class ArenaParameterType implements ParameterType<Arena> {

    public Arena transform(CommandSender sender, String source) {
        Arena arena = Arena.getByName(source);

        if (arena == null) {
            sender.sendMessage(CC.RED + "An arena with that name does not exist.");
            return null;
        }

        return arena;
    }

    public List<String> tabComplete(Player sender, Set<String> flags, String source) {
        List<String> completions = new ArrayList<>();

        for (Arena arena : Arena.getArenas()) {
            if (arena.getName() != null && StringUtils.startsWithIgnoreCase(arena.getName(), source)) {
                completions.add(arena.getName());
            }
        }

        return completions;
    }

}