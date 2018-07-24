package me.joeleoli.praxi.command;

import me.joeleoli.commons.command.Command;
import me.joeleoli.commons.command.CommandHelp;
import me.joeleoli.commons.command.param.Parameter;
import me.joeleoli.commons.util.CC;

import me.joeleoli.praxi.party.PartyState;
import me.joeleoli.praxi.player.PlayerState;
import me.joeleoli.praxi.config.Config;
import me.joeleoli.praxi.config.ConfigKey;
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

    @Command(names = "party create")
    public static void create(Player player) {
        PlayerData playerData = PlayerData.getByUuid(player.getUniqueId());

        if (playerData.getParty() != null) {
            player.sendMessage(Config.getString(ConfigKey.PARTY_ERROR_IN_PARTY));
            return;
        }

        if (playerData.getState() != PlayerState.IN_LOBBY) {
            player.sendMessage(Config.getString(ConfigKey.PARTY_CREATE_REJECTED_STATE));
            return;
        }

        playerData.setParty(new Party(player));
        playerData.loadLayout();

        Config.getStringList(ConfigKey.PARTY_CREATE_SUCCESS).forEach(line -> {
            player.sendMessage(Config.translatePlayerAndTarget(line, player, null));
        });
    }

    @Command(names = "party disband")
    public static void disband(Player player) {
        PlayerData playerData = PlayerData.getByUuid(player.getUniqueId());

        if (playerData.getParty() == null) {
            player.sendMessage(Config.getString(ConfigKey.PARTY_ERROR_NO_PARTY));
            return;
        }

        if (!playerData.getParty().isLeader(player)) {
            player.sendMessage(Config.getString(ConfigKey.PARTY_ERROR_NOT_LEADER));
            return;
        }

        playerData.getParty().disband();
    }

    @Command(names = "party invite")
    public static void invite(Player player, @Parameter(name = "target") Player target) {
        PlayerData playerData = PlayerData.getByUuid(player.getUniqueId());

        if (playerData.getParty() == null) {
            player.sendMessage(Config.getString(ConfigKey.PARTY_ERROR_NO_PARTY));
            return;
        }

        if (!playerData.getParty().canInvite(target)) {
            player.sendMessage(Config.getString(ConfigKey.PARTY_INVITE_ALREADY_INVITED, player, target));
            return;
        }

        if (playerData.getParty().containsPlayer(target)) {
            player.sendMessage(Config.getString(ConfigKey.PARTY_INVITE_ALREADY_MEMBER, player, target));
            return;
        }

        if (playerData.getParty().getState() == PartyState.OPEN) {
            player.sendMessage(Config.getString(ConfigKey.PARTY_INVITE_STATE_OPEN));
            return;
        }

        playerData.getParty().invite(target);
    }

    @Command(names = "party join")
    public static void join(Player player, @Parameter(name = "target") String targetId) {
        PlayerData playerData = PlayerData.getByUuid(player.getUniqueId());

        if (playerData.getParty() != null) {
            player.sendMessage(Config.getString(ConfigKey.PARTY_ERROR_IN_PARTY));
            return;
        }

        Player target;

        try {
            target = Bukkit.getPlayer(UUID.fromString(targetId));
        } catch (Exception e) {
            target = Bukkit.getPlayer(targetId);
        }

        if (target == null) {
            player.sendMessage(Config.getString(ConfigKey.PARTY_JOIN_NOT_FOUND));
            return;
        }

        PlayerData targetData = PlayerData.getByUuid(target.getUniqueId());
        Party party = targetData.getParty();

        if (party == null) {
            player.sendMessage(Config.getString(ConfigKey.PARTY_JOIN_NOT_FOUND));
            return;
        }

        if (party.getState() == PartyState.CLOSED) {
            if (!party.isInvited(player)) {
                player.sendMessage(Config.getString(ConfigKey.PARTY_JOIN_NOT_INVITED));
                return;
            }
        }

        if (party.getPlayers().size() >= Config.getInteger(ConfigKey.PARTY_MAX_SIZE)) {
            player.sendMessage(Config.getString(ConfigKey.PARTY_JOIN_FULL));
            return;
        }

        party.join(player);
    }

    @Command(names = "party leave")
    public static void leave(Player player) {
        PlayerData playerData = PlayerData.getByUuid(player.getUniqueId());

        if (playerData.getParty() == null) {
            player.sendMessage(Config.getString(ConfigKey.PARTY_ERROR_NO_PARTY));
            return;
        }

        if (playerData.getParty().getLeader().getUuid().equals(player.getUniqueId())) {
            playerData.getParty().disband();
        } else {
            playerData.getParty().leave(player, false);
        }
    }

    @Command(names = "party kick")
    public static void kick(Player player, @Parameter(name = "target") Player target) {
        PlayerData playerData = PlayerData.getByUuid(player.getUniqueId());

        if (playerData.getParty() == null) {
            player.sendMessage(Config.getString(ConfigKey.PARTY_ERROR_NO_PARTY));
            return;
        }

        if (!playerData.getParty().isLeader(player)) {
            player.sendMessage(Config.getString(ConfigKey.PARTY_ERROR_NOT_LEADER));
            return;
        }

        if (!playerData.getParty().containsPlayer(target)) {
            player.sendMessage(Config.getString(ConfigKey.PARTY_ERROR_MEMBER_NOT_FOUND));
            return;
        }

        if (player.equals(target)) {
            player.sendMessage(Config.getString(ConfigKey.PARTY_KICK_SELF));
            return;
        }

        playerData.getParty().leave(target, true);
    }

    @Command(names = "party close")
    public static void open(Player player) {
        PlayerData playerData = PlayerData.getByUuid(player.getUniqueId());

        if (playerData.getParty() == null) {
            player.sendMessage(Config.getString(ConfigKey.PARTY_ERROR_NO_PARTY));
            return;
        }

        if (!playerData.getParty().isLeader(player)) {
            player.sendMessage(Config.getString(ConfigKey.PARTY_ERROR_NOT_LEADER));
            return;
        }

        playerData.getParty().setState(PartyState.CLOSED);
    }

    @Command(names = "party open")
    public static void close(Player player) {
        PlayerData playerData = PlayerData.getByUuid(player.getUniqueId());

        if (playerData.getParty() == null) {
            player.sendMessage(Config.getString(ConfigKey.PARTY_ERROR_NO_PARTY));
            return;
        }

        if (!playerData.getParty().isLeader(player)) {
            player.sendMessage(Config.getString(ConfigKey.PARTY_ERROR_NOT_LEADER));
            return;
        }

        playerData.getParty().setState(PartyState.OPEN);
    }

    @Command(names = {"party info", "party information"})
    public static void information(Player player) {
        PlayerData playerData = PlayerData.getByUuid(player.getUniqueId());

        if (!playerData.isLoaded()) {
            return;
        }

        if (playerData.getParty() == null) {
            player.sendMessage(Config.getString(ConfigKey.PARTY_ERROR_NO_PARTY));
            return;
        }

        playerData.getParty().sendInformation(player);
    }

}
