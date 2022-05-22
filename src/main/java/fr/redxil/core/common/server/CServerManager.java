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
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;

public class CServerManager implements ServerManager {

    Map<String, Long> serverMap = API.getInstance().getRedisManager().getRedissonClient().getMap(ServerDataRedis.MAP_SERVER_REDIS.getString());

    @Override
    public Collection<String> getListServerName() {
        return getNameToLongMap().keySet();
    }

    @Override
    public Collection<Long> getListServerID() {
        return getNameToLongMap().values();
    }

    @Override
    public Collection<Server> getListServer() {
        return new ArrayList<>() {{
            for (long serverID : getListServerID()) {
                add(getServer(serverID));
            }
        }};
    }

    @Override
    public Collection<Server> getListServer(ServerType serverType) {

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
        return getNameToLongMap().containsKey(s);
    }

    @Override
    public boolean isServerExist(long l) {
        return getNameToLongMap().containsValue(l);
    }

    @Override
    public Server getServer(String s) {
        Map<String, Long> serverMap = getNameToLongMap();
        Long result = serverMap.get(s);
        if (result == null) return null;
        return new CServer(result);
    }

    @Override
    public Server getServer(long l) {
        if (!isServerExist(l)) return null;
        return new CServer(l);
    }


    @Override
    public Server createServer(ServerType serverType, String name, IpInfo ipInfo, int maxPlayer) {
        ///Temporaire
        return initServer(serverType, name, ipInfo, maxPlayer);
    }

    @Override
    public Server initServer(ServerType serverType, String name, IpInfo ipInfo, int maxPlayer) {
        Map<String, Long> serverMap = getNameToLongMap();
        Long id = serverMap.get(name);
        if (id != null)
            return new CServer(id);

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

        Collection<Server> availableServer = getListServer(serverType);
        Server server = null;

        for (Server testServer : availableServer) {
            Optional<Integer> maxPlayer = testServer.getMaxPlayers();
            Optional<String> serverName = testServer.getServerName();

            if (maxPlayer.isEmpty() || serverName.isEmpty())
                continue;

            if (testServer.getConnectedPlayer() < maxPlayer.get()) {
                if (testServer.getServerAccess().canAccess(testServer, apiPlayer)) {
                    if (server == null || server.getConnectedPlayer() > testServer.getConnectedPlayer()) {
                        server = testServer;
                    }
                }
            }
        }

        return server;

    }

    @Override
    public Map<String, Long> getNameToLongMap() {
        return serverMap;
    }

}
