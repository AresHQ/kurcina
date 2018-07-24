package me.joeleoli.praxi.arena.selection;

import me.joeleoli.commons.util.CC;
import me.joeleoli.praxi.Praxi;
import me.joeleoli.praxi.cuboid.Cuboid;

import lombok.Data;
import lombok.NonNull;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.Arrays;

@Data
public class Selection {

    private static final String SELECTION_METADATA_KEY = "CLAIM_SELECTION";
    public static final ItemStack SELECTION_WAND;

    @NonNull
    private Location point1;
    @NonNull
    private Location point2;

    static {
        ItemStack itemStack = new ItemStack(Material.GOLD_AXE);
        ItemMeta itemMeta = itemStack.getItemMeta();

        itemMeta.setDisplayName(CC.GOLD + CC.BOLD + "Selection Wand");
        itemMeta.setLore(Arrays.asList(
                CC.YELLOW + "Left-click to set position 1.",
                CC.YELLOW + "Right-click to set position 2."
        ));
        itemStack.setItemMeta(itemMeta);

        SELECTION_WAND = itemStack;
    }

    /**
     * Private, so that we can create a new instance in the Selection#createOrGetSelection method.
     */
    private Selection() {}

    /**
     * @return the cuboid
     */
    public Cuboid getCuboid() {
        return new Cuboid(point1, point2);
    }

    /**
     * @return if the Selection can form a full cuboid object
     */
    public boolean isFullObject() {
        return point1 != null && point2 != null;
    }

    /**
     * Resets both locations in the Selection
     */
    public void clear() {
        point1 = null;
        point2 = null;
    }

    /**
     * Selections are stored in the player's metadata. This method removes the need
     * to active Bukkit Metadata API calls all over the place.
     * <p>
     * This method can be modified structurally as needed, the plugin only access Selection objects
     * via this method.
     *
     * @param player the player for whom to grab the Selection object for
     * @return selection object, either new or created
     */
    public static Selection createOrGetSelection(Player player) {
        if (player.hasMetadata(SELECTION_METADATA_KEY)) {
            return (Selection) player.getMetadata(SELECTION_METADATA_KEY).get(0).value();
        }

        Selection selection = new Selection();

        player.setMetadata(SELECTION_METADATA_KEY, new FixedMetadataValue(Praxi.getInstance(), selection));

        return selection;
    }

}
