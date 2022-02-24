/*
 *
 * Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 *
 */

package fr.redxil.core.common.data.server;

import fr.redxil.api.common.API;
import fr.redxil.api.common.server.Server;
import fr.redxil.core.common.data.utils.DataType;
import org.redisson.api.RedissonClient;

public enum ServerDataRedis {

    SERVER_NAME_REDIS(DataType.SERVER, "servers/<serverID>/server_name", true),
    SERVER_MAXP_REDIS(DataType.SERVER, "servers/<serverID>/server_max_players", true),
    SERVER_TYPE_REDIS(DataType.SERVER, "servers/<serverID>/server_type", true),
    SERVER_IP_REDIS(DataType.SERVER, "servers/<serverID>/server_ip", true),
    SERVER_STATUS_REDIS(DataType.SERVER, "servers/<serverID>/server_status", true),
    SERVER_ACCESS_REDIS(DataType.SERVER, "servers/<serverID>/server_access", true),
    SERVER_NEEDRANK_REDIS(DataType.SERVER, "servers/<serverID>/server_needrank", true),
    SERVER_PORT_REDIS(DataType.SERVER, "servers/<serverID>/server_port", true),
    SERVER_PLAYER_REDIS(DataType.SERVER, "servers/<serverID>/server_player", true),

    MAP_SERVER_REDIS(DataType.GLOBAL, "servers/map", false);

    final DataType dataType;
    final String location;
    final boolean needID;

    ServerDataRedis(DataType dataType, String location, boolean needID) {
        this.dataType = dataType;
        this.location = location;
        this.needID = needID;
    }

    public static void clearRedisData(DataType dataType, Long playerID) {

        RedissonClient redissonClient = API.getInstance().getRedisManager().getRedissonClient();

        for (ServerDataRedis mdv : values())
            if ((dataType == null || mdv.isDataType(dataType)))
                if (mdv.hasNeedInfo(playerID))
                    redissonClient.getBucket(mdv.getString(playerID)).delete();

    }

    public String getString() {
        if (!hasNeedInfo(null)) return null;
        return location;
    }

    public String getString(Server apiPlayer) {
        return getString(apiPlayer.getServerID());
    }

    public String getString(Long playerID) {
        String location = this.location;

        if (needID) {
            if (playerID == null) return null;
            location = location.replace("<serverID>", playerID.toString());
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
