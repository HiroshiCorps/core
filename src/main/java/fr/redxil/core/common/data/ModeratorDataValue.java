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
import fr.redxil.api.common.player.moderators.APIPlayerModerator;
import fr.redxil.core.common.data.utils.DataBaseType;
import fr.redxil.core.common.data.utils.DataType;
import org.redisson.api.RedissonClient;

public enum ModeratorDataValue {

    LIST_MODERATOR(DataBaseType.REDIS, DataType.GLOBAL, "moderator/list", false),
    MODERATOR_MOD_SQL(DataBaseType.SQL, DataType.PLAYER, "moderator_mod", false),
    MODERATOR_VANISH_SQL(DataBaseType.SQL, DataType.PLAYER, "moderator_vanish", false),
    MODERATOR_CIBLE_SQL(DataBaseType.SQL, DataType.PLAYER, "moderator_cible", false),

    MODERATOR_NAME_REDIS(DataBaseType.REDIS, DataType.PLAYER, "moderator/<memberID>/moderator_name", true),
    MODERATOR_MOD_REDIS(DataBaseType.REDIS, DataType.PLAYER, "moderator/<memberID>/moderator_mod", true),
    MODERATOR_VANISH_REDIS(DataBaseType.REDIS, DataType.PLAYER, "moderator/<memberID>/moderator_vanish", true),
    MODERATOR_UUID_REDIS(DataBaseType.REDIS, DataType.PLAYER, "moderator/<memberID>/member_uuid", true),
    MODERATOR_CIBLE_REDIS(DataBaseType.REDIS, DataType.PLAYER, "moderator/<memberID>/moderator_cible", true);

    final DataType dataType;
    final DataBaseType dataBaseType;
    final String location;
    final boolean needId;

    ModeratorDataValue(DataBaseType dataBaseType, DataType dataType, String location, boolean needId) {
        this.dataBaseType = dataBaseType;
        this.dataType = dataType;
        this.location = location;
        this.needId = needId;
    }

    public static void clearRedisData(DataType dataType, Long playerID) {

        RedissonClient redissonClient = API.getInstance().getRedisManager().getRedissonClient();

        for (ModeratorDataValue mdv : values())
            if ((dataType == null || mdv.isDataType(dataType)) && mdv.isDataBase(DataBaseType.REDIS))
                if (mdv.hasNeedInfo(playerID))
                    redissonClient.getBucket(mdv.getString(playerID)).delete();

    }

    public String getString(APIPlayerModerator player) {
        return getString(player.getMemberId());
    }

    public String getString(Long memberID) {
        String location = this.location;

        if (needId) {
            if (memberID == null) return null;
            location = location.replace("<memberID>", memberID.toString());
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
