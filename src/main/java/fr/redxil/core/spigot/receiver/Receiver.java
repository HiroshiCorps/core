/* Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 */

package fr.redxil.core.spigot.receiver;

import fr.redline.pms.connect.linker.pm.PMManager;
import fr.redline.pms.connect.linker.pm.PMReceiver;
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.core.common.CoreAPI;
import fr.redxil.core.spigot.event.EventListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class Receiver implements PMReceiver {

    public Receiver() {

        PMManager.addRedissonPMListener(CoreAPI.get().getRedisManager().getRedissonClient(), "nickChange", String.class, this);
        PMManager.addRedissonPMListener(CoreAPI.get().getRedisManager().getRedissonClient(), "rankChange", String.class, this);

    }

    @Override
    public void pluginMessageReceived(String s, Object s1) {
        APIPlayer apiPlayer = CoreAPI.get().getPlayerManager().getPlayer(UUID.fromString((String) s1));

        Player player = Bukkit.getPlayer(apiPlayer.getUUID());
        if (player == null) return;

        EventListener.applyNick(player, apiPlayer, false);
    }
}
