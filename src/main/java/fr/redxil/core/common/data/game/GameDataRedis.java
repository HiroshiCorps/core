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
import fr.redxil.core.common.data.utils.DataType;
import org.redisson.api.RedissonClient;

public enum GameDataRedis {

    GAME_GAME_REDIS(DataType.SERVER, "game/<gameID>/game", true),

    GAME_SETTINGS_REDIS(DataType.SERVER, "game/<gameID>/settings", true),
    GAME_GAMESTATE_REDIS(DataType.SERVER, "game/<gameID>/gamestate", true),
    GAME_SERVER_REDIS(DataType.SERVER, "game/<gameID>/server", true),
    GAME_MINP_REDIS(DataType.SERVER, "game/<gameID>/minp", true),
    GAME_MAXP_REDIS(DataType.SERVER, "game/<gameID>/maxp", true),
    GAME_SUBGAME_REDIS(DataType.SERVER, "game/<gameID>/subgame", true),
    GAME_MAP_REDIS(DataType.SERVER, "game/<gameID>/map", true),

    GAME_INCOPLAYER_REDIS(DataType.SERVER, "game/<gameID>/incopllist", true),
    GAME_INPLAYER_REDIS(DataType.SERVER, "game/<gameID>/inpllist", true),

    GAME_MAXPLSPEC_REDIS(DataType.SERVER, "game/<gameID>/specmax", true),
    GAME_SPEC_PLAYER_REDIS(DataType.SERVER, "game/<gameID>/specingame", true),
    GAME_SPEC_MODERATOR_REDIS(DataType.SERVER, "game/<gameID>/specoutgame", true),
    MAP_STATE_REDIS(DataType.SERVER, "game/<gameID>/statemap", true),

    HOST_AUTHOR_REDIS(DataType.SERVER, "host/<gameID>/author", true),
    HOST_ACCESS_REDIS(DataType.SERVER, "host/<gameID>/access", true),
    HOST_ALLOWPLAYER_REDIS(DataType.SERVER, "host/<gameID>/allpl", true),
    HOST_ALLOWSPECTATOR_REDIS(DataType.SERVER, "host/<gameID>/allspec", true),

    GAME_ID_REDIS(DataType.GLOBAL, "game/id", false),
    MAP_GAME_REDIS(DataType.GLOBAL, "game/map", false),
    MAP_HOST_REDIS(DataType.GLOBAL, "host/map", false);

    final DataType dataType;
    final String location;
    final boolean needID;

    GameDataRedis(DataType dataType, String location, boolean needID) {
        this.dataType = dataType;
        this.location = location;
        this.needID = needID;
    }

    public static void clearRedisData(DataType dataType, Long playerID) {

        API.getInstance().getRedisManager().ifPresent(redis -> {
            RedissonClient redissonClient = redis.getRedissonClient();
            for (GameDataRedis mdv : values())
                if ((dataType == null || mdv.isDataType(dataType)))
                    if (mdv.hasNeedInfo(playerID))
                        redissonClient.getBucket(mdv.getString(playerID)).delete();
        });

    }

    public String getString() {
        if (!hasNeedInfo(null)) return null;
        return location;
    }

    public String getString(Game apiPlayer) {
        return getString(apiPlayer.getGameID());
    }

    public String getString(Long playerID) {
        String location = this.location;

        if (needID) {
            if (playerID == null) return null;
            location = location.replace("<gameID>", playerID.toString());
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
