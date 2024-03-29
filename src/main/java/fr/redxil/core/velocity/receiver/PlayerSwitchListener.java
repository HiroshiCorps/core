/*
 *
 * Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 *
 */

package fr.redxil.core.velocity.receiver;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import fr.redline.pms.pm.PMReceiver;
import fr.redline.pms.pm.RedisPMManager;
import fr.redxil.api.common.server.Server;
import fr.redxil.api.common.utils.Color;
import fr.redxil.core.common.CoreAPI;
import fr.redxil.core.velocity.CoreVelocity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

import java.util.Optional;

public class PlayerSwitchListener implements PMReceiver {

    public PlayerSwitchListener() {
        CoreAPI.getInstance().getRedisManager().ifPresent(redis -> RedisPMManager.addRedissonPMListener(redis.getRedissonClient(), "switchServer", String.class, this));
    }

    @Override
    public void redisPluginMessageReceived(String title, Object message) {

        if (!(message instanceof String)) return;

        String[] dataList = ((String) message).split("<switchSplit>");
        Optional<Player> playerO = CoreVelocity.getInstance().getProxyServer().getPlayer(dataList[0]);

        if (playerO.isEmpty()) return;

        Player player = playerO.get();

        Optional<Server> server = CoreAPI.getInstance().getServerManager().getServer(Long.parseLong(dataList[1]));
        if (server.isEmpty()) {
            player.sendMessage(Component.text("Cannot connect you to server: " + dataList[1]).color(TextColor.color(Color.RED.getRed(), Color.RED.getGreen(), Color.RED.getBlue())));
            return;
        }

        Optional<String> serverName = server.get().getServerName();
        if (serverName.isEmpty()) {
            player.sendMessage(Component.text("Cannot connect you to server: " + dataList[1]).color(TextColor.color(Color.RED.getRed(), Color.RED.getGreen(), Color.RED.getBlue())));
            return;
        }

        Optional<RegisteredServer> serverInfo = CoreVelocity.getInstance().getProxyServer().getServer(serverName.get());
        if (serverInfo.isEmpty()) {
            player.sendMessage(Component.text("Cannot connect you to server: " + dataList[1]).color(TextColor.color(Color.RED.getRed(), Color.RED.getGreen(), Color.RED.getBlue())));
            return;
        }

        player.sendMessage(Component.text("Le système me dits que vous êtes en cours de transfert"));
        player.createConnectionRequest(serverInfo.get()).connect();

    }

}
