package me.joeleoli.praxi.queue;

import me.joeleoli.nucleus.NucleusAPI;
import me.joeleoli.nucleus.util.TaskUtil;

import me.joeleoli.praxi.arena.Arena;
import me.joeleoli.praxi.match.MatchPlayer;
import me.joeleoli.praxi.match.impl.SoloMatch;
import me.joeleoli.praxi.player.PlayerState;
import me.joeleoli.praxi.match.Match;
import me.joeleoli.praxi.player.PraxiPlayer;
import me.joeleoli.praxi.player.PracticeSetting;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class QueueThread extends Thread {

    @Override
    public void run() {
        while (true) {
            try {
                for (Queue queue : Queue.getQueues()) {
                    queue.getPlayers().forEach(QueuePlayer::tickRange);

                    if (queue.getPlayers().size() < 2) {
                        continue;
                    }

                    for (QueuePlayer firstQueuePlayer : queue.getPlayers()) {
                        final Player firstPlayer = Bukkit.getPlayer(firstQueuePlayer.getPlayerUuid());

                        if (firstPlayer == null) {
                            continue;
                        }

                        final PraxiPlayer firstPraxiPlayer = PraxiPlayer.getByUuid(firstQueuePlayer.getPlayerUuid());

                        for (QueuePlayer secondQueuePlayer : queue.getPlayers()) {
                            if (firstQueuePlayer.equals(secondQueuePlayer)) {
                                continue;
                            }

                            final Player secondPlayer = Bukkit.getPlayer(secondQueuePlayer.getPlayerUuid());
                            final PraxiPlayer secondPraxiPlayer = PraxiPlayer.getByUuid(secondQueuePlayer.getPlayerUuid());

                            if (secondPlayer == null) {
                                continue;
                            }

                            if (NucleusAPI.<Boolean>getSetting(firstPlayer, PracticeSetting.PING_FACTOR) || NucleusAPI.<Boolean>getSetting(secondPlayer, PracticeSetting.PING_FACTOR)) {
                                if (firstPlayer.getPing() >= secondPlayer.getPing()) {
                                    if (firstPlayer.getPing() - secondPlayer.getPing() >= 50) {
                                        continue;
                                    }
                                } else {
                                    if (secondPlayer.getPing() - firstPlayer.getPing() >= 50) {
                                        continue;
                                    }
                                }
                            }

                            if (queue.isRanked()) {
                                if (!firstQueuePlayer.isInRange(secondQueuePlayer.getElo()) || !secondQueuePlayer.isInRange(firstQueuePlayer.getElo())) {
                                    continue;
                                }
                            }

                            // Find arena
                            final Arena arena = Arena.getRandom(queue.getLadder());

                            if (arena == null) {
                                continue;
                            }

                            // Update arena
                            arena.setActive(true);

                            // Remove players from queue
                            queue.getPlayers().remove(firstQueuePlayer);
                            queue.getPlayers().remove(secondQueuePlayer);

                            final MatchPlayer firstMatchPlayer = new MatchPlayer(firstPlayer);
                            final MatchPlayer secondMatchPlayer = new MatchPlayer(secondPlayer);

                            if (queue.isRanked()) {
                                firstMatchPlayer.setElo(firstPraxiPlayer.getStatistics().getElo(queue.getLadder()));
                                secondMatchPlayer.setElo(secondPraxiPlayer.getStatistics().getElo(queue.getLadder()));
                            }

                            // Create match
                            final Match match = new SoloMatch(queue.getUuid(), firstMatchPlayer, secondMatchPlayer, queue.getLadder(), arena, queue.isRanked());

                            // Update player's states
                            firstPraxiPlayer.setState(PlayerState.IN_MATCH);
                            firstPraxiPlayer.setQueuePlayer(null);
                            firstPraxiPlayer.setMatch(match);

                            secondPraxiPlayer.setState(PlayerState.IN_MATCH);
                            secondPraxiPlayer.setQueuePlayer(null);
                            secondPraxiPlayer.setMatch(match);

                            TaskUtil.run(match::handleStart);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }

            try {
                Thread.sleep(200L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
