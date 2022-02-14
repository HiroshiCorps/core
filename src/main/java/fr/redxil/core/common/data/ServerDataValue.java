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
import fr.redxil.api.common.server.Server;
import fr.redxil.core.common.data.utils.DataBaseType;
import fr.redxil.core.common.data.utils.DataType;
import org.redisson.api.RedissonClient;

public enum ServerDataValue {

    SERVER_ID_SQL(DataBaseType.SQL, DataType.SERVER, "server_id", false, false),
    SERVER_NAME_SQL(DataBaseType.SQL, DataType.SERVER, "server_name", false, false),
    SERVER_MAXP_SQL(DataBaseType.SQL, DataType.SERVER, "server_max_players", false, false),
    SERVER_TYPE_SQL(DataBaseType.SQL, DataType.SERVER, "server_type", false, false),
    SERVER_STATUS_SQL(DataBaseType.SQL, DataType.SERVER, "server_status", false, false),
    SERVER_ACCESS_SQL(DataBaseType.SQL, DataType.SERVER, "server_access", false, false),
    SERVER_NEEDRANK_SQL(DataBaseType.SQL, DataType.SERVER, "server_needrank", false, false),
    SERVER_IP_SQL(DataBaseType.SQL, DataType.SERVER, "server_ip", false, false),
    SERVER_PORT_SQL(DataBaseType.SQL, DataType.SERVER, "server_port", false, false),

    SERVER_NAME_REDIS(DataBaseType.REDIS, DataType.SERVER, "servers/<serverID>/server_name", false, true),
    SERVER_MAXP_REDIS(DataBaseType.REDIS, DataType.SERVER, "servers/<serverID>/server_max_players", false, true),
    SERVER_TYPE_REDIS(DataBaseType.REDIS, DataType.SERVER, "servers/<serverID>/server_type", false, true),
    SERVER_IP_REDIS(DataBaseType.REDIS, DataType.SERVER, "servers/<serverID>/server_ip", false, true),
    SERVER_STATUS_REDIS(DataBaseType.REDIS, DataType.SERVER, "servers/<serverID>/server_status", false, true),
    SERVER_ACCESS_REDIS(DataBaseType.REDIS, DataType.SERVER, "servers/<serverID>/server_access", false, true),
    SERVER_NEEDRANK_REDIS(DataBaseType.REDIS, DataType.SERVER, "servers/<serverID>/server_needrank", false, true),
    SERVER_PORT_REDIS(DataBaseType.REDIS, DataType.SERVER, "servers/<serverID>/server_port", false, true),
    SERVER_PLAYER_REDIS(DataBaseType.REDIS, DataType.SERVER, "servers/<serverID>/server_player", false, true),

    MAP_SERVER_REDIS(DataBaseType.REDIS, DataType.GLOBAL, "servers/map", false, false);

    final DataType dataType;
    final DataBaseType dataBaseType;
    final String location;
    final boolean needName, needId;

    ServerDataValue(DataBaseType dataBaseType, DataType dataType, String location, boolean needName, boolean needId) {
        this.dataBaseType = dataBaseType;
        this.dataType = dataType;
        this.location = location;
        this.needName = needName;
        this.needId = needId;
    }

    public static void clearRedisData(DataType dataType, String serverName, Long serverID) {

        RedissonClient redissonClient = API.getInstance().getRedisManager().getRedissonClient();

        for (ServerDataValue mdv : values())
            if ((dataType == null || mdv.isDataType(dataType)) && mdv.isDataBase(DataBaseType.REDIS))
                if (mdv.isArgNeeded() && mdv.hasNeedInfo(serverName, serverID))
                    redissonClient.getBucket(mdv.getString(serverName, serverID)).delete();
                else if (!mdv.isArgNeeded() && serverName == null && serverID == null)
                    redissonClient.getBucket(mdv.getString(null)).delete();

    }

    public static void clearRedisData(DataType dataType, Server server) {

        if (server != null)
            clearRedisData(dataType, server.getServerName(), server.getServerId());
        else
            clearRedisData(dataType, null, null);

    }

    public String getString(Server server) {
        String location = this.location;
        if (needName) {
            String pseudo = server.getServerName();
            if (pseudo == null) return null;
            location = location.replace("<serverName>", pseudo);
        }

        if (needId) {
            long memberId = server.getServerId();
            location = location.replace("<serverID>", Long.valueOf(memberId).toString());
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
            location = location.replace("<serverID>", serverId.toString());
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
