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
import fr.redline.pms.pm.PMReceiver;
import fr.redline.pms.pm.RedisPMManager;
import fr.redxil.api.common.API;
import fr.redxil.core.velocity.CoreVelocity;
import net.kyori.adventure.text.Component;

import java.util.Optional;

public class MessageListener implements PMReceiver {

    public MessageListener() {
        RedisPMManager.addRedissonPMListener(API.getInstance().getRedisManager().getRedissonClient(), "playerMessage", String.class, this);
    }

    @Override
    public void redisPluginMessageReceived(String title, Object message) {

        if (!(message instanceof String)) return;

        String[] dataList = ((String) message).split("<msp>");
        Optional<Player> playerO = CoreVelocity.getInstance().getProxyServer().getPlayer(dataList[0]);

        if (playerO.isEmpty()) return;

        Player player = playerO.get();

        player.sendMessage(Component.text(dataList[1]));

    }

}
