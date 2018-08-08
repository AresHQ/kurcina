package me.joeleoli.praxi.listener;

import me.joeleoli.nucleus.util.CC;
import me.joeleoli.praxi.arena.selection.Selection;

import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class ArenaListener implements Listener {

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!(event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        Block clicked = event.getClickedBlock();
        int location = 0;

        if (item == null || !item.equals(Selection.SELECTION_WAND)) {
            return;
        }

        Selection selection = Selection.createOrGetSelection(player);

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            selection.setPoint2(clicked.getLocation());
            location = 2;
        } else if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            selection.setPoint1(clicked.getLocation());
            location = 1;
        }

        event.setCancelled(true);
        event.setUseItemInHand(Event.Result.DENY);
        event.setUseInteractedBlock(Event.Result.DENY);

        String message = CC.AQUA + (location == 1 ? "First" : "Second") +
                " location " + CC.YELLOW + "(" + CC.GREEN +
                clicked.getX() + CC.YELLOW + ", " + CC.GREEN +
                clicked.getY() + CC.YELLOW + ", " + CC.GREEN +
                clicked.getZ() + CC.YELLOW + ")" + CC.AQUA + " has been set!";

        if (selection.isFullObject()) {
            message += CC.RED + " (" + CC.YELLOW + selection.getCuboid().volume() + CC.AQUA + " blocks" + CC.RED + ")";
        }

        player.sendMessage(message);
    }

}
