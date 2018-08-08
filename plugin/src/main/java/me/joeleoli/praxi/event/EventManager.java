package me.joeleoli.praxi.event;

import lombok.Getter;
import lombok.Setter;

import me.joeleoli.nucleus.config.ConfigCursor;
import me.joeleoli.nucleus.cooldown.Cooldown;
import me.joeleoli.nucleus.util.LocationUtil;

import me.joeleoli.praxi.Praxi;

import org.bukkit.Location;

@Getter
public class EventManager {

    private Event activeEvent;
    private Cooldown eventCooldown = new Cooldown(0);
    @Setter
    private Location sumoSpectator, sumoSpawn1, sumoSpawn2;

    public void load() {
        ConfigCursor cursor = new ConfigCursor(Praxi.getInstance().getMainConfig(), "event");

        if (cursor.exists("sumo.spectator")) {
            this.sumoSpectator = LocationUtil.deserialize(cursor.getString("sumo.spectator"));
        }

        if (cursor.exists("sumo.spawn1")) {
            this.sumoSpawn1 = LocationUtil.deserialize(cursor.getString("sumo.spawn1"));
        }

        if (cursor.exists("sumo.spawn2")) {
            this.sumoSpawn2 = LocationUtil.deserialize(cursor.getString("sumo.spawn2"));
        }
    }

    public void save() {
        ConfigCursor cursor = new ConfigCursor(Praxi.getInstance().getMainConfig(), "event");

        if (this.sumoSpectator != null) {
            cursor.set("sumo.spectator", LocationUtil.serialize(this.sumoSpectator));
        }

        if (this.sumoSpawn1 != null) {
            cursor.set("sumo.spawn1", LocationUtil.serialize(this.sumoSpawn1));
        }

        if (this.sumoSpawn2 != null) {
            cursor.set("sumo.spawn2", LocationUtil.serialize(this.sumoSpawn2));
        }

        cursor.save();
    }

}
