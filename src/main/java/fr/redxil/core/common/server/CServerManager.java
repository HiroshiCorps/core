/* Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 */

package fr.redxil.core.common.server;

import fr.redline.pms.utils.IpInfo;
import fr.redxil.api.common.server.Server;
import fr.redxil.api.common.server.ServerManager;
import fr.redxil.api.common.server.type.ServerType;
import fr.redxil.core.common.CoreAPI;
import fr.redxil.core.common.data.ServerDataValue;

import java.util.ArrayList;
import java.util.List;

public class CServerManager implements ServerManager {

    @Override
    public List<String> getListServerName() {
        return new ArrayList<String>() {{
            for (Object serverName : CoreAPI.get().getRedisManager().getRedissonClient().getMap(ServerDataValue.MAP_SERVER_REDIS.getString(null)).keySet())
                add((String) serverName);
        }};
    }

    @Override
    public List<Long> getListServerID() {
        return new ArrayList<Long>() {{
            for (Object serverName : CoreAPI.get().getRedisManager().getRedissonClient().getMap(ServerDataValue.MAP_SERVER_REDIS.getString(null)).values())
                add((long) serverName);
        }};
    }

    @Override
    public List<Server> getListServer() {
        return new ArrayList<Server>() {{
            for (long serverID : getListServerID())
                add(getServer(serverID));
        }};
    }

    @Override
    public List<Server> getListServer(ServerType ServerType) {

        if (ServerType == null) return getListServer();

        return new ArrayList<Server>() {{
            for (Server server : getListServer())
                if (server.getServerType().toString().equals(ServerType.toString()))
                    add(server);
        }};
    }

    @Override
    public boolean isServerExist(String s) {
        if (s == null) return false;
        return CoreAPI.get().getRedisManager().getRedissonClient().getMap(ServerDataValue.MAP_SERVER_REDIS.getString(null)).containsKey(s);
    }

    @Override
    public boolean isServerExist(long l) {
        return CoreAPI.get().getRedisManager().getRedissonClient().getMap(ServerDataValue.MAP_SERVER_REDIS.getString(null)).containsValue(l);
    }

    @Override
    public Server getServer(String s) {
        return getServer((long) CoreAPI.get().getRedisManager().getRedissonClient().getMap(ServerDataValue.MAP_SERVER_REDIS.getString(null)).get(s));
    }

    @Override
    public Server getServer(long l) {
        if (isServerExist(l))
            return new CServer(l);
        return null;
    }

    @Override
    public Server initServer(String name, IpInfo ipInfo) {
        if (name == null || ipInfo == null) return null;
        if (!isServerExist(name))
            return CServer.initServer(name, ipInfo);
        return getServer(name);
    }

}
