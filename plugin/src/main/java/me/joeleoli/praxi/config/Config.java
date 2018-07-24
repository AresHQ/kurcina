package me.joeleoli.praxi.config;

import lombok.Getter;

import me.joeleoli.commons.config.ConfigCursor;
import me.joeleoli.commons.config.ConfigVariable;
import me.joeleoli.commons.util.CC;
import me.joeleoli.commons.util.EnumUtil;

import me.joeleoli.praxi.Praxi;
import me.joeleoli.praxi.arena.Arena;
import me.joeleoli.praxi.kit.NamedKit;
import me.joeleoli.praxi.ladder.Ladder;
import me.joeleoli.praxi.party.Party;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Config {

    @Getter
    private static Map<ConfigKey, Object> variables = new HashMap<>();

    public static void init() {
        ConfigCursor cursor = new ConfigCursor(Praxi.getInstance().getMainConfig(), "praxi");

        if (cursor.exists()) {
            // nothing here yet
        }

        cursor.setPath("match");

        variables.put(ConfigKey.ENDER_PEARL_CD_TIME, cursor.getLong("enderpearl-cooldown.time"));
        variables.put(ConfigKey.ENDER_PEARL_CD_USE_BAR, cursor.getBoolean("enderpearl-cooldown.active-bar"));
        variables.put(ConfigKey.ENDER_PEARL_CD_REJECTED, cursor.getString("enderpearl-cooldown.rejected"));

        variables.put(ConfigKey.MATCH_KIT_RECEIVED, cursor.getString("kit-received"));
        variables.put(ConfigKey.MATCH_COUNTDOWN_LOOP, cursor.getStringList("countdown.loop"));
        variables.put(ConfigKey.MATCH_COUNTDOWN_FINISHED, cursor.getStringList("countdown.finished"));
        variables.put(ConfigKey.MATCH_START, cursor.getStringList("start"));
        variables.put(ConfigKey.MATCH_END, cursor.getStringList("end"));
        variables.put(ConfigKey.MATCH_DETAILS_NOT_FOUND, cursor.getStringList("details.not-found"));
        variables.put(ConfigKey.MATCH_DETAILS_SUCCESS, cursor.getStringList("details.success"));

        cursor.setPath("duel");

        variables.put(ConfigKey.DUEL_REJECTED_STATE, cursor.getString("rejected-state"));
        variables.put(ConfigKey.DUEL_REJECTED_TARGET_STATE, cursor.getString("rejected-target-state"));
        variables.put(ConfigKey.DUEL_REJECTED_TARGET_DISABLED, cursor.getString("rejected-target-disabled"));
        variables.put(ConfigKey.DUEL_INVITE_SELF, cursor.getString("invite-self"));
        variables.put(ConfigKey.DUEL_ALREADY_SENT, cursor.getString("already-sent"));
        variables.put(ConfigKey.DUEL_NOT_INVITED, cursor.getString("not-invited"));
        variables.put(ConfigKey.DUEL_INVITE_EXPIRED, cursor.getString("invite-expired"));
        variables.put(ConfigKey.DUEL_SUCCESS, cursor.getStringList("success"));
        variables.put(ConfigKey.DUEL_RECEIVER, cursor.getStringList("receiver"));

        cursor.setPath("queue");

        variables.put(ConfigKey.QUEUE_JOIN_REJECTED_STATE, cursor.getString("join.rejected-state"));
        variables.put(ConfigKey.QUEUE_JOIN_SUCCESS, cursor.getStringList("join.success"));
        variables.put(ConfigKey.QUEUE_LEFT_SUCCESS, cursor.getStringList("left.success"));

        cursor.setPath("spectate");

        variables.put(ConfigKey.SPECTATE_JOIN_REJECTED_STATE, cursor.getString("join.rejected-state"));
        variables.put(ConfigKey.SPECTATE_JOIN_REJECTED_TARGET_STATE, cursor.getString("join.rejected-target-state"));
        variables.put(ConfigKey.SPECTATE_JOIN_REJECTED_TARGET_DISABLED, cursor.getString("join.rejected-target-disabled"));
        variables.put(ConfigKey.SPECTATE_JOIN_SUCCESS, cursor.getStringList("join.success"));
        variables.put(ConfigKey.SPECTATE_JOIN_BROADCAST, cursor.getStringList("join.broadcast"));
        variables.put(ConfigKey.SPECTATE_QUIT_REJECTED_STATE, cursor.getString("quit.rejected-state"));
        variables.put(ConfigKey.SPECTATE_QUIT_BROADCAST, cursor.getStringList("quit.broadcast"));

        cursor.setPath("kit");

        variables.put(ConfigKey.KIT_DEFAULT_KIT, new ConfigItem(cursor, "default-kit"));
        variables.put(ConfigKey.KIT_EDITOR_RENAME_KIT_START, cursor.getString("editor.rename-kit.start"));
        variables.put(ConfigKey.KIT_EDITOR_RENAME_KIT_TOO_LONG, cursor.getString("editor.rename-kit.too-long"));

        cursor.setPath("party");

        variables.put(ConfigKey.PARTY_MAX_SIZE, cursor.getInt("max-size"));
        variables.put(ConfigKey.PARTY_INFORMATION, cursor.getStringList("information"));
        variables.put(ConfigKey.PARTY_CREATE_REJECTED_STATE, cursor.getString("create.rejected-state"));
        variables.put(ConfigKey.PARTY_CREATE_SUCCESS, cursor.getStringList("create.success"));
        variables.put(ConfigKey.PARTY_JOIN_NOT_FOUND, cursor.getString("join.not-found"));
        variables.put(ConfigKey.PARTY_JOIN_NOT_INVITED, cursor.getString("join.not-invited"));
        variables.put(ConfigKey.PARTY_JOIN_FULL, cursor.getString("join.full"));
        variables.put(ConfigKey.PARTY_JOIN_SUCCESS, cursor.getStringList("join.success"));
        variables.put(ConfigKey.PARTY_DISBANDED, cursor.getStringList("disbanded"));
        variables.put(ConfigKey.PARTY_LEAVE_SUCCESS, cursor.getStringList("leave.success"));
        variables.put(ConfigKey.PARTY_KICK_SELF, cursor.getString("kick.self"));
        variables.put(ConfigKey.PARTY_KICK_SUCCESS, cursor.getStringList("kick.success"));
        variables.put(ConfigKey.PARTY_INVITE_ALREADY_INVITED, cursor.getString("invite.already-invited"));
        variables.put(ConfigKey.PARTY_INVITE_ALREADY_MEMBER, cursor.getString("invite.already-member"));
        variables.put(ConfigKey.PARTY_INVITE_STATE_OPEN, cursor.getString("invite.state-open"));
        variables.put(ConfigKey.PARTY_INVITE_SUCCESS, cursor.getStringList("invite.success"));
        variables.put(ConfigKey.PARTY_INVITE_RECEIVER, cursor.getStringList("invite.receiver"));
        variables.put(ConfigKey.PARTY_STATE_CHANGE, cursor.getString("state-change"));
        variables.put(ConfigKey.PARTY_CHAT, cursor.getString("chat"));
        variables.put(ConfigKey.PARTY_ERROR_IN_PARTY, cursor.getString("error.in-party"));
        variables.put(ConfigKey.PARTY_ERROR_NO_PARTY, cursor.getString("error.no-party"));
        variables.put(ConfigKey.PARTY_ERROR_NOT_LEADER, cursor.getString("error.not-leader"));
        variables.put(ConfigKey.PARTY_ERROR_MEMBER_NOT_FOUND, cursor.getString("error.member-not-found"));

        cursor.setPath("menu");

        variables.put(ConfigKey.MENU_JOIN_QUEUE_TITLE, cursor.getString("join-queue.title"));
        variables.put(ConfigKey.MENU_JOIN_QUEUE_USE_AMOUNTS, cursor.getBoolean("join-queue.active-amounts"));
        variables.put(ConfigKey.MENU_JOIN_QUEUE_SELECT_LADDER_BUTTON, new ConfigItem(cursor, "join-queue.buttons.select-ladder"));
        variables.put(ConfigKey.MENU_OTHER_PARTIES_TITLE, cursor.getString("other-parties.title"));
        variables.put(ConfigKey.MENU_OTHER_PARTIES_PARTY_DISPLAY_BUTTON, new ConfigItem(cursor, "other-parties.buttons.party-display"));
        variables.put(ConfigKey.MENU_SELECT_LADDER_KIT_TITLE, cursor.getString("kit-editor.select-ladder-kit.title"));
        variables.put(ConfigKey.MENU_SELECT_LADDER_KIT_DISPLAY_BUTTON, new ConfigItem(cursor, "kit-editor.select-ladder-kit.display-button"));
        variables.put(ConfigKey.MENU_KIT_MANAGEMENT_TITLE, cursor.getString("kit-editor.kit-management.title"));
        variables.put(ConfigKey.MENU_KIT_MANAGEMENT_BACK_BUTTON, new ConfigItem(cursor, "kit-editor.kit-management.buttons.back"));
        variables.put(ConfigKey.MENU_KIT_MANAGEMENT_KIT_DISPLAY_BUTTON, new ConfigItem(cursor, "kit-editor.kit-management.buttons.kit-display"));
        variables.put(ConfigKey.MENU_KIT_MANAGEMENT_CREATE_KIT_BUTTON, new ConfigItem(cursor, "kit-editor.kit-management.buttons.create-kit"));
        variables.put(ConfigKey.MENU_KIT_MANAGEMENT_LOAD_KIT_BUTTON, new ConfigItem(cursor, "kit-editor.kit-management.buttons.load-kit"));
        variables.put(ConfigKey.MENU_KIT_MANAGEMENT_RENAME_KIT_BUTTON, new ConfigItem(cursor, "kit-editor.kit-management.buttons.rename-kit"));
        variables.put(ConfigKey.MENU_KIT_MANAGEMENT_DELETE_KIT_BUTTON, new ConfigItem(cursor, "kit-editor.kit-management.buttons.delete-kit"));
        variables.put(ConfigKey.MENU_KIT_EDITOR_TITLE, cursor.getString("kit-editor.kit-editor.title"));
        variables.put(ConfigKey.MENU_KIT_EDITOR_CURRENT_KIT_BUTTON, new ConfigItem(cursor, "kit-editor.kit-editor.buttons.current-kit"));
        variables.put(ConfigKey.MENU_KIT_EDITOR_CANCEL_BUTTON, new ConfigItem(cursor, "kit-editor.kit-editor.buttons.cancel"));
        variables.put(ConfigKey.MENU_KIT_EDITOR_SAVE_BUTTON, new ConfigItem(cursor, "kit-editor.kit-editor.buttons.save"));
        variables.put(ConfigKey.MENU_KIT_EDITOR_LOAD_DEFAULT_BUTTON, new ConfigItem(cursor, "kit-editor.kit-editor.buttons.load-default"));
        variables.put(ConfigKey.MENU_KIT_EDITOR_CLEAR_INVENTORY_BUTTON, new ConfigItem(cursor, "kit-editor.kit-editor.buttons.clear-inventory"));
        variables.put(ConfigKey.MENU_KIT_EDITOR_ARMOR_DISPLAY_BUTTON, new ConfigItem(cursor, "kit-editor.kit-editor.buttons.armor-display"));
        variables.put(ConfigKey.MENU_DUEL_ARENA_TITLE, cursor.getString("duel.arena.title"));
        variables.put(ConfigKey.MENU_DUEL_ARENA_SELECT_BUTTON, new ConfigItem(cursor, "duel.arena.buttons.select"));
        variables.put(ConfigKey.MENU_DUEL_LADDER_TITLE, cursor.getString("duel.ladder.title"));
        variables.put(ConfigKey.MENU_DUEL_LADDER_SELECT_BUTTON, new ConfigItem(cursor, "duel.ladder.buttons.select"));
        variables.put(ConfigKey.MENU_MATCH_DETAILS_TITLE, cursor.getString("match.details.title"));
        variables.put(ConfigKey.MENU_MATCH_DETAILS_HEALTH_BUTTON, new ConfigItem(cursor, "match.details.buttons.health"));
        variables.put(ConfigKey.MENU_MATCH_DETAILS_HUNGER_BUTTON, new ConfigItem(cursor, "match.details.buttons.hunger"));
        variables.put(ConfigKey.MENU_MATCH_DETAILS_EFFECTS_BUTTON, new ConfigItem(cursor, "match.details.buttons.effects"));
        variables.put(ConfigKey.MENU_MATCH_DETAILS_POTIONS_BUTTON, new ConfigItem(cursor, "match.details.buttons.potions"));
        variables.put(ConfigKey.MENU_MATCH_DETAILS_STATISTICS_BUTTON, new ConfigItem(cursor, "match.details.buttons.statistics"));
        variables.put(ConfigKey.MENU_MATCH_DETAILS_SWITCH_BUTTON, new ConfigItem(cursor, "match.details.buttons.switch"));
    }

    public static Boolean getBoolean(ConfigKey key) {
        return (Boolean) variables.get(key);
    }

    public static Long getLong(ConfigKey key) {
        return (Long) variables.get(key);
    }

    public static Integer getInteger(ConfigKey key) {
        return (Integer) variables.get(key);
    }

    public static String getString(ConfigKey key) {
        return translateGlobal((String) variables.get(key));
    }

    public static String getString(ConfigKey key, Player self) {
        return getString(key, self, null);
    }

    public static String getString(ConfigKey key, Player player, Player target) {
        return translatePlayerAndTarget((String) variables.get(key), player, target);
    }

    public static String getString(ConfigKey key, Party party) {
        return translateParty((String) variables.get(key), party);
    }

    public static String getString(ConfigKey key, Ladder ladder) {
        return translateLadder((String) variables.get(key), ladder);
    }

    public static String getString(ConfigKey key, NamedKit namedKit) {
        return translateKit((String) variables.get(key), namedKit);
    }

    public static List<String> getStringList(ConfigKey key) {
        List<String> toReturn = new ArrayList<>();

        ((List<String>) variables.get(key)).forEach(line -> toReturn.add(translateGlobal(line)));

        return toReturn;
    }

    public static ConfigItem getConfigItem(ConfigKey key) {
        return (ConfigItem) variables.get(key);
    }

    public static String translateGlobal(String source) {
        if (source == null) {
            return null;
        }

        return CC.translate(
                ConfigVariable.translate(source)
                        .replace("{online_players}", Bukkit.getOnlinePlayers().size() + "")
                        .replace("{in_queues}", Praxi.getInstance().getQueueingCount() + "")
                        .replace("{in_fights}", Praxi.getInstance().getFightingCount() + "")
        );
    }

    public static List<String> translateGlobal(List<String> source) {
        if (source == null) {
            return null;
        }

        List<String> translated = new ArrayList<>();

        source.forEach(line -> {
            translated.add(translateGlobal(line));
        });

        return translated;
    }

    public static String translatePlayerAndTarget(String source, Player player, Player target) {
        if (player != null) {
            source = source
                    .replace("{player_name}", player.getName())
                    .replace("{player_display_name}", player.getDisplayName());
        }

        if (target != null) {
            source = source
                    .replace("{target_name}", target.getName())
                    .replace("{target_display_name}", target.getDisplayName());
        }

        return translateGlobal(source);
    }

    public static String translateParty(String source, Party party) {
        if (party != null) {
            source = source
                    .replace("{party_count}", party.getPlayers().size() + "")
                    .replace("{party_leader_name}", party.getLeader().getName())
                    .replace("{party_leader_display_name}", party.getLeader().getDisplayName())
                    .replace("{party_state}", EnumUtil.toReadable(party.getState()));
        }

        return translateGlobal(source);
    }

    public static String translateLadder(String source, Ladder ladder) {
        if (ladder != null) {
            source = source
                    .replace("{ladder_name}", ladder.getName())
                    .replace("{ladder_display_name}", ladder.getDisplayName());
        }

        return translateGlobal(source);
    }

    public static String translateArena(String source, Arena arena) {
        if (arena != null) {
            source = source
                    .replace("{arena_name}", arena.getName());
        }

        return translateGlobal(source);
    }

    public static String translateKit(String source, NamedKit namedKit) {
        if (namedKit != null) {
            source = source
                    .replace("{kit_name}", namedKit.getName());
        }

        return translateGlobal(source);
    }

}
