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
import fr.redxil.api.common.player.moderators.APIPlayerModerator;
import fr.redxil.core.common.data.utils.DataBaseType;
import fr.redxil.core.common.data.utils.DataType;
import org.redisson.api.RedissonClient;

public enum ModeratorDataValue {

    MODERATOR_MEMBERID_SQL(DataBaseType.SQL, DataType.PLAYER, "member_id", false, false),
    MODERATOR_MOD_SQL(DataBaseType.SQL, DataType.PLAYER, "moderator_mod", false, false),
    MODERATOR_VANISH_SQL(DataBaseType.SQL, DataType.PLAYER, "moderator_vanish", false, false),
    MODERATOR_CIBLE_SQL(DataBaseType.SQL, DataType.PLAYER, "moderator_cible", false, false),

    LIST_MODERATOR(DataBaseType.REDIS, DataType.GLOBAL, "moderator/list", false, false),
    MODERATOR_NAME_REDIS(DataBaseType.REDIS, DataType.PLAYER, "moderator/<memberID>/moderator_name", false, true),
    MODERATOR_MOD_REDIS(DataBaseType.REDIS, DataType.PLAYER, "moderator/<memberID>/moderator_mod", false, true),
    MODERATOR_VANISH_REDIS(DataBaseType.REDIS, DataType.PLAYER, "moderator/<memberID>/moderator_vanish", false, true),
    MODERATOR_UUID_REDIS(DataBaseType.REDIS, DataType.PLAYER, "moderator/<memberID>/member_uuid", false, true),
    MODERATOR_CIBLE_REDIS(DataBaseType.REDIS, DataType.PLAYER, "moderator/<memberID>/moderator_cible", false, true);

    final DataType dataType;
    final DataBaseType dataBaseType;
    final String location;
    final boolean needName, needId;

    ModeratorDataValue(DataBaseType dataBaseType, DataType dataType, String location, boolean needName, boolean needId) {
        this.dataBaseType = dataBaseType;
        this.dataType = dataType;
        this.location = location;
        this.needName = needName;
        this.needId = needId;
    }

    public static void clearRedisData(DataType dataType, String moderatorName, Long moderatorID) {

        RedissonClient redissonClient = API.get().getRedisManager().getRedissonClient();

        for (ModeratorDataValue mdv : values())
            if ((dataType == null || mdv.isDataType(dataType)) && mdv.isDataBase(DataBaseType.REDIS))
                if (mdv.isArgNeeded() && mdv.hasNeedInfo(moderatorName, moderatorID))
                    redissonClient.getBucket(mdv.getString(moderatorName, moderatorID)).delete();
                else if (!mdv.isArgNeeded() && moderatorName == null && moderatorID == null)
                    redissonClient.getBucket(mdv.getString(null)).delete();

    }

    public static void clearRedisData(DataType dataType, APIPlayerModerator APIPlayerModerator) {

        if (APIPlayerModerator != null)
            clearRedisData(dataType, APIPlayerModerator.getName(), APIPlayerModerator.getMemberId());
        else
            clearRedisData(dataType, null, null);

    }

    public String getString(APIPlayerModerator APIPlayer) {
        String location = this.location;
        if (needName) {
            String pseudo = APIPlayer.getName();
            if (pseudo == null) return null;
            location = location.replace("<modName>", pseudo);
        }

        if (needId) {
            long memberId = APIPlayer.getMemberId();
            location = location.replace("<memberID>", Long.valueOf(memberId).toString());
        }

        return location;
    }

    public String getString(String modName, Long modID) {
        String location = this.location;
        if (needName) {
            if (modName == null) return null;
            location = location.replace("<modName>", modName);
        }

        if (needId) {
            if (modID == null) return null;
            location = location.replace("<memberID>", modID.toString());
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
