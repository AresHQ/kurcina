package me.joeleoli.praxi.match;

import lombok.Getter;
import lombok.Setter;

import me.joeleoli.commons.team.TeamPlayer;

import org.bukkit.entity.Player;

@Getter
@Setter
public class MatchPlayer extends TeamPlayer {

    private boolean alive = true;
    private boolean disconnected;
    private int elo, potionsThrown, potionsMissed, hits, combo, longestCombo;

    public MatchPlayer(Player player) {
        super(player.getUniqueId(), player.getName());
    }

    public double getPotionAccuracy() {
        if (this.potionsMissed == 0) {
            return 100.0;
        }

        return 100.0 - (this.potionsMissed / this.potionsThrown);
    }

}
