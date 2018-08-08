package me.joeleoli.praxi.queue.gui;

import me.joeleoli.nucleus.Nucleus;
import me.joeleoli.nucleus.menu.Button;
import me.joeleoli.nucleus.menu.Menu;
import me.joeleoli.nucleus.util.CC;
import me.joeleoli.nucleus.util.ItemBuilder;

import me.joeleoli.praxi.Praxi;
import me.joeleoli.praxi.player.PlayerData;
import me.joeleoli.praxi.player.PlayerState;
import me.joeleoli.praxi.queue.Queue;

import lombok.AllArgsConstructor;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.*;

@AllArgsConstructor
public class QueueJoinMenu extends Menu {

    private boolean ranked;

    @Override
    public String getTitle(Player player) {
        return CC.GOLD + "Join " + (this.ranked ? "Ranked" : "Unranked") + " Queue";
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();

        int i = 0;

        for (Queue queue : Queue.getQueues()) {
            if (queue.isRanked() == this.ranked) {
                buttons.put(i++, new SelectLadderButton(queue));
            }
        }

        return buttons;
    }

    @AllArgsConstructor
    private class SelectLadderButton extends Button {

        private Queue queue;

        @Override
        public ItemStack getButtonItem(Player player) {
            final Queue oppositeQueue = Queue.getByPredicate(q -> q.isRanked() != this.queue.isRanked() && q.getLadder() == this.queue.getLadder());
            final List<String> lore = new ArrayList<>();

            lore.add("");

            int rankedFighting = 0;
            int rankedQueueing = 0;
            int unrankedFighting = 0;
            int unrankedQueueing = 0;

            if (this.queue.isRanked()) {
                rankedFighting = Praxi.getInstance().getFightingCount(this.queue);
                rankedQueueing = this.queue.getPlayers().size();

                if (oppositeQueue != null) {
                    unrankedFighting = Praxi.getInstance().getFightingCount(oppositeQueue);
                    unrankedQueueing = oppositeQueue.getPlayers().size();
                }
            } else {
                unrankedFighting = Praxi.getInstance().getFightingCount(this.queue);
                unrankedQueueing = this.queue.getPlayers().size();

                if (oppositeQueue != null) {
                    rankedFighting = Praxi.getInstance().getFightingCount(oppositeQueue);
                    rankedQueueing = oppositeQueue.getPlayers().size();
                }
            }

            lore.add(CC.DARK_PURPLE + CC.BOLD + "Ranked");
            lore.add(" " + CC.GREEN + "In fights: " + CC.RESET + rankedFighting);
            lore.add(" " + CC.GREEN + "In queue: " + CC.RESET + rankedQueueing);
            lore.add("");
            lore.add(CC.DARK_PURPLE + CC.BOLD + "Unranked");
            lore.add(" " + CC.GREEN + "In fights: " + CC.RESET + unrankedFighting);
            lore.add(" " + CC.GREEN + "In queue: " + CC.RESET + unrankedQueueing);
            lore.add("");

            lore.add(CC.YELLOW + "Click here to select " + CC.BOLD + this.queue.getLadder().getDisplayName() + CC.YELLOW + ".");

            return new ItemBuilder(this.queue.getLadder().getDisplayIcon())
                    .name(this.queue.getLadder().getDisplayName()).lore(lore)
                    .build();
        }

        @Override
        public void clicked(Player player, int slot, ClickType clickType, int hotbarButton) {
            final PlayerData playerData = PlayerData.getByUuid(player.getUniqueId());

            if (playerData == null) {
                return;
            }

            if (playerData.getState() != PlayerState.IN_LOBBY) {
                player.sendMessage(CC.RED + "You must be in the lobby to join a queue.");
                return;
            }

            if (Nucleus.isFrozen(player)) {
                player.sendMessage(CC.RED + "You cannot queue while frozen.");
                return;
            }

            player.closeInventory();

            this.queue.addPlayer(player, !this.queue.isRanked() ? 0 : playerData.getStatistics().getElo(this.queue.getLadder()));
        }

    }
}
