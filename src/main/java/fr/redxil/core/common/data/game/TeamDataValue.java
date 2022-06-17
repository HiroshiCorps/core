/*
 *
 * Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 *
 */

package fr.redxil.core.common.data.game;

import fr.redxil.api.common.API;
import fr.redxil.api.common.game.Game;
import fr.redxil.api.common.group.team.Team;
import fr.redxil.core.common.data.utils.DataType;
import org.redisson.api.RedissonClient;

public enum TeamDataValue {

    TEAM_GAME_REDIS(DataType.TEAM, "team/<serverID>/<teamName>/game", true, true),
    TEAM_DISPLAY_NAME_REDIS(DataType.TEAM, "team/<serverID>/<teamName>/dname", true, true),

    TEAM_PREFIX_REDIS(DataType.TEAM, "team/<serverID>/<teamName>/prefix", true, true),
    TEAM_SUFFIX_REDIS(DataType.TEAM, "team/<serverID>/<teamName>/suffix", true, true),

    TEAM_HIDE_OTHER_REDIS(DataType.TEAM, "team/<serverID>/<teamName>/ho", true, true),
    TEAM_CS_AV_REDIS(DataType.TEAM, "team/<serverID>/<teamName>/csav", true, true),
    TEAM_FF_REDIS(DataType.TEAM, "team/<serverID>/<teamName>/ff", true, true),
    TEAM_COLISION_REDIS(DataType.TEAM, "team/<serverID>/<teamName>/col", true, true),

    TEAM_PLAYERS_REDIS(DataType.TEAM, "team/<serverID>/<teamName>/players", true, true),

    TEAM_MAXP_REDIS(DataType.TEAM, "team/<serverID>/<teamName>/maxp", true, true),

    TEAM_ATTACHED_REDIS(DataType.TEAM, "team/<serverID>/<teamName>/attached", true, true),

    TEAM_LINK_MAP_REDIS(DataType.GLOBAL, "team/<serverID>/link", true, false),
    TEAM_LIST_REDIS(DataType.GLOBAL, "team/<serverID>/list", true, false);

    final DataType dataType;
    final String location;
    final boolean needGame;
    final boolean needTeam;

    TeamDataValue(DataType dataType, String location, boolean needGame, boolean needTeam) {
        this.dataType = dataType;
        this.location = location;
        this.needGame = needGame;
        this.needTeam = needTeam;
    }

    public static void clearRedisData(DataType dataType, long serverID, String teamName) {

        API.getInstance().getRedisManager().ifPresent(redis -> {
            RedissonClient redissonClient = redis.getRedissonClient();
            for (TeamDataValue mdv : values())
                if ((dataType == null || mdv.isDataType(dataType)))
                    if (mdv.hasNeedInfo(serverID, teamName))
                        redissonClient.getBucket(mdv.getString(serverID, teamName)).delete();
        });

    }

    public static void clearRedisData(DataType dataType, Game game) {

        for (String team : API.getInstance().getTeamManager(game.getServerID()).getTeamList())
            clearRedisData(dataType, game.getServerID(), team);

    }

    public boolean hasNeedInfo(Long serverID, String teamName) {
        if (isNeedGame() && serverID == null)
            return false;
        return !isNeedTeam() || teamName != null;
    }

    public String getString(Long serverID, String teamName) {
        String location = this.location;
        if (needTeam) {
            if (teamName == null) return null;
            location = location.replace("<teamName>", teamName);
        }

        if (needGame) {
            if (serverID == null) return null;
            location = location.replace("<serverID>", serverID.toString());
        }

        return location;
    }

    public String getString(Team team) {
        String location = this.location;
        if (needTeam) {
            String teamName = team.getTeamName();
            if (teamName == null) return null;
            location = location.replace("<teamName>", teamName);
        }

        if (needGame) {
            long serverID = team.getServerID();
            location = location.replace("<serverID>", Long.valueOf(serverID).toString());
        }

        return location;
    }

    public boolean isNeedGame() {
        return needGame;
    }

    public boolean isNeedTeam() {
        return needTeam;
    }

    public boolean isDataType(DataType dataType) {
        return this.dataType.equals(dataType);
    }

}
