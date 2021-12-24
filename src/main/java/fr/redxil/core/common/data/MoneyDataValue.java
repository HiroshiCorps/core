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

public enum MoneyDataValue {

    PLAYER_COINS_SQL(DataBaseType.SQL, DataType.PLAYER, "coins", false, false),
    PLAYER_SOLDE_SQL(DataBaseType.SQL, DataType.PLAYER, "solde", false, false),

    PLAYER_COINS_REDIS(DataBaseType.REDIS, DataType.PLAYER, "player/<memberID>/coins", false, true),
    PLAYER_SOLDE_REDIS(DataBaseType.REDIS, DataType.PLAYER, "player/<memberID>/solde", false, true);

    final DataType dataType;
    final DataBaseType dataBaseType;
    final String location;
    final boolean needName, needId;

    MoneyDataValue(DataBaseType dataBaseType, DataType dataType, String location, boolean needName, boolean needId) {
        this.dataBaseType = dataBaseType;
        this.dataType = dataType;
        this.location = location;
        this.needName = needName;
        this.needId = needId;
    }

    public static void clearRedisData(DataType dataType, String playerName, Long playerID) {

        RedissonClient redissonClient = API.getInstance().getRedisManager().getRedissonClient();

        for (MoneyDataValue mdv : values())
            if ((dataType == null || mdv.isDataType(dataType)) && mdv.isDataBase(DataBaseType.REDIS))
                if (mdv.isArgNeeded() && mdv.hasNeedInfo(playerName, playerID))
                    redissonClient.getBucket(mdv.getString(playerName, playerID)).delete();
                else if (!mdv.isArgNeeded() && playerName == null && playerID == null)
                    redissonClient.getBucket(mdv.getString(null)).delete();

    }

    public static void clearRedisData(DataType dataType, APIOfflinePlayer apiPlayer) {

        if (apiPlayer != null) {
            if (apiPlayer instanceof APIPlayer)
                clearRedisData(dataType, ((APIPlayer) apiPlayer).getRealName(), apiPlayer.getMemberId());
            else clearRedisData(dataType, apiPlayer.getName(), apiPlayer.getMemberId());
        } else
            clearRedisData(dataType, null, null);

    }

    public String getString() {
        if (needId || needName) return null;
        return location;
    }

    public String getString(APIOfflinePlayer apiPlayer) {

        String location = this.location;
        if (needName) {
            String pseudo = apiPlayer instanceof APIPlayer ? ((APIPlayer) apiPlayer).getRealName() : apiPlayer.getName();
            if (pseudo == null) return null;
            location = location.replace("<pseudo>", pseudo);
        }

        if (needId) {
            long memberId = apiPlayer.getMemberId();
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
