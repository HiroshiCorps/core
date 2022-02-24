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

    private final long serverID;

    public CServer(long serverID) {
        this.serverID = serverID;
    }

    public static Server initServer(ServerType serverType, String name, IpInfo serverIp) {

        int maxPlayer = API.getInstance().getPluginEnabler().getMaxPlayer();

        ServerModel model = new SQLModels<>(ServerModel.class).getOrInsert(new HashMap<>() {{
            put(ServerDataValue.SERVER_NAME_SQL.getString(), name);
            put(ServerDataValue.SERVER_MAXP_SQL.getString(), maxPlayer);
            put(ServerDataValue.SERVER_STATUS_SQL.getString(), ServerStatus.ONLINE.toString());
            put(ServerDataValue.SERVER_TYPE_SQL.getString(), serverType.toString());
            put(ServerDataValue.SERVER_ACCESS_SQL.getString(), serverType.getRelatedServerAccess().toString());
            put(ServerDataValue.SERVER_NEEDRANK_SQL.getString(), Rank.JOUEUR.getRankPower().intValue());
            put(ServerDataValue.SERVER_IP_SQL.getString(), serverIp.getIp());
            put(ServerDataValue.SERVER_PORT_SQL.getString(), serverIp.getPort().toString());
        }}, "WHERE " + ServerDataValue.SERVER_NAME_SQL.getString() + " = ?", name);

        return initData(model, Integer.valueOf(model.getServerID()).longValue(), name, serverType, serverIp);

    }

    public static Server initServer(ServerType serverType, Long serverID, IpInfo serverIP) {

        ServerModel model = new SQLModels<>(ServerModel.class).getFirst("WHERE " + ServerDataValue.SERVER_ID_SQL.getString() + " = ?", serverID.intValue());

        return initData(model, serverID, model.getServerName(), serverType, serverIP);

    }

    private static CServer initData(ServerModel serverModel, long serverID, String serverName, ServerType serverType, IpInfo serverIP) {

        serverModel.set(ServerDataValue.SERVER_MAXP_SQL.getString(), API.getInstance().getPluginEnabler().getMaxPlayer());
        serverModel.set(ServerDataValue.SERVER_IP_SQL.getString(), serverIP.getIp());
        serverModel.set(ServerDataValue.SERVER_PORT_SQL.getString(), serverIP.getPort().toString());
        serverModel.set(ServerDataValue.SERVER_STATUS_SQL.getString(), ServerStatus.ONLINE.toString());
        serverModel.set(ServerDataValue.SERVER_TYPE_SQL.getString(), serverType.toString());

        ServerDataValue.clearRedisData(DataType.SERVER, serverID);
        RedisManager redisManager = API.getInstance().getRedisManager();

        redisManager.getRedisMap(ServerDataValue.MAP_SERVER_REDIS.getString()).put(serverName, serverID);
        redisManager.setRedisString(ServerDataValue.SERVER_NAME_REDIS.getString(serverID), serverName);
        redisManager.setRedisString(ServerDataValue.SERVER_TYPE_REDIS.getString(serverID), serverModel.getString(ServerDataValue.SERVER_TYPE_SQL.getString()));

        redisManager.setRedisLong(ServerDataValue.SERVER_MAXP_REDIS.getString(serverID), API.getInstance().getPluginEnabler().getMaxPlayer());
        redisManager.setRedisString(ServerDataValue.SERVER_STATUS_REDIS.getString(serverID), serverModel.getString(ServerDataValue.SERVER_STATUS_SQL.getString()));

        redisManager.setRedisString(ServerDataValue.SERVER_TYPE_REDIS.getString(serverID), serverModel.getString(ServerDataValue.SERVER_TYPE_SQL.getString()));
        redisManager.setRedisString(ServerDataValue.SERVER_ACCESS_REDIS.getString(serverID), serverModel.getString(ServerDataValue.SERVER_ACCESS_SQL.getString()));
        redisManager.setRedisLong(ServerDataValue.SERVER_NEEDRANK_REDIS.getString(serverID), serverModel.getInt(ServerDataValue.SERVER_NEEDRANK_SQL.getString()));

        redisManager.setRedisString(ServerDataValue.SERVER_IP_REDIS.getString(serverID), serverModel.getString(ServerDataValue.SERVER_IP_SQL.getString()));
        redisManager.setRedisLong(ServerDataValue.SERVER_PORT_REDIS.getString(serverID), Long.parseLong(serverModel.getString(ServerDataValue.SERVER_PORT_SQL.getString())));

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
        redisManager.getRedisMap(ServerDataValue.MAP_SERVER_REDIS.getString()).remove(currentName, serverID);
        redisManager.getRedisMap(ServerDataValue.MAP_SERVER_REDIS.getString()).put(s, serverID);
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
        if (API.getInstance().getServerID() != getServerID()) return;

        long id = getServerID();

        API.getInstance().getPluginEnabler().printLog(Level.INFO, "[Core] Clearing redis data");

        ServerModel model = new SQLModels<>(ServerModel.class).getFirst("WHERE " + ServerDataValue.SERVER_ID_SQL.getString() + " = ?", id);

        model.set(
                new HashMap<>() {{
                    put(ServerDataValue.SERVER_STATUS_SQL.getString(), ServerStatus.OFFLINE.toString());
                    put(ServerDataValue.SERVER_ACCESS_SQL.getString(id), getServerAccess().toString());
                    put(ServerDataValue.SERVER_NEEDRANK_SQL.getString(id), getReservedRank().getRankPower().intValue());
                    put(ServerDataValue.SERVER_TYPE_SQL.getString(id), getServerType().toString());
                    put(ServerDataValue.SERVER_NAME_SQL.getString(id), name);
                }}
        );

        ServerDataValue.clearRedisData(DataType.SERVER, id);

        API.getInstance().getRedisManager().getRedissonClient().getMap(ServerDataValue.MAP_SERVER_REDIS.getString()).remove(name);

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
    public long getServerID() {
        return serverID;
    }

    @Override
    public void setPlayerInServer(APIPlayer apiPlayer) {
        RList<String> listPlayer = API.getInstance().getRedisManager().getRedissonClient().getList(ServerDataValue.SERVER_PLAYER_REDIS.getString(this));
        UUID uuid = apiPlayer.getUUID();
        if (!listPlayer.contains(uuid.toString()))
            listPlayer.add(uuid.toString());

        if (API.getInstance().isVelocity())
            API.getInstance().getRedisManager().setRedisString(PlayerDataValue.PLAYER_BUNGEE_REDIS.getString(apiPlayer), getServerName());
        else {
            Server server = apiPlayer.getServer();
            if (server != null) server.removePlayerInServer(apiPlayer.getUUID());
            API.getInstance().getRedisManager().setRedisString(PlayerDataValue.PLAYER_SPIGOT_REDIS.getString(apiPlayer), getServerName());
        }
    }

    @Override
    public void removePlayerInServer(UUID uuid) {
        API.getInstance().getRedisManager().getRedissonClient().getList(ServerDataValue.SERVER_PLAYER_REDIS.getString(this)).remove(uuid.toString());
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
        return Rank.getRank(API.getInstance().getRedisManager().getRedisLong(ServerDataValue.SERVER_NEEDRANK_REDIS.getString(this)));
    }

    @Override
    public void setReservedRank(Rank Rank) {
        Long power = Rank == null ? fr.redxil.api.common.player.rank.Rank.JOUEUR.getRankPower() : Rank.getRankPower();
        API.getInstance().getRedisManager().setRedisLong(ServerDataValue.SERVER_NEEDRANK_REDIS.getString(this), power);
    }

}
