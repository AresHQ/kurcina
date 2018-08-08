package me.joeleoli.praxi.player;

import me.joeleoli.nucleus.util.CC;
import me.joeleoli.nucleus.util.ItemBuilder;
import me.joeleoli.praxi.hotbar.HotbarItem;
import me.joeleoli.praxi.hotbar.HotbarLayout;

import lombok.Getter;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class PlayerHotbar {

    @Getter
    private static Map<HotbarItem, ItemStack> items = new HashMap<>();
    private static Map<HotbarLayout, HotbarItem[]> layouts = new HashMap<>();

    // Utility class - cannot be instantiated
    private PlayerHotbar() {}

    public static void init() {
        items.put(HotbarItem.QUEUE_JOIN_UNRANKED, new ItemBuilder(Material.IRON_SWORD).name(CC.GRAY + CC.BOLD + "Unranked Queue").lore(CC.YELLOW + "Right-click to join an unranked queue.").build());
        items.put(HotbarItem.QUEUE_JOIN_RANKED, new ItemBuilder(Material.DIAMOND_SWORD).name(CC.GREEN + CC.BOLD + "Ranked Queue").lore(CC.YELLOW + "Right-click to join a ranked queue.").build());
        items.put(HotbarItem.QUEUE_LEAVE, new ItemBuilder(Material.INK_SACK).durability(1).name(CC.RED + CC.BOLD + "Leave Queue").lore(CC.YELLOW + "Right-click to leave your queue.").build());
        items.put(HotbarItem.PARTY_EVENTS, new ItemBuilder(Material.DIAMOND_SWORD).name(CC.GREEN + CC.BOLD + "Party Events").lore(CC.YELLOW + "Right-click to start a party event.").build());
        items.put(HotbarItem.PARTY_CREATE, new ItemBuilder(Material.NAME_TAG).name(CC.YELLOW + CC.BOLD + "Create Party").lore(CC.YELLOW + "Right-click to create a party.").build());
        items.put(HotbarItem.PARTY_DISBAND, new ItemBuilder(Material.INK_SACK).durability(1).name(CC.RED + CC.BOLD + "Disband Party").lore(CC.YELLOW + "Right-click to disband your party.").build());
        items.put(HotbarItem.PARTY_LEAVE, new ItemBuilder(Material.INK_SACK).durability(1).name(CC.RED + CC.BOLD + "Leave Party").lore(CC.YELLOW + "Right-click to leave your party.").build());
        items.put(HotbarItem.PARTY_INFORMATION, new ItemBuilder(Material.SKULL_ITEM).durability(3).name(CC.YELLOW + CC.BOLD + "Party Information").lore(CC.YELLOW + "Right-click to show your party's information.").build());
        items.put(HotbarItem.OTHER_PARTIES, new ItemBuilder(Material.CHEST).name(CC.BLUE + CC.BOLD + "Other Parties").lore(CC.YELLOW + "Right-click to show other parties.").build());
        items.put(HotbarItem.SETTINGS, new ItemBuilder(Material.WATCH).name(CC.PINK + CC.BOLD + "Settings").lore(CC.YELLOW + "Right-click to open your settings.").build());
        items.put(HotbarItem.KIT_EDITOR, new ItemBuilder(Material.BOOK).name(CC.RED + CC.BOLD + "Kit Editor").lore(CC.YELLOW + "Right-click to open the kit editor.").build());
        items.put(HotbarItem.SPECTATE_STOP, new ItemBuilder(Material.INK_SACK).durability(1).name(CC.RED + CC.BOLD + "Stop Spectating").lore(CC.YELLOW + "Right-click to stop spectating.").build());
        items.put(HotbarItem.VIEW_INVENTORY, new ItemBuilder(Material.BOOK).name(CC.GOLD + CC.BOLD + "View Inventory").lore(CC.YELLOW + "Right-click a player to view their inventory.").build());

        layouts.put(HotbarLayout.LOBBY_NO_PARTY, new HotbarItem[]{
                HotbarItem.QUEUE_JOIN_UNRANKED,
                HotbarItem.QUEUE_JOIN_RANKED,
                null,
                null,
                HotbarItem.PARTY_CREATE,
                null,
                null,
                HotbarItem.SETTINGS,
                HotbarItem.KIT_EDITOR
        });

        layouts.put(HotbarLayout.LOBBY_PARTY_LEADER, new HotbarItem[]{
                HotbarItem.PARTY_EVENTS,
                null,
                HotbarItem.PARTY_INFORMATION,
                HotbarItem.OTHER_PARTIES,
                null,
                HotbarItem.PARTY_DISBAND,
                null,
                HotbarItem.SETTINGS,
                HotbarItem.KIT_EDITOR
        });

        layouts.put(HotbarLayout.LOBBY_PARTY_MEMBER, new HotbarItem[]{
                HotbarItem.PARTY_INFORMATION,
                null,
                HotbarItem.OTHER_PARTIES,
                null,
                HotbarItem.PARTY_LEAVE,
                null,
                null,
                HotbarItem.SETTINGS,
                HotbarItem.KIT_EDITOR
        });

        layouts.put(HotbarLayout.QUEUE_NO_PARTY, new HotbarItem[]{
                HotbarItem.QUEUE_LEAVE
        });

        layouts.put(HotbarLayout.QUEUE_PARTY_LEADER, new HotbarItem[]{
                HotbarItem.QUEUE_LEAVE,
                null,
                HotbarItem.PARTY_INFORMATION,
                null,
                null,
                null,
                null,
                HotbarItem.SETTINGS,
                HotbarItem.KIT_EDITOR
        });

        layouts.put(HotbarLayout.QUEUE_PARTY_MEMBER, new HotbarItem[]{
                HotbarItem.PARTY_INFORMATION,
                null,
                null,
                null,
                null,
                null,
                null,
                HotbarItem.SETTINGS,
                HotbarItem.KIT_EDITOR
        });

        layouts.put(HotbarLayout.SPECTATE, new HotbarItem[]{
                HotbarItem.VIEW_INVENTORY,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                HotbarItem.SPECTATE_STOP
        });
    }

    public static ItemStack[] getLayout(HotbarLayout layout) {
        HotbarItem[] hotbarItems = layouts.get(layout);

        if (hotbarItems == null) {
            return new ItemStack[9];
        }

        ItemStack[] toReturn = new ItemStack[9];
        int i = 0;

        for (HotbarItem hotbarItem : hotbarItems) {
            toReturn[i++] = items.get(hotbarItem);
        }

        return toReturn;
    }

    public static HotbarItem fromItemStack(ItemStack itemStack) {
        for (Map.Entry<HotbarItem, ItemStack> entry : PlayerHotbar.getItems().entrySet()) {
            if (entry.getValue() != null && entry.getValue().equals(itemStack)) {
                return entry.getKey();
            }
        }

        return null;
    }

}
