/*
 *
 * Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 *
 */

package fr.redxil.core.common.server;

import fr.redline.pms.utils.IpInfo;
import fr.redxil.api.common.API;
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.api.common.server.Server;
import fr.redxil.api.common.server.ServerManager;
import fr.redxil.api.common.server.type.ServerType;
import fr.redxil.core.common.data.server.ServerDataRedis;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class CServerManager implements ServerManager {

    @Override
    public List<String> getListServerName() {
        return new ArrayList<>() {{
            for (Object serverName : API.getInstance().getRedisManager().getRedissonClient().getMap(ServerDataRedis.MAP_SERVER_REDIS.getString()).keySet()) {
                add((String) serverName);
            }
        }};
    }

    @Override
    public List<Long> getListServerID() {
        return new ArrayList<>() {{
            for (Object serverName : API.getInstance().getRedisManager().getRedissonClient().getMap(ServerDataRedis.MAP_SERVER_REDIS.getString()).values()) {
                add((long) serverName);
            }
        }};
    }

    @Override
    public List<Server> getListServer() {
        return new ArrayList<>() {{
            for (long serverID : getListServerID()) {
                add(getServer(serverID));
            }
        }};
    }

    @Override
    public List<Server> getListServer(ServerType serverType) {

        if (serverType == null) return getListServer();

        return new ArrayList<>() {{
            for (Server server : getListServer()) {
                if (server.getServerType() == serverType) {
                    add(server);
                }
            }
        }};
    }

    @Override
    public boolean isServerExist(String s) {
        return API.getInstance().getRedisManager().getRedissonClient().getMap(ServerDataRedis.MAP_SERVER_REDIS.getString()).containsKey(s);
    }

    @Override
    public boolean isServerExist(long l) {
        return API.getInstance().getRedisManager().getRedissonClient().getMap(ServerDataRedis.MAP_SERVER_REDIS.getString()).containsValue(l);
    }

    @Override
    public Server getServer(String s) {
        Map<String, Long> serverMap = API.getInstance().getRedisManager().getRedissonClient().getMap(ServerDataRedis.MAP_SERVER_REDIS.getString());
        if (!serverMap.containsKey(s)) return null;
        return new CServer(serverMap.get(s));
    }

    @Override
    public Server getServer(long l) {
        if (!isServerExist(l)) return null;
        return new CServer(l);
    }

    @Override
    public Server createServer(ServerType serverType, String name, IpInfo ipInfo, int maxPlayer) {
        Map<String, Long> serverMap = API.getInstance().getRedisManager().getRedissonClient().getMap(ServerDataRedis.MAP_SERVER_REDIS.getString());
        if (serverMap.containsKey(name))
            return new CServer(serverMap.get(name));

        API.getInstance().getPluginEnabler().printLog(Level.INFO, "Server init with name: " + name);
        return CServer.initServer(serverType, name, ipInfo);
    }

    @Override
    public Server loadServer(ServerType serverType, Long serverID, IpInfo ipInfo) {
        if (serverID == null || ipInfo == null) return null;
        if (isServerExist(serverID))
            return new CServer(serverID);

        API.getInstance().getPluginEnabler().printLog(Level.INFO, "Server init with id: " + serverID);
        return CServer.initServer(serverType, serverID, ipInfo);
    }

    @Override
    public Server getConnectableServer(APIPlayer apiPlayer, ServerType serverType) {

        List<Server> availableServer = getListServer(serverType);
        Server server = null;

        for (Server testServer : availableServer) {
            if (testServer.getConnectedPlayer() < testServer.getMaxPlayers()) {
                if (testServer.getServerAccess().canAccess(testServer, apiPlayer)) {
                    if (server == null || server.getConnectedPlayer() > testServer.getConnectedPlayer()) {
                        server = testServer;
                    }
                }
            }
        }

        return server;

    }

}
