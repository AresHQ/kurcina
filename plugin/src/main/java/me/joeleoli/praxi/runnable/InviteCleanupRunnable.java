package me.joeleoli.praxi.runnable;

import me.joeleoli.praxi.party.Party;

public class InviteCleanupRunnable implements Runnable {

    @Override
    public void run() {
        Party.getParties().forEach(party -> party.getInvited().entrySet().removeIf(entry -> System.currentTimeMillis() >= entry.getValue() + 30_000));
    }

}
