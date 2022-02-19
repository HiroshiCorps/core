/*
 *
 * Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 *
 */

package fr.redxil.core.common.data;

import fr.redxil.api.common.API;
import fr.redxil.api.common.player.APIOfflinePlayer;
import fr.redxil.core.common.data.utils.DataBaseType;
import fr.redxil.core.common.data.utils.DataType;
import org.redisson.api.RedissonClient;

public enum MoneyDataValue {

    PLAYER_COINS_SQL(DataBaseType.SQL, DataType.PLAYER, "coins", false),
    PLAYER_SOLDE_SQL(DataBaseType.SQL, DataType.PLAYER, "solde", false),

    PLAYER_COINS_REDIS(DataBaseType.REDIS, DataType.PLAYER, "player/<memberID>/coins", true),
    PLAYER_SOLDE_REDIS(DataBaseType.REDIS, DataType.PLAYER, "player/<memberID>/solde", true);

    final DataType dataType;
    final DataBaseType dataBaseType;
    final String location;
    final boolean needID;

    MoneyDataValue(DataBaseType dataBaseType, DataType dataType, String location, boolean needID) {
        this.dataBaseType = dataBaseType;
        this.dataType = dataType;
        this.location = location;
        this.needID = needID;
    }

    public static void clearRedisData(DataType dataType, Long playerID) {

        RedissonClient redissonClient = API.getInstance().getRedisManager().getRedissonClient();

        for (MoneyDataValue mdv : values())
            if ((dataType == null || mdv.isDataType(dataType)) && mdv.isDataBase(DataBaseType.REDIS))
                if (mdv.hasNeedInfo(playerID))
                    redissonClient.getBucket(mdv.getString(playerID)).delete();

    }

    public String getString() {
        if (!hasNeedInfo(null)) return null;
        return location;
    }

    public String getString(APIOfflinePlayer player) {
        return getString(player.getMemberID());
    }

    public String getString(Long serverID) {
        String location = this.location;

        if (needID) {
            if (serverID == null) return null;
            location = location.replace("<memberID>", serverID.toString());
        }

        return location;
    }

    public boolean hasNeedInfo(Long memberID) {
        return !isNeedID() || memberID != null;
    }

    public boolean isNeedID() {
        return needID;
    }

    public boolean isDataBase(DataBaseType dataBaseType) {
        return this.dataBaseType.sqlBase.equals(dataBaseType.sqlBase);
    }

    public boolean isDataType(DataType dataType) {
        return this.dataType.equals(dataType);
    }

}
