package net.shoreline.client.api.event;

import net.minecraft.network.packet.Packet;

public class PacketReceiveEvent extends Event {
    public Packet packet;

    public PacketReceiveEvent(Packet packet) {
        this.packet = packet;
    }

    public Packet getPacket() {
        return this.packet;
    }
}
