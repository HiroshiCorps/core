package fr.redxil.core.paper.tags;

import fr.hiroshi.paper.packet.PacketResult;
import fr.hiroshi.paper.packet.type.PacketOutboundHandler;
import fr.redxil.api.paper.Paper;
import net.minecraft.server.v1_12_R1.Packet;
import net.minecraft.server.v1_12_R1.PacketPlayOutUpdateHealth;
import org.bukkit.entity.Player;

public class OutboundHandler implements PacketOutboundHandler {

    @Override
    public PacketResult onPacketSend(Player player, Packet<?> packet) {
        if (packet instanceof PacketPlayOutUpdateHealth && Paper.getInstance().getTagsManager().isDisplayHealth()) {
            PacketPlayOutUpdateHealth updateHealth = (PacketPlayOutUpdateHealth) packet;
            if (updateHealth.getScaledHealth() != player.getLastScaledHealth()) {
                Paper.getInstance().getTagsManager().getScore(player).setScore((int) player.getHealth());
            }
        }
        return PacketResult.ACCEPT;
    }
}
