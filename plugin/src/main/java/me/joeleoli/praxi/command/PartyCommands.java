package me.joeleoli.praxi.command;

import me.joeleoli.nucleus.Nucleus;
import me.joeleoli.nucleus.command.Command;
import me.joeleoli.nucleus.command.CommandHelp;
import me.joeleoli.nucleus.command.param.Parameter;
import me.joeleoli.nucleus.util.CC;

import me.joeleoli.praxi.party.PartyState;
import me.joeleoli.praxi.player.PlayerState;
import me.joeleoli.praxi.party.Party;
import me.joeleoli.praxi.player.PlayerData;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PartyCommands {

    private static final CommandHelp[] HELP = new CommandHelp[] {
            new CommandHelp("/party create", "Create a party"),
            new CommandHelp("/party disband", "Disband your party"),
            new CommandHelp("/party leave", "Leave your party"),
            new CommandHelp("/party join <name>", "Join a party"),
            new CommandHelp("/party kick <player>", "Kick a player from your party"),
            new CommandHelp("/party open", "Make your party open"),
            new CommandHelp("/party close", "Make your party closed"),
    };

    @Command(names = {"party", "party help"})
    public static void help(Player player) {
        for (CommandHelp help : HELP) {
            player.sendMessage(CC.YELLOW + help.getSyntax() + CC.AQUA + " - " + CC.PINK + help.getDescription());
        }
    }

    @Command(names = {"p create", "party create"})
    public static void create(Player player) {
        if (Nucleus.isFrozen(player)) {
            player.sendMessage(CC.RED + "You cannot create a party while frozen.");
            return;
        }

        PlayerData playerData = PlayerData.getByUuid(player.getUniqueId());

        if (playerData.getParty() != null) {
            player.sendMessage(CC.RED + "You already have a party.");
            return;
        }

        if (playerData.getState() != PlayerState.IN_LOBBY) {
            player.sendMessage(CC.RED + "You must be in the lobby to create a party.");
            return;
        }

        playerData.setParty(new Party(player));
        playerData.loadLayout();

        player.sendMessage(CC.YELLOW + "You created a new party.");
    }

    @Command(names = {"p disband", "party disband"})
    public static void disband(Player player) {
        PlayerData playerData = PlayerData.getByUuid(player.getUniqueId());

        if (playerData.getParty() == null) {
            player.sendMessage(CC.RED + "You do not have a party.");
            return;
        }

        if (!playerData.getParty().isLeader(player)) {
            player.sendMessage(CC.RED + "You are not the leader of your party.");
            return;
        }

        playerData.getParty().disband();
    }

    @Command(names = {"p invite", "party invite"})
    public static void invite(Player player, @Parameter(name = "target") Player target) {
        final PlayerData playerData = PlayerData.getByUuid(player.getUniqueId());

        if (playerData.getParty() == null) {
            player.sendMessage(CC.RED + "You do not have a party.");
            return;
        }

        if (!playerData.getParty().canInvite(target)) {
            player.sendMessage(CC.RED + "That player has already been invited to your party.");
            return;
        }

        if (playerData.getParty().containsPlayer(target)) {
            player.sendMessage(CC.RED + "That player is already in your party.");
            return;
        }

        if (playerData.getParty().getState() == PartyState.OPEN) {
            player.sendMessage(CC.RED + "The party state is Open. You do not need to invite players.");
            return;
        }

        final PlayerData targetData = PlayerData.getByUuid(target.getUniqueId());

        if (targetData.isInMatch() || targetData.isInQueue()) {
            player.sendMessage(Nucleus.getColoredName(target) + CC.RED + " is currently busy.");
            return;
        }

        playerData.getParty().invite(target);
    }

    @Command(names = {"p join", "party join"})
    public static void join(Player player, @Parameter(name = "target") String targetId) {
        if (Nucleus.isFrozen(player)) {
            player.sendMessage(CC.RED + "You cannot join a party while frozen.");
            return;
        }

        PlayerData playerData = PlayerData.getByUuid(player.getUniqueId());

        if (playerData.getParty() != null) {
            player.sendMessage(CC.RED + "You already have a party.");
            return;
        }

        Player target;

        try {
            target = Bukkit.getPlayer(UUID.fromString(targetId));
        } catch (Exception e) {
            target = Bukkit.getPlayer(targetId);
        }

        if (target == null) {
            player.sendMessage(CC.RED + "A player with that name could not be found.");
            return;
        }

        PlayerData targetData = PlayerData.getByUuid(target.getUniqueId());
        Party party = targetData.getParty();

        if (party == null) {
            player.sendMessage(CC.RED + "A party with that name could not be found.");
            return;
        }

        if (party.getState() == PartyState.CLOSED) {
            if (!party.isInvited(player)) {
                player.sendMessage(CC.RED + "You have not been invited to that party.");
                return;
            }
        }

        if (party.getPlayers().size() >= 32) {
            player.sendMessage(CC.RED + "That party is full and cannot hold anymore players.");
            return;
        }

        party.join(player);
    }

    @Command(names = {"p leave", "party leave"})
    public static void leave(Player player) {
        PlayerData playerData = PlayerData.getByUuid(player.getUniqueId());

        if (playerData.getParty() == null) {
            player.sendMessage(CC.RED + "You do not have a party.");
            return;
        }

        if (playerData.getParty().getLeader().getUuid().equals(player.getUniqueId())) {
            playerData.getParty().disband();
        } else {
            playerData.getParty().leave(player, false);
        }
    }

    @Command(names = {"p kick", "party kick"})
    public static void kick(Player player, @Parameter(name = "target") Player target) {
        PlayerData playerData = PlayerData.getByUuid(player.getUniqueId());

        if (playerData.getParty() == null) {
            player.sendMessage(CC.RED + "You do not have a party.");
            return;
        }

        if (!playerData.getParty().isLeader(player)) {
            player.sendMessage(CC.RED + "You are not the leader of your party.");
            return;
        }

        if (!playerData.getParty().containsPlayer(target)) {
            player.sendMessage(CC.RED + "That player is not a member of your party.");
            return;
        }

        if (player.equals(target)) {
            player.sendMessage(CC.RED + "You cannot kick yourself from your party.");
            return;
        }

        playerData.getParty().leave(target, true);
    }

    @Command(names = {"p close", "party close"})
    public static void open(Player player) {
        PlayerData playerData = PlayerData.getByUuid(player.getUniqueId());

        if (playerData.getParty() == null) {
            player.sendMessage(CC.RED + "You do not have a party.");
            return;
        }

        if (!playerData.getParty().isLeader(player)) {
            player.sendMessage(CC.RED + "You are not the leader of your party.");
            return;
        }

        playerData.getParty().setState(PartyState.CLOSED);
    }

    @Command(names = {"p open", "party open"})
    public static void close(Player player) {
        PlayerData playerData = PlayerData.getByUuid(player.getUniqueId());

        if (playerData.getParty() == null) {
            player.sendMessage(CC.RED + "You do not have a party.");
            return;
        }

        if (!playerData.getParty().isLeader(player)) {
            player.sendMessage(CC.RED + "You are not the leader of your party.");
            return;
        }

        playerData.getParty().setState(PartyState.OPEN);
    }

    @Command(names = {"p info", "party info", "party information"})
    public static void information(Player player) {
        PlayerData playerData = PlayerData.getByUuid(player.getUniqueId());

        if (!playerData.isLoaded()) {
            return;
        }

        if (playerData.getParty() == null) {
            player.sendMessage(CC.RED + "You do not have a party.");
            return;
        }

        playerData.getParty().sendInformation(player);
    }

}
