package me.joeleoli.praxi.runnable;

import me.joeleoli.praxi.match.MatchSnapshot;

public class InventoryCleanupRunnable implements Runnable {

    @Override
    public void run() {
        MatchSnapshot.getCache().entrySet().removeIf(entry -> System.currentTimeMillis() - entry.getValue()
                .getCreated() >= 30_000);
    }

}
