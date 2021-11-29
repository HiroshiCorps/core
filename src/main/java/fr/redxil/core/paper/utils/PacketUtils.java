package fr.redxil.core.paper.utils;

import net.minecraft.server.v1_12_R1.Packet;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class PacketUtils {

    public static void sendPacket(Player player, Packet<?> packet) {
        ((CraftPlayer) player).getHandle().playerConnection.sendPacketDirect(packet);
    }
}
