package me.joeleoli.praxi;

import com.comphenix.protocol.ProtocolLibrary;

import com.google.gson.JsonParser;

import lombok.Getter;
import me.joeleoli.commons.Commons;
import me.joeleoli.commons.board.BoardManager;
import me.joeleoli.commons.command.CommandHandler;
import me.joeleoli.commons.config.ConfigCursor;
import me.joeleoli.commons.config.ConfigVariable;
import me.joeleoli.commons.config.FileConfig;
import me.joeleoli.commons.listener.ListenerHandler;
import me.joeleoli.commons.redis.JedisSettings;
import me.joeleoli.commons.util.LocationUtil;

import me.joeleoli.praxi.arena.Arena;
import me.joeleoli.praxi.arena.ArenaType;
import me.joeleoli.praxi.arena.SharedArena;
import me.joeleoli.praxi.arena.StandaloneArena;
import me.joeleoli.praxi.board.PracticeBoardAdapter;
import me.joeleoli.praxi.command.param.ArenaParameterType;
import me.joeleoli.praxi.command.param.ArenaTypeParameterType;
import me.joeleoli.praxi.command.param.LadderParameterType;
import me.joeleoli.praxi.command.param.QueueParameterType;
import me.joeleoli.praxi.config.Config;
import me.joeleoli.praxi.config.ConfigKey;
import me.joeleoli.praxi.ladder.Ladder;
import me.joeleoli.praxi.mongo.PracticeMongo;
import me.joeleoli.praxi.packet.PotionPacketListener;
import me.joeleoli.praxi.packet.SoundPacketListener;
import me.joeleoli.praxi.player.PlayerHotbar;
import me.joeleoli.praxi.player.PlayerData;
import me.joeleoli.praxi.queue.Queue;
import me.joeleoli.praxi.queue.QueueThread;
import me.joeleoli.praxi.runnable.ExpBarRunnable;
import me.joeleoli.praxi.runnable.InventoryCleanupRunnable;
import me.joeleoli.praxi.runnable.InviteCleanupRunnable;
import me.joeleoli.praxi.runnable.SaveRunnable;
import me.joeleoli.praxi.script.processor.ConditionProcessor;
import me.joeleoli.praxi.script.processor.ForEachProcessor;
import me.joeleoli.praxi.script.processor.VariableProcessor;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.Random;

@Getter
public class Praxi extends PraxiProvider {

    @Getter
    private static Praxi instance;
    public static Random RANDOM = new Random();
    public static JsonParser PARSER = new JsonParser();

    private long startup = System.currentTimeMillis();
    private boolean loaded;
    private FileConfig mainConfig, arenaConfig, ladderConfig;

    @Override
    public void onEnable() {
        // Set instance
        instance = this;

        // Load configs
        this.mainConfig = new FileConfig(this, "config.yml");
        this.arenaConfig = new FileConfig(this, "arenas.yml");
        this.ladderConfig = new FileConfig(this, "ladders.yml");

        // Setup Commons
        Commons.init(this);
        Commons.setupJedis(new JedisSettings(
                this.mainConfig.getConfig().getString("redis.host"),
                this.mainConfig.getConfig().getInt("redis.port"),
                this.mainConfig.getConfig().getString("redis.password")
        ));

        // Load modules
        ConfigVariable.init(new ConfigCursor(this.mainConfig, "context"));
        Config.init();
        Ladder.init();
        Queue.init();
        PlayerHotbar.init();
        this.loadArenas();

        // Load database
        new PracticeMongo();

        // Register parameter types
        CommandHandler.registerParameterType(Arena.class, new ArenaParameterType());
        CommandHandler.registerParameterType(ArenaType.class, new ArenaTypeParameterType());
        CommandHandler.registerParameterType(Ladder.class, new LadderParameterType());
        CommandHandler.registerParameterType(Queue.class, new QueueParameterType());

        // Load commands and listeners
        CommandHandler.loadCommandsFromPackage(this, "me.joeleoli.praxi.command");
        ListenerHandler.loadListenersFromPackage(this, "me.joeleoli.praxi.listener");

        // Instantiate board
        Commons.setBoardManager(new BoardManager(this, new PracticeBoardAdapter()));

        // Start threads
        new QueueThread().start();

        // Start tasks
        if (Config.getBoolean(ConfigKey.ENDER_PEARL_CD_USE_BAR)) {
            this.getServer().getScheduler().runTaskTimer(this, new ExpBarRunnable(), 2L, 2L);
        }

        // Add packet listeners
        ProtocolLibrary.getProtocolManager().addPacketListener(new PotionPacketListener());
        ProtocolLibrary.getProtocolManager().addPacketListener(new SoundPacketListener());

        // Start tasks
        this.getServer().getScheduler().runTaskTimerAsynchronously(this, this, 0L, 10L);
        this.getServer().getScheduler().runTaskTimerAsynchronously(this, new InventoryCleanupRunnable(), 20L * 5, 20L * 5);
        this.getServer().getScheduler().runTaskTimerAsynchronously(this, new InviteCleanupRunnable(), 20L * 5, 20L * 5);
        this.getServer().getScheduler().runTaskTimerAsynchronously(this, new SaveRunnable(), 20L * 60 * 5, 20L * 60 * 5);

        // Prevent players from joining too early
        this.getServer().getScheduler().runTaskLater(this, () -> {
            this.loaded = true;
        }, 20L * 10);
    }

    @Override
    public void onDisable() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            final PlayerData playerData = PlayerData.getByUuid(player.getUniqueId());

            if (playerData != null) {
                this.getServer().getScheduler().runTaskAsynchronously(this, playerData::save);
            }
        }
    }

    private void loadArenas() {
        ConfigCursor cursor = new ConfigCursor(this.arenaConfig, "arenas");

        if (cursor.exists()) {
            for (String arenaName : cursor.getKeys()) {
                cursor.setPath("arenas." + arenaName);

                ArenaType arenaType = ArenaType.valueOf(cursor.getString("type"));
                Location location1 = LocationUtil.deserialize(cursor.getString("cuboid.location1"));
                Location location2 = LocationUtil.deserialize(cursor.getString("cuboid.location2"));

                Arena arena;

                if (arenaType == ArenaType.STANDALONE) {
                    arena = new StandaloneArena(arenaName, location1, location2);
                } else if (arenaType == ArenaType.SHARED) {
                    arena = new SharedArena(arenaName, location1, location2);
                } else {
                    continue;
                }

                if (cursor.exists("spawn1")) {
                    arena.setSpawn1(LocationUtil.deserialize(cursor.getString("spawn1")));
                }

                if (cursor.exists("spawn2")) {
                    arena.setSpawn2(LocationUtil.deserialize(cursor.getString("spawn2")));
                }

                if (arena.getType() == ArenaType.STANDALONE && cursor.exists("duplicates")) {
                    for (String duplicateId : cursor.getKeys("duplicates")) {
                        cursor.setPath("arenas." + arenaName + ".duplicates." + duplicateId);

                        location1 = LocationUtil.deserialize(cursor.getString("cuboid.location1"));
                        location2 = LocationUtil.deserialize(cursor.getString("cuboid.location2"));
                        Location spawn1 = LocationUtil.deserialize(cursor.getString("spawn1"));
                        Location spawn2 = LocationUtil.deserialize(cursor.getString("spawn2"));

                        Arena duplicate = new Arena(arenaName, ArenaType.DUPLICATE, location1, location2);

                        duplicate.setSpawn1(spawn1);
                        duplicate.setSpawn2(spawn2);

                        ((StandaloneArena) arena).getDuplicates().add(duplicate);
                    }
                }

                this.getLogger().info("Loaded arena " + arenaName);

                Arena.getArenas().add(arena);
            }
        }
    }

}
