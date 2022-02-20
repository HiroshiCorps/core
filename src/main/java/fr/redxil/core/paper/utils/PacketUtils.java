package fr.redxil.core.paper.utils;

import net.minecraft.network.protocol.Packet;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class PacketUtils {

    public static void sendPacket(Player player, Packet<?> packet) {
        ((CraftPlayer) player).getHandle().b.sendPacket(packet);
    }
}
