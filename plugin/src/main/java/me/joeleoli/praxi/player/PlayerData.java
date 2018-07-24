package me.joeleoli.praxi.player;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import com.mongodb.client.model.ReplaceOptions;

import me.joeleoli.commons.util.InventoryUtil;
import me.joeleoli.commons.util.MongoUtil;
import me.joeleoli.commons.util.PlayerUtil;
import me.joeleoli.commons.util.Position;

import me.joeleoli.praxi.Praxi;
import me.joeleoli.praxi.config.Config;
import me.joeleoli.praxi.config.ConfigKey;
import me.joeleoli.praxi.hotbar.HotbarLayout;
import me.joeleoli.praxi.kit.NamedKit;
import me.joeleoli.praxi.cooldown.Cooldown;
import me.joeleoli.praxi.duel.DuelRequest;
import me.joeleoli.praxi.duel.DuelProcedure;
import me.joeleoli.praxi.ladder.Ladder;
import me.joeleoli.praxi.match.Match;
import me.joeleoli.praxi.mongo.PracticeMongo;
import me.joeleoli.praxi.party.Party;
import me.joeleoli.praxi.queue.QueuePlayer;

import lombok.Getter;
import lombok.Setter;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

@Getter
public class PlayerData extends PlayerInfo {

    @Getter
    private static Map<UUID, PlayerData> players = new HashMap<>();

    private final Bson filter;
    @Setter
    private PlayerState state;
    private PlayerStatistics playerStatistics = new PlayerStatistics();
    private PlayerSettings playerSettings = new PlayerSettings();
    private KitEditor kitEditor = new KitEditor();
    private Map<String, NamedKit[]> kits = new HashMap<>();
    @Setter
    private Party party;
    @Setter
    private Match match;
    @Setter
    private QueuePlayer queuePlayer;
    @Setter
    private Cooldown enderpearlCooldown;
    private List<Position> splashPositions = new ArrayList<>();
    private List<Position> soundPositions = new ArrayList<>();
    private List<DuelRequest> duelRequests = new ArrayList<>();
    @Setter
    private DuelProcedure duelProcedure;

    private boolean loaded;

    public PlayerData(UUID uuid, String name) {
        super(uuid, name);

        this.filter = MongoUtil.getFindDocument("uuid", this.getUuid().toString());
        this.state = PlayerState.IN_LOBBY;

        for (Ladder ladder : Ladder.getLadders()) {
            this.kits.put(ladder.getName(), new NamedKit[4]);
        }
    }

    public boolean isInQueue() {
        return this.state == PlayerState.IN_QUEUE && this.queuePlayer != null;
    }

    public boolean isInMatch() {
        return (this.state == PlayerState.IN_FIGHT) && this.match != null;
    }

    public boolean isSpectating() {
        return this.state == PlayerState.SPECTATE_MATCH && this.match != null;
    }

    public boolean isOnEnderpearlCooldown() {
        return this.enderpearlCooldown != null && this.enderpearlCooldown.getPassed() < Config.getLong(ConfigKey
                .ENDER_PEARL_CD_TIME);
    }

    public void loadLayout() {
        Player player = this.toPlayer();

        if (player == null) {
            return;
        }

        PlayerUtil.reset(player);

        if (this.state == PlayerState.IN_LOBBY) {
            if (this.party == null) {
                player.getInventory().setContents(PlayerHotbar.getLayout(HotbarLayout.LOBBY_NO_PARTY));
            } else {
                if (this.party.getLeader().getUuid().equals(player.getUniqueId())) {
                    player.getInventory().setContents(PlayerHotbar.getLayout(HotbarLayout.LOBBY_PARTY_LEADER));
                } else {
                    player.getInventory().setContents(PlayerHotbar.getLayout(HotbarLayout.LOBBY_PARTY_MEMBER));
                }
            }
        } else if (this.isInQueue()) {
            if (this.party == null) {
                player.getInventory().setContents(PlayerHotbar.getLayout(HotbarLayout.QUEUE_NO_PARTY));
            } else {
                if (this.party.getLeader().getUuid().equals(player.getUniqueId())) {
                    player.getInventory().setContents(PlayerHotbar.getLayout(HotbarLayout.QUEUE_PARTY_LEADER));
                } else {
                    player.getInventory().setContents(PlayerHotbar.getLayout(HotbarLayout.QUEUE_PARTY_MEMBER));
                }
            }
        } else if (this.state == PlayerState.SPECTATE_MATCH && this.match != null) {
            player.getInventory().setContents(PlayerHotbar.getLayout(HotbarLayout.SPECTATE));
        }

        player.updateInventory();
    }

    public NamedKit[] getKits(Ladder ladder) {
        return this.kits.get(ladder.getName());
    }

    public NamedKit getKit(Ladder ladder, int index) {
        return this.kits.get(ladder.getName())[index];
    }

    public void replaceKit(Ladder ladder, int index, NamedKit kit) {
        NamedKit[] kits = this.kits.get(ladder.getName());
        kits[index] = kit;

        this.kits.put(ladder.getName(), kits);
    }

    public void deleteKit(Ladder ladder, NamedKit kit) {
        if (kit == null) {
            return;
        }

        NamedKit[] kits = this.kits.get(ladder.getName());

        for (int i = 0; i < 4; i++) {
            if (kits[i] != null && kits[i].equals(kit)) {
                kits[i] = null;
                break;
            }
        }

        this.kits.put(ladder.getName(), kits);
    }

    public List<ItemStack> getKitItems(Ladder ladder) {
        List<ItemStack> toReturn = new ArrayList<>();

        toReturn.add(Config.getConfigItem(ConfigKey.KIT_DEFAULT_KIT).toItemStack());

        for (NamedKit kit : this.kits.get(ladder.getName())) {
            if (kit != null) {
                final ItemStack itemStack = new ItemStack(Material.ENCHANTED_BOOK);
                final ItemMeta itemMeta = itemStack.getItemMeta();

                itemMeta.setDisplayName(ChatColor.GOLD + "Kit: " + ChatColor.YELLOW + kit.getName());
                itemMeta.setLore(Arrays.asList(
                        ChatColor.GRAY + "Right-click with this book in your",
                        ChatColor.GRAY + "hand to receive this kit."
                ));
                itemStack.setItemMeta(itemMeta);

                toReturn.add(itemStack);
            }
        }

        return toReturn;
    }

    public void load() {
        try {
            Document document = PracticeMongo.getInstance().getPlayers().find(this.filter).first();

            if (document == null) {
                // If the document does not exist, go ahead
                // and save their data. Set loaded to true
                // prematurely so the player doesn't get
                // kicked from the server.
                this.loaded = true;
                this.save();
                return;
            }

            if (this.getName() == null) {
                this.setName(document.getString("name"));
            }

            final Document settingsDocument = (Document) document.get("settings");
            final Document statisticsDocument = (Document) document.get("statistics");
            final Document laddersDocument = (Document) statisticsDocument.get("ladders");
            final Document kitsDocument = (Document) document.get("kits");

            for (String key : laddersDocument.keySet()) {
                final Document ladderDocument = (Document) laddersDocument.get(key);
                final Ladder ladder = Ladder.getByName(key);

                if (ladder == null) {
                    continue;
                }

                LadderStatistics ladderStatistics = new LadderStatistics();

                ladderStatistics.setElo(ladderDocument.getInteger("elo"));
                ladderStatistics.setWon(ladderDocument.getInteger("won"));
                ladderStatistics.setLost(ladderDocument.getInteger("lost"));

                this.playerStatistics.getLadders().put(ladder, ladderStatistics);
            }

            this.playerSettings.setGlobalChat(settingsDocument.getBoolean("global-chat"));
            this.playerSettings.setReceiveDuelRequests(settingsDocument.getBoolean("receive-duel-requests"));
            this.playerSettings.setShowScoreboard(settingsDocument.getBoolean("show-scoreboard"));
            this.playerSettings.setAllowSpectators(settingsDocument.getBoolean("allow-spectators"));

            for (String key : kitsDocument.keySet()) {
                Ladder ladder = Ladder.getByName(key);

                if (ladder == null) {
                    continue;
                }

                JsonArray kitsArray = Praxi.PARSER.parse(kitsDocument.getString(key)).getAsJsonArray();
                NamedKit[] kits = new NamedKit[4];

                for (JsonElement kitElement : kitsArray) {
                    JsonObject kitObject = kitElement.getAsJsonObject();

                    NamedKit kit = new NamedKit(kitObject.get("name").getAsString());

                    kit.setArmor(InventoryUtil.deserializeInventory(kitObject.get("armor").getAsString()));
                    kit.setContents(InventoryUtil.deserializeInventory(kitObject.get("contents").getAsString()));

                    kits[kitObject.get("index").getAsInt()] = kit;
                }

                this.kits.put(ladder.getName(), kits);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        this.loaded = true;
    }

    public void save() {
        Document laddersDocument = new Document();

        for (Map.Entry<Ladder, LadderStatistics> entry : this.playerStatistics.getLadders().entrySet()) {
            Document ladder = new Document();

            ladder.put("elo", entry.getValue().getElo());
            ladder.put("won", entry.getValue().getWon());
            ladder.put("lost", entry.getValue().getLost());

            laddersDocument.put(entry.getKey().getName(), ladder);
        }

        Document statisticsDocument = new Document();

        statisticsDocument.put("ladders", laddersDocument);

        Document settingsDocument = new Document();

        settingsDocument.put("global-chat", this.playerSettings.isGlobalChat());
        settingsDocument.put("receive-duel-requests", this.playerSettings.isReceiveDuelRequests());
        settingsDocument.put("show-scoreboard", this.playerSettings.isShowScoreboard());
        settingsDocument.put("allow-spectators", this.playerSettings.isAllowSpectators());

        Document kitsDocument = new Document();

        for (Map.Entry<String, NamedKit[]> entry : this.kits.entrySet()) {
            JsonArray kitsArray = new JsonArray();

            for (int i = 0; i < 4; i++) {
                NamedKit kit = entry.getValue()[i];

                if (kit != null) {
                    JsonObject kitObject = new JsonObject();

                    kitObject.addProperty("index", i);
                    kitObject.addProperty("name", kit.getName());
                    kitObject.addProperty("armor", InventoryUtil.serializeInventory(kit.getArmor()));
                    kitObject.addProperty("contents", InventoryUtil.serializeInventory(kit.getContents()));

                    kitsArray.add(kitObject);
                }
            }

            kitsDocument.put(entry.getKey(), kitsArray.toString());
        }

        Document document = new Document();

        document.put("uuid", this.getUuid().toString());
        document.put("name", this.getName());
        document.put("settings", settingsDocument);
        document.put("statistics", statisticsDocument);
        document.put("kits", kitsDocument);

        PracticeMongo.getInstance().getPlayers().replaceOne(this.filter, document, new ReplaceOptions().upsert(true));
    }

    public static PlayerData getByUuid(UUID uuid) {
        PlayerData playerData = players.get(uuid);

        if (playerData == null) {
            playerData = new PlayerData(uuid, null);
        }

        return playerData;
    }

}
