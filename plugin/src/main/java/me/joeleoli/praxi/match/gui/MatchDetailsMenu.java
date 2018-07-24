package me.joeleoli.praxi.match.gui;

import lombok.AllArgsConstructor;

import me.joeleoli.commons.command.CommandHandler;
import me.joeleoli.commons.menu.Button;
import me.joeleoli.commons.menu.Menu;
import me.joeleoli.commons.menu.buttons.DisplayButton;
import me.joeleoli.commons.util.InventoryUtil;

import me.joeleoli.praxi.config.Config;
import me.joeleoli.praxi.config.ConfigItem;
import me.joeleoli.praxi.config.ConfigKey;
import me.joeleoli.praxi.match.MatchPlayer;
import me.joeleoli.praxi.match.MatchSnapshot;
import me.joeleoli.praxi.script.ScriptContext;
import me.joeleoli.praxi.script.wrapper.PlayerInfoWrapper;
import me.joeleoli.praxi.script.wrapper.PlayerWrapper;
import me.joeleoli.praxi.script.wrapper.PotionEffectWrapper;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;

import java.util.*;

@AllArgsConstructor
public class MatchDetailsMenu extends Menu {

    private MatchSnapshot snapshot;

    @Override
    public String getTitle(Player player) {
        ScriptContext context = new ScriptContext(Config.getString(ConfigKey.MENU_MATCH_DETAILS_TITLE));

        context.addCondition("canSwitch", this.snapshot.getSwitchTo() != null);
        context.getReplaceables().add(new PlayerWrapper(player));
        context.getReplaceables().add(new PlayerInfoWrapper(this.snapshot.getMatchPlayer(), "target"));

        if (this.snapshot.getSwitchTo() != null) {
            context.getReplaceables().add(new PlayerInfoWrapper(this.snapshot.getMatchPlayer(), "switch_to"));
        }

        return context.buildSingleLine();
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        Map<Integer, Button> buttons = new HashMap<>();

        for (ItemStack itemStack : InventoryUtil.fixInventoryOrder(this.snapshot.getContents())) {
            if (itemStack != null && itemStack.getType() != Material.AIR) {
                buttons.put(buttons.size(), new DisplayButton(itemStack, true));
            }
        }

        for (int i = 0; i < this.snapshot.getArmor().length; i++) {
            ItemStack itemStack = this.snapshot.getArmor()[i];

            if (itemStack != null && itemStack.getType() != Material.AIR) {
                buttons.put(39 - i, new DisplayButton(itemStack, true));
            }
        }

        int pos = 45;

        buttons.put(pos++, new HealthButton(this.snapshot.getHealth()));
        buttons.put(pos++, new HungerButton(this.snapshot.getHunger()));
        buttons.put(pos++, new EffectsButton(this.snapshot.getEffects()));

        if (this.snapshot.shouldDisplayRemainingPotions()) {
            buttons.put(pos++, new PotionsButton(this.snapshot.getRemainingPotions()));
        }

        buttons.put(pos, new StatisticsButton(this.snapshot.getMatchPlayer()));

        if (this.snapshot.getSwitchTo() != null) {
            buttons.put(53, new SwitchInventoryButton(this.snapshot.getSwitchTo()));
        }

        return buttons;
    }

    @Override
    public void onOpen(Player player) {
        final ScriptContext context = new ScriptContext(Config.getStringList(ConfigKey.MATCH_DETAILS_SUCCESS));

        context.addCondition("canSwitch", this.snapshot.getSwitchTo() != null);
        context.getReplaceables().add(new PlayerWrapper(player));
        context.getReplaceables().add(new PlayerInfoWrapper(this.snapshot.getMatchPlayer(), "target"));

        if (this.snapshot.getSwitchTo() != null) {
            context.getReplaceables().add(new PlayerInfoWrapper(this.snapshot.getSwitchTo(), "switch_to"));
        }

        context.buildComponents().forEach(player::sendMessage);
    }

    @AllArgsConstructor
    private class SwitchInventoryButton extends Button {

        private MatchPlayer switchTo;

        @Override
        public ItemStack getButtonItem(Player player) {
            final ConfigItem configItem = Config.getConfigItem(ConfigKey.MENU_MATCH_DETAILS_SWITCH_BUTTON);
            final ItemStack itemStack = new ItemStack(configItem.getMaterial(), 1, configItem.getDurability());
            final ItemMeta itemMeta = itemStack.getItemMeta();
            final ScriptContext context = new ScriptContext(configItem.getName());

            context.getReplaceables().add(new PlayerWrapper(player));
            context.getReplaceables().add(new PlayerInfoWrapper(this.switchTo, "switch_to"));

            itemMeta.setDisplayName(context.buildSingleLine());

            context.setLines(configItem.getLore());

            itemMeta.setLore(context.buildMultipleLines());
            itemStack.setItemMeta(itemMeta);

            return itemStack;
        }

        @Override
        public void clicked(Player player, int slot, ClickType clickType, int hb) {
            CommandHandler.executeCommand(player, "viewinv " + this.switchTo.getUuid().toString());
        }

    }

    @AllArgsConstructor
    private class HealthButton extends Button {

        private int health;

        @Override
        public ItemStack getButtonItem(Player player) {
            final ConfigItem configItem = Config.getConfigItem(ConfigKey.MENU_MATCH_DETAILS_HEALTH_BUTTON);
            final ItemStack itemStack = new ItemStack(configItem.getMaterial(), this.health == 0 ? 1 : this.health, configItem.getDurability());
            final ItemMeta itemMeta = itemStack.getItemMeta();
            final ScriptContext context = new ScriptContext(configItem.getName());

            context.addVariable("health", this.health + "");
            context.getReplaceables().add(new PlayerWrapper(player));

            itemMeta.setDisplayName(context.buildSingleLine());

            context.setLines(configItem.getLore());

            itemMeta.setLore(context.buildMultipleLines());
            itemStack.setItemMeta(itemMeta);

            return itemStack;
        }

    }

    @AllArgsConstructor
    private class HungerButton extends Button {

        private int hunger;

        @Override
        public ItemStack getButtonItem(Player player) {
            final ConfigItem configItem = Config.getConfigItem(ConfigKey.MENU_MATCH_DETAILS_HUNGER_BUTTON);
            final ItemStack itemStack = new ItemStack(configItem.getMaterial(), this.hunger == 0 ? 1 : this.hunger, configItem.getDurability());
            final ItemMeta itemMeta = itemStack.getItemMeta();
            final ScriptContext context = new ScriptContext(configItem.getName());

            context.addVariable("hunger", this.hunger + "");
            context.getReplaceables().add(new PlayerWrapper(player));

            itemMeta.setDisplayName(context.buildSingleLine());

            context.setLines(configItem.getLore());

            itemMeta.setLore(context.buildMultipleLines());
            itemStack.setItemMeta(itemMeta);

            return itemStack;
        }

    }

    @AllArgsConstructor
    private class EffectsButton extends Button {

        private Collection<PotionEffect> effects;

        @Override
        public ItemStack getButtonItem(Player player) {
            final ConfigItem configItem = Config.getConfigItem(ConfigKey.MENU_MATCH_DETAILS_EFFECTS_BUTTON);
            final ItemStack itemStack = new ItemStack(configItem.getMaterial(), 1, configItem.getDurability());
            final ItemMeta itemMeta = itemStack.getItemMeta();
            final ScriptContext context = new ScriptContext(configItem.getName());

            context.addCondition("effectsEmpty", this.effects.isEmpty());
            context.setForLoopEntries(new ArrayList<>());
            context.getReplaceables().add(new PlayerWrapper(player));

            this.effects.forEach(effect -> context.getForLoopEntries().add(new PotionEffectWrapper(effect)));

            itemMeta.setDisplayName(context.buildSingleLine());

            context.setLines(configItem.getLore());

            itemMeta.setLore(context.buildMultipleLines());
            itemStack.setItemMeta(itemMeta);

            return itemStack;
        }

    }

    @AllArgsConstructor
    private class PotionsButton extends Button {

        private int potions;

        @Override
        public ItemStack getButtonItem(Player player) {
            final ConfigItem configItem = Config.getConfigItem(ConfigKey.MENU_MATCH_DETAILS_POTIONS_BUTTON);
            final ItemStack itemStack = new ItemStack(configItem.getMaterial(), 1, configItem.getDurability());
            final ItemMeta itemMeta = itemStack.getItemMeta();
            final ScriptContext context = new ScriptContext(configItem.getName());

            context.addCondition("noPotions", this.potions == 0);
            context.addVariable("potions", this.potions + "");
            context.getReplaceables().add(new PlayerWrapper(player));

            itemMeta.setDisplayName(context.buildSingleLine());

            context.setLines(configItem.getLore());

            itemMeta.setLore(context.buildMultipleLines());
            itemStack.setItemMeta(itemMeta);

            return itemStack;
        }

    }

    @AllArgsConstructor
    private class StatisticsButton extends Button {

        private MatchPlayer matchPlayer;

        @Override
        public ItemStack getButtonItem(Player player) {
            final ConfigItem configItem = Config.getConfigItem(ConfigKey.MENU_MATCH_DETAILS_STATISTICS_BUTTON);
            final ItemStack itemStack = new ItemStack(configItem.getMaterial(), 1, configItem.getDurability());
            final ItemMeta itemMeta = itemStack.getItemMeta();
            final ScriptContext context = new ScriptContext(configItem.getName());

            context.addVariable("hits", this.matchPlayer.getHits() + "");
            context.addVariable("longest_combo", this.matchPlayer.getLongestCombo() + "");
            context.addVariable("potions_thrown", this.matchPlayer.getPotionsThrown() + "");
            context.addVariable("potions_missed", this.matchPlayer.getPotionsMissed() + "");
            context.addVariable("potion_accuracy", this.matchPlayer.getPotionAccuracy() + "");
            context.getReplaceables().add(new PlayerWrapper(player));

            itemMeta.setDisplayName(context.buildSingleLine());

            context.setLines(configItem.getLore());

            itemMeta.setLore(context.buildMultipleLines());
            itemStack.setItemMeta(itemMeta);

            return itemStack;
        }

    }

}
