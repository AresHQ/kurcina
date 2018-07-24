package me.joeleoli.praxi.board;

import me.joeleoli.praxi.player.PlayerData;

import java.io.Serializable;
import java.util.List;

public interface BoardProcessor {

    /**
     * Checks if a player meets the criteria to be processed by this processor.
     *
     * @param player The player to check against.
     * @return If this player meets the criteria to be processed by this processor.
     */
    boolean canProcess(PlayerData player);

    /**
     * Processes a list of strings and returns them.
     *
     * @param player The player needed to build the list.
     * @param source The list of strings to build.
     * @return The value.
     */
    List<String> process(PlayerData player, List<String> source);

}
