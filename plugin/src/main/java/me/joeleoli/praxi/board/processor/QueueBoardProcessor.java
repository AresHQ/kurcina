package me.joeleoli.praxi.board.processor;

import me.joeleoli.commons.util.DateUtil;

import me.joeleoli.praxi.board.BoardProcessor;
import me.joeleoli.praxi.script.ScriptContext;
import me.joeleoli.praxi.player.PlayerData;
import me.joeleoli.praxi.queue.Queue;

import java.util.List;

public class QueueBoardProcessor implements BoardProcessor {

    @Override
    public boolean canProcess(PlayerData playerData) {
        return playerData.isInQueue();
    }

    @Override
    public List<String> process(PlayerData playerData, List<String> lines) {
        Queue queue = Queue.getByUuid(playerData.getQueuePlayer().getQueueUuid());

        ScriptContext context = new ScriptContext(lines);

        if (queue != null) {
            context.addCondition("ranked", queue.isRanked());
            context.addCondition("unranked", queue.isRanked());
            context.addCondition("solo", !queue.isParty());
            context.addCondition("party", queue.isParty());
            context.addVariable("queue_name", queue.getLadder().getDisplayName());
            context.addVariable("queue_time", DateUtil.formatGameTime(playerData.getQueuePlayer().getPassed()));
            context.addVariable("queue_range_min", playerData.getQueuePlayer().getMinRange() + "");
            context.addVariable("queue_range_max", playerData.getQueuePlayer().getMaxRange() + "");
        }

        context.buildComponents();

        return context.getLines();
    }

}
