/* Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 */

package fr.redxil.core.paper.receiver;

import fr.redline.pms.connect.linker.pm.PMManager;
import fr.redline.pms.connect.linker.pm.PMReceiver;
import fr.redxil.api.common.API;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class UpdaterReceiver implements PMReceiver {

    public UpdaterReceiver() {
        /// API.getInstance().getPMSReceiverManager().setSocketReceiver("update", this);
        PMManager.addRedissonPMListener(API.getInstance().getRedisManager().getRedissonClient(), "restart", String.class, this);
        PMManager.addRedissonPMListener(API.getInstance().getRedisManager().getRedissonClient(), "stop", String.class, this);
    }

    @Override
    public void pluginMessageReceived(String s, Object s1) {

        switch (s) {

            case "restart": {
                for (Player player : Bukkit.getOnlinePlayers())
                    player.kickPlayer((String) s1);
                Bukkit.getServer().reload();
                break;
            }

            case "stop": {
                API.getInstance().getPluginEnabler().shutdownServer("Updater shutdown");
                break;
            }

            default:
                break;

        }

    }

}
