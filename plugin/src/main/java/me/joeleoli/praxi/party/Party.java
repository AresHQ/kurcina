package me.joeleoli.praxi.party;

import lombok.Getter;

import me.joeleoli.commons.chat.ChatComponentBuilder;
import me.joeleoli.commons.chat.ChatComponentExtras;
import me.joeleoli.commons.util.CC;

import me.joeleoli.commons.util.EnumUtil;
import me.joeleoli.commons.util.Pair;
import me.joeleoli.praxi.script.ScriptContext;
import me.joeleoli.praxi.script.wrapper.PlayerWrapper;
import me.joeleoli.praxi.team.Team;
import me.joeleoli.praxi.team.TeamPlayer;
import me.joeleoli.praxi.player.PlayerState;
import me.joeleoli.praxi.config.Config;
import me.joeleoli.praxi.config.ConfigKey;
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
    private Map<TeamPlayer, Long> invited;

    public Party(Player player) {
        super(new TeamPlayer(player.getUniqueId(), player.getName()));

        this.invited = new HashMap<>();

        parties.add(this);
    }

    @Override
    public List<Pair<String, String>> getReplacements() {
        return Arrays.asList(
                new Pair<>("party_count", this.getTeamPlayers().size() + ""),
                new Pair<>("party_leader_name", this.getLeader().getName()),
                new Pair<>("party_leader_display_name", this.getLeader().getDisplayName()),
                new Pair<>("party_state", EnumUtil.toReadable(this.getState()))
        );
    }

    public void setState(PartyState state) {
        this.state = state;

        this.broadcast(Config.getString(ConfigKey.PARTY_STATE_CHANGE, this));
    }

    public boolean canInvite(Player player) {
        for (TeamPlayer playerInfo : this.invited.keySet()) {
            if (playerInfo.getUuid().equals(player.getUniqueId())) {
                return System.currentTimeMillis() - this.invited.get(playerInfo) + 30_0000 >= 0;
            }
        }

        return true;
    }

    public boolean isInvited(Player player) {
        for (TeamPlayer playerInfo : this.invited.keySet()) {
            if (playerInfo.getUuid().equals(player.getUniqueId())) {
                return System.currentTimeMillis() - this.invited.get(playerInfo) + 30_0000 < 0;
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
                queue.removePlayer(leader);
            }
        }
    }

    public void invite(Player target) {
        this.invited.put(new TeamPlayer(target.getUniqueId(), target.getName()), System.currentTimeMillis());

        final HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ChatComponentBuilder(CC.GRAY + "Click to join the party.").create());
        final ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/party join " + this.getLeader().getName());
        final ScriptContext context = new ScriptContext(Config.getStringList(ConfigKey.PARTY_INVITE_SUCCESS));

        context.getReplaceables().add(this);
        context.getReplaceables().add(new PlayerWrapper(target, "target"));
        context.addComponentExtras("clickable", new ChatComponentExtras(hoverEvent, clickEvent));

        this.broadcastComponents(context.buildComponents());

        context.setLines(Config.getStringList(ConfigKey.PARTY_INVITE_RECEIVER));
        context.buildComponents().forEach(target::sendMessage);
    }

    public void join(Player player) {
        if (this.isInQueue()) {
            this.cancelQueue();
        }

        this.getTeamPlayers().add(new TeamPlayer(player.getUniqueId(), player.getName()));
        this.invited.keySet().removeIf(playerInfo -> playerInfo.getUuid().equals(player.getUniqueId()));

        final ScriptContext context = new ScriptContext(Config.getStringList(ConfigKey.PARTY_JOIN_SUCCESS));

        context.getReplaceables().add(this);
        context.getReplaceables().add(new PlayerWrapper(player));

        this.broadcastComponents(context.buildComponents());

        PlayerData playerData = PlayerData.getByUuid(player.getUniqueId());

        playerData.setParty(this);
        playerData.loadLayout();
    }

    public void leave(Player player, boolean kick) {
        if (this.isInQueue()) {
            this.cancelQueue();
        }

        final ScriptContext context = new ScriptContext(Config.getStringList(kick ? ConfigKey.PARTY_KICK_SUCCESS : ConfigKey.PARTY_LEAVE_SUCCESS));

        context.getReplaceables().add(this);
        context.getReplaceables().add(new PlayerWrapper(player));

        this.broadcastComponents(context.buildComponents());
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

        final ScriptContext context = new ScriptContext(Config.getStringList(ConfigKey.PARTY_DISBANDED));

        context.getReplaceables().add(this);

        this.broadcastComponents(context.buildComponents());

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

        for (String line : Config.getStringList(ConfigKey.PARTY_INFORMATION)) {
            player.sendMessage(
                    Config.translateParty(line, this)
                            .replace("{party_member_list}", builder.substring(0, builder.length() - 2))
            );
        }
    }

}
