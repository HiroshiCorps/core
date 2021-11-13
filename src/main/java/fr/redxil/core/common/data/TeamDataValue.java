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
import fr.redxil.api.common.game.team.Team;
import fr.redxil.core.common.data.utils.DataBaseType;
import fr.redxil.core.common.data.utils.DataType;
import org.redisson.api.RedissonClient;

public enum TeamDataValue {

    TEAM_NAME_REDIS(DataBaseType.REDIS, DataType.TEAM, "team/<teamID>/name", true),
    TEAM_SERVER_REDIS(DataBaseType.REDIS, DataType.TEAM, "team/<teamID>/server", true),
    TEAM_DISPLAY_NAME_REDIS(DataBaseType.REDIS, DataType.TEAM, "team/<teamID>/dname", true),

    TEAM_COLOR_REDIS(DataBaseType.REDIS, DataType.TEAM, "team/<teamID>/color", true),
    TEAM_CHAT_COLOR_REDIS(DataBaseType.REDIS, DataType.TEAM, "team/<teamID>/chatcolor", true),

    TEAM_PREFIX_REDIS(DataBaseType.REDIS, DataType.TEAM, "team/<teamID>/prefix", true),
    TEAM_SUFFIX_REDIS(DataBaseType.REDIS, DataType.TEAM, "team/<teamID>/suffix", true),

    TEAM_HIDE_OTHER_REDIS(DataBaseType.REDIS, DataType.TEAM, "team/<teamID>/ho", true),
    TEAM_CS_AV_REDIS(DataBaseType.REDIS, DataType.TEAM, "team/<teamID>/csav", true),
    TEAM_FF_REDIS(DataBaseType.REDIS, DataType.TEAM, "team/<teamID>/ff", true),
    TEAM_COLISION_REDIS(DataBaseType.REDIS, DataType.TEAM, "team/<teamID>/col", true),

    TEAM_PLAYERS_REDIS(DataBaseType.REDIS, DataType.TEAM, "team/<teamID>/players", true),

    TEAM_MAXP_REDIS(DataBaseType.REDIS, DataType.TEAM, "team/<teamID>/maxp", true),

    TEAM_ATTACHED_REDIS(DataBaseType.REDIS, DataType.TEAM, "team/<teamID>/attached", true),

    TEAM_LINK_MAP_REDIS(DataBaseType.REDIS, DataType.GLOBAL, "team/link/map", false),
    TEAM_LIST_REDIS(DataBaseType.REDIS, DataType.GLOBAL, "team/map", false);

    final DataType dataType;
    final DataBaseType dataBaseType;
    final String location;
    final boolean needId;

    TeamDataValue(DataBaseType dataBaseType, DataType dataType, String location, boolean needId) {
        this.dataBaseType = dataBaseType;
        this.dataType = dataType;
        this.location = location;
        this.needId = needId;
    }

    public static void clearRedisData(DataType dataType, Long hostID) {

        RedissonClient redissonClient = API.get().getRedisManager().getRedissonClient();

        for (TeamDataValue mdv : values())
            if ((dataType == null || mdv.isDataType(dataType)) && mdv.isDataBase(DataBaseType.REDIS))
                if (mdv.isNeedId() && hostID != null)
                    redissonClient.getBucket(mdv.getString(hostID)).delete();
                else if (!mdv.isNeedId() && hostID == null) redissonClient.getBucket(mdv.getString(hostID)).delete();

    }

    public static void clearRedisData(DataType dataType, Team host) {

        clearRedisData(dataType, host.getTeamID());

    }

    public String getString(Team hosts) {
        String location = this.location;

        if (needId) {
            long memberId = hosts.getTeamID();
            location = location.replace("<hostID>", Long.valueOf(memberId).toString());
        }

        return location;
    }

    public String getString(Long serverId) {
        String location = this.location;
        if (needId) {
            if (serverId == null) return null;
            location = location.replace("<hostID>", serverId.toString());
        }

        return location;
    }

    public String getLocation() {
        return location;
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
