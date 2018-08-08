package me.joeleoli.praxi.command.param;

import me.joeleoli.nucleus.command.param.ParameterType;
import me.joeleoli.nucleus.util.CC;
import me.joeleoli.praxi.queue.Queue;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public class QueueParameterType implements ParameterType<Queue> {

    public Queue transform(CommandSender sender, String source) {
        try {
            Queue queue = Queue.getByUuid(UUID.fromString(source));

            if (queue == null) {
                sender.sendMessage(CC.RED + "A queue with that ID does not exist.");
                return null;
            }

            return queue;
        } catch (Exception e) {
            sender.sendMessage(CC.RED + "A queue with that ID does not exist.");
            return null;
        }
    }

    public List<String> tabComplete(Player sender, Set<String> flags, String source) {
        return Collections.emptyList();
    }

}