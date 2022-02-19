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

public enum PlayerDataValue {

    PLAYER_RANK_SQL(DataBaseType.SQL, DataType.PLAYER, "member_rank", false),
    PLAYER_RANK_TIME_SQL(DataBaseType.SQL, DataType.PLAYER, "rank_limit", false),


    PLAYER_MEMBERID_SQL(DataBaseType.SQL, DataType.PLAYER, "member_id", false),
    PLAYER_IP_SQL(DataBaseType.SQL, DataType.PLAYER, "member_ip", false),

    PLAYER_NAME_SQL(DataBaseType.SQL, DataType.PLAYER, "member_name", false),
    PLAYER_UUID_SQL(DataBaseType.SQL, DataType.PLAYER, "member_uuid", false),

    PLAYER_UUID_REDIS(DataBaseType.REDIS, DataType.PLAYER, "player/<memberID>/uuid", true),

    PLAYER_NAME_REDIS(DataBaseType.REDIS, DataType.PLAYER, "player/<memberID>/name", true),
    PLAYER_RANK_REDIS(DataBaseType.REDIS, DataType.PLAYER, "player/<memberID>/rank", true),
    PLAYER_RANK_TIME_REDIS(DataBaseType.REDIS, DataType.PLAYER, "player/<memberID>/ranklimit", true),

    PLAYER_REAL_NAME_REDIS(DataBaseType.REDIS, DataType.PLAYER, "player/<memberID>/real_name", true),
    PLAYER_REAL_RANK_REDIS(DataBaseType.REDIS, DataType.PLAYER, "player/<memberID>/real_rank", true),
    PLAYER_REAL_RANK_TIME_REDIS(DataBaseType.REDIS, DataType.PLAYER, "player/<memberID>/real_ranklimit", true),

    PLAYER_MAP_REDIS(DataBaseType.REDIS, DataType.PLAYER, "player/<memberID>/map", true),

    PLAYER_IPINFO_REDIS(DataBaseType.REDIS, DataType.PLAYER, "player/<memberID>/ipinfo", true),
    PLAYER_FREEZE_REDIS(DataBaseType.REDIS, DataType.PLAYER, "player/<memberID>/freeze", true),

    PLAYER_HUBLOGGED_REDIS(DataBaseType.REDIS, DataType.PLAYER, "player/<memberID>/hublogged", true),
    PLAYER_HUBPASS_REDIS(DataBaseType.REDIS, DataType.PLAYER, "player/<memberID>/hubpass", true),
    PLAYER_HUBLEVEL_REDIS(DataBaseType.REDIS, DataType.PLAYER, "player/<memberID>/hublevel", true),
    PLAYER_HUBREWARD_REDIS(DataBaseType.REDIS, DataType.PLAYER, "player/<memberID>/hubreward", true),

    PLAYER_INPUT_REDIS(DataBaseType.REDIS, DataType.PLAYER, "player/<memberID>/input", true),

    CONNECTED_BUNGEESERVER_REDIS(DataBaseType.REDIS, DataType.PLAYER, "player/<memberID>/bungee_server", true),
    CONNECTED_SPIGOTSERVER_REDIS(DataBaseType.REDIS, DataType.PLAYER, "player/<memberID>/spigot_server", true),

    PLAYER_LASTMSG_REDIS(DataBaseType.REDIS, DataType.PLAYER, "player/<memberID>/lastmsg", true),

    MAP_PLAYER_NAME(DataBaseType.REDIS, DataType.GLOBAL, "player/name", false),
    MAP_PLAYER_UUID(DataBaseType.REDIS, DataType.GLOBAL, "player/uuid", false),
    LIST_PLAYER_ID(DataBaseType.REDIS, DataType.GLOBAL, "player/list", false);

    final DataType dataType;
    final DataBaseType dataBaseType;
    final String location;
    final boolean needId;

    PlayerDataValue(DataBaseType dataBaseType, DataType dataType, String location, boolean needId) {
        this.dataBaseType = dataBaseType;
        this.dataType = dataType;
        this.location = location;
        this.needId = needId;
    }

    public static void clearRedisData(DataType dataType, Long playerID) {

        RedissonClient redissonClient = API.getInstance().getRedisManager().getRedissonClient();

        for (PlayerDataValue mdv : values())
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
