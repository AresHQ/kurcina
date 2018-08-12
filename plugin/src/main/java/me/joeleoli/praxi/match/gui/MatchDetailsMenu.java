package me.joeleoli.praxi.match.gui;

import lombok.AllArgsConstructor;

import me.joeleoli.nucleus.command.CommandHandler;
import me.joeleoli.nucleus.menu.Button;
import me.joeleoli.nucleus.menu.Menu;
import me.joeleoli.nucleus.menu.buttons.DisplayButton;
import me.joeleoli.nucleus.util.*;

import me.joeleoli.praxi.match.MatchPlayer;
import me.joeleoli.praxi.match.MatchSnapshot;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.*;

@AllArgsConstructor
public class MatchDetailsMenu extends Menu {

    private MatchSnapshot snapshot;

    @Override
    public String getTitle(Player player) {
        return ChatColor.YELLOW + "Snapshot of " + this.snapshot.getMatchPlayer().getName();
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        final Map<Integer, Button> buttons = new HashMap<>();
        final ItemStack[] fixedContents = InventoryUtil.fixInventoryOrder(this.snapshot.getContents());

        for (int i = 0; i < fixedContents.length; i++) {
            final ItemStack itemStack = fixedContents[i];

            if (itemStack == null || itemStack.getType() == Material.AIR) {
                continue;
            }

            buttons.put(i, new DisplayButton(itemStack, true));
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
            buttons.put(pos++, new PotionsButton(this.snapshot.getMatchPlayer().getName(), this.snapshot.getRemainingPotions()));
        }

        buttons.put(pos, new StatisticsButton(this.snapshot.getMatchPlayer()));

        if (this.snapshot.getSwitchTo() != null) {
            buttons.put(53, new SwitchInventoryButton(this.snapshot.getSwitchTo()));
        }

        return buttons;
    }

    @Override
    public void onOpen(Player player) {
        player.sendMessage(Style.YELLOW + "You are viewing " + Style.AQUA + this.snapshot.getMatchPlayer().getName() + Style.YELLOW + "'s inventory...");
    }

    @AllArgsConstructor
    private class SwitchInventoryButton extends Button {

        private MatchPlayer switchTo;

        @Override
        public ItemStack getButtonItem(Player player) {
            return new ItemBuilder(Material.LEVER)
                    .name(Style.DARK_AQUA + "Switch inventory")
                    .lore(Arrays.asList(
                            "",
                            Style.YELLOW + "Switch to " + Style.AQUA + this.switchTo.getName() + Style.YELLOW + "'s inventory"
                    ))
                    .build();
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
            return new ItemBuilder(Material.MELON)
                    .name(Style.DARK_AQUA + this.health + "/10 " + Style.UNICODE_HEART)
                    .amount(this.health == 0 ? 1 : this.health)
                    .build();
        }

    }

    @AllArgsConstructor
    private class HungerButton extends Button {

        private int hunger;

        @Override
        public ItemStack getButtonItem(Player player) {
            return new ItemBuilder(Material.COOKED_BEEF)
                    .name(Style.DARK_AQUA + this.hunger + "/20 Hunger")
                    .amount(this.hunger == 0 ? 1 : this.hunger)
                    .build();
        }

    }

    @AllArgsConstructor
    private class EffectsButton extends Button {

        private Collection<PotionEffect> effects;

        @Override
        public ItemStack getButtonItem(Player player) {
            final ItemBuilder builder = new ItemBuilder(Material.POTION)
                    .name(Style.DARK_AQUA + Style.BOLD + "Potion Effects");

            if (this.effects.isEmpty()) {
                builder.lore(Style.YELLOW + "No potion effects");
            } else {
                final List<String> lore = new ArrayList<>();

                this.effects.forEach(effect -> {
                    final String name = BukkitUtil.getName(effect.getType()) + " " + (effect.getAmplifier() + 1);
                    final String duration = Style.GRAY + " (" + TimeUtil.millisToTimer((effect.getDuration() / 20) * 1000) + ")";

                    lore.add(Style.AQUA + name + duration);
                });

                builder.lore(lore);
            }

            return builder.build();
        }

    }

    @AllArgsConstructor
    private class PotionsButton extends Button {

        private String name;
        private int potions;

        @Override
        public ItemStack getButtonItem(Player player) {
            return new ItemBuilder(Material.POTION)
                    .durability(16421)
                    .amount(this.potions == 0 ? 1 : this.potions)
                    .name(Style.DARK_AQUA + this.potions + " Potion" + (this.potions == 1 ? "" : "s"))
                    .lore(Style.AQUA + this.name + Style.YELLOW + " had " + Style.AQUA + this.potions + Style.YELLOW + " potion" + (this.potions == 1 ? "" : "s") + " left.")
                    .build();
        }

    }

    @AllArgsConstructor
    private class StatisticsButton extends Button {

        private MatchPlayer matchPlayer;

        @Override
        public ItemStack getButtonItem(Player player) {
            return new ItemBuilder(Material.PAPER)
                    .name(Style.DARK_AQUA + Style.BOLD + "Statistics")
                    .lore(Arrays.asList(
                            Style.WHITE + "Hits: " + Style.AQUA + this.matchPlayer.getHits(),
                            Style.WHITE + "Longest Combo: " + Style.AQUA + this.matchPlayer.getLongestCombo(),
                            Style.WHITE + "Potions Thrown: " + Style.AQUA + this.matchPlayer.getPotionsThrown(),
                            Style.WHITE + "Potions Missed: " + Style.AQUA + this.matchPlayer.getPotionsMissed(),
                            Style.WHITE + "Potion Accuracy: " + Style.AQUA + this.matchPlayer.getPotionAccuracy()
                    ))
                    .build();
        }

    }

}
