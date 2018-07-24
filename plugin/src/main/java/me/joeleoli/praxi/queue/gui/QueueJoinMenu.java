package me.joeleoli.praxi.queue.gui;

import me.joeleoli.commons.menu.Button;
import me.joeleoli.commons.menu.Menu;

import me.joeleoli.praxi.Praxi;
import me.joeleoli.praxi.config.Config;
import me.joeleoli.praxi.config.ConfigItem;
import me.joeleoli.praxi.config.ConfigKey;
import me.joeleoli.praxi.script.ScriptContext;
import me.joeleoli.praxi.player.PlayerData;
import me.joeleoli.praxi.player.PlayerState;
import me.joeleoli.praxi.queue.Queue;

import lombok.AllArgsConstructor;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

@AllArgsConstructor
public class QueueJoinMenu extends Menu {

    private boolean party;
    private boolean ranked;

    @Override
    public String getTitle(Player player) {
        ScriptContext context = new ScriptContext(Config.getString(ConfigKey.MENU_JOIN_QUEUE_TITLE));

        context.addCondition("ranked", this.ranked);
        context.addCondition("unranked", !this.ranked);
        context.addCondition("solo", !this.party);
        context.addCondition("party", this.party);
        context.buildComponents();

        return context.buildSingleLine().length() > 32 ? context.buildSingleLine().substring(0, 32) : context.buildSingleLine();
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();

        int i = 0;

        for (Queue queue : Queue.getQueues()) {
            if (queue.isRanked() == this.ranked && queue.isParty() == this.party) {
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
            final ConfigItem configItem = Config.getConfigItem(ConfigKey.MENU_JOIN_QUEUE_SELECT_LADDER_BUTTON);
            final ItemStack itemStack = this.queue.getLadder().getDisplayIcon();
            final ItemMeta itemMeta = itemStack.getItemMeta();
            final Queue oppositeQueue = Queue.getByPredicate(q -> !q.isRanked() && q.isParty() == this.queue.isParty());

            ScriptContext context = new ScriptContext(configItem.getLore());

            context.addCondition("ranked", this.queue.isRanked());
            context.addCondition("unranked", !this.queue.isRanked());
            context.addCondition("solo", !this.queue.isParty());
            context.addCondition("party", this.queue.isParty());
            context.addVariable("queue_count", this.queue.getPlayers().size() + "");
            context.addVariable("fight_count", Praxi.getInstance().getFightingCount(this.queue) + "");
            context.addVariable("queue_count", this.queue.getPlayers().size() + "");
            context.addVariable("queue_count", this.queue.getPlayers().size() + "");
            context.getReplaceables().add(this.queue.getLadder());

            if (oppositeQueue != null) {
                if (oppositeQueue.isRanked()) {
                    context.addVariable("ranked_queue_count", oppositeQueue.getPlayers().size() + "");
                    context.addVariable("ranked_fight_count", Praxi.getInstance().getFightingCount(oppositeQueue) + "");
                    context.addVariable("unranked_queue_count", this.queue.getPlayers().size() + "");
                    context.addVariable("unranked_fight_count", Praxi.getInstance().getFightingCount(this.queue) + "");
                } else {
                    context.addVariable("ranked_queue_count", this.queue.getPlayers().size() + "");
                    context.addVariable("ranked_fight_count", Praxi.getInstance().getFightingCount(this.queue) + "");
                    context.addVariable("unranked_queue_count", oppositeQueue.getPlayers().size() + "");
                    context.addVariable("unranked_fight_count", Praxi.getInstance().getFightingCount(oppositeQueue) + "");
                }
            }

            context.buildComponents();

            itemMeta.setDisplayName(Config.translateLadder(configItem.getName(), this.queue.getLadder()));
            itemMeta.setLore(context.getLines());
            itemStack.setItemMeta(itemMeta);

            return itemStack;
        }

        @Override
        public void clicked(Player player, int slot, ClickType clickType, int hotbarButton) {
            final PlayerData playerData = PlayerData.getByUuid(player.getUniqueId());

            if (playerData == null) {
                return;
            }

            if (playerData.getState() != PlayerState.IN_LOBBY) {
                player.sendMessage(Config.getString(ConfigKey.QUEUE_JOIN_REJECTED_STATE));
                return;
            }

            player.closeInventory();

            this.queue.addPlayer(player, this.queue.isParty() || !this.queue.isRanked() ? 0 : playerData.getPlayerStatistics().getElo(this.queue.getLadder()));
        }

    }
}
