/* Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 */

package fr.redxil.core.common.server;

import fr.redline.pms.utils.IpInfo;
import fr.redxil.api.common.API;
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.api.common.server.Server;
import fr.redxil.api.common.server.ServerManager;
import fr.redxil.api.common.server.type.ServerType;
import fr.redxil.core.common.data.ServerDataValue;

import java.util.ArrayList;
import java.util.List;

public class CServerManager implements ServerManager {

    @Override
    public List<String> getListServerName() {
        return new ArrayList<String>() {{
            for (Object serverName : API.getInstance().getRedisManager().getRedissonClient().getMap(ServerDataValue.MAP_SERVER_REDIS.getString(null)).keySet())
                add((String) serverName);
        }};
    }

    @Override
    public List<Long> getListServerID() {
        return new ArrayList<Long>() {{
            for (Object serverName : API.getInstance().getRedisManager().getRedissonClient().getMap(ServerDataValue.MAP_SERVER_REDIS.getString(null)).values())
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
        return API.getInstance().getRedisManager().getRedissonClient().getMap(ServerDataValue.MAP_SERVER_REDIS.getString(null)).containsKey(s);
    }

    @Override
    public boolean isServerExist(long l) {
        return API.getInstance().getRedisManager().getRedissonClient().getMap(ServerDataValue.MAP_SERVER_REDIS.getString(null)).containsValue(l);
    }

    @Override
    public Server getServer(String s) {
        if (!isServerExist(s)) return null;
        return getServer((long) API.getInstance().getRedisManager().getRedissonClient().getMap(ServerDataValue.MAP_SERVER_REDIS.getString(null)).get(s));
    }

    @Override
    public Server getServer(long l) {
        if (!isServerExist(l)) return null;
        return new CServer(l);
    }

    @Override
    public Server initServer(ServerType serverType, String name, IpInfo ipInfo) {
        if (name == null || ipInfo == null) return null;
        if (!isServerExist(name))
            return CServer.initServer(serverType, name, ipInfo);
        return getServer(name);
    }

    @Override
    public Server getConnectableServer(APIPlayer apiPlayer, ServerType serverType) {

        List<Server> availableServer = getListServer(serverType);
        final Server[] server = {null};

        availableServer.forEach((testServer) -> {

            if (testServer.getConnectedPlayer() < testServer.getMaxPlayers() && testServer.getServerAccess().canAccess(testServer, apiPlayer))
                if (server[0] == null || server[0].getConnectedPlayer() > testServer.getConnectedPlayer())
                    server[0] = testServer;

        });

        return server[0];

    }

}
