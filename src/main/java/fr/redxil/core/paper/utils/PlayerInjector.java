package fr.redxil.core.paper.utils;

import io.netty.channel.*;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class PlayerInjector {

    public static void injectPlayer(Player player) {
        ChannelDuplexHandler channelDuplexHandler = new ChannelDuplexHandler() {
            @Override
            public void channelRead(ChannelHandlerContext chc, Object packet) throws Exception {
                super.channelRead(chc, packet);
            }

            @Override
            public void write(ChannelHandlerContext chc, Object packet, ChannelPromise channelPromise) throws Exception {
                super.write(chc, packet, channelPromise);
            }
        };
        ChannelPipeline pipeline = ((CraftPlayer) player).getHandle().playerConnection.networkManager.channel.pipeline();
        pipeline.addBefore("packet_handler", player.getUniqueId().toString(), channelDuplexHandler);
    }

    public static void removeInject(Player player) {
        Channel channel = ((CraftPlayer) player).getHandle().playerConnection.networkManager.channel;
        channel.eventLoop().submit(() -> {
            channel.pipeline().remove(player.getUniqueId().toString());
            return null;
        });
    }

}
