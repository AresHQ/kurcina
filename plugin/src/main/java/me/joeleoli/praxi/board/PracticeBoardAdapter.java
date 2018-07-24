package me.joeleoli.praxi.board;

import me.joeleoli.commons.board.Board;
import me.joeleoli.commons.board.BoardAdapter;
import me.joeleoli.commons.config.ConfigCursor;

import me.joeleoli.praxi.config.Config;
import me.joeleoli.praxi.Praxi;
import me.joeleoli.praxi.board.processor.MatchBoardProcessor;
import me.joeleoli.praxi.board.processor.PartyBoardProcessor;
import me.joeleoli.praxi.board.processor.QueueBoardProcessor;
import me.joeleoli.praxi.player.PlayerState;
import me.joeleoli.praxi.player.PlayerData;

import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;

import java.util.*;

public class PracticeBoardAdapter implements BoardAdapter {

    private String title;
    private Map<PlayerState, List<String>> layouts = new HashMap<>();
    private List<BoardProcessor> processors = new ArrayList<>();

    public PracticeBoardAdapter() {
        ConfigCursor cursor = new ConfigCursor(Praxi.getInstance().getMainConfig(), "scoreboard");

        this.title = cursor.getString("title");

        cursor.setPath("scoreboard.layouts");

        if (!cursor.exists()) {
            return;
        }

        for (String key : cursor.getKeys()) {
            try {
                PlayerState state = PlayerState.valueOf(key);

                this.layouts.put(state, cursor.getStringList(key));
            } catch (EnumConstantNotPresentException e) {
                e.printStackTrace();
            }
        }

        this.processors.add(new QueueBoardProcessor());
        this.processors.add(new MatchBoardProcessor());
        this.processors.add(new PartyBoardProcessor());
    }

    @Override
    public String getTitle(Player player) {
        return Config.translateGlobal(this.title);
    }

    @Override
    public List<String> getScoreboard(Player player, Board board) {
        final PlayerData playerData = PlayerData.getByUuid(player.getUniqueId());

        if (playerData == null || !playerData.getPlayerSettings().isShowScoreboard()) {
            return null;
        }

        if (!this.layouts.containsKey(playerData.getState())) {
            return null;
        }

        List<String> layout = this.layouts.get(playerData.getState());

        for (BoardProcessor processor : this.processors) {
            if (processor.canProcess(playerData)) {
                layout = processor.process(playerData, layout);
            }
        }

        List<String> toReturn = new ArrayList<>();

        for (String line : layout) {
            if (line != null) {
                toReturn.add(Config.translatePlayerAndTarget(line, player, null));
            }
        }

        return toReturn;
    }

    @Override
    public long getInterval() {
        return 2L;
    }

    @Override
    public void preLoop() {}

    @Override
    public void onScoreboardCreate(Player player, Scoreboard scoreboard) {}

}
