package me.joeleoli.praxi.command;

import me.joeleoli.nucleus.command.Command;
import me.joeleoli.nucleus.command.param.Parameter;
import me.joeleoli.nucleus.util.Style;

import me.joeleoli.praxi.Praxi;
import me.joeleoli.praxi.events.EventState;
import me.joeleoli.praxi.events.impl.SumoEvent;

import org.bukkit.entity.Player;

public class EventCommands {

    @Command(names = {"host", "startevent"}, permissionNode = "praxi.events.host")
    public static void hostEvent(Player player) {
        if (Praxi.getInstance().getEventManager().getActiveEvent() != null) {
            player.sendMessage(Style.RED + "There is an active events.");
            return;
        }

        if (!Praxi.getInstance().getEventManager().getEventCooldown().hasExpired()) {
            player.sendMessage(Style.RED + "There is currently a events host cooldown active.");
            return;
        }

        Praxi.getInstance().getEventManager().setActiveEvent(new SumoEvent(player));
    }

    @Command(names = {"events join"})
    public static void eventJoin(Player player) {
        if (Praxi.getInstance().getEventManager().getActiveEvent() == null) {
            player.sendMessage(Style.RED + "There is no active events.");
            return;
        }

        if (Praxi.getInstance().getEventManager().getActiveEvent().getState() != EventState.WAITING) {
            player.sendMessage(Style.RED + "This events is currently on-going and cannot be joined.");
            return;
        }

        Praxi.getInstance().getEventManager().getActiveEvent().handleJoin(player);
    }

    @Command(names = {"events leave"})
    public static void eventLeave(Player player) {
        if (Praxi.getInstance().getEventManager().getActiveEvent() == null) {
            player.sendMessage(Style.RED + "There is no active events.");
            return;
        }

        if (!Praxi.getInstance().getEventManager().getActiveEvent().getEventPlayers().containsKey(player.getUniqueId())) {
            player.sendMessage(Style.RED + "You are not apart of the active events.");
            return;
        }

        Praxi.getInstance().getEventManager().getActiveEvent().handleDeath(player);
    }

    @Command(names = "eventmanager setspawn spec", permissionNode = "praxi.events.setspawn")
    public static void setSpawnSpectator(Player player) {
        Praxi.getInstance().getEventManager().setSumoSpawn1(player.getLocation());
        Praxi.getInstance().getEventManager().save();

        player.sendMessage(Style.GREEN + "Updated events's spawn spectator location.");
    }

    @Command(names = "eventmanager setspawn pos", permissionNode = "praxi.events.setspawn")
    public static void setSpawnPosition(Player player, @Parameter(name = "pos") int position) {
        if (!(position == 1 || position == 2)) {
            player.sendMessage(Style.RED + "The position must be 1 or 2.");
        } else {
            if (position == 1) {
                Praxi.getInstance().getEventManager().setSumoSpawn1(player.getLocation());
            } else {
                Praxi.getInstance().getEventManager().setSumoSpawn2(player.getLocation());
            }

            Praxi.getInstance().getEventManager().save();

            player.sendMessage(Style.GREEN + "Updated events's spawn location " + position + ".");
        }
    }

}
