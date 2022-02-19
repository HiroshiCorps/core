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
import java.util.Map;
import java.util.logging.Level;

public class CServerManager implements ServerManager {

    @Override
    public List<String> getListServerName() {
        return new ArrayList<>() {{
            for (Object serverName : API.getInstance().getRedisManager().getRedissonClient().getMap(ServerDataValue.MAP_SERVER_REDIS.getString(null)).keySet()) {
                add((String) serverName);
                API.getInstance().getPluginEnabler().printLog(Level.FINE, "Boucle 1");
            }
        }};
    }

    @Override
    public List<Long> getListServerID() {
        return new ArrayList<>() {{
            for (Object serverName : API.getInstance().getRedisManager().getRedissonClient().getMap(ServerDataValue.MAP_SERVER_REDIS.getString(null)).values()) {
                add((long) serverName);
                API.getInstance().getPluginEnabler().printLog(Level.FINE, "Boucle 2");
            }
        }};
    }

    @Override
    public List<Server> getListServer() {
        return new ArrayList<>() {{
            for (long serverID : getListServerID()) {
                add(getServer(serverID));
                API.getInstance().getPluginEnabler().printLog(Level.FINE, "Boucle 3");
            }
        }};
    }

    @Override
    public List<Server> getListServer(ServerType serverType) {

        if (serverType == null) return getListServer();

        return new ArrayList<>() {{
            for (Server server : getListServer()) {
                API.getInstance().getPluginEnabler().printLog(Level.FINE, "Boucle 4");
                if (server.getServerType() == serverType) {
                    API.getInstance().getPluginEnabler().printLog(Level.FINE, "Boucle 6");
                    add(server);
                } else {
                    API.getInstance().getPluginEnabler().printLog(Level.FINE, "Boucle 7");
                }
            }
        }};
    }

    @Override
    public boolean isServerExist(String s) {
        return API.getInstance().getRedisManager().getRedissonClient().getMap(ServerDataValue.MAP_SERVER_REDIS.getString(null)).containsKey(s);
    }

    @Override
    public boolean isServerExist(long l) {
        return API.getInstance().getRedisManager().getRedissonClient().getMap(ServerDataValue.MAP_SERVER_REDIS.getString(null)).containsValue(l);
    }

    @Override
    public Server getServer(String s) {
        Map<String, Long> serverMap = API.getInstance().getRedisManager().getRedissonClient().getMap(ServerDataValue.MAP_SERVER_REDIS.getString(null));
        if (!serverMap.containsKey(s)) return null;
        return new CServer(serverMap.get(s));
    }

    @Override
    public Server getServer(long l) {
        if (!isServerExist(l)) return null;
        return new CServer(l);
    }

    @Override
    public Server initServer(ServerType serverType, String name, IpInfo ipInfo) {
        Map<String, Long> serverMap = API.getInstance().getRedisManager().getRedissonClient().getMap(ServerDataValue.MAP_SERVER_REDIS.getString(null));
        if (!serverMap.containsKey(name)) {
            API.getInstance().getPluginEnabler().printLog(Level.INFO, "Server init with name: " + name);
            return CServer.initServer(serverType, name, ipInfo);
        }
        return new CServer(serverMap.get(name));
    }

    @Override
    public Server initServer(ServerType serverType, Long serverID, IpInfo ipInfo) {
        if (serverID == null || ipInfo == null) return null;
        if (!isServerExist(serverID)) {
            API.getInstance().getPluginEnabler().printLog(Level.INFO, "Server init with id: " + serverID);
            return CServer.initServer(serverType, serverID, ipInfo);
        }
        return new CServer(serverID);
    }

    @Override
    public Server getConnectableServer(APIPlayer apiPlayer, ServerType serverType) {

        List<Server> availableServer = getListServer(serverType);
        Server server = null;

        for (Server testServer : availableServer) {
            API.getInstance().getPluginEnabler().printLog(Level.FINE, "Boucle 5");
            API.getInstance().getPluginEnabler().printLog(Level.FINE, "Server test 1");
            if(testServer.getConnectedPlayer() < testServer.getMaxPlayers()) {
                API.getInstance().getPluginEnabler().printLog(Level.FINE, "Server test 1");
                if (testServer.getServerAccess().canAccess(testServer, apiPlayer)) {
                    API.getInstance().getPluginEnabler().printLog(Level.FINE, "Server test 2");
                    if (server == null || server.getConnectedPlayer() > testServer.getConnectedPlayer()) {
                        API.getInstance().getPluginEnabler().printLog(Level.FINE, "Server test 3");
                        server = testServer;
                    }
                }
            }
        }

        return server;

    }

}
