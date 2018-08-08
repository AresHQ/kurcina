package me.joeleoli.praxi.queue;

import me.joeleoli.nucleus.util.CC;
import me.joeleoli.praxi.ladder.Ladder;
import me.joeleoli.praxi.player.PlayerState;
import me.joeleoli.praxi.player.PlayerData;

import lombok.Getter;

import org.bukkit.Bukkit;
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
    private LinkedList<QueuePlayer> players;

    public Queue(Ladder ladder, boolean ranked) {
        this.ladder = ladder;
        this.ranked = ranked;
        this.players = new LinkedList<>();

        queues.add(this);
    }

    public void addPlayer(Player player, int elo) {
        final PlayerData playerData = PlayerData.getByUuid(player.getUniqueId());
        final QueuePlayer queuePlayer = new QueuePlayer(this.uuid, player.getUniqueId());
        final String message = CC.GREEN + "You joined the " + (this.ranked ? "Ranked" : "Unranked") + " " + this.ladder.getDisplayName() + CC.GREEN + " queue.";

        if (this.ranked) {
            queuePlayer.setElo(elo);
        }

        playerData.setState(PlayerState.IN_QUEUE);
        playerData.setQueuePlayer(queuePlayer);
        playerData.loadLayout();
        player.sendMessage(message);

        this.players.add(queuePlayer);
    }

    public QueuePlayer removePlayer(QueuePlayer queuePlayer) {
        this.players.remove(queuePlayer);

        final Player player = Bukkit.getPlayer(queuePlayer.getPlayerUuid());

        if (player != null && player.isOnline()) {
            player.sendMessage(CC.GREEN + "You left the " + (this.ranked ? "Ranked" : "Unranked") + " " + this.ladder.getDisplayName() + CC.GREEN + " queue.");
        }

        final PlayerData playerData = PlayerData.getByUuid(queuePlayer.getPlayerUuid());

        playerData.setQueuePlayer(null);
        playerData.setState(PlayerState.IN_LOBBY);
        playerData.loadLayout();

        return queuePlayer;
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
