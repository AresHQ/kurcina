package me.joeleoli.praxi.player.gui;

import lombok.AllArgsConstructor;

import me.joeleoli.nucleus.menu.Button;
import me.joeleoli.nucleus.menu.Menu;
import me.joeleoli.nucleus.player.PlayerData;
import me.joeleoli.nucleus.player.Setting;
import me.joeleoli.nucleus.util.CC;
import me.joeleoli.nucleus.util.ItemBuilder;
import me.joeleoli.nucleus.util.TextSplitter;
import me.joeleoli.praxi.player.PracticeSetting;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerSettingsMenu extends Menu {

    @Override
    public String getTitle(Player player) {
        return CC.PINK + CC.BOLD + "Your Settings";
    }

    @Override
    public Map<Integer, Button> getButtons(Player player) {
        final Map<Integer, Button> buttons = new HashMap<>();

        for (SettingInfo settingInfo : SettingInfo.values()) {
            buttons.put(buttons.size(), new ToggleButton(settingInfo));
        }

        return buttons;
    }

    @AllArgsConstructor
    private enum SettingInfo {
        SCOREBOARD(
                PracticeSetting.SHOW_SCOREBOARD,
                CC.PINK + CC.BOLD + "Scoreboard",
                "If enabled, information will be displayed on your side scoreboard.",
                Material.ITEM_FRAME,
                "Show your scoreboard",
                "Hide your scoreboard"
        ),
        SPECTATORS(
                PracticeSetting.ALLOW_SPECTATORS,
                CC.AQUA + CC.BOLD + "Spectators",
                "If enabled, players can spectate your match with /spectate.",
                Material.REDSTONE_TORCH_ON,
                "Let players spectate your matches",
                "Don't let players spectate your matches"
        ),
        DUEL_REQUESTS(
                PracticeSetting.RECEIVE_DUEL_REQUESTS,
                CC.RED + CC.BOLD + "Duel Requests",
                "If enabled, players can send you duel requests.",
                Material.BLAZE_ROD,
                "Let players send you duel requests",
                "Don't let players send you duel requests"
        ),
        GLOBAL_MESSAGES(
                Setting.GlobalSetting.RECEIVE_GLOBAL_MESSAGES,
                CC.GREEN + CC.BOLD + "Global Messages",
                "If enabled, you will receive global chat messages.",
                Material.BOOK_AND_QUILL,
                "Receive global chat messages",
                "Don't receive global chat message"
        ),
        PRIVATE_MESSAGES(
                Setting.GlobalSetting.RECEIVE_PRIVATE_MESSAGES,
                CC.BLUE + CC.BOLD + "Private Messages",
                "If enabled, you will receive private chat messages.",
                Material.NAME_TAG,
                "Receive private chat messages",
                "Don't receive private chat message"
        ),
        MESSAGE_SOUNDS(
                Setting.GlobalSetting.PLAY_MESSAGE_SOUNDS,
                CC.YELLOW + CC.BOLD + "Message Sounds",
                "If enabled, you will be notified via sound when you receive private messages.",
                Material.RECORD_7,
                "Play message sounds",
                "Don't play message sounds"
        ),
        PING_FACTOR(
                PracticeSetting.PING_FACTOR,
                CC.DARK_PURPLE + CC.BOLD + "Ping Factor",
                "If enabled, you will only be matched against players that have a similar ping to you.",
                Material.EYE_OF_ENDER,
                "Be matched against players with similar ping",
                "Be matched against players with any ping"
        );

        private Setting setting;
        private String title;
        private String description;
        private Material material;
        private String enabledDescription;
        private String disabledDescription;

        public void toggle(Player player) {
            final PlayerData playerData = PlayerData.getByUuid(player.getUniqueId());

            playerData.getSettings().getSettings().put(this.setting, !this.get(player));
        }

        public boolean get(Player player) {
            final PlayerData playerData = PlayerData.getByUuid(player.getUniqueId());

            return playerData.getSettings().getBoolean(this.setting);
        }
    }

    @AllArgsConstructor
    private static class ToggleButton extends Button {

        private SettingInfo settingInfo;

        @Override
        public ItemStack getButtonItem(Player player) {
            final List<String> lore = new ArrayList<>();

            lore.add("");
            lore.addAll(TextSplitter.split(this.settingInfo.description, CC.BLUE));
            lore.add("");
            lore.add(" " + (this.settingInfo.get(player) ? CC.BLUE + CC.ARROW_RIGHT : " ") + " " + CC.YELLOW + this.settingInfo.enabledDescription);
            lore.add(" " + (!this.settingInfo.get(player) ? CC.BLUE + CC.ARROW_RIGHT : " ") + " " + CC.YELLOW + this.settingInfo.disabledDescription);

            return new ItemBuilder(this.settingInfo.material)
                    .name(this.settingInfo.title)
                    .lore(lore)
                    .build();
        }

        @Override
        public void clicked(Player player, int slot, ClickType clickType, int hbSlot) {
            this.settingInfo.toggle(player);
        }

        @Override
        public boolean shouldUpdate(Player player, int slot, ClickType clickType) {
            return true;
        }

    }

}
