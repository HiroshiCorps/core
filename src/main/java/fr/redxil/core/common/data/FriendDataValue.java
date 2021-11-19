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

public enum FriendDataValue {

    PLAYER_BLACKLIST_REDIS(DataBaseType.REDIS, DataType.PLAYER, "friend/<memberID>/blackList", false, true),
    PLAYER_BLACKLIST_SQL(DataBaseType.SQL, DataType.PLAYER, "blackList", false, false),

    PLAYER_FRIENDLIST_REDIS(DataBaseType.REDIS, DataType.PLAYER, "friend/<memberID>/friendList", false, true),
    PLAYER_FRIENDLIST_SQL(DataBaseType.SQL, DataType.PLAYER, "friendList", false, false),

    PLAYER_FRIENDRECEIVEDLIST_REDIS(DataBaseType.REDIS, DataType.PLAYER, "friend/<memberID>/receivedList", false, true),
    PLAYER_FRIENDRECEIVEDLIST_SQL(DataBaseType.SQL, DataType.PLAYER, "receivedList", false, false),

    PLAYER_FRIENDSENDEDLIST_REDIS(DataBaseType.REDIS, DataType.PLAYER, "friend/<memberID>/sendList", false, true),
    PLAYER_FRIENDSENDEDLIST_SQL(DataBaseType.SQL, DataType.PLAYER, "sendList", false, false);

    final DataType dataType;
    final DataBaseType dataBaseType;
    final String location;
    final boolean needName, needId;

    FriendDataValue(DataBaseType dataBaseType, DataType dataType, String location, boolean needName, boolean needId) {
        this.dataBaseType = dataBaseType;
        this.dataType = dataType;
        this.location = location;
        this.needName = needName;
        this.needId = needId;
    }

    public static void clearRedisData(DataType dataType, String playerName, Long playerID) {

        RedissonClient redissonClient = API.getInstance().getRedisManager().getRedissonClient();

        for (FriendDataValue mdv : values())
            if ((dataType == null || mdv.isDataType(dataType)) && mdv.isDataBase(DataBaseType.REDIS))
                if (mdv.isArgNeeded() && mdv.hasNeedInfo(playerName, playerID))
                    redissonClient.getBucket(mdv.getString(playerName, playerID)).delete();
                else if (!mdv.isArgNeeded() && playerName == null && playerID == null)
                    redissonClient.getBucket(mdv.getString(null)).delete();

    }

    public static void clearRedisData(DataType dataType, APIOfflinePlayer APIPlayer) {

        if (APIPlayer != null)
            clearRedisData(dataType, APIPlayer.getName(), APIPlayer.getMemberId());
        else
            clearRedisData(dataType, null, null);

    }

    public String getString() {
        if (needId || needName) return null;
        return location;
    }

    public String getString(APIOfflinePlayer APIPlayer) {

        String location = this.location;
        if (needName) {
            String pseudo = APIPlayer.getName();
            if (pseudo == null) return null;
            location = location.replace("<pseudo>", pseudo);
        }

        if (needId) {
            long memberId = APIPlayer.getMemberId();
            location = location.replace("<memberID>", Long.valueOf(memberId).toString());
        }

        return location;
    }

    public String getString(String playerName, Long playerID) {
        String location = this.location;
        if (needName) {
            if (playerName == null) return null;
            location = location.replace("<pseudo>", playerName);
        }

        if (needId) {
            if (playerID == null) return null;
            location = location.replace("<memberID>", playerID.toString());
        }

        return location;
    }

    public boolean hasNeedInfo(String playerName, Long memberID) {
        if (isNeedId() && memberID == null)
            return false;
        return !isNeedName() || playerName != null;
    }

    public boolean isArgNeeded() {
        return isNeedId() || isNeedName();
    }

    public boolean isNeedName() {
        return needName;
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
