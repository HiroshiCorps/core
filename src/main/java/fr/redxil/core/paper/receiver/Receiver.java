/* Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 */

package fr.redxil.core.paper.receiver;

import fr.redline.pms.pm.PMReceiver;
import fr.redline.pms.pm.RedisPMManager;
import fr.redxil.api.common.API;
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.core.paper.utils.Nick;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class Receiver implements PMReceiver {

    public Receiver() {

        RedisPMManager.addRedissonPMListener(API.getInstance().getRedisManager().getRedissonClient(), "nickChange", String.class, this);
        RedisPMManager.addRedissonPMListener(API.getInstance().getRedisManager().getRedissonClient(), "rankChange", String.class, this);

    }

    @Override
    public void redisPluginMessageReceived(String s, Object s1) {
        APIPlayer apiPlayer = API.getInstance().getPlayerManager().getPlayer(UUID.fromString((String) s1));

        Player player = Bukkit.getPlayer(apiPlayer.getUUID());
        if (player == null) return;

        Nick.applyNick(player, apiPlayer);
    }
}
