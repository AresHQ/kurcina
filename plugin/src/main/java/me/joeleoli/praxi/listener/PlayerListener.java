package me.joeleoli.praxi.listener;

import me.joeleoli.commons.command.CommandHandler;
import me.joeleoli.commons.packet.Packet;
import me.joeleoli.commons.util.*;

import me.joeleoli.praxi.cooldown.Cooldown;
import me.joeleoli.praxi.hotbar.HotbarItem;
import me.joeleoli.praxi.kit.Kit;
import me.joeleoli.praxi.kit.NamedKit;
import me.joeleoli.praxi.player.PlayerState;
import me.joeleoli.praxi.config.Config;
import me.joeleoli.praxi.config.ConfigKey;
import me.joeleoli.praxi.kit.editor.gui.KitManagementMenu;
import me.joeleoli.praxi.kit.editor.gui.SelectLadderKitMenu;
import me.joeleoli.praxi.packet.PacketFactory;
import me.joeleoli.praxi.party.gui.OtherPartiesMenu;
import me.joeleoli.praxi.player.PlayerData;
import me.joeleoli.praxi.player.PlayerHotbar;
import me.joeleoli.praxi.queue.Queue;
import me.joeleoli.praxi.queue.gui.QueueJoinMenu;
import me.joeleoli.praxi.script.ScriptContext;

import org.bukkit.*;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class PlayerListener implements Listener {

    @EventHandler
    public void onProjectileHitEvent(ProjectileHitEvent event) {
        if (event.getEntity() instanceof Arrow) {
            if (event.getEntity().getShooter() instanceof Player) {
                final Player shooter = (Player) event.getEntity().getShooter();
                final Position position = Position.fromLocation(event.getEntity().getLocation());
                final Packet packet = PacketFactory.createSound("random.bowhit", position.getX(), position.getY(), position.getZ());

                PlayerData shooterData = PlayerData.getByUuid(shooter.getUniqueId());

                shooterData.getSoundPositions().add(position);
                packet.send(shooter);

                if (shooterData.isInMatch()) {
                    shooterData.getMatch().getInvolvedPlayers().forEach(uuid -> {
                        Player player = Bukkit.getPlayer(uuid);

                        if (player != null) {
                            PlayerData playerData = PlayerData.getByUuid(player.getUniqueId());

                            playerData.getSoundPositions().add(position);
                            packet.send(player);
                        }
                    });
                }
            }
        }
    }

    @EventHandler
    public void onPotionSplash(PotionSplashEvent event) {
        if (event.getPotion().getShooter() instanceof Player) {
            Player shooter = (Player) event.getPotion().getShooter();
            PlayerData shooterData = PlayerData.getByUuid(shooter.getUniqueId());

            shooterData.getSplashPositions().add(Position.fromLocation(event.getPotion().getLocation()));

            if (shooterData.isInMatch()) {
                for (Player player : shooterData.getMatch().getPlayers()) {
                    PlayerData playerData = PlayerData.getByUuid(player.getUniqueId());
                    playerData.getSplashPositions().add(Position.fromLocation(event.getPotion().getLocation()));
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        final PlayerData playerData = PlayerData.getByUuid(event.getPlayer().getUniqueId());

        if (event.getMessage().startsWith("@") || event.getMessage().startsWith("!")) {
            if (playerData.getParty() != null) {
                event.setCancelled(true);

                final ScriptContext context = new ScriptContext(Config.getString(ConfigKey.PARTY_CHAT));

                context.addVariable("chat_message", CC.strip(event.getMessage().substring(1)));

                return;
            }
        }

        if (playerData.getKitEditor().isRenaming()) {
            event.setCancelled(true);

            if (event.getMessage().length() > 16) {
                event.getPlayer().sendMessage(Config.getString(ConfigKey.KIT_EDITOR_RENAME_KIT_TOO_LONG));
                return;
            }

            playerData.getKitEditor().getSelectedKit().setName(event.getMessage());
            playerData.getKitEditor().setActive(false);
            playerData.getKitEditor().setRename(false);
            playerData.getKitEditor().setSelectedKit(null);

            new KitManagementMenu(playerData.getKitEditor().getSelectedLadder()).openMenu(event.getPlayer());
        }
    }

    @EventHandler
    public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
//        if (!Praxi.getInstance().isLoaded()) {
//            event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
//            event.setKickMessage(ChatColor.RED + "The server is currently starting...");
//            return;
//        }

        PlayerData playerData = new PlayerData(event.getUniqueId(), null);

        playerData.setName(event.getName());
        playerData.load();

        if (!playerData.isLoaded()) {
            event.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
            event.setKickMessage(ChatColor.RED + "Failed to load your profile. Try again later.");
            return;
        }

        PlayerData.getPlayers().put(event.getUniqueId(), playerData);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        event.setJoinMessage(null);

        PlayerData playerData = PlayerData.getByUuid(event.getPlayer().getUniqueId());

        PlayerUtil.spawn(event.getPlayer());
        playerData.loadLayout();

        Bukkit.getOnlinePlayers().forEach(player -> {
            player.hidePlayer(event.getPlayer());
            event.getPlayer().hidePlayer(player);
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        event.setQuitMessage(null);

        PlayerData playerData = PlayerData.getPlayers().remove(event.getPlayer().getUniqueId());

        if (playerData.getParty() != null) {
            if (playerData.getParty().isLeader(event.getPlayer())) {
                playerData.getParty().disband();
            } else {
                playerData.getParty().leave(event.getPlayer(), false);
            }
        }

        TaskUtil.runAsync(playerData::save);

        if (playerData.isInMatch()) {
            playerData.getMatch().handleDeath(event.getPlayer(), null, true);
        }

        if (playerData.isInQueue()) {
            Queue queue = Queue.getByUuid(playerData.getQueuePlayer().getQueueUuid());

            if (queue == null) {
                return;
            }

            queue.removePlayer(event.getPlayer());
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getItem() != null && event.getAction().name().contains("RIGHT")) {
            final Player player = event.getPlayer();
            final PlayerData playerData = PlayerData.getByUuid(player.getUniqueId());

            if (event.getItem().getType() == Material.POTION && event.getItem().getDurability() >= 16000) {
                final Position position = Position.fromLocation(player.getLocation());
                final Packet packet = PacketFactory.createSound("random.bow", position.getX(), position.getY(), position.getZ());

                playerData.getSoundPositions().add(position);
                packet.send(player);

                if (playerData.isInMatch()) {
                    playerData.getMatch().getInvolvedPlayers().forEach(uuid -> {
                        Player other = Bukkit.getPlayer(uuid);

                        if (other != null) {
                            PlayerData otherData = PlayerData.getByUuid(other.getUniqueId());

                            otherData.getSoundPositions().add(position);
                            packet.send(other);
                        }
                    });
                }

                return;
            }

            if (playerData.isInMatch()) {
                if (event.getItem().getType() == Material.ENDER_PEARL) {
                    if (!playerData.isInMatch() || (playerData.isInMatch() && !playerData.getMatch().isFighting())) {
                        event.setCancelled(true);
                        return;
                    }

                    long cooldown = Config.getLong(ConfigKey.ENDER_PEARL_CD_TIME);

                    if (playerData.getEnderpearlCooldown() == null || playerData.getEnderpearlCooldown().getPassed() >= cooldown) {
                        playerData.setEnderpearlCooldown(new Cooldown(cooldown));
                    } else {
                        event.setCancelled(true);

                        final ScriptContext context = new ScriptContext(Config.getString(ConfigKey.ENDER_PEARL_CD_REJECTED));

                        context.addVariable("enderpearl_time", DateUtil.formatGameSeconds(playerData.getEnderpearlCooldown().getRemaining()));

                        player.sendMessage(context.buildSingleLine());
                    }

                    return;
                }

                if (event.getItem().hasItemMeta() && event.getItem().getItemMeta().hasDisplayName()) {
                    if (Config.getConfigItem(ConfigKey.KIT_DEFAULT_KIT).getName().equals(event.getItem().getItemMeta().getDisplayName())) {
                        event.setCancelled(true);

                        final Kit kit = playerData.getMatch().getLadder().getDefaultKit();
                        final ScriptContext context = new ScriptContext(Config.getString(ConfigKey.MATCH_KIT_RECEIVED));

                        context.addVariable("kit_name", "Default");

                        player.getInventory().setArmorContents(kit.getArmor());
                        player.getInventory().setContents(kit.getContents());
                        player.updateInventory();
                        player.sendMessage(context.buildSingleLine());
                        return;
                    }
                }

                if (event.getItem().hasItemMeta() && event.getItem().getItemMeta().hasDisplayName()) {
                    final String displayName = ChatColor.stripColor(event.getItem().getItemMeta().getDisplayName());

                    if (displayName.startsWith("Kit: ")) {
                        final String kitName = displayName.replace("Kit: ", "");

                        for (NamedKit kit : playerData.getKits(playerData.getMatch().getLadder())) {
                            if (kit != null) {
                                if (ChatColor.stripColor(kit.getName()).equals(kitName)) {
                                    event.setCancelled(true);

                                    final ScriptContext context = new ScriptContext(Config.getString(ConfigKey.MATCH_KIT_RECEIVED));

                                    context.getReplaceables().add(kit);

                                    player.getInventory().setArmorContents(kit.getArmor());
                                    player.getInventory().setContents(kit.getContents());
                                    player.sendMessage(context.buildSingleLine());
                                    return;
                                }
                            }
                        }
                    }
                }
            } else {
                HotbarItem hotbarItem = PlayerHotbar.fromItemStack(event.getItem());

                if (hotbarItem == null) {
                    return;
                }

                event.setCancelled(true);

                switch (hotbarItem) {
                    case QUEUE_JOIN_RANKED: {
                        if (playerData.getState() == PlayerState.IN_LOBBY) {
                            new QueueJoinMenu(false, true).openMenu(event.getPlayer());
                        }
                    }
                    break;
                    case QUEUE_JOIN_UNRANKED: {
                        if (playerData.getState() == PlayerState.IN_LOBBY) {
                            new QueueJoinMenu(false, false).openMenu(event.getPlayer());
                        }
                    }
                    break;
                    case QUEUE_LEAVE: {
                        if (playerData.isInQueue()) {
                            Queue queue = Queue.getByUuid(playerData.getQueuePlayer().getQueueUuid());

                            if (queue != null) {
                                queue.removePlayer(event.getPlayer());
                            }
                        }
                    }
                    break;
                    case SPECTATE_STOP: {
                        CommandHandler.executeCommand(event.getPlayer(), "stopspectate");
                    }
                    break;
                    case PARTY_CREATE: {
                        CommandHandler.executeCommand(event.getPlayer(), "party create");
                    }
                    break;
                    case PARTY_DISBAND: {
                        CommandHandler.executeCommand(event.getPlayer(), "party disband");
                    }
                    break;
                    case PARTY_INFORMATION: {
                        CommandHandler.executeCommand(event.getPlayer(), "party info");
                    }
                    break;
                    case PARTY_LEAVE: {
                        CommandHandler.executeCommand(event.getPlayer(), "party leave");
                    }
                    break;
                    case OTHER_PARTIES: {
                        new OtherPartiesMenu().openMenu(event.getPlayer());
                    }
                    break;
                    case KIT_EDITOR: {
                        if (playerData.getState() == PlayerState.IN_LOBBY) {
                            new SelectLadderKitMenu().openMenu(event.getPlayer());
                        }
                    }
                    break;
                }
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        event.setDeathMessage(null);

        PlayerData playerData = PlayerData.getByUuid(event.getEntity().getUniqueId());

        if (playerData.isInMatch()) {
            if (playerData.getMatch() != null) {
                List<Item> entities = new ArrayList<>();

                event.getDrops().forEach(itemStack -> entities.add(event.getEntity().getLocation().getWorld()
                        .dropItemNaturally(event.getEntity().getLocation(), itemStack)));
                event.getDrops().clear();

                EntityUtil.hideEntitiesForAllExcluding(playerData.getMatch().getInvolvedPlayers(), entities);

                playerData.getMatch().getDrops().addAll(entities);
                playerData.getMatch().handleDeath(event.getEntity(), null, false);
            }
        }

        event.getEntity().spigot().respawn();
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        PlayerData playerData = PlayerData.getByUuid(event.getPlayer().getUniqueId());

        if (playerData.isInMatch()) {
            if (playerData.getMatch() != null) {
                if (!playerData.getMatch().getLadder().isBuild()) {
                    event.setCancelled(true);
                }
            }
        } else {
            if (event.getPlayer().getGameMode() != GameMode.CREATIVE || !event.getPlayer().isOp()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            PlayerData playerData = PlayerData.getByUuid(player.getUniqueId());

            if (!playerData.isInMatch() || (playerData.isInMatch() && !playerData.getMatch().isFighting())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        Player attacker = EventUtil.getDamager(event);

        if (event.getEntity() instanceof Player && attacker != null) {
            PlayerData attackerData = PlayerData.getByUuid(attacker.getUniqueId());

            if (attackerData.isSpectating()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            PlayerData playerData = PlayerData.getByUuid(player.getUniqueId());

            if (!playerData.isInMatch() || (playerData.isInMatch() && !playerData.getMatch().isFighting())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        PlayerData playerData = PlayerData.getByUuid(event.getPlayer().getUniqueId());

        if (playerData.isInMatch()) {
            if (playerData.getMatch() != null) {
                Iterator<Item> itemIterator = playerData.getMatch().getDrops().iterator();

                while (itemIterator.hasNext()) {
                    Item item = itemIterator.next();

                    if (item.equals(event.getItem())) {
                        itemIterator.remove();
                        return;
                    }
                }

                event.setCancelled(true);
            }
        } else if (playerData.isSpectating()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        PlayerData playerData = PlayerData.getByUuid(event.getPlayer().getUniqueId());

        if (playerData.isInMatch()) {
            if (event.getItemDrop().getItemStack().getType().name().contains("SWORD")) {
                event.setCancelled(true);
                event.getPlayer().sendMessage(CC.RED + "Careful! You almost dropped your sword.");
                return;
            }

            if (playerData.getMatch() != null) {
                EntityUtil.hideEntitiesForAllExcluding(playerData.getMatch().getInvolvedPlayers(), Collections.singletonList(event.getItemDrop()));
                playerData.getMatch().getDrops().add(event.getItemDrop());
            }
        } else {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        final Player player = (Player) event.getWhoClicked();
        final PlayerData playerData = PlayerData.getByUuid(player.getUniqueId());

        if (!playerData.isInMatch() && player.getGameMode() == GameMode.SURVIVAL) {
            final Inventory clicked = event.getClickedInventory();

            if (playerData.getKitEditor().isActive()) {
                if (clicked == null) {
                    event.setCancelled(true);
                    event.setCursor(null);
                    player.updateInventory();
                } else if (clicked.equals(player.getOpenInventory().getTopInventory())) {
                    if (event.getCursor().getType() != Material.AIR && event.getCurrentItem().getType() == Material.AIR || event.getCursor().getType() != Material.AIR && event.getCurrentItem().getType() != Material.AIR) {
                        event.setCancelled(true);
                        event.setCursor(null);
                        player.updateInventory();
                    }
                }
            } else {
                if (clicked != null && clicked.equals(player.getInventory())) {
                    event.setCancelled(true);
                }
            }
        }
    }

}
