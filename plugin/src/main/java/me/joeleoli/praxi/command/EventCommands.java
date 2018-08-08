package me.joeleoli.praxi.command;

import me.joeleoli.nucleus.command.Command;
import me.joeleoli.nucleus.command.param.Parameter;
import me.joeleoli.nucleus.util.CC;

import me.joeleoli.praxi.Praxi;
import me.joeleoli.praxi.event.EventState;

import org.bukkit.entity.Player;

public class EventCommands {

    @Command(names = {"host", "startevent"}, permissionNode = "praxi.event.host")
    public static void hostEvent(Player player) {
        if (Praxi.getInstance().getEventManager().getActiveEvent() != null) {
            player.sendMessage(CC.RED + "There is an active event.");
            return;
        }

        if (!Praxi.getInstance().getEventManager().getEventCooldown().hasExpired()) {
            player.sendMessage(CC.RED + "There is currently a event host cooldown active.");
            return;
        }
    }

    @Command(names = {"event join"})
    public static void eventJoin(Player player) {
        if (Praxi.getInstance().getEventManager().getActiveEvent() == null) {
            player.sendMessage(CC.RED + "There is no active event.");
            return;
        }

        if (Praxi.getInstance().getEventManager().getActiveEvent().getState() != EventState.WAITING) {
            player.sendMessage(CC.RED + "This event is currently on-going and cannot be joined.");
            return;
        }

        Praxi.getInstance().getEventManager().getActiveEvent().handleJoin(player);
    }

    @Command(names = {"event leave"})
    public static void eventLeave(Player player) {
        if (Praxi.getInstance().getEventManager().getActiveEvent() == null) {
            player.sendMessage(CC.RED + "There is no active event.");
            return;
        }

        if (!Praxi.getInstance().getEventManager().getActiveEvent().getPlayers().contains(player)) {
            player.sendMessage(CC.RED + "You are not apart of the active event.");
            return;
        }

        Praxi.getInstance().getEventManager().getActiveEvent().handleLeave(player);
    }

    @Command(names = "eventmanager setspawn spec", permissionNode = "praxi.event.setspawn")
    public static void setSpawnSpectator(Player player) {
        Praxi.getInstance().getEventManager().setSumoSpawn1(player.getLocation());
        Praxi.getInstance().getEventManager().save();

        player.sendMessage(CC.GREEN + "Updated event's spawn spectator location.");
    }

    @Command(names = "eventmanager setspawn pos", permissionNode = "praxi.event.setspawn")
    public static void setSpawnPosition(Player player, @Parameter(name = "pos") int position) {
        if (!(position == 1 || position == 2)) {
            player.sendMessage(CC.RED + "The position must be 1 or 2.");
        } else {
            if (position == 1) {
                Praxi.getInstance().getEventManager().setSumoSpawn1(player.getLocation());
            } else {
                Praxi.getInstance().getEventManager().setSumoSpawn2(player.getLocation());
            }

            Praxi.getInstance().getEventManager().save();

            player.sendMessage(CC.GREEN + "Updated event's spawn location " + position + ".");
        }
    }

}
