package me.joeleoli.praxi.player;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import me.joeleoli.nucleus.util.ItemBuilder;
import me.joeleoli.nucleus.util.Style;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class PlayerHotbar {

	@Getter
	private static Map<HotbarItem, ItemStack> items = new HashMap<>();
	private static Map<HotbarLayout, HotbarItem[]> layouts = new HashMap<>();

	// Utility class - cannot be instantiated
	private PlayerHotbar() {
	}

	public static void init() {
		items.put(HotbarItem.QUEUE_JOIN_UNRANKED,
				new ItemBuilder(Material.IRON_SWORD).name(Style.GRAY + Style.BOLD + "Unranked Queue")
				                                    .lore(Style.YELLOW + "Right-click to join an unranked queue.")
				                                    .build()
		);
		items.put(HotbarItem.QUEUE_JOIN_RANKED,
				new ItemBuilder(Material.DIAMOND_SWORD).name(Style.GREEN + Style.BOLD + "Ranked Queue")
				                                       .lore(Style.YELLOW + "Right-click to join a ranked queue.")
				                                       .build()
		);
		items.put(HotbarItem.QUEUE_LEAVE,
				new ItemBuilder(Material.INK_SACK).durability(1).name(Style.RED + Style.BOLD + "Leave Queue")
				                                  .lore(Style.YELLOW + "Right-click to leave your queue.").build()
		);
		items.put(HotbarItem.PARTY_EVENTS,
				new ItemBuilder(Material.DIAMOND_SWORD).name(Style.GREEN + Style.BOLD + "Party Events")
				                                       .lore(Style.YELLOW + "Right-click to start a party event.")
				                                       .build()
		);
		items.put(HotbarItem.PARTY_CREATE,
				new ItemBuilder(Material.NAME_TAG).name(Style.YELLOW + Style.BOLD + "Create Party")
				                                  .lore(Style.YELLOW + "Right-click to create a party.").build()
		);
		items.put(HotbarItem.PARTY_DISBAND,
				new ItemBuilder(Material.INK_SACK).durability(1).name(Style.RED + Style.BOLD + "Disband Party")
				                                  .lore(Style.YELLOW + "Right-click to disband your party.").build()
		);
		items.put(HotbarItem.PARTY_LEAVE,
				new ItemBuilder(Material.INK_SACK).durability(1).name(Style.RED + Style.BOLD + "Leave Party")
				                                  .lore(Style.YELLOW + "Right-click to leave your party.").build()
		);
		items.put(HotbarItem.PARTY_INFORMATION,
				new ItemBuilder(Material.SKULL_ITEM).durability(3).name(Style.YELLOW + Style.BOLD + "Party Information")
				                                    .lore(Style.YELLOW +
				                                          "Right-click to show your party's information.").build()
		);
		items.put(HotbarItem.OTHER_PARTIES,
				new ItemBuilder(Material.CHEST).name(Style.BLUE + Style.BOLD + "Other Parties")
				                               .lore(Style.YELLOW + "Right-click to show other parties.").build()
		);
		items.put(HotbarItem.SETTINGS, new ItemBuilder(Material.WATCH).name(Style.PINK + Style.BOLD + "Settings")
		                                                              .lore(Style.YELLOW +
		                                                                    "Right-click to open your settings.")
		                                                              .build());
		items.put(HotbarItem.KIT_EDITOR, new ItemBuilder(Material.BOOK).name(Style.RED + Style.BOLD + "Kit Editor")
		                                                               .lore(Style.YELLOW +
		                                                                     "Right-click to open the kit editor.")
		                                                               .build());
		items.put(HotbarItem.SPECTATE_STOP,
				new ItemBuilder(Material.INK_SACK).durability(1).name(Style.RED + Style.BOLD + "Stop Spectating")
				                                  .lore(Style.YELLOW + "Right-click to stop spectating.").build()
		);
		items.put(HotbarItem.VIEW_INVENTORY,
				new ItemBuilder(Material.BOOK).name(Style.GOLD + Style.BOLD + "View Inventory")
				                              .lore(Style.YELLOW + "Right-click a player to view their inventory.")
				                              .build()
		);
		items.put(HotbarItem.EVENT_JOIN,
				new ItemBuilder(Material.NETHER_STAR).name(Style.AQUA + Style.BOLD + "Join Event")
				                                     .lore(Style.YELLOW + "Right-click to join the event.").build()
		);
		items.put(HotbarItem.EVENT_LEAVE,
				new ItemBuilder(Material.NETHER_STAR).name(Style.RED + Style.BOLD + "Leave Event")
				                                     .lore(Style.YELLOW + "Right-click to leave the event.").build()
		);

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

	public enum HotbarItem {
		QUEUE_JOIN_RANKED,
		QUEUE_JOIN_UNRANKED,
		QUEUE_LEAVE,
		MATCH_LEAVE,
		PARTY_EVENTS,
		PARTY_CREATE,
		PARTY_DISBAND,
		PARTY_LEAVE,
		PARTY_INFORMATION,
		OTHER_PARTIES,
		SETTINGS,
		KIT_EDITOR,
		SPECTATE_STOP,
		VIEW_INVENTORY,
		EVENT_JOIN,
		EVENT_LEAVE
	}

	public enum HotbarLayout {
		LOBBY_NO_PARTY,
		LOBBY_PARTY_LEADER,
		LOBBY_PARTY_MEMBER,
		QUEUE_NO_PARTY,
		QUEUE_PARTY_LEADER,
		QUEUE_PARTY_MEMBER,
		SPECTATE
	}

}
