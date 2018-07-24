package me.joeleoli.praxi.ladder;

import me.joeleoli.commons.config.ConfigCursor;
import me.joeleoli.commons.util.CC;
import me.joeleoli.commons.util.InventoryUtil;

import me.joeleoli.commons.util.Pair;
import me.joeleoli.praxi.Praxi;
import me.joeleoli.praxi.config.ConfigItem;
import me.joeleoli.praxi.kit.Kit;
import me.joeleoli.praxi.script.Replaceable;

import lombok.Data;
import lombok.Getter;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Data
public class Ladder implements Replaceable {

    @Getter
    private static List<Ladder> ladders = new ArrayList<>();

    private String name;
    private String displayName;
    private ItemStack displayIcon;
    private Kit defaultKit = new Kit();
    private List<ItemStack> kitEditorItems = new ArrayList<>();
    private boolean enabled, build, sumo, parkour, regeneration, allowPotionFill;
    private int hitDelay;

    public Ladder(String name) {
        this.name = name;
        this.displayName = ChatColor.AQUA + this.name;
        this.displayIcon = new ItemStack(Material.DIAMOND_SWORD);

        ladders.add(this);
    }

    @Override
    public List<Pair<String, String>> getReplacements() {
        return Arrays.asList(
                new Pair<>("ladder_name", this.name),
                new Pair<>("ladder_display_name", this.displayName)
        );
    }

    public void save() {
        ConfigCursor cursor = new ConfigCursor(Praxi.getInstance().getLadderConfig(), "ladders." + this.name);

        cursor.set("display-name", this.displayName);
        cursor.set("display-icon.material", this.displayIcon.getType().name());
        cursor.set("display-icon.durability", this.displayIcon.getDurability());
        cursor.set("display-icon.amount", this.displayIcon.getAmount());
        cursor.set("enabled", this.enabled);
        cursor.set("buildComponents", this.build);
        cursor.set("sumo", this.sumo);
        cursor.set("parkour", this.parkour);
        cursor.set("regeneration", this.regeneration);

        if (this.displayIcon.hasItemMeta()) {
            final ItemMeta itemMeta = this.displayIcon.getItemMeta();

            if (itemMeta.hasDisplayName()) {
                cursor.set("display-icon.name", itemMeta.getDisplayName());
            }

            if (itemMeta.hasLore()) {
                cursor.set("display-icon.lore", itemMeta.getLore());
            }
        }

        cursor.set("default-kit.armor", InventoryUtil.serializeInventory(this.defaultKit.getArmor()));
        cursor.set("default-kit.contents", InventoryUtil.serializeInventory(this.defaultKit.getContents()));

        cursor.save();
    }

    public static void init() {
        ConfigCursor cursor = new ConfigCursor(Praxi.getInstance().getLadderConfig(), "ladders");

        for (String key : cursor.getKeys()) {
            cursor.setPath("ladders." + key);

            Ladder ladder = new Ladder(key);

            ladder.setDisplayName(CC.translate(cursor.getString("display-name")));
            ladder.setDisplayIcon(new ConfigItem(cursor, "display-icon").toItemStack());
            ladder.setEnabled(cursor.getBoolean("enabled"));
            ladder.setBuild(cursor.getBoolean("buildComponents"));
            ladder.setSumo(cursor.getBoolean("sumo"));
            ladder.setParkour(cursor.getBoolean("parkour"));
            ladder.setRegeneration(cursor.getBoolean("regeneration"));

            if (cursor.exists("default-kit")) {
                final ItemStack[] armor = InventoryUtil.deserializeInventory(cursor.getString("default-kit.armor"));
                final ItemStack[] contents = InventoryUtil.deserializeInventory(cursor.getString("default-kit.contents"));

                ladder.setDefaultKit(new Kit(armor, contents));
            }

            if (cursor.exists("kit-editor.allow-potion-fill")) {
                ladder.setAllowPotionFill(cursor.getBoolean("kit-editor.allow-potion-fill"));
            }

            if (cursor.exists("kit-editor.items")) {
                for (String itemKey : cursor.getKeys("kit-editor.items")) {
                    ladder.getKitEditorItems().add(new ConfigItem(cursor, "kit-editor.items." + itemKey).toItemStack());
                }
            }
        }
    }

    public static Ladder getByName(String name) {
        for (Ladder ladder : ladders) {
            if (ladder.getName().equalsIgnoreCase(name)) {
                return ladder;
            }
        }

        return null;
    }

}
