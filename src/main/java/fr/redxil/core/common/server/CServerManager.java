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
import fr.redxil.api.common.server.ServerCreator;
import fr.redxil.api.common.server.ServerManager;
import fr.xilitra.hiroshisav.enums.ServerType;
import fr.redxil.api.common.utils.DataReminder;
import fr.redxil.core.common.data.server.ServerDataRedis;

import java.util.*;

public class CServerManager implements ServerManager {

    DataReminder<Map<String, Long>> serverMap = DataReminder.generateMapReminder(ServerDataRedis.MAP_SERVER_REDIS.getString());
    Map<Long, CServer> server = new HashMap<>();

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
                getServer(serverID).ifPresent(this::add);
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
    public Optional<Server> getServer(String s) {
        Map<String, Long> serverMap = getNameToLongMap();
        Long result = serverMap.get(s);
        if (result == null) return Optional.empty();
        return Optional.ofNullable(API.getInstance().isOnlineMod() ? new CServer(result) : server.get(result));
    }

    @Override
    public Optional<Server> getServer(long l) {
        if (!isServerExist(l)) return Optional.empty();
        return Optional.ofNullable(API.getInstance().isOnlineMod() ? new CServer(l) : server.get(l));
    }


    @Override
    public Optional<Server> createServer(ServerCreator serverCreator) {
        if (isServerExist(serverCreator.getServerName()))
            return Optional.empty();

        CServer cServer = new CServer(serverCreator);

        if (!API.getInstance().isOnlineMod())
            server.put(cServer.getServerID(), cServer);
        return Optional.of(cServer);
    }

    @Override
    public Optional<Server> loadServer(Long serverID, String serverName) {

        if (serverID == null)
            return Optional.empty();

        if(!API.getInstance().isOnlineMod())
            return Optional.empty();

        CServer cServer = new CServer(serverID, serverName);

        if (!API.getInstance().isOnlineMod())
            server.put(cServer.getServerID(), cServer);
        return Optional.of(cServer);

    }

    @Override
    public Optional<Server> getConnectableServer(APIPlayer apiPlayer, ServerType serverType) {

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

        return Optional.ofNullable(server);

    }

    @Override
    public Map<String, Long> getNameToLongMap() {
        return serverMap.getData();
    }

    public Map<Long, CServer> getMap() {
        return server;
    }

}
