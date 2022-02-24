/*
 *
 * Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 *
 */

package fr.redxil.core.common.data.money;

import fr.redxil.api.common.API;
import fr.redxil.api.common.player.APIOfflinePlayer;
import fr.redxil.core.common.data.utils.DataType;
import org.redisson.api.RedissonClient;

public enum MoneyDataRedis {

    PLAYER_COINS_REDIS(DataType.PLAYER, "player/<memberID>/coins", true),
    PLAYER_SOLDE_REDIS(DataType.PLAYER, "player/<memberID>/solde", true);

    final DataType dataType;
    final String location;
    final boolean needID;

    MoneyDataRedis(DataType dataType, String location, boolean needID) {
        this.dataType = dataType;
        this.location = location;
        this.needID = needID;
    }

    public static void clearRedisData(DataType dataType, Long playerID) {

        RedissonClient redissonClient = API.getInstance().getRedisManager().getRedissonClient();

        for (MoneyDataRedis mdv : values())
            if ((dataType == null || mdv.isDataType(dataType)))
                if (mdv.hasNeedInfo(playerID))
                    redissonClient.getBucket(mdv.getString(playerID)).delete();

    }

    public String getString() {
        if (!hasNeedInfo(null)) return null;
        return location;
    }

    public String getString(APIOfflinePlayer apiPlayer) {
        return getString(apiPlayer.getMemberID());
    }

    public String getString(Long playerID) {
        String location = this.location;

        if (needID) {
            if (playerID == null) return null;
            location = location.replace("<memberID>", playerID.toString());
        }

        return location;
    }

    public boolean hasNeedInfo(Long memberID) {
        return !isNeedID() || memberID != null;
    }

    public boolean isNeedID() {
        return needID;
    }

    public boolean isDataType(DataType dataType) {
        return this.dataType.equals(dataType);
    }

}
