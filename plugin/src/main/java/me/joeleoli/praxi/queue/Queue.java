package me.joeleoli.praxi.queue;

import me.joeleoli.praxi.config.Config;
import me.joeleoli.praxi.config.ConfigKey;
import me.joeleoli.praxi.script.ScriptContext;
import me.joeleoli.praxi.ladder.Ladder;
import me.joeleoli.praxi.party.Party;
import me.joeleoli.praxi.player.PlayerState;
import me.joeleoli.praxi.player.PlayerData;

import lombok.Getter;

import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.function.Predicate;

@Getter
public class Queue {

    @Getter
    private static List<Queue> queues = new ArrayList<>();

    private UUID uuid = UUID.randomUUID();
    private Ladder ladder;
    private boolean ranked;
    private boolean party;
    private LinkedList<QueuePlayer> players;

    public Queue(Ladder ladder, boolean party, boolean ranked) {
        this.ladder = ladder;
        this.party = party;
        this.ranked = ranked;
        this.players = new LinkedList<>();

        queues.add(this);
    }

    public void addPlayer(Player player, int elo) {
        final PlayerData playerData = PlayerData.getByUuid(player.getUniqueId());
        final QueuePlayer queuePlayer = new QueuePlayer(this.uuid, player.getUniqueId());
        final ScriptContext context = new ScriptContext(Config.getStringList(ConfigKey.QUEUE_JOIN_SUCCESS));

        context.addCondition("ranked", this.isRanked());
        context.addCondition("unranked", this.isRanked());
        context.addCondition("solo", !this.isParty());
        context.addCondition("party", this.isParty());
        context.getReplaceables().add(this.ladder);

        List<BaseComponent[]> components = context.buildComponents();

        if (this.party && playerData.getParty() != null) {
            playerData.getParty().getPlayers().forEach(other -> {
                PlayerData otherData = PlayerData.getByUuid(other.getUniqueId());

                otherData.setState(PlayerState.IN_QUEUE);
                otherData.setQueuePlayer(queuePlayer);
                otherData.loadLayout();

                components.forEach(other::sendMessage);
            });
        } else {
            if (this.ranked) {
                queuePlayer.setElo(elo);
            }

            playerData.setState(PlayerState.IN_QUEUE);
            playerData.setQueuePlayer(queuePlayer);
            playerData.loadLayout();

            components.forEach(player::sendMessage);
        }

        this.players.add(queuePlayer);
    }

    public QueuePlayer removePlayer(Player player) {
        final PlayerData playerData = PlayerData.getByUuid(player.getUniqueId());
        final QueuePlayer queuePlayer = playerData.getQueuePlayer();
        final ScriptContext context = new ScriptContext(Config.getStringList(ConfigKey.QUEUE_LEFT_SUCCESS));

        context.addCondition("ranked", this.isRanked());
        context.addCondition("unranked", this.isRanked());
        context.addCondition("solo", !this.isParty());
        context.addCondition("party", this.isParty());
        context.getReplaceables().add(this.ladder);

        List<BaseComponent[]> components = context.buildComponents();

        if (this.party && playerData.getParty() != null) {
            Party party = playerData.getParty();

            party.getPlayers().forEach(other -> {
                PlayerData otherData = PlayerData.getByUuid(other.getUniqueId());

                otherData.setQueuePlayer(null);
                otherData.setState(PlayerState.IN_LOBBY);
                otherData.loadLayout();

                components.forEach(other::sendMessage);
            });
        } else {
            playerData.setQueuePlayer(null);
            playerData.setState(PlayerState.IN_LOBBY);
            playerData.loadLayout();

            components.forEach(player::sendMessage);
        }

        this.players.remove(queuePlayer);

        return queuePlayer;
    }

    public static void init() {
        for (Ladder ladder : Ladder.getLadders()) {
            if (ladder.isEnabled()) {
                new Queue(ladder, false, false);
                new Queue(ladder, false, true);
                new Queue(ladder, true, false);
            }
        }
    }

    public static Queue getByUuid(UUID uuid) {
        for (Queue queue : queues) {
            if (queue.getUuid().equals(uuid)) {
                return queue;
            }
        }

        return null;
    }

    public static Queue getByPredicate(Predicate<Queue> predicate) {
        for (Queue queue : queues) {
            if (predicate.test(queue)) {
                return queue;
            }
        }

        return null;
    }

}
