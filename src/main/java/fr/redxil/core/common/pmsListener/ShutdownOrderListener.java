/* Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 */

package fr.redxil.core.common.pmsListener;

import fr.redline.pms.pm.PMReceiver;
import fr.redline.pms.pm.RedisPMManager;
import fr.redxil.api.common.API;

public class ShutdownOrderListener implements PMReceiver {

    public ShutdownOrderListener() {
        RedisPMManager.addRedissonPMListener(API.getInstance().getRedisManager().getRedissonClient(), "shutdownOrder", String.class, this);
    }

    @Override
    public void redisPluginMessageReceived(String title, Object message) {

        API.getInstance().getPluginEnabler().shutdownServer("Shutdown Order from server: " + message.toString());

    }

}
