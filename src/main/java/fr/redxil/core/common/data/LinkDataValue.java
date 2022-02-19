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
import fr.redxil.core.common.data.utils.DataBaseType;
import fr.redxil.core.common.data.utils.DataType;
import org.redisson.api.RedissonClient;

public enum LinkDataValue {

    LINK_ID_SQL(DataBaseType.SQL, DataType.PLAYER, "link_id", false),
    FROM_ID_SQL(DataBaseType.SQL, DataType.PLAYER, "from_id", false),
    TO_ID_SQL(DataBaseType.SQL, DataType.PLAYER, "to_id", false),
    LINK_TYPE_SQL(DataBaseType.SQL, DataType.PLAYER, "link_state", false),

    PLAYER_BLACKLIST_REDIS(DataBaseType.REDIS, DataType.PLAYER, "friend/<memberID>/blackList", true),

    PLAYER_FRIENDLIST_REDIS(DataBaseType.REDIS, DataType.PLAYER, "friend/<memberID>/friendList", true),

    PLAYER_FRIENDRECEIVEDLIST_REDIS(DataBaseType.REDIS, DataType.PLAYER, "friend/<memberID>/receivedList", true),

    PLAYER_FRIENDSENDEDLIST_REDIS(DataBaseType.REDIS, DataType.PLAYER, "friend/<memberID>/sendList", true);

    final DataType dataType;
    final DataBaseType dataBaseType;
    final String location;
    final boolean needId;

    LinkDataValue(DataBaseType dataBaseType, DataType dataType, String location, boolean needId) {
        this.dataBaseType = dataBaseType;
        this.dataType = dataType;
        this.location = location;
        this.needId = needId;
    }

    public static void clearRedisData(DataType dataType, Long playerID) {

        RedissonClient redissonClient = API.getInstance().getRedisManager().getRedissonClient();

        for (LinkDataValue mdv : values())
            if ((dataType == null || mdv.isDataType(dataType)) && mdv.isDataBase(DataBaseType.REDIS))
                if (mdv.hasNeedInfo(playerID))
                    redissonClient.getBucket(mdv.getString(playerID)).delete();

    }

    public String getString() {
        if (!hasNeedInfo(null)) return null;
        return location;
    }

    public String getString(APIOfflinePlayer apiPlayer) {
        return getString(apiPlayer.getMemberId());
    }

    public String getString(Long playerID) {
        String location = this.location;

        if (needId) {
            if (playerID == null) return null;
            location = location.replace("<memberID>", playerID.toString());
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
