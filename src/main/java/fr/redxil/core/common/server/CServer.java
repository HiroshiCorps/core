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
import fr.redxil.api.common.game.Game;
import fr.redxil.api.common.game.Host;
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.api.common.player.rank.Rank;
import fr.redxil.api.common.redis.RedisManager;
import fr.redxil.api.common.server.Server;
import fr.redxil.api.common.server.type.ServerAccess;
import fr.redxil.api.common.server.type.ServerStatus;
import fr.redxil.api.common.server.type.ServerType;
import fr.redxil.core.common.data.player.PlayerDataRedis;
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

        redisManager.getRedisMap(ServerDataRedis.MAP_SERVER_REDIS.getString()).put(serverName, serverID);
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
                redisManager.getRedisMap(ServerDataRedis.MAP_SERVER_REDIS.getString()).remove(serverName, serverID)
        );

        redisManager.getRedisMap(ServerDataRedis.MAP_SERVER_REDIS.getString()).put(s, serverID);
        redisManager.setRedisString(ServerDataRedis.SERVER_NAME_REDIS.getString(this), s);
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
        return API.getInstance().getServerManager().isServerExist(getServerID());
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
        if (API.getInstance().getServerID() != getServerID()) return;

        long id = getServerID();

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

        serverName.ifPresent(s -> API.getInstance().getRedisManager().getRedissonClient().getMap(ServerDataRedis.MAP_SERVER_REDIS.getString()).remove(s));

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
    public void setPlayerInServer(APIPlayer apiPlayer) {
        RList<String> listPlayer = API.getInstance().getRedisManager().getRedissonClient().getList(ServerDataRedis.SERVER_PLAYER_REDIS.getString(this));
        UUID uuid = apiPlayer.getUUID();
        if (!listPlayer.contains(uuid.toString()))
            listPlayer.add(uuid.toString());

        Optional<String> optionalServerName = getServerName();

        if (optionalServerName.isEmpty())
            return;

        if (API.getInstance().isVelocity())
            API.getInstance().getRedisManager().setRedisString(PlayerDataRedis.PLAYER_BUNGEE_REDIS.getString(apiPlayer), optionalServerName.get());
        else {
            Server server = apiPlayer.getServer();
            if (server != null) server.removePlayerInServer(apiPlayer.getUUID());
            API.getInstance().getRedisManager().setRedisString(PlayerDataRedis.PLAYER_SPIGOT_REDIS.getString(apiPlayer), optionalServerName.get());
        }
    }

    @Override
    public void removePlayerInServer(UUID uuid) {
        API.getInstance().getRedisManager().getRedissonClient().getList(ServerDataRedis.SERVER_PLAYER_REDIS.getString(this)).remove(uuid.toString());
    }

    @Override
    public boolean isHostServer() {
        return API.getInstance().getGameManager().isHostExistByServerID(getServerID());
    }

    @Override
    public boolean isGameServer() {
        return API.getInstance().getGame() != null;
    }

    @Override
    public Game getGame() {
        return API.getInstance().getGameManager().getGameByServerID(getServerID());
    }

    @Override
    public Host getHost() {
        return API.getInstance().getGameManager().getHostByServerID(getServerID());
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

}
