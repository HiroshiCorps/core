/* Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 */

package fr.redxil.core.common.server;

import de.dytanic.cloudnet.driver.CloudNetDriver;
import de.dytanic.cloudnet.driver.service.ServiceInfoSnapshot;
import de.dytanic.cloudnet.driver.service.ServiceTask;
import fr.redline.pms.connect.linker.pm.PMManager;
import fr.redline.pms.utils.IpInfo;
import fr.redxil.api.common.game.Games;
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.api.common.redis.RedisManager;
import fr.redxil.api.common.server.Server;
import fr.redxil.api.common.server.type.ServerStatus;
import fr.redxil.api.common.server.type.ServerTasks;
import fr.redxil.api.common.server.type.ServerType;
import fr.redxil.core.common.CoreAPI;
import fr.redxil.core.common.data.PlayerDataValue;
import fr.redxil.core.common.data.ServerDataValue;
import fr.redxil.core.common.data.utils.DataType;
import fr.redxil.core.common.sql.SQLModels;
import fr.redxil.core.common.sql.server.ServerModel;
import org.redisson.api.RList;

import java.util.*;

public class CServer implements Server {

    private final String name;

    private final long serverId;

    public CServer(long serverID) {
        this.serverId = serverID;
        this.name = CoreAPI.get().getRedisManager().getRedisString(ServerDataValue.SERVER_NAME_REDIS.getString(this));
    }

    public CServer(String serverName) {
        this.serverId = Long.parseLong((String) CoreAPI.get().getRedisManager().getRedisMap(ServerDataValue.MAP_SERVER_REDIS.getString(null)).get(serverName));
        this.name = serverName;
    }

    public static Server initServer(String name, IpInfo serverIp) {

        ServerType newServerType;
        int maxPlayer = CoreAPI.get().getPluginEnabler().getMaxPlayer();
        if (CoreAPI.get().isBungee()) {
            CoreAPI.get().getRedisManager().setRedisLong("servers/bungee/server_max_players", maxPlayer);
            newServerType = ServerType.BUNGEE;
        } else
            newServerType = ServerType.UNKNOWN;

        ServerTasks serverTasks = ServerTasks.UNKNOWN;

        ServiceInfoSnapshot serviceInfoSnapshot = CloudNetDriver.getInstance().getCloudServiceProvider().getCloudServiceByName(name);

        if (serviceInfoSnapshot != null && ServerTasks.getTasksByService(serviceInfoSnapshot.getName()) != null) {
            serverTasks = ServerTasks.getTasksByService(serviceInfoSnapshot.getName());
        }

        ServerTasks finalServerTasks = serverTasks;

        ServerModel model = new SQLModels<>(ServerModel.class).getOrInsert(new HashMap<String, Object>() {{
            put(ServerDataValue.SERVER_NAME_SQL.getString(null), name);
            put(ServerDataValue.SERVER_MAXP_SQL.getString(null), maxPlayer);
            put(ServerDataValue.SERVER_ACCES_SQL.getString(null), 0);
            put(ServerDataValue.SERVER_MAINTENANCE_SQL.getString(null), 0);
            put(ServerDataValue.SERVER_TYPE_SQL.getString(null), newServerType.toString());
            put(ServerDataValue.SERVER_IP_SQL.getString(null), serverIp.getIp());
            put(ServerDataValue.SERVER_PORT_SQL.getString(null), serverIp.getPort().toString());
            put(ServerDataValue.SERVER_TASKS_SQL.getString(null), finalServerTasks.toString().toUpperCase());
        }}, "WHERE " + ServerDataValue.SERVER_NAME_SQL.getString(null) + " = ?", name);

        Long serverId = Integer.valueOf(model.getServerID()).longValue();

        model.set(ServerDataValue.SERVER_MAXP_SQL.getString(null), maxPlayer);
        model.set(ServerDataValue.SERVER_IP_SQL.getString(null), serverIp.getIp());
        model.set(ServerDataValue.SERVER_PORT_SQL.getString(null), serverIp.getPort().toString());

        ServerDataValue.clearRedisData(DataType.SERVER, name, serverId);
        RedisManager redisManager = CoreAPI.get().getRedisManager();

        redisManager.getRedisMap(ServerDataValue.MAP_SERVER_REDIS.getString(null)).put(name, serverId);
        redisManager.setRedisBoolean(ServerDataValue.SERVER_MAINTENANCE_REDIS.getString(name, serverId), model.getServerAccess() == 1);
        redisManager.setRedisString(ServerDataValue.SERVER_NAME_REDIS.getString(name, serverId), name);
        redisManager.setRedisString(ServerDataValue.SERVER_TYPE_REDIS.getString(name, serverId), model.getString(ServerDataValue.SERVER_TYPE_SQL.getString(null, null)));

        redisManager.setRedisLong(ServerDataValue.SERVER_MAXP_REDIS.getString(name, serverId), maxPlayer);
        redisManager.setRedisLong(ServerDataValue.SERVER_ACCES_REDIS.getString(name, serverId), model.getLong(ServerDataValue.SERVER_ACCES_SQL.getString(null, null)));

        redisManager.setRedisString(ServerDataValue.SERVER_IP_REDIS.getString(name, serverId), model.getString(ServerDataValue.SERVER_IP_SQL.getString(null, null)));
        redisManager.setRedisLong(ServerDataValue.SERVER_PORT_REDIS.getString(name, serverId), Long.parseLong(model.getString(ServerDataValue.SERVER_PORT_SQL.getString(null, null))));

        redisManager.setRedisString(ServerDataValue.SERVER_TASKS_REDIS.getString(name, serverId), finalServerTasks.toString().toUpperCase());

        return new CServer(serverId);

    }

    @Override
    public int getMaxPlayers() {
        return Long.valueOf(CoreAPI.get().getRedisManager().getRedisLong(ServerDataValue.SERVER_MAXP_REDIS.getString(this))).intValue();
    }

    @Override
    public void setMaxPlayers(int i) {
        CoreAPI.get().getRedisManager().setRedisLong(ServerDataValue.SERVER_MAXP_REDIS.getString(this), i);
    }

    @Override
    public void changeMaintenance(boolean b) {
        CoreAPI.get().getRedisManager().setRedisBoolean(ServerDataValue.SERVER_MAINTENANCE_REDIS.getString(this), b);
    }

    @Override
    public String getServerName() {
        return name;
    }

    @Override
    public Collection<APIPlayer> getPlayerList() {

        List<APIPlayer> playerList = new ArrayList<>();

        getPlayerUUIDList().forEach((uuid) -> {
            APIPlayer apiPlayer = CoreAPI.get().getPlayerManager().getPlayer(uuid);
            if (apiPlayer != null)
                playerList.add(apiPlayer);
        });

        return playerList;

    }

    @Override
    public Collection<UUID> getPlayerUUIDList() {

        List<UUID> playerList = new ArrayList<>();
        List<String> uuidList = CoreAPI.get().getRedisManager().getRedissonClient().getList(ServerDataValue.SERVER_PLAYER_REDIS.getString(this));
        uuidList.forEach((uuidString) -> playerList.add(UUID.fromString(uuidString)));

        return playerList;

    }

    @Override
    public long getLastPing() {
        return 0;
    }

    @Override
    public Object getServerResponseMessage(int i) {
        return null;
    }

    @Override
    public boolean isInMaintenance() {
        return CoreAPI.get().getRedisManager().getRedisBoolean(ServerDataValue.SERVER_MAINTENANCE_REDIS.getString(this));
    }

    @Override
    public boolean isOnline() {
        return CoreAPI.get().getServerManager().isServerExist(getServerName());
    }

    @Override
    public IpInfo getServerIP() {
        return new IpInfo(
                CoreAPI.get().getRedisManager().getRedisString(ServerDataValue.SERVER_IP_REDIS.getString(this)),
                Long.valueOf(CoreAPI.get().getRedisManager().getRedisLong(ServerDataValue.SERVER_PORT_REDIS.getString(this))).intValue()
        );
    }

    @Override
    public int getPowerAcces() {
        return Long.valueOf(CoreAPI.get().getRedisManager().getRedisLong(ServerDataValue.SERVER_ACCES_REDIS.getString(this))).intValue();
    }

    @Override
    public boolean shutdown() {

        String name = getServerName();
        if (!CoreAPI.get().getPluginEnabler().getServerName().equals(name)) return false;

        long id = getServerId();

        //model.set(ServerDataValue.SERVER_TASKS_SQL.getString(null), getTasks().toString().toUpperCase());

        System.out.println("[Core] Clearing redis data");

        getTeamLinked().forEach((teamID) -> CoreAPI.get().getTeamManager().getTeam(teamID).deleteTeam());

        ServerTasks serverTasks = getTasks();

        ServerDataValue.clearRedisData(DataType.SERVER, name, id);

        CoreAPI.get().getRedisManager().getRedissonClient().getMap(ServerDataValue.MAP_SERVER_REDIS.getString(null)).remove(name);

        if (!(serverTasks != null && serverTasks != ServerTasks.UNKNOWN)) return true;

        if (!(CloudNetDriver.getInstance().getServiceTaskProvider().isServiceTaskPresent(serverTasks.getStaticName())))
            return true;

        ServiceTask serviceTask = CloudNetDriver.getInstance().getServiceTaskProvider().getServiceTask(serverTasks.getStaticName());
        if (serviceTask == null || !serviceTask.isAutoDeleteOnStop()) return true;

        System.out.println("DELETE SERVER BDD " + name);
        CoreAPI.get().getSQLConnection().execute("DELETE FROM `list_servers` WHERE `" + ServerDataValue.SERVER_NAME_SQL.getString(null) + "`='" + name + "'");

        return true;

    }

    @Override
    public ServerStatus getServerStatus() {
        return null;
    }

    @Override
    public ServerType getServerType() {
        return ServerType.fromString(CoreAPI.get().getRedisManager().getRedisString(ServerDataValue.SERVER_TYPE_REDIS.getString(this)).toUpperCase());
    }

    @Override
    public long getServerId() {
        return serverId;
    }

    @Override
    public void setPlayerInServer(APIPlayer apiPlayer) {
        RList<String> listPlayer = CoreAPI.get().getRedisManager().getRedissonClient().getList(ServerDataValue.SERVER_PLAYER_REDIS.getString(this));
        UUID uuid = apiPlayer.getUUID();
        if (!listPlayer.contains(uuid.toString()))
            listPlayer.add(uuid.toString());

        if (CoreAPI.get().isBungee())
            CoreAPI.get().getRedisManager().setRedisString(PlayerDataValue.CONNECTED_BUNGEESERVER_REDIS.getString(apiPlayer), CoreAPI.get().getPluginEnabler().getServerName());
        else {
            Server server = apiPlayer.getServer();
            if (server != null) server.removePlayerInServer(apiPlayer.getUUID());
            CoreAPI.get().getRedisManager().setRedisString(PlayerDataValue.CONNECTED_SPIGOTSERVER_REDIS.getString(apiPlayer), CoreAPI.get().getPluginEnabler().getServerName());
        }
    }

    @Override
    public void removePlayerInServer(UUID uuid) {
        CoreAPI.get().getRedisManager().getRedissonClient().getList(ServerDataValue.SERVER_PLAYER_REDIS.getString(this)).remove(uuid.toString());
    }

    @Override
    public ServerTasks getTasks() {
        return ServerTasks.valueOf(CoreAPI.get().getRedisManager().getRedisString(ServerDataValue.SERVER_TASKS_REDIS.getString(this)).toUpperCase());
    }

    @Override
    public void changeTask(ServerTasks serverTasks) {
        CoreAPI.get().getRedisManager().setRedisString(ServerDataValue.SERVER_TASKS_REDIS.getString(this), serverTasks.toString().toUpperCase());
    }

    @Override
    public void sendShutdownOrder() {
        if (getServerName().equals(CoreAPI.get().getPluginEnabler().getServerName())) {
            CoreAPI.get().getPluginEnabler().shutdownServer("Shutdown Order from: " + getServerName());
            return;
        }
        PMManager.sendRedissonPluginMessage(CoreAPI.get().getRedisManager().getRedissonClient(), "shutdownOrder", CoreAPI.get().getPluginEnabler().getServerName());
    }

    @Override
    public boolean isHostDedicated() {
        return CoreAPI.get().getRedisManager().getRedisString(ServerDataValue.SERVER_HOSTED_REDIS.getString(this)) != null;
    }

    @Override
    public String getHostAuthor() {
        return CoreAPI.get().getRedisManager().getRedisString(ServerDataValue.SERVER_HOSTED_REDIS.getString(this));
    }

    @Override
    public boolean isGamesServer() {
        return CoreAPI.get().getGamesManager().getGame(getServerName()) != null;
    }

    @Override
    public Games getGames() {
        return CoreAPI.get().getGamesManager().getGame(getServerName());
    }

    @Override
    public List<Long> getTeamLinked() {
        return CoreAPI.get().getRedisManager().getRedisList(ServerDataValue.SERVER_LINK_TEAM_REDIS.getString(this));
    }

}