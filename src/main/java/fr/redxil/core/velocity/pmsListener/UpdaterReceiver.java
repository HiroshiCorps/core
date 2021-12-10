/* Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 */

package fr.redxil.core.velocity.pmsListener;

import fr.redline.pms.pm.PMReceiver;
import fr.redline.pms.pm.RedisPMManager;
import fr.redxil.api.common.API;

public class UpdaterReceiver implements PMReceiver {

    public UpdaterReceiver() {
        RedisPMManager.addRedissonPMListener(API.getInstance().getRedisManager().getRedissonClient(), "stop", String.class, this);
    }

    @Override
    public void redisPluginMessageReceived(String s, Object s1) {

        switch (s) {

            case "stop": {
                API.getInstance().getPluginEnabler().shutdownServer((String) s1);
                break;
            }

            default:
                break;

        }

    }
}
