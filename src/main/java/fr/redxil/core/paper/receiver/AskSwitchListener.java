/*
 *
 * Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 *
 */

package fr.redxil.core.paper.receiver;

import com.velocitypowered.api.proxy.Player;
import fr.redline.pms.pm.PMReceiver;
import fr.redline.pms.pm.RedisPMManager;
import fr.redxil.api.common.API;
import fr.redxil.core.velocity.CoreVelocity;

import java.util.Optional;

public class AskSwitchListener implements PMReceiver {

    public AskSwitchListener() {
        API.getInstance().getRedisManager().ifPresent(redis ->
                RedisPMManager.addRedissonPMListener(redis.getRedissonClient(), "askSwitchServer", String.class, this));
    }

    @Override
    public void redisPluginMessageReceived(String title, Object message) {

        if (!(message instanceof String)) return;

        String[] dataList = ((String) message).split("<switchSplit>");
        Optional<Player> playerO = CoreVelocity.getInstance().getProxyServer().getPlayer(dataList[0]);

        if (playerO.isEmpty()) return;

        Player player = playerO.get();

        if (API.getInstance().getServerID() != Long.parseLong(dataList[1])) {
            return;
        }

        API.getInstance().getRedisManager().ifPresent(redis ->
                RedisPMManager.sendRedissonPluginMessage(redis.getRedissonClient(), "switchServer", player.getUsername() + "<switchSplit>" + Long.valueOf(API.getInstance().getServerID()).toString()));

    }

}
