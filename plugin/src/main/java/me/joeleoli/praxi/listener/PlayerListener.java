package me.joeleoli.praxi.listener;

import me.joeleoli.nucleus.Nucleus;
import me.joeleoli.nucleus.command.CommandHandler;
import me.joeleoli.nucleus.cooldown.Cooldown;
import me.joeleoli.nucleus.util.*;

import me.joeleoli.praxi.arena.Arena;
import me.joeleoli.praxi.arena.ArenaType;
import me.joeleoli.praxi.hotbar.HotbarItem;
import me.joeleoli.praxi.kit.Kit;
import me.joeleoli.praxi.kit.NamedKit;
import me.joeleoli.praxi.match.Match;
import me.joeleoli.praxi.match.MatchTeam;
import me.joeleoli.praxi.party.gui.PartyEventSelectEventMenu;
import me.joeleoli.praxi.player.PlayerState;
import me.joeleoli.praxi.kit.editor.gui.KitManagementMenu;
import me.joeleoli.praxi.kit.editor.gui.SelectLadderKitMenu;
import me.joeleoli.praxi.party.gui.OtherPartiesMenu;
import me.joeleoli.praxi.player.PlayerData;
import me.joeleoli.praxi.player.PlayerHotbar;
import me.joeleoli.praxi.match.gui.ViewInventoryMenu;
import me.joeleoli.praxi.queue.Queue;
import me.joeleoli.praxi.queue.gui.QueueJoinMenu;

import org.apache.commons.lang.StringEscapeUtils;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class PlayerListener implements Listener {

    @EventHandler
    public void onRegenerate(EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        if (event.getRegainReason() != EntityRegainHealthEvent.RegainReason.SATIATED) {
            return;
        }

        final Player player = (Player) event.getEntity();
        final PlayerData playerData = PlayerData.getByUuid(player.getUniqueId());

        if (playerData.isInMatch()) {
            if (!playerData.getMatch().getLadder().isRegeneration()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        if (event.getItem().getType() == Material.GOLDEN_APPLE) {
            if (!event.getItem().hasItemMeta() || !event.getItem().getItemMeta().getDisplayName().contains("Golden Head")) {
                return;
            }

            final Player player = event.getPlayer();

            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 200, 1));
            player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 2400, 0));
            player.setFoodLevel(Math.min(player.getFoodLevel() + 6, 20));
        }
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        final Player player = event.getPlayer();
        final PlayerData playerData = PlayerData.getByUuid(player.getUniqueId());

        if (playerData.isSpectating() && event.getRightClicked() instanceof Player && player.getItemInHand() != null) {
            final Player target = (Player) event.getRightClicked();

            if (PlayerHotbar.fromItemStack(player.getItemInHand()) == HotbarItem.VIEW_INVENTORY) {
                new ViewInventoryMenu(target).openMenu(player);
            }
        }
    }

    @EventHandler
    public void onProjectileLaunch(ProjectileLaunchEvent event) {
        if (event.getEntity() instanceof ThrownPotion) {
            if (event.getEntity().getShooter() instanceof Player) {
                final Player shooter = (Player) event.getEntity().getShooter();
                final PlayerData shooterData = PlayerData.getByUuid(shooter.getUniqueId());

                if (shooterData.isInMatch()) {
                    shooterData.getMatch().getMatchPlayer(shooter).incrementPotionsThrown();
                }
            }
        }
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        if (event.getEntity() instanceof Arrow) {
            if (event.getEntity().getShooter() instanceof Player) {
                final Player shooter = (Player) event.getEntity().getShooter();
                final PlayerData shooterData = PlayerData.getByUuid(shooter.getUniqueId());

                if (shooterData.isInMatch()) {
                    shooterData.getMatch().getEntities().add(event.getEntity());
                }
            }
        }
    }

    @EventHandler
    public void onProjectileHitEvent(ProjectileHitEvent event) {
        if (event.getEntity() instanceof Arrow) {
            if (event.getEntity().getShooter() instanceof Player) {
                final Player shooter = (Player) event.getEntity().getShooter();
                final PlayerData shooterData = PlayerData.getByUuid(shooter.getUniqueId());

                if (shooterData.isInMatch()) {
                    shooterData.getMatch().getMatchPlayer(shooter).handleHit();
                }
            }
        }
    }

    @EventHandler
    public void onPotionSplash(PotionSplashEvent event) {
        if (event.getPotion().getShooter() instanceof Player) {
            final Player shooter = (Player) event.getPotion().getShooter();
            final PlayerData shooterData = PlayerData.getByUuid(shooter.getUniqueId());

            if (shooterData.isInMatch()) {
                if (event.getIntensity(shooter) < 0.6D) {
                    shooterData.getMatch().getMatchPlayer(shooter).incrementPotionsMissed();
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOWEST)
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        final PlayerData playerData = PlayerData.getByUuid(event.getPlayer().getUniqueId());

        if (event.getMessage().startsWith("@") || event.getMessage().startsWith("!")) {
            if (playerData.getParty() != null) {
                event.setCancelled(true);
                playerData.getParty().broadcast(CC.GOLD + "[Party]" + CC.RESET + " " + event.getPlayer().getDisplayName() + CC.RESET + ": " + CC.strip(event.getMessage().substring(1)));
                return;
            }
        }

        if (playerData.getKitEditor().isRenaming()) {
            event.setCancelled(true);

            if (event.getMessage().length() > 16) {
                event.getPlayer().sendMessage(CC.RED + "A kit name cannot be more than 16 characters long.");
                return;
            }

            if (!playerData.isInMatch()) {
                new KitManagementMenu(playerData.getKitEditor().getSelectedLadder()).openMenu(event.getPlayer());
            }

            playerData.getKitEditor().getSelectedKit().setName(event.getMessage());
            playerData.getKitEditor().setActive(false);
            playerData.getKitEditor().setRename(false);
            playerData.getKitEditor().setSelectedKit(null);
        }
    }

    @EventHandler
    public void onAsyncPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
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

        event.getPlayer().sendMessage(new String[]{
                CC.HORIZONTAL_SEPARATOR,
                "",
                CC.YELLOW + " Welcome to " + CC.GOLD + "MineXD" + CC.YELLOW + "!",
                "",
                CC.
                "",
                CC.HORIZONTAL_SEPARATOR
        });
    }

    @EventHandler
    public void onPlayerKick(PlayerKickEvent event) {
        if (event.getReason() != null) {
            if (event.getReason().contains("Flying is not enabled")) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        event.setQuitMessage(null);

        final PlayerData playerData = PlayerData.getPlayers().remove(event.getPlayer().getUniqueId());

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

            queue.removePlayer(playerData.getQueuePlayer());
        }
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getItem() != null && event.getAction().name().contains("RIGHT")) {
            final Player player = event.getPlayer();
            final PlayerData playerData = PlayerData.getByUuid(player.getUniqueId());

            if (playerData.isInMatch()) {
                if (event.getItem().hasItemMeta() && event.getItem().getItemMeta().hasDisplayName()) {
                    if (event.getItem().equals(Kit.DEFAULT_KIT)) {
                        event.setCancelled(true);

                        final Kit kit = playerData.getMatch().getLadder().getDefaultKit();

                        player.getInventory().setArmorContents(kit.getArmor());
                        player.getInventory().setContents(kit.getContents());
                        player.updateInventory();
                        player.sendMessage(CC.YELLOW + "You have been given the" + CC.AQUA + " Default " + CC.YELLOW + "kit.");
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

                                    player.getInventory().setArmorContents(kit.getArmor());
                                    player.getInventory().setContents(kit.getContents());
                                    player.updateInventory();
                                    player.sendMessage(CC.YELLOW + "You have been given the " + CC.AQUA + kit.getName() + CC.YELLOW + " kit.");
                                    return;
                                }
                            }
                        }
                    }
                }

                if (event.getItem().getType() == Material.ENDER_PEARL || (event.getItem().getType() == Material.POTION && event.getItem().getDurability() >= 16000)) {
                    if (playerData.isInMatch() && playerData.getMatch().isStarting()) {
                        event.setCancelled(true);
                        return;
                    }
                }

                if (event.getItem().getType() == Material.ENDER_PEARL && event.getClickedBlock() == null) {
                    if (!playerData.isInMatch() || (playerData.isInMatch() && !playerData.getMatch().isFighting())) {
                        event.setCancelled(true);
                        return;
                    }

                    if (playerData.getMatch().isStarting()) {
                        event.setCancelled(true);
                        return;
                    }

                    if (playerData.isOnEnderpearlCooldown()) {
                        final String time = TimeUtil.formatSeconds(playerData.getEnderpearlCooldown().getRemaining());

                        event.setCancelled(true);
                        player.sendMessage(CC.RED + "You cannot enderpearl for another " + CC.BOLD + time + CC.RED + " seconds.");
                    } else {
                        playerData.setEnderpearlCooldown(new Cooldown(16_000));
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
                            new QueueJoinMenu(true).openMenu(event.getPlayer());
                        }
                    }
                    break;
                    case QUEUE_JOIN_UNRANKED: {
                        if (playerData.getState() == PlayerState.IN_LOBBY) {
                            new QueueJoinMenu(false).openMenu(event.getPlayer());
                        }
                    }
                    break;
                    case QUEUE_LEAVE: {
                        if (playerData.isInQueue()) {
                            Queue queue = Queue.getByUuid(playerData.getQueuePlayer().getQueueUuid());

                            if (queue != null) {
                                queue.removePlayer(playerData.getQueuePlayer());
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
                    case PARTY_EVENTS: {
                        new PartyEventSelectEventMenu().openMenu(player);
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
                    case SETTINGS: {
                        CommandHandler.executeCommand(event.getPlayer(), "settings");
                    }
                    break;
                }
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        event.setDeathMessage(null);

        final PlayerData playerData = PlayerData.getByUuid(event.getEntity().getUniqueId());

        if (playerData.isInMatch()) {
            final List<Item> entities = new ArrayList<>();

            event.getDrops().forEach(itemStack -> {
                entities.add(event.getEntity().getLocation().getWorld().dropItemNaturally(event.getEntity().getLocation(), itemStack));
            });
            event.getDrops().clear();

            EntityUtil.hideEntitiesForAllExcluding(playerData.getMatch().getInvolvedPlayers(), entities);

            playerData.getMatch().getEntities().addAll(entities);
            playerData.getMatch().handleDeath(event.getEntity(), event.getEntity().getKiller(), false);
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        event.setRespawnLocation(event.getPlayer().getLocation());

        final PlayerData playerData = PlayerData.getByUuid(event.getPlayer().getUniqueId());

        if (playerData.isInMatch()) {
            playerData.getMatch().handleRespawn(event.getPlayer());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        final PlayerData playerData = PlayerData.getByUuid(event.getPlayer().getUniqueId());

        if (playerData.isInMatch()) {
            final Match match = playerData.getMatch();

            if (match.getLadder().isBuild() && playerData.getMatch().isFighting()) {
                if (match.getLadder().isSpleef()) {
                    event.setCancelled(true);
                    return;
                }

                final Arena arena = match.getArena();
                final int x = (int) event.getBlockPlaced().getLocation().getX();
                final int y = (int) event.getBlockPlaced().getLocation().getY();
                final int z = (int) event.getBlockPlaced().getLocation().getZ();

                if (y > arena.getMaxBuildHeight()) {
                    event.getPlayer().sendMessage(CC.RED + "You have reached the maximum build height.");
                    event.setCancelled(true);
                    return;
                }

                if (x >= arena.getX1() && x <= arena.getX2() && y >= arena.getY1() && y <= arena.getY2() && z >= arena.getZ1() && z <= arena.getZ2()) {
                    match.getPlacedBlocks().add(event.getBlock().getLocation());
                } else {
                    event.setCancelled(true);
                }
            } else {
                event.setCancelled(true);
            }
        } else {
            if (event.getPlayer().getGameMode() != GameMode.CREATIVE || !event.getPlayer().isOp()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        final PlayerData playerData = PlayerData.getByUuid(event.getPlayer().getUniqueId());

        if (playerData.isInMatch()) {
            final Match match = playerData.getMatch();

            if (match.getLadder().isBuild() && playerData.getMatch().isFighting()) {
                final Arena arena = match.getArena();
                final Block block = event.getBlockClicked().getRelative(event.getBlockFace());
                final int x = (int) block.getLocation().getX();
                final int y = (int) block.getLocation().getY();
                final int z = (int) block.getLocation().getZ();

                if (y > arena.getMaxBuildHeight()) {
                    event.getPlayer().sendMessage(CC.RED + "You have reached the maximum build height.");
                    event.setCancelled(true);
                    return;
                }

                if (x >= arena.getX1() && x <= arena.getX2() && y >= arena.getY1() && y <= arena.getY2() && z >= arena.getZ1() && z <= arena.getZ2()) {
                    match.getPlacedBlocks().add(block.getLocation());
                } else {
                    event.setCancelled(true);
                }
            } else {
                event.setCancelled(true);
            }
        } else {
            if (event.getPlayer().getGameMode() != GameMode.CREATIVE || !event.getPlayer().isOp()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        final PlayerData playerData = PlayerData.getByUuid(event.getPlayer().getUniqueId());

        if (playerData.isInMatch()) {
            final Match match = playerData.getMatch();

            if (match.getLadder().isBuild() && playerData.getMatch().isFighting()) {
                if (match.getLadder().isSpleef()) {
                    if (event.getBlock().getType() == Material.SNOW_BLOCK || event.getBlock().getType() == Material.SNOW) {
                        match.getChangedBlocks().add(event.getBlock().getState());

                        event.getBlock().setType(Material.AIR);
                        event.getPlayer().getInventory().addItem(new ItemStack(Material.SNOW_BALL, 4));
                        event.getPlayer().updateInventory();
                    } else {
                        event.setCancelled(true);
                    }
                } else if (!match.getPlacedBlocks().remove(event.getBlock().getLocation())) {
                    event.setCancelled(true);
                }
            } else {
                event.setCancelled(true);
            }
        } else {
            if (event.getPlayer().getGameMode() != GameMode.CREATIVE || !event.getPlayer().isOp()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockFromTo(BlockFromToEvent event) {
        final int x = event.getBlock().getX();
        final int y = event.getBlock().getY();
        final int z = event.getBlock().getZ();

        Arena foundArena = null;

        for (Arena arena : Arena.getArenas()) {
            if (!(arena.getType() == ArenaType.STANDALONE || arena.getType() == ArenaType.DUPLICATE)) {
                continue;
            }

            if (!arena.isActive()) {
                continue;
            }

            if (x >= arena.getX1() && x <= arena.getX2() && y >= arena.getY1() && y <= arena.getY2() && z >= arena.getZ1() && z <= arena.getZ2()) {
                foundArena = arena;
                break;
            }
        }

        if (foundArena == null) {
            return;
        }

        for (Match match : Match.getMatches()) {
            if (match.getArena().equals(foundArena)) {
                if (match.isFighting()) {
                    match.getPlacedBlocks().add(event.getToBlock().getLocation());
                }

                break;
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

            if (playerData.isInMatch()) {
                if (playerData.getMatch().isTeamMatch()) {
                    if (!playerData.getMatch().getMatchPlayer(player).isAlive()) {
                        event.setCancelled(true);
                        return;
                    }
                }

                if (playerData.getMatch().getLadder().isSumo() || playerData.getMatch().getLadder().isSpleef()) {
                    event.setDamage(0);
                    player.setHealth(20.0);
                    player.updateInventory();
                }
            }

            if (event.getCause() == EntityDamageEvent.DamageCause.VOID) {
                PlayerUtil.spawn(player);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        final Player attacker = BukkitUtil.getDamager(event);

        if (event.getEntity() instanceof Player && attacker != null) {
            final PlayerData attackerData = PlayerData.getByUuid(attacker.getUniqueId());

            if (attackerData.isSpectating()) {
                event.setCancelled(true);
                return;
            }

            if (!attackerData.isInMatch()) {
                event.setCancelled(true);
                return;
            }

            final Match match = attackerData.getMatch();
            final Player damaged = (Player) event.getEntity();
            final PlayerData damagedData = PlayerData.getByUuid(damaged.getUniqueId());

            if (damagedData.isInMatch()) {
                if (match.getMatchId().equals(damagedData.getMatch().getMatchId())) {
                    if (!match.getMatchPlayer(attacker).isAlive()) {
                        event.setCancelled(true);
                        return;
                    }

                    if (match.isSoloMatch()) {
                        attackerData.getMatch().getMatchPlayer(attacker).handleHit();
                        damagedData.getMatch().getMatchPlayer(damaged).resetCombo();

                        if (event.getDamager() instanceof Arrow) {
                            double health = Math.ceil(damaged.getHealth() - event.getFinalDamage()) / 2.0D;

                            attacker.sendMessage(CC.GOLD + "You shot " + CC.AQUA + damaged.getName() + CC.GOLD + "!" + CC.GRAY + " (" + CC.RED + health + CC.DARK_RED + " " + StringEscapeUtils.unescapeJava("\u2764") + CC.GRAY + ")");
                        }
                    } else if (match.isTeamMatch()) {
                        final MatchTeam attackerTeam = match.getTeam(attacker);
                        final MatchTeam damagedTeam = match.getTeam(damaged);

                        if (attackerTeam == null || damagedTeam == null) {
                            event.setCancelled(true);
                        } else {
                            if (attackerTeam.equals(damagedTeam)) {
                                event.setCancelled(true);
                            } else {
                                attackerData.getMatch().getMatchPlayer(attacker).handleHit();
                                damagedData.getMatch().getMatchPlayer(damaged).resetCombo();

                                if (event.getDamager() instanceof Arrow) {
                                    double health = Math.ceil(damaged.getHealth() - event.getFinalDamage()) / 2.0D;

                                    attacker.sendMessage(CC.GOLD + "You shot " + CC.AQUA + damaged.getName() + CC.GOLD + "!" + CC.GRAY + " (" + CC.RED + health + CC.DARK_RED + " " + StringEscapeUtils.unescapeJava("\u2764") + CC.GRAY + ")");
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player) {
            final Player player = (Player) event.getEntity();
            final PlayerData playerData = PlayerData.getByUuid(player.getUniqueId());

            if (!playerData.isInMatch() || (playerData.isInMatch() && !playerData.getMatch().isFighting())) {
                event.setCancelled(true);
            } else {
                if (event.getFoodLevel() >= 20) {
                    event.setFoodLevel(20);
                    player.setSaturation(20);
                } else {
                    event.setCancelled(Nucleus.RANDOM.nextInt(100) > 25);
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        final PlayerData playerData = PlayerData.getByUuid(event.getPlayer().getUniqueId());

        if (playerData.isInMatch()) {
            if (!playerData.getMatch().getMatchPlayer(event.getPlayer()).isAlive()) {
                event.setCancelled(true);
                return;
            }

            Iterator<Entity> entityIterator = playerData.getMatch().getEntities().iterator();

            while (entityIterator.hasNext()) {
                Entity entity = entityIterator.next();

                if (entity instanceof Item && entity.equals(event.getItem())) {
                    entityIterator.remove();
                    return;
                }
            }

            event.setCancelled(true);
        } else if (playerData.isSpectating()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        PlayerData playerData = PlayerData.getByUuid(event.getPlayer().getUniqueId());

        if (event.getItemDrop().getItemStack().getType() == Material.BOOK) {
            event.getItemDrop().remove();
            return;
        }

        if (playerData.isInMatch()) {
            if (playerData.getMatch() != null) {
                if (event.getItemDrop().getItemStack().getType() == Material.GLASS_BOTTLE) {
                    event.getItemDrop().remove();
                    return;
                }

                EntityUtil.hideEntitiesForAllExcluding(playerData.getMatch().getInvolvedPlayers(), Collections.singletonList(event.getItemDrop()));

                playerData.getMatch().getEntities().add(event.getItemDrop());
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

    @EventHandler(ignoreCancelled = true)
    public void onPlayerItemDamage(PlayerItemDamageEvent event) {
        final PlayerData playerData = PlayerData.getByUuid(event.getPlayer().getUniqueId());

        if (playerData.getState() == PlayerState.IN_LOBBY) {
            event.setCancelled(true);
        }
    }

}
