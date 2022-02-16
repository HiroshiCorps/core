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
import fr.redxil.api.common.game.Game;
import fr.redxil.core.common.data.utils.DataBaseType;
import fr.redxil.core.common.data.utils.DataType;
import org.redisson.api.RedissonClient;

public enum GameDataValue {

    GAME_GAME_REDIS(DataBaseType.REDIS, DataType.SERVER, "game/<gameID>/game", true),

    GAME_SETTINGS_REDIS(DataBaseType.REDIS, DataType.SERVER, "game/<gameID>/settings", true),
    GAME_GAMESTATE_REDIS(DataBaseType.REDIS, DataType.SERVER, "game/<gameID>/gamestate", true),
    GAME_SERVER_REDIS(DataBaseType.REDIS, DataType.SERVER, "game/<gameID>/server", true),
    GAME_MINP_REDIS(DataBaseType.REDIS, DataType.SERVER, "game/<gameID>/minp", true),
    GAME_MAXP_REDIS(DataBaseType.REDIS, DataType.SERVER, "game/<gameID>/maxp", true),
    GAME_SUBGAME_REDIS(DataBaseType.REDIS, DataType.SERVER, "game/<gameID>/subgame", true),
    GAME_MAP_REDIS(DataBaseType.REDIS, DataType.SERVER, "game/<gameID>/map", true),

    GAME_INCOPLAYER_REDIS(DataBaseType.REDIS, DataType.SERVER, "game/<gameID>/incopllist", true),
    GAME_PLAYER_REDIS(DataBaseType.REDIS, DataType.SERVER, "game/<gameID>/pllist", true),

    GAME_MAXPLSPEC_REDIS(DataBaseType.REDIS, DataType.SERVER, "game/<gameID>/specmax", true),
    GAME_SPEC_PLAYER_REDIS(DataBaseType.REDIS, DataType.SERVER, "game/<gameID>/specingame", true),
    GAME_SPEC_MODERATOR_REDIS(DataBaseType.REDIS, DataType.SERVER, "game/<gameID>/specoutgame", true),

    HOST_AUTHOR_REDIS(DataBaseType.REDIS, DataType.SERVER, "game/<gameID>/author", true),
    HOST_ACCESS_REDIS(DataBaseType.REDIS, DataType.SERVER, "game/<gameID>/access", true),
    HOST_ALLOWPLAYER_REDIS(DataBaseType.REDIS, DataType.SERVER, "game/<gameID>/allpl",  true),
    HOST_ALLOWSPECTATOR_REDIS(DataBaseType.REDIS, DataType.SERVER, "game/<gameID>/allspec", true),

    GAME_ID_REDIS(DataBaseType.REDIS, DataType.GLOBAL, "game/id", false),
    LIST_GAME_REDIS(DataBaseType.REDIS, DataType.GLOBAL, "game/list", false),
    LIST_HOST_REDIS(DataBaseType.REDIS, DataType.GLOBAL, "game/host/list", false),
    MAP_SERVER_REDIS(DataBaseType.REDIS, DataType.GLOBAL, "game/map", false);

    final DataType dataType;
    final DataBaseType dataBaseType;
    final String location;
    final boolean needId;

    GameDataValue(DataBaseType dataBaseType, DataType dataType, String location, boolean needId) {
        this.dataBaseType = dataBaseType;
        this.dataType = dataType;
        this.location = location;
        this.needId = needId;
    }

    public static void clearRedisData(DataType dataType, long gameID) {

        RedissonClient redissonClient = API.getInstance().getRedisManager().getRedissonClient();

        for (GameDataValue mdv : values())
            if ((dataType == null || mdv.isDataType(dataType)) && mdv.isDataBase(DataBaseType.REDIS))
                if (mdv.isArgNeeded() && mdv.hasNeedInfo(gameID))
                    redissonClient.getBucket(mdv.getString(gameID)).delete();

    }

    public static void clearRedisData(DataType dataType, Game host) {

        clearRedisData(dataType, host.getGameID());

    }

    public String getString(Game hosts) {
        String location = this.location;

        if (needId) {
            long memberId = hosts.getGameID();
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

    public String getString(){
        if(isArgNeeded())
            return null;
        return location;
    }

    public boolean hasNeedInfo(Long gameID) {
        if(isNeedId())
            return gameID != null;
        return false;
    }

    public boolean isArgNeeded() {
        return isNeedId();
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
