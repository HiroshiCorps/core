/*
 *
 * Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 *
 */

package fr.redxil.core.common.data.moderator;

import fr.redxil.api.common.API;
import fr.redxil.api.common.player.moderators.APIPlayerModerator;
import fr.redxil.core.common.data.utils.DataBaseType;
import fr.redxil.core.common.data.utils.DataType;
import org.redisson.api.RedissonClient;

public enum ModeratorDataRedis {

    MAP_MODERATOR_NAME(DataBaseType.REDIS, DataType.GLOBAL, "moderator/name_map", false),
    MAP_MODERATOR_UUID(DataBaseType.REDIS, DataType.GLOBAL, "moderator/uuid_map", false),

    MODERATOR_NAME_REDIS(DataBaseType.REDIS, DataType.PLAYER, "moderator/<memberID>/moderator_name", true),
    MODERATOR_MOD_REDIS(DataBaseType.REDIS, DataType.PLAYER, "moderator/<memberID>/moderator_mod", true),
    MODERATOR_VANISH_REDIS(DataBaseType.REDIS, DataType.PLAYER, "moderator/<memberID>/moderator_vanish", true),
    MODERATOR_UUID_REDIS(DataBaseType.REDIS, DataType.PLAYER, "moderator/<memberID>/member_uuid", true),
    MODERATOR_CIBLE_REDIS(DataBaseType.REDIS, DataType.PLAYER, "moderator/<memberID>/moderator_cible", true);

    final DataType dataType;
    final DataBaseType dataBaseType;
    final String location;
    final boolean needID;

    ModeratorDataRedis(DataBaseType dataBaseType, DataType dataType, String location, boolean needID) {
        this.dataBaseType = dataBaseType;
        this.dataType = dataType;
        this.location = location;
        this.needID = needID;
    }

    public static void clearRedisData(DataType dataType, Long playerID) {

        RedissonClient redissonClient = API.getInstance().getRedisManager().getRedissonClient();

        for (ModeratorDataRedis mdv : values())
            if ((dataType == null || mdv.isDataType(dataType)) && mdv.isDataBase(DataBaseType.REDIS))
                if (mdv.hasNeedInfo(playerID))
                    redissonClient.getBucket(mdv.getString(playerID)).delete();

    }

    public String getString() {
        if (!hasNeedInfo(null)) return null;
        return location;
    }

    public String getString(APIPlayerModerator player) {
        return getString(player.getMemberID());
    }

    public String getString(Long memberID) {
        String location = this.location;

        if (needID) {
            if (memberID == null) return null;
            location = location.replace("<memberID>", memberID.toString());
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
