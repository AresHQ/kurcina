package me.joeleoli.praxi.party;

import lombok.Getter;

import lombok.Setter;
import me.joeleoli.nucleus.chat.ChatComponentBuilder;
import me.joeleoli.nucleus.team.Team;
import me.joeleoli.nucleus.team.TeamPlayer;
import me.joeleoli.nucleus.util.CC;
import me.joeleoli.nucleus.util.ObjectUtil;
import me.joeleoli.praxi.player.PlayerState;
import me.joeleoli.praxi.player.PlayerData;
import me.joeleoli.praxi.queue.Queue;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;

import org.bukkit.entity.Player;

import java.util.*;

@Getter
public class Party extends Team<TeamPlayer> {

    @Getter
    private static List<Party> parties = new ArrayList<>();

    private PartyState state = PartyState.CLOSED;
    private Map<UUID, Long> invited;
    @Setter
    private PartyEvent selectedEvent;

    public Party(Player player) {
        super(new TeamPlayer(player.getUniqueId(), player.getName()));

        this.invited = new HashMap<>();

        parties.add(this);
    }

    public void setState(PartyState state) {
        this.state = state;

        this.broadcast(CC.YELLOW + "The party state has been changed to: " + CC.RESET + this.state.name());
    }

    public boolean canInvite(Player player) {
        for (UUID uuid : this.invited.keySet()) {
            if (uuid.equals(player.getUniqueId())) {
                if (System.currentTimeMillis() - this.invited.get(uuid) >= 30_000) {
                    this.invited.remove(uuid);
                    return true;
                }

                return false;
            }
        }

        return true;
    }

    public boolean isInvited(Player player) {
        for (UUID uuid : this.invited.keySet()) {
            if (uuid.equals(player.getUniqueId())) {
                if (System.currentTimeMillis() - this.invited.get(uuid) >= 30_000) {
                    this.invited.remove(uuid);
                    return false;
                }

                return true;
            }
        }

        return false;
    }

    public boolean isInQueue() {
        return PlayerData.getByUuid(this.getLeader().getUuid()).isInQueue();
    }

    public void cancelQueue() {
        Player leader = this.getLeader().toPlayer();
        PlayerData leaderData = PlayerData.getByUuid(leader.getUniqueId());

        if (leaderData.isInQueue()) {
            Queue queue = Queue.getByUuid(leaderData.getQueuePlayer().getQueueUuid());

            if (queue != null) {
                queue.removePlayer(leaderData.getQueuePlayer());
            }
        }
    }

    public void invite(Player target) {
        this.invited.put(target.getUniqueId(), System.currentTimeMillis());

        final HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentBuilder(CC.GRAY + "Click to join the party.").create());
        final ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/party join " + this.getLeader().getName());

        this.broadcast(CC.RESET + target.getDisplayName() + " " + CC.YELLOW + "has been invited to the party.");

        target.sendMessage(CC.YELLOW + "You have been invited to join " + CC.RESET + this.getLeader().getDisplayName() + CC.YELLOW + "'s party.");
        target.sendMessage(new ChatComponentBuilder("").parse(CC.GOLD + "Click here to join the party.").attachToEachPart(clickEvent).attachToEachPart(hoverEvent).create());
    }

    public void join(Player player) {
        if (this.isInQueue()) {
            this.cancelQueue();
        }

        this.getTeamPlayers().add(new TeamPlayer(player.getUniqueId(), player.getName()));
        this.invited.keySet().removeIf(uuid -> uuid.equals(player.getUniqueId()));
        this.broadcast(CC.RESET + player.getDisplayName() + CC.YELLOW + " has joined the party.");

        PlayerData playerData = PlayerData.getByUuid(player.getUniqueId());

        playerData.setParty(this);
        playerData.loadLayout();
    }

    public void leave(Player player, boolean kick) {
        if (this.isInQueue()) {
            this.cancelQueue();
        }

        this.broadcast(CC.RESET + player.getDisplayName() + CC.YELLOW + " has " + (kick ? "been kicked" : "left") + " the party.");
        this.getTeamPlayers().removeIf(playerInfo -> playerInfo.getUuid().equals(player.getUniqueId()));

        PlayerData playerData = PlayerData.getByUuid(player.getUniqueId());

        playerData.setParty(null);
        playerData.loadLayout();
    }

    public void disband() {
        parties.remove(this);

        if (this.isInQueue()) {
            this.cancelQueue();
        }

        this.broadcast(CC.YELLOW + "The party has been disbanded.");

        this.getPlayers().forEach(player -> {
            PlayerData playerData = PlayerData.getByUuid(player.getUniqueId());

            playerData.setParty(null);

            if (playerData.getState() == PlayerState.IN_LOBBY) {
                playerData.loadLayout();
            }
        });
    }

    public void sendInformation(Player player) {
        StringBuilder builder = new StringBuilder();

        for (Player member : this.getPlayers()) {
            builder.append(member.getName());
            builder.append(", ");
        }

        final String[] lines = new String[]{
                CC.HORIZONTAL_SEPARATOR,
                CC.GOLD + "Party of " + this.getLeader().getName(),
                CC.YELLOW + "State: " + CC.GRAY + ObjectUtil.toReadable(this.state),
                CC.YELLOW + "Members: " + CC.GRAY + builder.toString().substring(0, builder.toString().length() - 2),
                CC.HORIZONTAL_SEPARATOR
        };

        player.sendMessage(lines);
    }

}
