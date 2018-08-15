package me.joeleoli.praxi.listener;

import me.joeleoli.nucleus.Nucleus;
import me.joeleoli.nucleus.util.BukkitUtil;
import me.joeleoli.nucleus.util.PlayerUtil;
import me.joeleoli.nucleus.util.Style;
import me.joeleoli.praxi.match.Match;
import me.joeleoli.praxi.match.MatchTeam;
import me.joeleoli.praxi.player.PraxiPlayer;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;

public class EntityListener implements Listener {

	@EventHandler(ignoreCancelled = true)
	public void onEntityDamage(EntityDamageEvent event) {
		if (event.getEntity() instanceof Player) {
			Player player = (Player) event.getEntity();
			PraxiPlayer praxiPlayer = PraxiPlayer.getByUuid(player.getUniqueId());

			if (!praxiPlayer.isInMatch() || (praxiPlayer.isInMatch() && !praxiPlayer.getMatch().isFighting())) {
				event.setCancelled(true);
			}

			if (praxiPlayer.isInMatch()) {
				if (praxiPlayer.getMatch().isTeamMatch()) {
					if (!praxiPlayer.getMatch().getMatchPlayer(player).isAlive()) {
						event.setCancelled(true);
						return;
					}
				}

				if (praxiPlayer.getMatch().getLadder().isSumo() || praxiPlayer.getMatch().getLadder().isSpleef()) {
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

		if (attacker != null && event.getEntity() instanceof Player) {
			final PraxiPlayer attackerData = PraxiPlayer.getByUuid(attacker.getUniqueId());

			if (attackerData.isSpectating() || !attackerData.isInMatch()) {
				event.setCancelled(true);
				return;
			}

			final Match match = attackerData.getMatch();
			final Player damaged = (Player) event.getEntity();
			final PraxiPlayer damagedData = PraxiPlayer.getByUuid(damaged.getUniqueId());

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

							attacker.sendMessage(
									Style.YELLOW + "You shot " + Style.PINK + damaged.getName() + Style.YELLOW + "!" +
									Style.GRAY + " (" + Style.RED + health + Style.DARK_RED + " " +
									Style.UNICODE_HEART + Style.GRAY + ")");
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

									attacker.sendMessage(
											Style.YELLOW + "You shot " + Style.PINK + damaged.getName() + Style.YELLOW +
											"!" + Style.GRAY + " (" + Style.RED + health + Style.DARK_RED + " " +
											Style.UNICODE_HEART + Style.GRAY + ")");
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
			final PraxiPlayer praxiPlayer = PraxiPlayer.getByUuid(player.getUniqueId());

			if (!praxiPlayer.isInMatch() || (praxiPlayer.isInMatch() && !praxiPlayer.getMatch().isFighting())) {
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

}
