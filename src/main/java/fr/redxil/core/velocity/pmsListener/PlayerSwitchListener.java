/*
 *
 * Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 *
 */

package fr.redxil.core.velocity.pmsListener;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import fr.redline.pms.pm.PMReceiver;
import fr.redline.pms.pm.RedisPMManager;
import fr.redxil.api.common.API;
import fr.redxil.api.common.message.Color;
import fr.redxil.api.common.message.TextComponentBuilder;
import fr.redxil.api.common.server.Server;
import fr.redxil.api.velocity.Velocity;
import net.kyori.adventure.text.Component;

import java.util.Optional;

public class PlayerSwitchListener implements PMReceiver {

    public PlayerSwitchListener() {
        RedisPMManager.addRedissonPMListener(API.getInstance().getRedisManager().getRedissonClient(), "switchServer", String.class, this);
    }

    @Override
    public void redisPluginMessageReceived(String title, Object message) {

        if (!(message instanceof String)) return;

        String[] dataList = ((String) message).split("<switchSplit>");
        Optional<Player> playerO = Velocity.getInstance().getProxyServer().getPlayer(dataList[0]);

        if (playerO.isEmpty()) return;

        Player player = playerO.get();

        Server server = API.getInstance().getServerManager().getServer(Long.parseLong(dataList[1]));
        if (server == null) {
            player.sendMessage((Component) TextComponentBuilder.createTextComponent(Color.RED + "Cannot connect you to server: " + dataList[1]).getFinalTextComponent());
            return;
        }

        Optional<RegisteredServer> serverInfo = Velocity.getInstance().getProxyServer().getServer(server.getServerName());
        if (serverInfo.isEmpty()) {
            player.sendMessage((Component) TextComponentBuilder.createTextComponent(Color.RED + "Cannot connect you to server: " + dataList[1]).getFinalTextComponent());
            return;
        }

        player.sendMessage((Component) TextComponentBuilder.createTextComponent("Le système me dits que vous êtes en cours de transfert").getFinalTextComponent());
        player.createConnectionRequest(serverInfo.get()).connect();

    }

}
