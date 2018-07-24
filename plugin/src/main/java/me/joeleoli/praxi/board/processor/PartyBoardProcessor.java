package me.joeleoli.praxi.board.processor;

import me.joeleoli.commons.util.EnumUtil;

import me.joeleoli.praxi.board.BoardProcessor;
import me.joeleoli.praxi.script.ScriptContext;
import me.joeleoli.praxi.player.PlayerData;

import java.util.List;

public class PartyBoardProcessor implements BoardProcessor {

    @Override
    public boolean canProcess(PlayerData playerData) {
        return true;
    }

    @Override
    public List<String> process(PlayerData playerData, List<String> lines) {
        ScriptContext context = new ScriptContext(lines);

        context.addCondition("party", playerData.getParty() != null);

        if (playerData.getParty() != null) {
            context.addVariable("party_count", playerData.getParty().getPlayers().size() + "");
            context.addVariable("party_leader_name", playerData.getParty().getLeader().getName());
            context.addVariable("party_leader_display_name", playerData.getParty().getLeader().getDisplayName());
            context.addVariable("party_state", EnumUtil.toReadable(playerData.getParty().getState()));
        }

        context.buildComponents();

        return context.getLines();
    }

}
