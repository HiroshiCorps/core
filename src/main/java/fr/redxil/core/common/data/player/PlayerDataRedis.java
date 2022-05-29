/*
 *
 * Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 *
 */

package fr.redxil.core.common.data.player;

import fr.redxil.api.common.API;
import fr.redxil.api.common.player.APIOfflinePlayer;
import fr.redxil.core.common.data.utils.DataType;
import org.redisson.api.RedissonClient;

public enum PlayerDataRedis {

    PLAYER_UUID_REDIS(DataType.PLAYER, "player/<memberID>/uuid", true),

    PLAYER_NAME_REDIS(DataType.PLAYER, "player/<memberID>/name", true),
    PLAYER_RANK_REDIS(DataType.PLAYER, "player/<memberID>/rank", true),
    PLAYER_RANK_TIME_REDIS(DataType.PLAYER, "player/<memberID>/ranklimit", true),

    PLAYER_REAL_NAME_REDIS(DataType.PLAYER, "player/<memberID>/real_name", true),
    PLAYER_REAL_RANK_REDIS(DataType.PLAYER, "player/<memberID>/real_rank", true),
    PLAYER_REAL_RANK_TIME_REDIS(DataType.PLAYER, "player/<memberID>/real_ranklimit", true),

    PLAYER_MAP_REDIS(DataType.PLAYER, "player/<memberID>/map", true),

    PLAYER_IP_REDIS(DataType.PLAYER, "player/<memberID>/ip", true),
    PLAYER_FREEZE_REDIS(DataType.PLAYER, "player/<memberID>/freeze", true),

    PLAYER_HUBLOGGED_REDIS(DataType.PLAYER, "player/<memberID>/hublogged", true),
    PLAYER_HUBPASS_REDIS(DataType.PLAYER, "player/<memberID>/hubpass", true),
    PLAYER_HUBLEVEL_REDIS(DataType.PLAYER, "player/<memberID>/hublevel", true),
    PLAYER_HUBREWARD_REDIS(DataType.PLAYER, "player/<memberID>/hubreward", true),

    PLAYER_BUNGEE_REDIS(DataType.PLAYER, "player/<memberID>/bungee_server", true),
    PLAYER_SPIGOT_REDIS(DataType.PLAYER, "player/<memberID>/spigot_server", true),

    PLAYER_LASTMSG_REDIS(DataType.PLAYER, "player/<memberID>/lastmsg", true),

    MAP_PLAYER_NAME(DataType.GLOBAL, "player/name", false),
    MAP_PLAYER_UUID(DataType.GLOBAL, "player/uuid", false);

    final DataType dataType;
    final String location;
    final boolean needID;

    PlayerDataRedis(DataType dataType, String location, boolean needID) {
        this.dataType = dataType;
        this.location = location;
        this.needID = needID;
    }

    public static void clearRedisData(DataType dataType, Long playerID) {

        API.getInstance().getRedisManager().ifPresent(redis -> {
            RedissonClient redissonClient = redis.getRedissonClient();
            for (PlayerDataRedis mdv : values())
                if (dataType == null || mdv.isDataType(dataType))
                    if (mdv.hasNeedInfo(playerID))
                        redissonClient.getBucket(mdv.getString(playerID)).delete();
        });

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
