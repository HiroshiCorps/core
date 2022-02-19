/* Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 */

package fr.redxil.core.common.server;

import fr.redline.pms.utils.IpInfo;
import fr.redxil.api.common.API;
import fr.redxil.api.common.game.Game;
import fr.redxil.api.common.game.Host;
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.api.common.player.rank.Rank;
import fr.redxil.api.common.redis.RedisManager;
import fr.redxil.api.common.server.Server;
import fr.redxil.api.common.server.type.ServerAccess;
import fr.redxil.api.common.server.type.ServerStatus;
import fr.redxil.api.common.server.type.ServerType;
import fr.redxil.core.common.data.PlayerDataValue;
import fr.redxil.core.common.data.ServerDataValue;
import fr.redxil.core.common.data.utils.DataType;
import fr.redxil.core.common.sql.SQLModels;
import org.redisson.api.RList;

import java.util.*;
import java.util.logging.Level;

public class CServer implements Server {

    private final long serverId;

    public CServer(long serverID) {
        this.serverId = serverID;
    }

    public static Server initServer(ServerType serverType, String name, IpInfo serverIp) {

        int maxPlayer = API.getInstance().getPluginEnabler().getMaxPlayer();

        ServerModel model = new SQLModels<>(ServerModel.class).getOrInsert(new HashMap<>() {{
            put(ServerDataValue.SERVER_NAME_SQL.getString(null), name);
            put(ServerDataValue.SERVER_MAXP_SQL.getString(null), maxPlayer);
            put(ServerDataValue.SERVER_STATUS_SQL.getString(null), ServerStatus.ONLINE.toString());
            put(ServerDataValue.SERVER_TYPE_SQL.getString(null), serverType.toString());
            put(ServerDataValue.SERVER_ACCESS_SQL.getString(null), serverType.getRelatedServerAccess().toString());
            put(ServerDataValue.SERVER_NEEDRANK_SQL.getString(null), Rank.JOUEUR.getRankPower().intValue());
            put(ServerDataValue.SERVER_IP_SQL.getString(null), serverIp.getIp());
            put(ServerDataValue.SERVER_PORT_SQL.getString(null), serverIp.getPort().toString());
        }}, "WHERE " + ServerDataValue.SERVER_NAME_SQL.getString(null) + " = ?", name);

        return initData(model, Integer.valueOf(model.getServerID()).longValue(), name, serverType, serverIp);

    }

    public static Server initServer(ServerType serverType, Long serverID, IpInfo serverIP) {

        ServerModel model = new SQLModels<>(ServerModel.class).getFirst("WHERE " + ServerDataValue.SERVER_ID_SQL.getString(null) + " = ?", serverID.intValue());

        return initData(model, serverID, model.getServerName(), serverType, serverIP);

    }

    private static CServer initData(ServerModel serverModel, long serverID, String serverName, ServerType serverType, IpInfo serverIP){

        serverModel.set(ServerDataValue.SERVER_MAXP_SQL.getString(null), API.getInstance().getPluginEnabler().getMaxPlayer());
        serverModel.set(ServerDataValue.SERVER_IP_SQL.getString(null), serverIP.getIp());
        serverModel.set(ServerDataValue.SERVER_PORT_SQL.getString(null), serverIP.getPort().toString());
        serverModel.set(ServerDataValue.SERVER_STATUS_SQL.getString(null), ServerStatus.ONLINE.toString());
        serverModel.set(ServerDataValue.SERVER_TYPE_SQL.getString(null), serverType.toString());

        ServerDataValue.clearRedisData(DataType.SERVER, serverName, serverID);
        RedisManager redisManager = API.getInstance().getRedisManager();

        redisManager.getRedisMap(ServerDataValue.MAP_SERVER_REDIS.getString(null)).put(serverName, serverID);
        redisManager.setRedisString(ServerDataValue.SERVER_NAME_REDIS.getString(serverName, serverID), serverName);
        redisManager.setRedisString(ServerDataValue.SERVER_TYPE_REDIS.getString(serverName, serverID), serverModel.getString(ServerDataValue.SERVER_TYPE_SQL.getString(null, null)));

        redisManager.setRedisLong(ServerDataValue.SERVER_MAXP_REDIS.getString(serverName, serverID), API.getInstance().getPluginEnabler().getMaxPlayer());
        redisManager.setRedisString(ServerDataValue.SERVER_STATUS_REDIS.getString(serverName, serverID), serverModel.getString(ServerDataValue.SERVER_STATUS_SQL.getString(null, null)));

        redisManager.setRedisString(ServerDataValue.SERVER_TYPE_REDIS.getString(serverName, serverID), serverModel.getString(ServerDataValue.SERVER_TYPE_SQL.getString(null)));
        redisManager.setRedisString(ServerDataValue.SERVER_ACCESS_REDIS.getString(serverName, serverID), serverModel.getString(ServerDataValue.SERVER_ACCESS_SQL.getString(null)));
        redisManager.setRedisLong(ServerDataValue.SERVER_NEEDRANK_REDIS.getString(serverName, serverID), serverModel.getInt(ServerDataValue.SERVER_NEEDRANK_SQL.getString(null)));

        redisManager.setRedisString(ServerDataValue.SERVER_IP_REDIS.getString(serverName, serverID), serverModel.getString(ServerDataValue.SERVER_IP_SQL.getString(null, null)));
        redisManager.setRedisLong(ServerDataValue.SERVER_PORT_REDIS.getString(serverName, serverID), Long.parseLong(serverModel.getString(ServerDataValue.SERVER_PORT_SQL.getString(null, null))));

        return new CServer(serverID);

    }

    @Override
    public int getMaxPlayers() {
        return Long.valueOf(API.getInstance().getRedisManager().getRedisLong(ServerDataValue.SERVER_MAXP_REDIS.getString(this))).intValue();
    }

    @Override
    public void setMaxPlayers(int i) {
        API.getInstance().getRedisManager().setRedisLong(ServerDataValue.SERVER_MAXP_REDIS.getString(this), i);
    }

    @Override
    public String getServerName() {
        return API.getInstance().getRedisManager().getRedisString(ServerDataValue.SERVER_NAME_REDIS.getString(this));
    }

    @Override
    public void setServerName(String s) {
        String currentName = getServerName();
        RedisManager redisManager = API.getInstance().getRedisManager();
        redisManager.getRedisMap(ServerDataValue.MAP_SERVER_REDIS.getString(null)).remove(currentName, serverId);
        redisManager.getRedisMap(ServerDataValue.MAP_SERVER_REDIS.getString(null)).put(s, serverId);
        redisManager.setRedisString(ServerDataValue.SERVER_NAME_REDIS.getString(this), s);
    }

    @Override
    public Collection<APIPlayer> getPlayerList() {

        List<APIPlayer> playerList = new ArrayList<>();

        getPlayerUUIDList().forEach((uuid) -> {
            APIPlayer apiPlayer = API.getInstance().getPlayerManager().getPlayer(uuid);
            if (apiPlayer != null)
                playerList.add(apiPlayer);
        });

        return playerList;

    }

    @Override
    public Collection<UUID> getPlayerUUIDList() {

        List<UUID> playerList = new ArrayList<>();

        API.getInstance().getRedisManager().getRedissonClient().getList(ServerDataValue.SERVER_PLAYER_REDIS.getString(this))
                .forEach((uuidString) -> playerList.add(UUID.fromString((String) uuidString)));

        return playerList;

    }

    @Override
    public int getConnectedPlayer() {
        return API.getInstance().getRedisManager().getRedissonClient().getList(ServerDataValue.SERVER_PLAYER_REDIS.getString(this)).size();
    }

    @Override
    public boolean isOnline() {
        return API.getInstance().getServerManager().isServerExist(getServerName());
    }

    @Override
    public IpInfo getServerIP() {
        return new IpInfo(
                API.getInstance().getRedisManager().getRedisString(ServerDataValue.SERVER_IP_REDIS.getString(this)),
                Long.valueOf(API.getInstance().getRedisManager().getRedisLong(ServerDataValue.SERVER_PORT_REDIS.getString(this))).intValue()
        );
    }

    @Override
    public void shutdown() {

        String name = getServerName();
        if (API.getInstance().getServerID() != getServerId()) return;

        long id = getServerId();

        //model.set(ServerDataValue.SERVER_TASKS_SQL.getString(null), getTasks().toString().toUpperCase());

        API.getInstance().getPluginEnabler().printLog(Level.INFO, "[Core] Clearing redis data");

        ServerModel model = new SQLModels<>(ServerModel.class).getFirst("WHERE " + ServerDataValue.SERVER_ID_SQL.getString(null) + " = ?", id);

        model.set(ServerDataValue.SERVER_STATUS_SQL.getString(name, id), ServerStatus.OFFLINE.toString());
        model.set(ServerDataValue.SERVER_ACCESS_SQL.getString(name, id), getServerAccess().toString());
        model.set(ServerDataValue.SERVER_NEEDRANK_SQL.getString(name, id), getReservedRank().getRankPower().intValue());
        model.set(ServerDataValue.SERVER_TYPE_SQL.getString(name, id), getServerType().toString());
        model.set(ServerDataValue.SERVER_NAME_SQL.getString(name, id), name);

        ServerDataValue.clearRedisData(DataType.SERVER, name, id);

        API.getInstance().getRedisManager().getRedissonClient().getMap(ServerDataValue.MAP_SERVER_REDIS.getString(null)).remove(name);

    }

    @Override
    public ServerStatus getServerStatus() {
        return ServerStatus.getServerStatus(API.getInstance().getRedisManager().getRedisString(ServerDataValue.SERVER_STATUS_REDIS.getString(this)));
    }

    @Override
    public void setServerStatus(ServerStatus serverStatus) {
        API.getInstance().getRedisManager().setRedisString(ServerDataValue.SERVER_STATUS_REDIS.getString(this), serverStatus.toString());
    }

    @Override
    public ServerAccess getServerAccess() {
        return ServerAccess.getServerAccess(API.getInstance().getRedisManager().getRedisString(ServerDataValue.SERVER_ACCESS_REDIS.getString(this)));
    }

    @Override
    public void setServerAccess(ServerAccess serverAccess) {
        API.getInstance().getRedisManager().setRedisString(ServerDataValue.SERVER_ACCESS_REDIS.getString(this), serverAccess.toString());
    }

    @Override
    public ServerType getServerType() {
        return ServerType.getServerType(API.getInstance().getRedisManager().getRedisString(ServerDataValue.SERVER_TYPE_REDIS.getString(this)));
    }

    @Override
    public long getServerId() {
        return serverId;
    }

    @Override
    public void setPlayerInServer(APIPlayer apiPlayer) {
        RList<String> listPlayer = API.getInstance().getRedisManager().getRedissonClient().getList(ServerDataValue.SERVER_PLAYER_REDIS.getString(this));
        UUID uuid = apiPlayer.getUUID();
        if (!listPlayer.contains(uuid.toString()))
            listPlayer.add(uuid.toString());

        if (API.getInstance().isVelocity())
            API.getInstance().getRedisManager().setRedisString(PlayerDataValue.CONNECTED_BUNGEESERVER_REDIS.getString(apiPlayer), getServerName());
        else {
            Server server = apiPlayer.getServer();
            if (server != null) server.removePlayerInServer(apiPlayer.getUUID());
            API.getInstance().getRedisManager().setRedisString(PlayerDataValue.CONNECTED_SPIGOTSERVER_REDIS.getString(apiPlayer), getServerName());
        }
    }

    @Override
    public void removePlayerInServer(UUID uuid) {
        API.getInstance().getRedisManager().getRedissonClient().getList(ServerDataValue.SERVER_PLAYER_REDIS.getString(this)).remove(uuid.toString());
    }

    @Override
    public boolean isHostServer() {
        return API.getInstance().getGameManager().isHostExistByServerID(getServerId());
    }

    @Override
    public boolean isGameServer() {
        return API.getInstance().getGame() != null;
    }

    @Override
    public Game getGame() {
        return API.getInstance().getGameManager().getGameByServerID(getServerId());
    }

    @Override
    public Host getHost() {
        return API.getInstance().getGameManager().getHostByServerID(getServerId());
    }

    @Override
    public Rank getReservedRank() {
        return Rank.getRank(API.getInstance().getRedisManager().getRedisLong(ServerDataValue.SERVER_NEEDRANK_REDIS.getString(this)));
    }

    @Override
    public void setReservedRank(Rank Rank) {
        Long power = Rank == null ? fr.redxil.api.common.player.rank.Rank.JOUEUR.getRankPower() : Rank.getRankPower();
        API.getInstance().getRedisManager().setRedisLong(ServerDataValue.SERVER_NEEDRANK_REDIS.getString(this), power);
    }

}
