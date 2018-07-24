package me.joeleoli.praxi;

import me.joeleoli.praxi.hotbar.HotbarLayout;
import me.joeleoli.praxi.ladder.Ladder;
import me.joeleoli.praxi.queue.QueuePlayer;
import me.joeleoli.praxi.player.PlayerState;
import me.joeleoli.praxi.match.Match;
import me.joeleoli.praxi.player.PlayerHotbar;
import me.joeleoli.praxi.player.PlayerData;
import me.joeleoli.praxi.queue.Queue;

import org.apache.commons.lang3.Validate;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class PraxiProvider extends JavaPlugin implements PraxiAPI, Runnable {

    private int inQueues, inFights;
    private Map<UUID, AtomicInteger> queueFightCounts = new HashMap<>();

    @Override
    public void run() {
        int inQueues = 0;
        int inFights = 0;

        for (Player player : Bukkit.getOnlinePlayers()) {
            PlayerData playerData = PlayerData.getByUuid(player.getUniqueId());

            if (playerData != null) {
                if (playerData.getState() == PlayerState.IN_QUEUE) {
                    inQueues++;
                } else if (playerData.getState() == PlayerState.IN_FIGHT) {
                    inFights++;
                }
            }
        }

        this.inQueues = inQueues;
        this.inFights = inFights;

        Map<UUID, AtomicInteger> queueFightCounts = new HashMap<>();

        for (Match match : Match.getMatches()) {
            if (match.getQueueUuid() != null) {
                Queue queue = Queue.getByUuid(match.getQueueUuid());

                if (queue == null) {
                    continue;
                }

                if (queueFightCounts.containsKey(queue.getUuid())) {
                    queueFightCounts.get(queue.getUuid()).addAndGet(match.getMatchPlayers().size());
                } else {
                    queueFightCounts.put(queue.getUuid(), new AtomicInteger(match.getMatchPlayers().size()));
                }
            }
        }

        this.queueFightCounts = queueFightCounts;
    }

    @Override
    public PlayerData getPlayerData(Player player) {
        Validate.notNull(player);

        return PlayerData.getByUuid(player.getUniqueId());
    }

    @Override
    public Queue getQueue(UUID uuid) {
        return Queue.getByUuid(uuid);
    }

    @Override
    public Queue getQueue(Ladder ladder, boolean ranked) {
        for (Queue queue : Queue.getQueues()) {
            if (queue.getLadder().equals(ladder) && queue.isRanked() == ranked) {
                return queue;
            }
        }

        return null;
    }

    @Override
    public void loadHotbarLayout(Player player, HotbarLayout layout) {
        player.getInventory().setContents(PlayerHotbar.getLayout(layout));
        player.updateInventory();
    }

    @Override
    public Boolean joinQueue(Player player, Queue queue) {
        Validate.notNull(player);
        Validate.notNull(queue);

        PlayerData playerData = PlayerData.getByUuid(player.getUniqueId());

        if (playerData != null && playerData.getState() == PlayerState.IN_LOBBY) {
            queue.addPlayer(player, playerData.getPlayerStatistics().getElo(queue.getLadder()));
            return true;
        } else {
            return false;
        }
    }

    @Override
    public QueuePlayer leaveQueue(Player player) {
        Validate.notNull(player);

        PlayerData playerData = PlayerData.getByUuid(player.getUniqueId());

        if (playerData == null || playerData.getQueuePlayer() == null) {
            return null;
        }

        Queue queue = Queue.getByUuid(playerData.getQueuePlayer().getQueueUuid());

        if (queue == null) {
            return null;
        }

        return queue.removePlayer(player);
    }

    @Override
    public int getQueueingCount() {
        return this.inQueues;
    }

    @Override
    public int getFightingCount() {
        return this.inFights;
    }

    @Override
    public int getQueueingCount(Queue queue) {
        if (queue == null) {
            return 0;
        }

        return queue.getPlayers().size();
    }

    @Override
    public int getFightingCount(Queue queue) {
        if (queue == null) {
            return 0;
        }

        AtomicInteger atomic = this.queueFightCounts.get(queue.getUuid());

        if (atomic == null) {
            return 0;
        } else {
            return atomic.intValue();
        }
    }

}
