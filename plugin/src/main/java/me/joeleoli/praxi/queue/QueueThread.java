package me.joeleoli.praxi.queue;

import me.joeleoli.commons.util.TaskUtil;

import me.joeleoli.praxi.arena.Arena;
import me.joeleoli.praxi.arena.ArenaType;
import me.joeleoli.praxi.match.MatchPlayer;
import me.joeleoli.praxi.match.MatchType;
import me.joeleoli.praxi.player.PlayerState;
import me.joeleoli.praxi.match.Match;
import me.joeleoli.praxi.player.PlayerData;
import me.joeleoli.praxi.team.Team;

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

                        final PlayerData firstPlayerData = PlayerData.getByUuid(firstQueuePlayer.getPlayerUuid());

                        for (QueuePlayer secondQueuePlayer : queue.getPlayers()) {
                            if (firstQueuePlayer.equals(secondQueuePlayer)) {
                                continue;
                            }

                            final Player secondPlayer = Bukkit.getPlayer(secondQueuePlayer.getPlayerUuid());
                            final PlayerData secondPlayerData = PlayerData.getByUuid(secondQueuePlayer.getPlayerUuid());

                            if (secondPlayer == null) {
                                continue;
                            }

                            if (queue.isRanked()) {
                                if (!firstQueuePlayer.isInRange(secondQueuePlayer.getElo()) || !secondQueuePlayer.isInRange(firstQueuePlayer.getElo())) {
                                    continue;
                                }
                            }

                            // Find arena
                            final Arena arena = Arena.getRandomByType(queue.getLadder().isBuild() ? ArenaType.STANDALONE : ArenaType.SHARED);

                            if (arena == null) {
                                continue;
                            }

                            // Remove players from queue
                            queue.getPlayers().remove(firstQueuePlayer);
                            queue.getPlayers().remove(secondQueuePlayer);

                            // Create match
                            final Match match = new Match(MatchType.ONE_VS_ONE, queue.getLadder(), arena, queue.isRanked());

                            final MatchPlayer firstMatchPlayer = new MatchPlayer(firstPlayer);
                            final MatchPlayer secondMatchPlayer = new MatchPlayer(secondPlayer);

                            if (queue.isRanked()) {
                                firstMatchPlayer.setElo(firstPlayerData.getPlayerStatistics().getElo(queue.getLadder()));
                                secondMatchPlayer.setElo(secondPlayerData.getPlayerStatistics().getElo(queue.getLadder()));
                            }

                            match.setQueueUuid(queue.getUuid());
                            match.setTeamA(new Team<>(firstMatchPlayer));
                            match.setTeamB(new Team<>(secondMatchPlayer));

                            // Update arena
                            arena.setActive(true);

                            // Update player's states
                            firstPlayerData.setState(PlayerState.IN_FIGHT);
                            firstPlayerData.setQueuePlayer(null);
                            firstPlayerData.setMatch(match);

                            secondPlayerData.setState(PlayerState.IN_FIGHT);
                            secondPlayerData.setQueuePlayer(null);
                            secondPlayerData.setMatch(match);

                            TaskUtil.run(match::start);
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
