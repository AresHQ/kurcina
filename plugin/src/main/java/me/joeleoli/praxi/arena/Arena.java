package me.joeleoli.praxi.arena;

import lombok.Getter;
import lombok.Setter;

import me.joeleoli.commons.composer.Replaceable;

import me.joeleoli.praxi.cuboid.Cuboid;

import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Getter
@Setter
public class Arena extends Cuboid implements Replaceable {

    @Getter
    private static List<Arena> arenas = new ArrayList<>();

    protected String name;
    private ArenaType type;
    protected Location spawn1;
    protected Location spawn2;
    protected boolean active;

    public Arena(String name, ArenaType type, Location location1, Location location2) {
        super(location1, location2);

        this.name = name;
        this.type = type;
    }

    public void save() {}

    public void delete() {}

    public boolean isSetup() {
        return this.spawn1 != null && this.spawn2 != null;
    }

    @Override
    public String replace(String source) {
        return source
                .replace("{arena_name}", this.name)
                .replace("{arena_type}", this.type.name());
    }

    public static Arena getByName(String name) {
        for (Arena arena : arenas) {
            if (arena.getType() != ArenaType.DUPLICATE && arena.getName() != null && arena.getName().equalsIgnoreCase(name)) {
                return arena;
            }
        }

        return null;
    }

    public static Arena getRandomByType(ArenaType type) {
        List<Arena> _arenas = arenas.stream().filter(arena -> arena.getType() == type && arena.isSetup() && !arena.isActive()).collect(Collectors.toList());

        if (_arenas.isEmpty()) {
            return null;
        }

        return _arenas.get(ThreadLocalRandom.current().nextInt(_arenas.size()));
    }

}
