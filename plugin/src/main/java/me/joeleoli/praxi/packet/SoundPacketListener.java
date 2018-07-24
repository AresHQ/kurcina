package me.joeleoli.praxi.packet;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import me.joeleoli.commons.util.Position;
import me.joeleoli.praxi.Praxi;
import me.joeleoli.praxi.player.PlayerData;

import java.util.Iterator;

public class SoundPacketListener extends PacketAdapter {

    public SoundPacketListener() {
        super(Praxi.getInstance(), PacketType.Play.Server.NAMED_SOUND_EFFECT);
    }

    @Override
    public void onPacketSending(PacketEvent event) {
        final PlayerData playerData = PlayerData.getByUuid(event.getPlayer().getUniqueId());

        if (!playerData.isInMatch()) {
            event.setCancelled(true);
            return;
        }

        String soundName = event.getPacket().getStrings().read(0);

        if (soundName.equalsIgnoreCase("random.bow") || soundName.equalsIgnoreCase("random.bowhit")) {
            final int x = event.getPacket().getIntegers().read(0) / 8;
            final int y = event.getPacket().getIntegers().read(1) / 8;
            final int z = event.getPacket().getIntegers().read(2) / 8;
            final Iterator<Position> positionIterator = playerData.getSoundPositions().iterator();

            while (positionIterator.hasNext()) {
                final Position position = positionIterator.next();

                if (position.getX() == x && position.getY() == y && position.getZ() == z) {
                    positionIterator.remove();
                    return;
                }
            }

            event.setCancelled(true);
        }
    }

}
