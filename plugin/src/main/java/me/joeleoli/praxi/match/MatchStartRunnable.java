package me.joeleoli.praxi.match;

import me.joeleoli.praxi.config.Config;
import me.joeleoli.praxi.config.ConfigKey;
import me.joeleoli.praxi.script.ScriptContext;

import org.bukkit.scheduler.BukkitRunnable;

public class MatchStartRunnable extends BukkitRunnable {

    private Match match;
    private int tick;

    public MatchStartRunnable(Match match) {
        this.match = match;
    }

    @Override
    public void run() {
        int seconds = 5 - this.tick;

        if (seconds == 0) {
            this.match.setState(MatchState.FIGHTING);
            this.match.setStartTimestamp(System.currentTimeMillis());
            this.match.broadcast(Config.getStringList(ConfigKey.MATCH_COUNTDOWN_FINISHED));
            this.cancel();
            return;
        }

        final ScriptContext context = new ScriptContext(Config.getStringList(ConfigKey.MATCH_COUNTDOWN_LOOP));

        context.addVariable("seconds", seconds + "");
        context.addVariable("seconds_literal", seconds == 1 ? "second" : "seconds");

        this.match.broadcast(context.buildMultipleLines());

        this.tick++;
    }

}
