package me.joeleoli.praxi.party.gui;

import lombok.AllArgsConstructor;

import me.joeleoli.nucleus.menu.Button;
import me.joeleoli.nucleus.menu.Menu;
import me.joeleoli.nucleus.util.CC;
import me.joeleoli.nucleus.util.ItemBuilder;
import me.joeleoli.praxi.arena.Arena;
import me.joeleoli.praxi.ladder.Ladder;
import me.joeleoli.praxi.match.Match;
import me.joeleoli.praxi.match.MatchPlayer;
import me.joeleoli.praxi.match.MatchTeam;
import me.joeleoli.praxi.match.impl.TeamMatch;
import me.joeleoli.praxi.party.Party;
import me.joeleoli.praxi.party.PartyEvent;
import me.joeleoli.praxi.player.PlayerData;
import me.joeleoli.praxi.player.PlayerState;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class PartyEventSelectLadderMenu extends Menu {

    @Override
    public String getTitle(Player player) {
        return CC.GOLD + CC.BOLD + "Select a ladder...";
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        final Map<Integer, Button> buttons = new HashMap<>();

        for (Ladder ladder : Ladder.getLadders()) {
            if (ladder.isEnabled()) {
                buttons.put(buttons.size(), new SelectLadderButton(ladder));
            }
        }

        return buttons;
    }

    @Override
    public void onClose(Player player) {
        if (!this.isClosedByMenu()) {
            final PlayerData playerData = PlayerData.getByUuid(player.getUniqueId());

            if (playerData.getParty() != null) {
                playerData.getParty().setSelectedEvent(null);
            }
        }
    }

    @AllArgsConstructor
    private class SelectLadderButton extends Button {

        private Ladder ladder;

        @Override
        public ItemStack getButtonItem(Player player) {
            return new ItemBuilder(this.ladder.getDisplayIcon())
                    .name(this.ladder.getDisplayName())
                    .lore(Arrays.asList(
                            "",
                            CC.YELLOW + "Click here to select " + CC.BOLD + this.ladder.getDisplayName() + CC.YELLOW + "."
                    ))
                    .build();
        }

        @Override
        public void clicked(Player player, int slot, ClickType clickType, int hbSlot) {
            Menu.currentlyOpenedMenus.get(player.getName()).setClosedByMenu(true);

            player.closeInventory();

            final PlayerData playerData = PlayerData.getByUuid(player.getUniqueId());

            if (playerData.getParty() == null) {
                player.sendMessage(CC.RED + "You are not in a party.");
                return;
            }

            if (playerData.getParty().getSelectedEvent() == null) {
                return;
            }

            if (playerData.getParty().getTeamPlayers().size() <= 1) {
                player.sendMessage(CC.RED + "You do not have enough players in your party to start an event.");
                return;
            }

            Party party = playerData.getParty();
            Arena arena = Arena.getRandom(this.ladder);

            if (arena == null) {
                player.sendMessage(CC.RED + "There are no available arenas.");
                return;
            }

            arena.setActive(true);

            Match match;

            if (party.getSelectedEvent() == PartyEvent.FFA) {
                player.sendMessage(CC.RED + "The FFA party event is currently disabled.");
                return;
            } else {
                MatchTeam teamA = new MatchTeam(new MatchPlayer(party.getLeader().toPlayer()));
                MatchTeam teamB = new MatchTeam(new MatchPlayer(party.getPlayers().get(1)));

                final List<Player> players = new ArrayList<>();

                players.addAll(party.getPlayers());

                Collections.shuffle(players);

                // Create match
                match = new TeamMatch(teamA, teamB, this.ladder, arena);

                for (Player other : players) {
                    final PlayerData otherData = PlayerData.getByUuid(other.getUniqueId());

                    otherData.setState(PlayerState.IN_MATCH);
                    otherData.setMatch(match);

                    if (teamA.getLeader().getUuid().equals(other.getUniqueId()) || teamB.getLeader().getUuid().equals(other.getUniqueId())) {
                        continue;
                    }

                    if (teamA.getTeamPlayers().size() > teamB.getTeamPlayers().size()) {
                        teamB.getTeamPlayers().add(new MatchPlayer(other));
                    } else {
                        teamA.getTeamPlayers().add(new MatchPlayer(other));
                    }
                }
            }

            // Start match
            match.handleStart();
        }

    }

}
