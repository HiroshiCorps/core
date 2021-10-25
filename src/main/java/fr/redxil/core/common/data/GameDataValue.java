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
import fr.redxil.api.common.game.Games;
import fr.redxil.core.common.data.utils.DataBaseType;
import fr.redxil.core.common.data.utils.DataType;
import org.redisson.api.RedissonClient;

public enum GameDataValue {

    GAME_GAME_REDIS(DataBaseType.REDIS, DataType.SERVER, "game/<gameID>/game", false, true),
    GAME_TEAM_REDIS(DataBaseType.REDIS, DataType.SERVER, "game/<gameID>/team", false, true),

    GAME_SETTINGS_REDIS(DataBaseType.REDIS, DataType.SERVER, "game/<gameID>/settings", false, true),
    GAME_GAMESTATE_REDIS(DataBaseType.REDIS, DataType.SERVER, "game/<gameID>/gamestate", false, true),
    GAME_SERVER_REDIS(DataBaseType.REDIS, DataType.SERVER, "game/<gameID>/server", false, true),
    GAME_MINP_REDIS(DataBaseType.REDIS, DataType.SERVER, "game/<gameID>/minp", false, true),
    GAME_MAXP_REDIS(DataBaseType.REDIS, DataType.SERVER, "game/<gameID>/maxp", false, true),

    GAME_INCOPLAYER_REDIS(DataBaseType.REDIS, DataType.SERVER, "game/<gameID>/incopllist", false, true),
    GAME_PLAYER_REDIS(DataBaseType.REDIS, DataType.SERVER, "game/<gameID>/pllist", false, true),

    GAME_MAXPLSPEC_REDIS(DataBaseType.REDIS, DataType.SERVER, "game/<gameID>/specmax", false, true),
    GAME_SPEC_INGAME_REDIS(DataBaseType.REDIS, DataType.SERVER, "game/<gameID>/specingame", false, true),
    GAME_SPEC_OUTGAME_REDIS(DataBaseType.REDIS, DataType.SERVER, "game/<gameID>/specoutgame", false, true),

    HOST_AUTHOR_REDIS(DataBaseType.REDIS, DataType.SERVER, "game/<gameID>/author", false, true),
    HOST_ACCESS_REDIS(DataBaseType.REDIS, DataType.SERVER, "game/<gameID>/access", false, true),
    HOST_ALLOWPLAYER_REDIS(DataBaseType.REDIS, DataType.SERVER, "game/<gameID>/allpl", false, true),
    HOST_ALLOWSPECTATOR_REDIS(DataBaseType.REDIS, DataType.SERVER, "game/<gameID>/allspec", false, true),

    GAME_ID_REDIS(DataBaseType.REDIS, DataType.GLOBAL, "game/id", false, false),
    GAMEMAP_SERVER_REDIS(DataBaseType.REDIS, DataType.GLOBAL, "game/map", false, false),
    HOSTMAP_SERVER_REDIS(DataBaseType.REDIS, DataType.GLOBAL, "game/hostmap", false, false);

    final DataType dataType;
    final DataBaseType dataBaseType;
    final String location;
    final boolean needName, needId;

    GameDataValue(DataBaseType dataBaseType, DataType dataType, String location, boolean needName, boolean needId) {
        this.dataBaseType = dataBaseType;
        this.dataType = dataType;
        this.location = location;
        this.needName = needName;
        this.needId = needId;
    }

    public static void clearRedisData(DataType dataType, String serverName, Long hostID) {

        RedissonClient redissonClient = API.get().getRedisManager().getRedissonClient();

        for (GameDataValue mdv : values())
            if ((dataType == null || mdv.isDataType(dataType)) && mdv.isDataBase(DataBaseType.REDIS))
                if (mdv.isArgNeeded() && mdv.hasNeedInfo(serverName, hostID))
                    redissonClient.getBucket(mdv.getString(serverName, hostID)).delete();
                else if (!mdv.isArgNeeded() && serverName == null && hostID == null)
                    redissonClient.getBucket(mdv.getString(null)).delete();

    }

    public static void clearRedisData(DataType dataType, Games host) {

        if (host != null)
            clearRedisData(dataType, host.getServerName(), host.getGameID());
        else
            clearRedisData(dataType, null, null);

    }

    public String getString(Games hosts) {
        String location = this.location;
        if (needName) {
            String pseudo = hosts.getServerName();
            if (pseudo == null) return null;
            location = location.replace("<serverName>", pseudo);
        }

        if (needId) {
            long memberId = hosts.getGameID();
            location = location.replace("<hostID>", Long.valueOf(memberId).toString());
        }

        return location;
    }

    public String getString(String serverName, Long serverId) {
        String location = this.location;
        if (needName) {
            if (serverName == null) return null;
            location = location.replace("<serverName>", serverName);
        }

        if (needId) {
            if (serverId == null) return null;
            location = location.replace("<hostID>", serverId.toString());
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
