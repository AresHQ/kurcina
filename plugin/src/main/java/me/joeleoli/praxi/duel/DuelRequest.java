package me.joeleoli.praxi.duel;

import lombok.Data;

import me.joeleoli.praxi.arena.Arena;
import me.joeleoli.praxi.ladder.Ladder;

import java.util.UUID;

@Data
public class DuelRequest {

    private UUID sender;
    private Ladder ladder;
    private Arena arena;
    private long timestamp = System.currentTimeMillis();

    public DuelRequest(UUID uuid) {
        this.sender = uuid;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() - this.timestamp >= 30_000;
    }

}
