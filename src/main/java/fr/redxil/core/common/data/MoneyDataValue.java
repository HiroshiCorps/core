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
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.api.common.server.Server;
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
    final boolean needId;

    MoneyDataValue(DataBaseType dataBaseType, DataType dataType, String location, boolean needId) {
        this.dataBaseType = dataBaseType;
        this.dataType = dataType;
        this.location = location;
        this.needId = needId;
    }

    public static void clearRedisData(DataType dataType, Long playerID) {

        RedissonClient redissonClient = API.getInstance().getRedisManager().getRedissonClient();

        for (MoneyDataValue mdv : values())
            if ((dataType == null || mdv.isDataType(dataType)) && mdv.isDataBase(DataBaseType.REDIS))
                if (mdv.hasNeedInfo(playerID))
                    redissonClient.getBucket(mdv.getString(playerID)).delete();

    }

    public String getString(APIOfflinePlayer player) {
        return getString(player.getMemberId());
    }

    public String getString(Long serverId) {
        String location = this.location;

        if (needId) {
            if (serverId == null) return null;
            location = location.replace("<memberID>", serverId.toString());
        }

        return location;
    }

    public boolean hasNeedInfo(Long memberID) {
        return !isNeedId() || memberID != null;
    }

    public boolean isNeedId() {
        return needId;
    }

    public boolean isDataBase(DataBaseType dataBaseType) {
        return this.dataBaseType.sqlBase.equals(dataBaseType.sqlBase);
    }

    public boolean isDataType(DataType dataType) {
        return this.dataType.equals(dataType);
    }

}
