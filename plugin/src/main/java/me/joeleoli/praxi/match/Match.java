package me.joeleoli.praxi.match;

import me.joeleoli.commons.chat.ChatComponentBuilder;
import me.joeleoli.commons.chat.ChatComponentExtras;
import me.joeleoli.commons.composer.Composer;
import me.joeleoli.commons.composer.Replaceable;
import me.joeleoli.commons.composer.context.ConditionContext;
import me.joeleoli.commons.composer.context.LoopContext;
import me.joeleoli.commons.composer.context.ReplacementContext;
import me.joeleoli.commons.composer.factory.TeamPlayerFactory;
import me.joeleoli.commons.composer.processor.ConditionProcessor;
import me.joeleoli.commons.composer.processor.ForEachProcessor;
import me.joeleoli.commons.composer.processor.ReplacementProcessor;
import me.joeleoli.commons.nametag.NameTagHandler;
import me.joeleoli.commons.team.Team;
import me.joeleoli.commons.team.TeamPlayer;
import me.joeleoli.commons.util.*;

import me.joeleoli.praxi.ladder.Ladder;
import me.joeleoli.praxi.player.PlayerState;
import me.joeleoli.praxi.config.Config;
import me.joeleoli.praxi.config.ConfigKey;
import me.joeleoli.praxi.arena.Arena;
import me.joeleoli.praxi.elo.EloUtil;
import me.joeleoli.praxi.player.PlayerData;

import lombok.Getter;
import lombok.Setter;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;

import org.bukkit.ChatColor;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;

@Getter
public class Match {

    @Getter
    private static List<Match> matches = new ArrayList<>();
    private static final BaseComponent[] HOVER_TEXT = new ChatComponentBuilder(CC.GRAY + "Click to view this player's" +
            " inventory.").create();

    private UUID uuid = UUID.randomUUID();
    @Setter
    private UUID queueUuid;
    private MatchType type;
    @Setter
    private MatchState state;
    private Ladder ladder;
    private Arena arena;
    private boolean ranked;
    @Setter
    private Team<MatchPlayer> teamA;
    @Setter
    private Team<MatchPlayer> teamB;
    private List<MatchSnapshot> matchSnapshots = new ArrayList<>();
    private List<UUID> spectators = new ArrayList<>();
    private List<Item> drops = new ArrayList<>();
    @Setter
    private long startTimestamp;

    public Match(MatchType type, Ladder ladder, Arena arena, boolean ranked) {
        this.type = type;
        this.state = MatchState.STARTING;
        this.ladder = ladder;
        this.arena = arena;
        this.ranked = ranked;

        matches.add(this);
    }

    public boolean isStarting() {
        return this.state == MatchState.STARTING;
    }

    public boolean isFighting() {
        return this.state == MatchState.FIGHTING;
    }

    public boolean isEnding() {
        return this.state == MatchState.ENDING;
    }

    public void start() {
        this.teamA.getPlayers().forEach(player -> {
            // Setup friendly nametags
            this.teamA.getPlayers().forEach(teamMate -> {
                NameTagHandler.addToTeam(player, teamMate, ChatColor.GREEN);
            });

            // Setup enemy nametags
            this.teamB.getPlayers().forEach(enemy -> {
                NameTagHandler.addToTeam(player, enemy, ChatColor.RED);
            });

            // Teleport to starting position
            player.teleport(this.arena.getSpawn1());
        });

        this.teamB.getPlayers().forEach(player -> {
            this.teamB.getPlayers().forEach(teamMate -> {
                NameTagHandler.addToTeam(player, teamMate, ChatColor.GREEN);
            });

            this.teamA.getPlayers().forEach(enemy -> {
                NameTagHandler.addToTeam(player, enemy, ChatColor.RED);
            });

            player.teleport(this.arena.getSpawn2());
        });

        final List<Player> players = this.getPlayers();

        players.forEach(player -> {
            final Team<MatchPlayer> team = this.getTeam(player);
            final Team<MatchPlayer> opponent = this.getOpponent(team);
            final PlayerData playerData = PlayerData.getByUuid(player.getUniqueId());
            final PlayerData opponentData = PlayerData.getByUuid(opponent.getLeader().getUuid());

            final Composer composer = new Composer();

            composer.getContexts().add(
                    new ConditionContext()
                            .addCondition("ranked", this.isRanked())
                            .addCondition("unranked", !this.isRanked())
                            .addCondition("solo", this.isSolo())
                            .addCondition("party", !this.isSolo())
            );
            composer.getContexts().add(
                    new ReplacementContext()
                            .addReplacement("match_type", this.type.getName())
                            .addReplacement("team_leader_name", team.getLeader().getName())
                            .addReplacement("team_leader_display_name", team.getLeader().getDisplayName())
                            .addReplacement("opponent_team_leader_name", team.getLeader().getName())
                            .addReplacement("opponent_team_leader_display_name", team.getLeader().getDisplayName())
            );
            composer.getContexts().add(
                    new LoopContext<MatchPlayer>()
                            .addList("team", new TeamPlayerFactory<MatchPlayer>().createList(team.getTeamPlayers()))
                            .addList("opponent_team", new TeamPlayerFactory<MatchPlayer>().createList(opponent.getTeamPlayers()))
            );

            context.getReplaceables().clear();
            context.getReplaceables().add(this.ladder);
            context.getReplaceables().add(new PlayerInfoWrapper(team.getLeader(), "team"));
            context.getReplaceables().add(new PlayerInfoWrapper(opponent.getLeader(), "opponent"));

            if (this.ranked) {
                final int playerElo = this.isSolo() && this.ranked ? playerData.getPlayerStatistics().getElo(this
                        .ladder) : 0;
                final int opponentElo = this.isSolo() && this.ranked ? opponentData.getPlayerStatistics().getElo(this
                        .ladder) : 0;

                context.addVariable("player_elo", playerElo + "");
                context.addVariable("opponent_elo", opponentElo + "");
            }

            players.forEach(player::showPlayer);

            PlayerUtil.reset(player);

            // Add custom kit items
            for (ItemStack itemStack : playerData.getKitItems(this.ladder)) {
                player.getInventory().addItem(itemStack);
            }

            player.updateInventory();

            context.buildMultipleLines().forEach(player::sendMessage);
        });

        TaskUtil.runTimer(new MatchStartRunnable(this), 20L, 20L);
    }

    public void end() {
        matches.remove(this);

        this.state = MatchState.ENDING;
        this.arena.setActive(false);

        final Team<MatchPlayer> winnerTeam = this.getWinner();
        final Team<MatchPlayer> loserTeam = this.getOpponent(winnerTeam);
        final MatchPlayer winnerPlayer = winnerTeam.getLeader();
        final MatchPlayer loserPlayer = loserTeam.getLeader();
        final ScriptContext context = new ScriptContext(Config.getStringList(ConfigKey.MATCH_END));

        context.addCondition("ranked", this.isRanked());
        context.addCondition("unranked", this.isRanked());
        context.addCondition("solo", this.isSolo());
        context.addCondition("party", !this.isSolo());
        context.getReplaceables().add(new PlayerInfoWrapper(winnerPlayer, "winner"));
        context.getReplaceables().add(new PlayerInfoWrapper(loserPlayer, "loser"));

        // Add context to script context based on match type
        if (this.type == MatchType.ONE_VS_ONE) {
            final HoverEvent winnerHoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, HOVER_TEXT);
            final ClickEvent winnerClickEvent = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/viewinv " +
                    winnerPlayer.getUuid().toString());

            final HoverEvent loserHoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, HOVER_TEXT);
            final ClickEvent loserClickEvent = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/viewinv " +
                    loserPlayer.getUuid().toString());

            context.addComponentExtras("winner_clickable", new ChatComponentExtras(winnerHoverEvent, winnerClickEvent));
            context.addComponentExtras("loser_clickable", new ChatComponentExtras(loserHoverEvent, loserClickEvent));

            if (this.ranked) {
                int newWinnerElo = EloUtil.getNewRating(winnerPlayer.getElo(), loserPlayer.getElo(), true);
                int newLoserElo = EloUtil.getNewRating(loserPlayer.getElo(), winnerPlayer.getElo(), false);

                int winnerEloChange = newWinnerElo - winnerPlayer.getElo();
                int loserEloChange = loserPlayer.getElo() - newLoserElo;

                context.addVariable("winner_new_elo", newWinnerElo + "");
                context.addVariable("loser_new_elo", newLoserElo + "");
                context.addVariable("winner_elo_change", winnerEloChange + "");
                context.addVariable("loser_elo_change", loserEloChange + "");

                PlayerData winnerPlayerData = PlayerData.getByUuid(winnerPlayer.getUuid());
                PlayerData loserPlayerData = PlayerData.getByUuid(loserPlayer.getUuid());

                if (winnerPlayerData.isLoaded()) {
                    winnerPlayerData.getPlayerStatistics().getLadderStatistics(this.ladder).setElo(newWinnerElo);
                }

                if (loserPlayerData.isLoaded()) {
                    loserPlayerData.getPlayerStatistics().getLadderStatistics(this.ladder).setElo(newLoserElo);
                }
            }
        } else {

        }

        // Build the components after the script context is finished
        List<BaseComponent[]> components = context.buildComponents();

        // Store list of players so we don't have to fetch from bukkit every time
        List<Player> players = this.getPlayers();

        for (MatchPlayer matchPlayer : this.getMatchPlayers()) {
            if (matchPlayer.isDisconnected()) {
                continue;
            }

            Player player = matchPlayer.toPlayer();

            if (player != null) {
                if (matchPlayer.isAlive()) {
                    MatchSnapshot matchInventory = new MatchSnapshot(matchPlayer);

                    if (this.isSolo()) {
                        MatchPlayer opponent = this.getOpponent(player).getLeader();

                        matchInventory.setSwitchTo(opponent);
                    }

                    this.matchSnapshots.add(matchInventory);
                }

                PlayerData playerData = PlayerData.getByUuid(player.getUniqueId());

                PlayerUtil.spawn(player);
                playerData.setState(PlayerState.IN_LOBBY);
                playerData.setMatch(null);
                playerData.setEnderpearlCooldown(null);
                playerData.getSplashPositions().clear();
                playerData.getSoundPositions().clear();
                playerData.loadLayout();

                players.forEach(otherPlayer -> {
                    player.hidePlayer(otherPlayer);
                    NameTagHandler.removeFromTeams(player, otherPlayer);
                });

                components.forEach(player::sendMessage);
            }
        }

        this.getSpectators().forEach(this::removeSpectator);
        this.drops.forEach(Item::remove);
        this.matchSnapshots.forEach(matchInventory -> {
            matchInventory.setCreated(System.currentTimeMillis());
            MatchSnapshot.getCache().put(matchInventory.getMatchPlayer().getUuid(), matchInventory);
        });
    }

    public void cancel() {

    }

    public boolean isSolo() {
        return this.type == MatchType.ONE_VS_ONE;
    }

    public List<Player> getPlayers() {
        List<Player> players = new ArrayList<>();

        this.teamA.getTeamPlayers().forEach(matchPlayer -> {
            Player player = matchPlayer.toPlayer();

            if (player != null) {
                players.add(player);
            }
        });

        this.teamB.getTeamPlayers().forEach(matchPlayer -> {
            Player player = matchPlayer.toPlayer();

            if (player != null) {
                players.add(player);
            }
        });

        return players;
    }

    public List<MatchPlayer> getMatchPlayers() {
        List<MatchPlayer> matchPlayers = new ArrayList<>();
        matchPlayers.addAll(this.teamA.getTeamPlayers());
        matchPlayers.addAll(this.teamB.getTeamPlayers());
        return matchPlayers;
    }

    public MatchPlayer getMatchPlayer(Player player) {
        for (MatchPlayer matchPlayer : this.teamA.getTeamPlayers()) {
            if (matchPlayer.getUuid().equals(player.getUniqueId())) {
                return matchPlayer;
            }
        }

        for (MatchPlayer matchPlayer : this.teamB.getTeamPlayers()) {
            if (matchPlayer.getUuid().equals(player.getUniqueId())) {
                return matchPlayer;
            }
        }

        return null;
    }

    public Team<MatchPlayer> getTeam(Player player) {
        for (MatchPlayer matchPlayer : this.teamA.getTeamPlayers()) {
            if (matchPlayer.getUuid().equals(player.getUniqueId())) {
                return this.teamA;
            }
        }

        return this.teamB;
    }

    public Team<MatchPlayer> getOpponent(Team team) {
        return this.teamA.equals(team) ? this.teamB : this.teamA;
    }

    public Team<MatchPlayer> getOpponent(Player player) {
        Team team = this.getTeam(player);

        return this.teamA.equals(team) ? this.teamB : this.teamA;
    }

    public Team<MatchPlayer> getWinner() {
        if (this.teamA.getAliveTeamPlayers().isEmpty()) {
            return this.teamB;
        } else if (this.teamB.getAliveTeamPlayers().isEmpty()) {
            return this.teamA;
        } else {
            return null;
        }
    }

    public List<Player> getSpectators() {
        return PlayerUtil.toBukkitPlayers(this.spectators);
    }

    public void addSpectator(Player player, Player target) {
        this.getPlayers().forEach(other -> {
            player.showPlayer(other);
            other.hidePlayer(player);
        });

        this.spectators.add(player.getUniqueId());

        PlayerUtil.reset(player);

        PlayerData playerData = PlayerData.getByUuid(player.getUniqueId());

        playerData.setMatch(this);
        playerData.setState(PlayerState.SPECTATE_MATCH);
        playerData.loadLayout();
        player.setAllowFlight(true);
        player.setFlying(true);
        player.updateInventory();
        player.teleport(target.getLocation().add(0, 2, 0));

        Config.getStringList(ConfigKey.SPECTATE_JOIN_SUCCESS).forEach(line -> {
            player.sendMessage(Config.translatePlayerAndTarget(line, player, target));
        });

        if (!player.hasPermission("praxi.spectate.hidden")) {
            List<String> toBroadcast = new ArrayList<>();

            Config.getStringList(ConfigKey.SPECTATE_JOIN_BROADCAST).forEach(line -> {
                toBroadcast.add(Config.translatePlayerAndTarget(line, player, null));
            });

            this.broadcast(toBroadcast);
        }
    }

    public void removeSpectator(Player player) {
        this.getPlayers().forEach(other -> {
            player.hidePlayer(other);
            other.hidePlayer(player);
        });

        this.spectators.remove(player.getUniqueId());

        PlayerUtil.spawn(player);

        PlayerData playerData = PlayerData.getByUuid(player.getUniqueId());

        if (this.state != MatchState.ENDING && !player.hasPermission("praxi.spectate.hidden")) {
            playerData.getMatch().broadcast(Config.getString(ConfigKey.SPECTATE_QUIT_BROADCAST, player, null));
        }

        playerData.setState(PlayerState.IN_LOBBY);
        playerData.setMatch(null);
        playerData.getSplashPositions().clear();
        playerData.getSoundPositions().clear();
        playerData.loadLayout();
    }

    public void handleDeath(Player player, Player killer, boolean disconnected) {
        MatchPlayer matchPlayer = this.getMatchPlayer(player);

        matchPlayer.setAlive(false);
        matchPlayer.setDisconnected(disconnected);

        MatchSnapshot matchInventory = new MatchSnapshot(matchPlayer);

        if (this.isSolo()) {
            MatchPlayer opponent = this.getOpponent(player).getLeader();

            matchInventory.setSwitchTo(opponent);
        }

        this.matchSnapshots.add(matchInventory);

        if (this.isFinished()) {
            TaskUtil.runLater(this::end, 2L);
        }
    }

    public long getElapsedDuration() {
        return System.currentTimeMillis() - this.startTimestamp;
    }

    public boolean isFinished() {
        return this.teamA.getAliveTeamPlayers().isEmpty() || this.teamB.getAliveTeamPlayers().isEmpty();
    }

    public void broadcast(String message) {
        this.getPlayers().forEach(player -> player.sendMessage(message));
        this.getSpectators().forEach(player -> player.sendMessage(message));
    }

    public void broadcast(List<String> messages) {
        this.getPlayers().forEach(player -> messages.forEach(player::sendMessage));
        this.getSpectators().forEach(player -> messages.forEach(player::sendMessage));
    }

    public List<UUID> getInvolvedPlayers() {
        List<UUID> toReturn = new ArrayList<>();

        toReturn.addAll(this.spectators);
        this.getMatchPlayers().forEach(matchPlayer -> toReturn.add(matchPlayer.getUuid()));

        return toReturn;
    }

}
