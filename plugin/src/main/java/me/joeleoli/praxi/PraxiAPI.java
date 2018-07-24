package me.joeleoli.praxi;

import me.joeleoli.praxi.hotbar.HotbarLayout;
import me.joeleoli.praxi.ladder.Ladder;
import me.joeleoli.praxi.player.PlayerData;
import me.joeleoli.praxi.queue.Queue;
import me.joeleoli.praxi.queue.QueuePlayer;

import org.bukkit.entity.Player;

import java.util.UUID;

public interface PraxiAPI {

    /**
     * Gets the player data of a player.
     *
     * @param player The player.
     * @return The player's data.
     */
    PlayerData getPlayerData(Player player);

    /**
     * Gets a queue from a UUID if present.
     *
     * @param uuid The UUID.
     * @return The queue with the UUID that matches the given UUID, otherwise null.
     */
    Queue getQueue(UUID uuid);

    /**
     * Gets a queue that matches criteria if present.
     *
     * @param ladder The ladder.
     * @param ranked If the queue to get is ranked or not.
     * @return The queue that matches the given criteria, otherwise null.
     */
    Queue getQueue(Ladder ladder, boolean ranked);

    /**
     * Sets the player's inventory contents to the contents of a hotbar layout.
     *
     * @param player The player that receives the layout.
     * @param layout The layout to give.
     */
    void loadHotbarLayout(Player player, HotbarLayout layout);

    /**
     * Adds a player into a queue.
     *
     * @param player The player.
     * @param queue The queue.
     * @return If the player was allowed into the queue.
     */
    Boolean joinQueue(Player player, Queue queue);

    /**
     * @param player The player leaving the queue.
     * @return The queue the player has left.
     *         If the player was not in a queue, the result is null.
     */
    QueuePlayer leaveQueue(Player player);

    /**
     * Gets the amount of players that are queueing.
     *
     * @return The amount of players that are queueing.
     */
    int getQueueingCount();

    /**
     * Gets the amount of players that are fighting.
     *
     * @return The amount of players that are fighting.
     */
    int getFightingCount();

    /**
     * Gets the amount of players in a queue.
     *
     * @param queue The queue.
     * @return The amount of players in the given queue.
     */
    int getQueueingCount(Queue queue);

    /**
     * Gets the amount of players in matches originating from a queue.
     *
     * @param queue The queue.
     * @return The amount of players in matches originating from the given queue.
     */
    int getFightingCount(Queue queue);

}
