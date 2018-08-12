package me.joeleoli.praxi.match.gui;

import lombok.AllArgsConstructor;

import me.joeleoli.nucleus.menu.Button;
import me.joeleoli.nucleus.menu.Menu;
import me.joeleoli.nucleus.menu.buttons.DisplayButton;
import me.joeleoli.nucleus.util.*;

import org.apache.commons.lang.StringEscapeUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.*;

public class ViewInventoryMenu extends Menu {

    private Player target;

    public ViewInventoryMenu(Player target) {
        this.target = target;
    }

    @Override
    public String getTitle(Player player) {
        return Style.GOLD + this.target.getName() + "'s Inventory";
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        final Map<Integer, Button> buttons = new HashMap<>();

        if (player == null) {
            return buttons;
        }

        final ItemStack[] fixedContents = InventoryUtil.fixInventoryOrder(this.target.getInventory().getContents());

        for (int i = 0; i < fixedContents.length; i++) {
            final ItemStack itemStack = fixedContents[i];

            if (itemStack == null || itemStack.getType() == Material.AIR) {
                continue;
            }

            buttons.put(i, new DisplayButton(itemStack, true));
        }

        for (int i = 0; i < this.target.getInventory().getArmorContents().length; i++) {
            ItemStack itemStack = this.target.getInventory().getArmorContents()[i];

            if (itemStack != null && itemStack.getType() != Material.AIR) {
                buttons.put(39 - i, new DisplayButton(itemStack, true));
            }
        }

        int pos = 45;

        buttons.put(pos++, new HealthButton(this.target.getHealth() == 0 ? 0 : (int) Math.round(this.target.getHealth() / 2)));
        buttons.put(pos++, new HungerButton(this.target.getFoodLevel()));
        buttons.put(pos, new EffectsButton(this.target.getActivePotionEffects()));

        return buttons;
    }

    @Override
    public boolean isAutoUpdate() {
        return true;
    }

    @AllArgsConstructor
    private class HealthButton extends Button {

        private int health;

        @Override
        public ItemStack getButtonItem(Player player) {
            return new ItemBuilder(Material.MELON)
                    .name("&a" + this.health + "/10 " + StringEscapeUtils.unescapeJava("\u2764"))
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
                    .name("&a" + this.hunger + "/20 Hunger")
                    .amount(this.hunger == 0 ? 1 : this.hunger)
                    .build();
        }

    }

    @AllArgsConstructor
    private class EffectsButton extends Button {

        private Collection<PotionEffect> effects;

        @Override
        public ItemStack getButtonItem(Player player) {
            final ItemBuilder builder = new ItemBuilder(Material.POTION).name(Style.GREEN + "Potion Effects");

            if (this.effects.isEmpty()) {
                builder.lore(Arrays.asList(
                        "",
                        Style.GRAY + "No effects"
                ));
            } else {
                final List<String> lore = new ArrayList<>();

                lore.add("");

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

}
