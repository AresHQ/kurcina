package me.joeleoli.praxi.command;

import me.joeleoli.commons.command.Command;
import me.joeleoli.commons.command.CommandHelp;
import me.joeleoli.commons.command.param.Parameter;
import me.joeleoli.commons.util.CC;

import me.joeleoli.praxi.arena.Arena;
import me.joeleoli.praxi.arena.ArenaType;
import me.joeleoli.praxi.arena.*;
import me.joeleoli.praxi.arena.selection.Selection;

import org.bukkit.entity.Player;

public class ArenaCommands {

    private static final CommandHelp[] HELP = new CommandHelp[] {
            new CommandHelp("/arena list", "List all arenas"),
            new CommandHelp("/arena create <name> <type>", "Create an arena"),
            new CommandHelp("/arena delete <name>", "Delete an arena"),
            new CommandHelp("/arena setspawn <1:2> <name>", "Set a spawn point"),
    };

    @Command(names = {"arena", "arena help"}, permissionNode = "praxi.arena.admin")
    public static void help(Player player) {
        for (CommandHelp help : HELP) {
            player.sendMessage(CC.YELLOW + help.getSyntax() + CC.AQUA + " - " + CC.PINK + help.getDescription());
        }
    }

    @Command(names = "arena wand", permissionNode = "praxi.arena.admin")
    public static void wand(Player player) {
        player.getInventory().addItem(Selection.SELECTION_WAND);
        player.sendMessage(CC.YELLOW + "You have been given the selection wand.");
    }

    @Command(names = "arena list", permissionNode = "praxi.arena.admin")
    public static void list(Player player) {
        player.sendMessage(CC.GOLD + "Arenas:");

        if (Arena.getArenas().isEmpty()) {
            player.sendMessage(CC.GRAY + "There are no arenas.");
            return;
        }

        for (Arena arena : Arena.getArenas()) {
            if (arena.getType() != ArenaType.DUPLICATE) {
                player.sendMessage(CC.GRAY + " - " + (arena.isSetup() ? CC.GREEN : CC.RED) + arena.getName() + CC.GRAY + " (" + arena.getType().name() + ")");
            }
        }
    }

    @Command(names = "arena create", permissionNode = "praxi.arena.admin")
    public static void create(Player player, @Parameter(name = "name") String name, @Parameter(name = "type") ArenaType type) {
        Arena arena = Arena.getByName(name);

        if (arena != null) {
            player.sendMessage(CC.RED + "An arena with that name already exists.");
            return;
        }

        Selection selection = Selection.createOrGetSelection(player);

        if (!selection.isFullObject()) {
            player.sendMessage(CC.RED + "You must have a full selection to create an arena.");
            return;
        }

        if (type == ArenaType.STANDALONE) {
            arena = new StandaloneArena(name, selection.getPoint1(), selection.getPoint2());
        } else {
            arena = new SharedArena(name, selection.getPoint1(), selection.getPoint2());
        }

        arena.save();

        player.sendMessage(CC.GREEN + "Arena `" + arena.getName() + "` has been created.");
    }

    @Command(names = "arena delete", permissionNode = "praxi.arena.admin")
    public static void delete(Player player, @Parameter(name = "arena") Arena arena) {
        arena.delete();

        Arena.getArenas().remove(arena);

        if (arena instanceof StandaloneArena) {
            Arena.getArenas().removeAll(((StandaloneArena) arena).getDuplicates());
        }

        player.sendMessage(CC.GREEN + "Arena `" + arena.getName() + "` has been deleted.");
    }

    @Command(names = "arena setspawn", permissionNode = "praxi.arena.admin")
    public static void setSpawn(Player player, @Parameter(name = "loc") int loc, @Parameter(name = "arena") Arena arena) {
        if (loc == 1) {
            arena.setSpawn1(player.getLocation());
        } else if (loc == 2) {
            arena.setSpawn2(player.getLocation());
        } else {
            player.sendMessage(CC.RED + "Choose position `1` or position `2`.");
            return;
        }

        arena.save();

        player.sendMessage(CC.GREEN + "You have set spawn position " + loc + " for `" + arena.getName() + "`.");
    }

}