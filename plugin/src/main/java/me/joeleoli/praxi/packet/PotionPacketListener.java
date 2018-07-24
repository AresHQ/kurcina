package me.joeleoli.praxi.packet;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.BlockPosition;

import me.joeleoli.commons.util.Position;
import me.joeleoli.praxi.Praxi;
import me.joeleoli.praxi.player.PlayerData;

import java.util.Iterator;

public class PotionPacketListener extends PacketAdapter {

    public PotionPacketListener() {
        super(Praxi.getInstance(), PacketType.Play.Server.WORLD_EVENT);
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        PlayerData playerData = PlayerData.getByUuid(event.getPlayer().getUniqueId());
        Iterator<Position> positionIterator = playerData.getSplashPositions().iterator();

        int id = event.getPacket().getIntegers().read(0);
        BlockPosition blockPosition = event.getPacket().getBlockPositionModifier().read(0);

        if (id == 2002) {
            while (positionIterator.hasNext()) {
                final Position position = positionIterator.next();

                if (position.getX() == blockPosition.getX() && position.getY() == blockPosition.getY() && position.getZ() == blockPosition.getZ()) {
                    positionIterator.remove();
                    return;
                }
            }

            event.setCancelled(true);
        }
    }

}
