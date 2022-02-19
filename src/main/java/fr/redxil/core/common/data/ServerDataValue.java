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

    SERVER_ID_SQL(DataBaseType.SQL, DataType.SERVER, "server_id", false),
    SERVER_NAME_SQL(DataBaseType.SQL, DataType.SERVER, "server_name", false),
    SERVER_MAXP_SQL(DataBaseType.SQL, DataType.SERVER, "server_max_players", false),
    SERVER_TYPE_SQL(DataBaseType.SQL, DataType.SERVER, "server_type", false),
    SERVER_STATUS_SQL(DataBaseType.SQL, DataType.SERVER, "server_status", false),
    SERVER_ACCESS_SQL(DataBaseType.SQL, DataType.SERVER, "server_access", false),
    SERVER_NEEDRANK_SQL(DataBaseType.SQL, DataType.SERVER, "server_needrank", false),
    SERVER_IP_SQL(DataBaseType.SQL, DataType.SERVER, "server_ip", false),
    SERVER_PORT_SQL(DataBaseType.SQL, DataType.SERVER, "server_port", false),

    SERVER_NAME_REDIS(DataBaseType.REDIS, DataType.SERVER, "servers/<serverID>/server_name", true),
    SERVER_MAXP_REDIS(DataBaseType.REDIS, DataType.SERVER, "servers/<serverID>/server_max_players", true),
    SERVER_TYPE_REDIS(DataBaseType.REDIS, DataType.SERVER, "servers/<serverID>/server_type", true),
    SERVER_IP_REDIS(DataBaseType.REDIS, DataType.SERVER, "servers/<serverID>/server_ip", true),
    SERVER_STATUS_REDIS(DataBaseType.REDIS, DataType.SERVER, "servers/<serverID>/server_status", true),
    SERVER_ACCESS_REDIS(DataBaseType.REDIS, DataType.SERVER, "servers/<serverID>/server_access", true),
    SERVER_NEEDRANK_REDIS(DataBaseType.REDIS, DataType.SERVER, "servers/<serverID>/server_needrank", true),
    SERVER_PORT_REDIS(DataBaseType.REDIS, DataType.SERVER, "servers/<serverID>/server_port", true),
    SERVER_PLAYER_REDIS(DataBaseType.REDIS, DataType.SERVER, "servers/<serverID>/server_player", true),

    MAP_SERVER_REDIS(DataBaseType.REDIS, DataType.GLOBAL, "servers/map", false);

    final DataType dataType;
    final DataBaseType dataBaseType;
    final String location;
    final boolean needID;

    ServerDataValue(DataBaseType dataBaseType, DataType dataType, String location, boolean needID) {
        this.dataBaseType = dataBaseType;
        this.dataType = dataType;
        this.location = location;
        this.needID = needID;
    }

    public static void clearRedisData(DataType dataType, Long serverID) {

        RedissonClient redissonClient = API.getInstance().getRedisManager().getRedissonClient();

        for (ServerDataValue mdv : values())
            if ((dataType == null || mdv.isDataType(dataType)) && mdv.isDataBase(DataBaseType.REDIS))
                if (mdv.hasNeedInfo(serverID))
                    redissonClient.getBucket(mdv.getString(serverID)).delete();

    }

    public String getString() {
        if (!hasNeedInfo(null)) return null;
        return location;
    }

    public String getString(Server server) {
        return getString(server.getServerID());
    }

    public String getString(Long serverID) {
        String location = this.location;

        if (needID) {
            if (serverID == null) return null;
            location = location.replace("<serverID>", serverID.toString());
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
