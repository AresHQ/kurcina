package me.joeleoli.praxi.packet;

import com.comphenix.protocol.PacketType;

import me.joeleoli.commons.packet.Packet;

public class PacketFactory {

    public static Packet createSound(String soundName, int x, int y, int z) {
        Packet packet = new Packet(PacketType.Play.Server.NAMED_SOUND_EFFECT);

        packet.getStrings()
                .write(0, soundName);
        packet.getIntegers()
                .write(0, x * 8)
                .write(1, y * 8)
                .write(2, z * 8)
                .write(3, 63);
        packet.getFloat()
                .write(0, 0.6F);

        return packet;
    }

}
