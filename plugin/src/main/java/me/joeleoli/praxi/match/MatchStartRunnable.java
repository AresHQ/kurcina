package me.joeleoli.praxi.match;

import me.joeleoli.fairfight.FairFight;
import me.joeleoli.nucleus.util.CC;

import org.bukkit.Sound;
import org.bukkit.scheduler.BukkitRunnable;

public class MatchStartRunnable extends BukkitRunnable {

    private Match match;
    private int tick;

    public MatchStartRunnable(Match match) {
        this.match = match;
    }

    @Override
    public void run() {
        int seconds = 5 - this.tick;

        if (this.match.isEnding()) {
            this.cancel();
            return;
        }

        if (this.match.getLadder().isSumo()) {
            if (seconds == 2) {
                if (this.match.isSoloMatch()) {
                    FairFight.getInstance().getPlayerDataManager().getPlayerData(this.match.getPlayerA()).setAllowTeleport(false);
                    FairFight.getInstance().getPlayerDataManager().getPlayerData(this.match.getPlayerB()).setAllowTeleport(false);
                } else if (this.match.isTeamMatch()) {
                    this.match.getMatchPlayers().forEach(matchPlayer -> {
                        if (!matchPlayer.isDisconnected()) {
                            FairFight.getInstance().getPlayerDataManager().getPlayerData(matchPlayer.toPlayer()).setAllowTeleport(false);
                        }
                    });
                }

                this.match.setState(MatchState.FIGHTING);
                this.match.setStartTimestamp(System.currentTimeMillis());
                this.match.broadcast(CC.GREEN + "The round has started!");
                this.match.broadcast(Sound.NOTE_BASS);
                this.cancel();
                return;
            }

            this.match.broadcast(CC.YELLOW + "The round will start in " + CC.GREEN + (seconds - 2) + " second" + (seconds - 2== 1 ? "" : "s") + CC.YELLOW + "...");
            this.match.broadcast(Sound.NOTE_PLING);
        } else {
            if (seconds == 0) {
                this.match.setState(MatchState.FIGHTING);
                this.match.setStartTimestamp(System.currentTimeMillis());
                this.match.broadcast(CC.GREEN + "The match has started!");
                this.match.broadcast(Sound.NOTE_BASS);
                this.cancel();
                return;
            }

            this.match.broadcast(CC.YELLOW + "The match will start in " + CC.GREEN + seconds + " second" + (seconds == 1 ? "" : "s") + CC.YELLOW + "...");
            this.match.broadcast(Sound.NOTE_PLING);
        }

        this.tick++;
    }

}
