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
import fr.redxil.api.common.player.rank.Rank;
import fr.redxil.api.common.redis.RedisManager;
import fr.redxil.api.common.server.Server;
import fr.redxil.api.common.server.type.ServerAccess;
import fr.redxil.api.common.server.type.ServerStatus;
import fr.redxil.api.common.server.type.ServerType;
import fr.redxil.core.common.data.server.ServerDataRedis;
import fr.redxil.core.common.data.server.ServerDataSql;
import fr.redxil.core.common.data.utils.DataType;
import fr.redxil.core.common.sql.SQLModels;
import org.redisson.api.RList;

import java.util.*;
import java.util.logging.Level;

public class CServer implements Server {

    private final long serverID;

    public CServer(long serverID) {
        this.serverID = serverID;
    }

    public static Server initServer(ServerType serverType, String name, IpInfo serverIp) {

        int maxPlayer = API.getInstance().getPluginEnabler().getMaxPlayer();

        ServerModel model = new SQLModels<>(ServerModel.class).getOrInsert(new HashMap<>() {{
            put(ServerDataSql.SERVER_NAME_SQL.getSQLColumns(), name);
            put(ServerDataSql.SERVER_MAXP_SQL.getSQLColumns(), maxPlayer);
            put(ServerDataSql.SERVER_STATUS_SQL.getSQLColumns(), ServerStatus.ONLINE.toString());
            put(ServerDataSql.SERVER_TYPE_SQL.getSQLColumns(), serverType.toString());
            put(ServerDataSql.SERVER_ACCESS_SQL.getSQLColumns(), serverType.getRelatedServerAccess().toString());
            put(ServerDataSql.SERVER_NEEDRANK_SQL.getSQLColumns(), Rank.JOUEUR.getRankPower().intValue());
            put(ServerDataSql.SERVER_IP_SQL.getSQLColumns(), serverIp.getIp());
            put(ServerDataSql.SERVER_PORT_SQL.getSQLColumns(), serverIp.getPort().toString());
        }}, "WHERE " + ServerDataSql.SERVER_NAME_SQL.getSQLColumns().toSQL() + " = ?", name);

        return initServer(model, Integer.valueOf(model.getServerID()).longValue(), name, serverType, serverIp);

    }

    public static Server initServer(ServerType serverType, Long serverID, IpInfo serverIP) {

        ServerModel model = new SQLModels<>(ServerModel.class).getFirst("WHERE " + ServerDataSql.SERVER_ID_SQL.getSQLColumns().toSQL() + " = ?", serverID.intValue());

        return initServer(model, serverID, model.getServerName(), serverType, serverIP);

    }

    private static CServer initServer(ServerModel serverModel, long serverID, String serverName, ServerType serverType, IpInfo serverIP) {

        if (serverModel == null)
            return null;
        serverModel.set(new HashMap<>() {{
            put(ServerDataSql.SERVER_MAXP_SQL.getSQLColumns(), API.getInstance().getPluginEnabler().getMaxPlayer());
            put(ServerDataSql.SERVER_IP_SQL.getSQLColumns(), serverIP.getIp());
            put(ServerDataSql.SERVER_PORT_SQL.getSQLColumns(), serverIP.getPort().toString());
            put(ServerDataSql.SERVER_STATUS_SQL.getSQLColumns(), ServerStatus.ONLINE.toString());
            put(ServerDataSql.SERVER_TYPE_SQL.getSQLColumns(), serverType.toString());
        }});

        ServerDataRedis.clearRedisData(DataType.SERVER, serverID);
        RedisManager redisManager = API.getInstance().getRedisManager();

        API.getInstance().getServerManager().getNameToLongMap().put(serverName, serverID);
        redisManager.setRedisString(ServerDataRedis.SERVER_NAME_REDIS.getString(serverID), serverName);
        redisManager.setRedisString(ServerDataRedis.SERVER_TYPE_REDIS.getString(serverID), serverModel.getString(ServerDataSql.SERVER_TYPE_SQL.getSQLColumns()));

        redisManager.setRedisLong(ServerDataRedis.SERVER_MAXP_REDIS.getString(serverID), API.getInstance().getPluginEnabler().getMaxPlayer());
        redisManager.setRedisString(ServerDataRedis.SERVER_STATUS_REDIS.getString(serverID), serverModel.getString(ServerDataSql.SERVER_STATUS_SQL.getSQLColumns()));

        redisManager.setRedisString(ServerDataRedis.SERVER_TYPE_REDIS.getString(serverID), serverModel.getString(ServerDataSql.SERVER_TYPE_SQL.getSQLColumns()));
        redisManager.setRedisString(ServerDataRedis.SERVER_ACCESS_REDIS.getString(serverID), serverModel.getString(ServerDataSql.SERVER_ACCESS_SQL.getSQLColumns()));
        redisManager.setRedisLong(ServerDataRedis.SERVER_NEEDRANK_REDIS.getString(serverID), serverModel.getInt(ServerDataSql.SERVER_NEEDRANK_SQL.getSQLColumns()));

        redisManager.setRedisString(ServerDataRedis.SERVER_IP_REDIS.getString(serverID), serverModel.getString(ServerDataSql.SERVER_IP_SQL.getSQLColumns()));
        redisManager.setRedisLong(ServerDataRedis.SERVER_PORT_REDIS.getString(serverID), Long.parseLong(serverModel.getString(ServerDataSql.SERVER_PORT_SQL.getSQLColumns())));

        return new CServer(serverID);

    }

    @Override
    public Optional<Integer> getMaxPlayers() {
        Long maxPlayer = API.getInstance().getRedisManager().getRedisLong(ServerDataRedis.SERVER_MAXP_REDIS.getString(this));
        if (maxPlayer == null)
            return Optional.empty();
        return Optional.of(maxPlayer.intValue());
    }

    @Override
    public void setMaxPlayers(int i) {
        API.getInstance().getRedisManager().setRedisLong(ServerDataRedis.SERVER_MAXP_REDIS.getString(this), i);
    }

    @Override
    public Optional<String> getServerName() {
        return Optional.ofNullable(API.getInstance().getRedisManager().getRedisString(ServerDataRedis.SERVER_NAME_REDIS.getString(this)));
    }

    @Override
    public void setServerName(String s) {
        Optional<String> currentName = getServerName();
        RedisManager redisManager = API.getInstance().getRedisManager();

        currentName.ifPresent((serverName) ->
                API.getInstance().getServerManager().getNameToLongMap().remove(serverName, serverID)
        );

        API.getInstance().getServerManager().getNameToLongMap().put(s, serverID);
        redisManager.setRedisString(ServerDataRedis.SERVER_NAME_REDIS.getString(this), s);
    }

    @Override
    public Collection<UUID> getPlayerList() {

        List<UUID> playerList = new ArrayList<>();

        API.getInstance().getRedisManager().getRedissonClient().getList(ServerDataRedis.SERVER_PLAYER_REDIS.getString(this))
                .forEach((uuidString) -> playerList.add(UUID.fromString((String) uuidString)));

        return playerList;

    }

    @Override
    public int getConnectedPlayer() {
        return API.getInstance().getRedisManager().getRedissonClient().getList(ServerDataRedis.SERVER_PLAYER_REDIS.getString(this)).size();
    }

    @Override
    public boolean isOnline() {
        return API.getInstance().getServerManager().isServerExist(serverID);
    }

    @Override
    public IpInfo getServerIP() {
        return new IpInfo(
                API.getInstance().getRedisManager().getRedisString(ServerDataRedis.SERVER_IP_REDIS.getString(this)),
                API.getInstance().getRedisManager().getRedisLong(ServerDataRedis.SERVER_PORT_REDIS.getString(this)).intValue()
        );
    }

    @Override
    public void shutdown() {

        Optional<String> serverName = getServerName();
        if (API.getInstance().getServerID() != serverID) return;

        long id = serverID;

        API.getInstance().getPluginEnabler().printLog(Level.INFO, "[Core] Clearing redis data");

        ServerModel model = new SQLModels<>(ServerModel.class).getFirst("WHERE " + ServerDataSql.SERVER_ID_SQL.getSQLColumns().toSQL() + " = ?", id);

        model.set(
                new HashMap<>() {{
                    put(ServerDataSql.SERVER_STATUS_SQL.getSQLColumns(), ServerStatus.OFFLINE.toString());
                    put(ServerDataSql.SERVER_ACCESS_SQL.getSQLColumns(), getServerAccess().toString());
                    put(ServerDataSql.SERVER_NEEDRANK_SQL.getSQLColumns(), getReservedRank().getRankPower().intValue());
                    put(ServerDataSql.SERVER_TYPE_SQL.getSQLColumns(), getServerType().toString());
                    serverName.ifPresentOrElse((name) -> put(ServerDataSql.SERVER_NAME_SQL.getSQLColumns(), name), () -> put(ServerDataSql.SERVER_NAME_SQL.getSQLColumns(), null));
                }}
        );

        ServerDataRedis.clearRedisData(DataType.SERVER, id);

        serverName.ifPresent(s -> API.getInstance().getServerManager().getNameToLongMap().remove(s));

    }

    @Override
    public void setPlayerConnected(UUID uuid, boolean b) {
        RList<String> listPlayer = API.getInstance().getRedisManager().getRedissonClient().getList(ServerDataRedis.SERVER_PLAYER_REDIS.getString(this));
        String uuidString = uuid.toString();
        if (listPlayer.contains(uuidString) != b)
            if (b)
                listPlayer.add(uuidString);
            else listPlayer.remove(uuidString);
    }

    @Override
    public ServerStatus getServerStatus() {
        return ServerStatus.getServerStatus(API.getInstance().getRedisManager().getRedisString(ServerDataRedis.SERVER_STATUS_REDIS.getString(this)));
    }

    @Override
    public void setServerStatus(ServerStatus serverStatus) {
        API.getInstance().getRedisManager().setRedisString(ServerDataRedis.SERVER_STATUS_REDIS.getString(this), serverStatus.toString());
    }

    @Override
    public ServerAccess getServerAccess() {
        return ServerAccess.getServerAccess(API.getInstance().getRedisManager().getRedisString(ServerDataRedis.SERVER_ACCESS_REDIS.getString(this)));
    }

    @Override
    public void setServerAccess(ServerAccess serverAccess) {
        API.getInstance().getRedisManager().setRedisString(ServerDataRedis.SERVER_ACCESS_REDIS.getString(this), serverAccess.toString());
    }

    @Override
    public ServerType getServerType() {
        return ServerType.getServerType(API.getInstance().getRedisManager().getRedisString(ServerDataRedis.SERVER_TYPE_REDIS.getString(this)));
    }

    @Override
    public long getServerID() {
        return serverID;
    }

    @Override
    public Rank getReservedRank() {
        return Rank.getRank(API.getInstance().getRedisManager().getRedisLong(ServerDataRedis.SERVER_NEEDRANK_REDIS.getString(this)));
    }

    @Override
    public void setReservedRank(Rank Rank) {
        Long power = Rank == null ? fr.redxil.api.common.player.rank.Rank.JOUEUR.getRankPower() : Rank.getRankPower();
        API.getInstance().getRedisManager().setRedisLong(ServerDataRedis.SERVER_NEEDRANK_REDIS.getString(this), power);
    }

    @Override
    public void setAllowedConnect(UUID uuid, boolean b) {
        RList<String> listPlayer = API.getInstance().getRedisManager().getRedissonClient().getList(ServerDataRedis.SERVER_ALLOW_PLAYER_REDIS.getString(this));
        String uuidString = uuid.toString();
        if (listPlayer.contains(uuidString) != b)
            if (b)
                listPlayer.add(uuidString);
            else listPlayer.remove(uuidString);
    }

    @Override
    public boolean getAllowedConnect(UUID uuid) {
        return API.getInstance().getRedisManager().getRedissonClient().getList(ServerDataRedis.SERVER_ALLOW_PLAYER_REDIS.getString(this)).contains(uuid.toString());
    }

}
