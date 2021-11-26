/* Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 */

package fr.redxil.core.common.server;

import fr.redline.pms.connect.linker.pm.PMManager;
import fr.redline.pms.utils.IpInfo;
import fr.redxil.api.common.API;
import fr.redxil.api.common.game.Game;
import fr.redxil.api.common.game.Host;
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.api.common.rank.RankList;
import fr.redxil.api.common.redis.RedisManager;
import fr.redxil.api.common.server.Server;
import fr.redxil.api.common.server.type.ServerAccess;
import fr.redxil.api.common.server.type.ServerStatus;
import fr.redxil.api.common.server.type.ServerType;
import fr.redxil.core.common.data.PlayerDataValue;
import fr.redxil.core.common.data.ServerDataValue;
import fr.redxil.core.common.data.utils.DataType;
import fr.redxil.core.common.sql.SQLModels;
import fr.redxil.core.common.sql.server.ServerModel;
import org.redisson.api.RList;

import java.util.*;
import java.util.logging.Level;

public class CServer implements Server {

    private final String name;

    private final long serverId;

    public CServer(long serverID) {
        this.serverId = serverID;
        this.name = API.getInstance().getRedisManager().getRedisString(ServerDataValue.SERVER_NAME_REDIS.getString(this));
    }

    public CServer(String serverName) {
        this.serverId = Long.parseLong((String) API.getInstance().getRedisManager().getRedisMap(ServerDataValue.MAP_SERVER_REDIS.getString(null)).get(serverName));
        this.name = serverName;
    }

    public static Server initServer(ServerType serverType, String name, IpInfo serverIp) {

        int maxPlayer = API.getInstance().getPluginEnabler().getMaxPlayer();

        ServerModel model = new SQLModels<>(ServerModel.class).getOrInsert(new HashMap<String, Object>() {{
            put(ServerDataValue.SERVER_NAME_SQL.getString(null), name);
            put(ServerDataValue.SERVER_MAXP_SQL.getString(null), maxPlayer);
            put(ServerDataValue.SERVER_STATUS_SQL.getString(null), ServerStatus.ONLINE.toString());
            put(ServerDataValue.SERVER_TYPE_SQL.getString(null), serverType.toString());
            put(ServerDataValue.SERVER_ACCESS_SQL.getString(null), serverType.getRelatedServerAccess().toString());
            put(ServerDataValue.SERVER_NEEDRANK_SQL.getString(null), RankList.JOUEUR.getRankPower().intValue());
            put(ServerDataValue.SERVER_IP_SQL.getString(null), serverIp.getIp());
            put(ServerDataValue.SERVER_PORT_SQL.getString(null), serverIp.getPort().toString());
        }}, "WHERE " + ServerDataValue.SERVER_NAME_SQL.getString(null) + " = ?", name);

        Long serverId = Integer.valueOf(model.getServerID()).longValue();

        model.set(ServerDataValue.SERVER_MAXP_SQL.getString(null), maxPlayer);
        model.set(ServerDataValue.SERVER_IP_SQL.getString(null), serverIp.getIp());
        model.set(ServerDataValue.SERVER_PORT_SQL.getString(null), serverIp.getPort().toString());
        model.set(ServerDataValue.SERVER_STATUS_SQL.getString(null), ServerStatus.ONLINE.toString());
        model.set(ServerDataValue.SERVER_TYPE_SQL.getString(null), serverType.toString());

        ServerDataValue.clearRedisData(DataType.SERVER, name, serverId);
        RedisManager redisManager = API.getInstance().getRedisManager();

        redisManager.getRedisMap(ServerDataValue.MAP_SERVER_REDIS.getString(null)).put(name, serverId);
        redisManager.setRedisString(ServerDataValue.SERVER_NAME_REDIS.getString(name, serverId), name);
        redisManager.setRedisString(ServerDataValue.SERVER_TYPE_REDIS.getString(name, serverId), model.getString(ServerDataValue.SERVER_TYPE_SQL.getString(null, null)));

        redisManager.setRedisLong(ServerDataValue.SERVER_MAXP_REDIS.getString(name, serverId), maxPlayer);
        redisManager.setRedisString(ServerDataValue.SERVER_STATUS_REDIS.getString(name, serverId), model.getString(ServerDataValue.SERVER_STATUS_SQL.getString(null, null)));

        redisManager.setRedisString(ServerDataValue.SERVER_TYPE_REDIS.getString(name, serverId), model.getString(ServerDataValue.SERVER_TYPE_SQL.getString(null)));
        redisManager.setRedisString(ServerDataValue.SERVER_ACCESS_REDIS.getString(name, serverId), model.getString(ServerDataValue.SERVER_ACCESS_SQL.getString(null)));
        redisManager.setRedisLong(ServerDataValue.SERVER_NEEDRANK_REDIS.getString(name, serverId), model.getInt(ServerDataValue.SERVER_NEEDRANK_SQL.getString(null)));

        redisManager.setRedisString(ServerDataValue.SERVER_IP_REDIS.getString(name, serverId), model.getString(ServerDataValue.SERVER_IP_SQL.getString(null, null)));
        redisManager.setRedisLong(ServerDataValue.SERVER_PORT_REDIS.getString(name, serverId), Long.parseLong(model.getString(ServerDataValue.SERVER_PORT_SQL.getString(null, null))));

        return new CServer(serverId);

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
        return name;
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
    public long getLastPing() {
        return 0;
    }

    @Override
    public Object getServerResponseMessage(int i) {
        return null;
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
    public boolean shutdown() {

        String name = getServerName();
        if (!API.getInstance().getServerName().equals(name)) return false;

        long id = getServerId();

        //model.set(ServerDataValue.SERVER_TASKS_SQL.getString(null), getTasks().toString().toUpperCase());

        API.getInstance().getPluginEnabler().printLog(Level.INFO, "[Core] Clearing redis data");

        getTeamLinked().forEach((teamID) -> API.getInstance().getTeamManager().getTeam(teamID).deleteTeam());

        ServerModel model = new SQLModels<>(ServerModel.class).getFirst("WHERE " + ServerDataValue.SERVER_ID_SQL.getString(null) + " = ?", id);

        model.set(ServerDataValue.SERVER_STATUS_SQL.getString(name, id), ServerStatus.OFFLINE.toString());
        model.set(ServerDataValue.SERVER_ACCESS_SQL.getString(name, id), getServerAccess().toString());
        model.set(ServerDataValue.SERVER_NEEDRANK_SQL.getString(name, id), getReservedRank().getRankPower().intValue());
        model.set(ServerDataValue.SERVER_TYPE_SQL.getString(name, id), getServerType().toString());

        ServerDataValue.clearRedisData(DataType.SERVER, name, id);

        API.getInstance().getRedisManager().getRedissonClient().getMap(ServerDataValue.MAP_SERVER_REDIS.getString(null)).remove(name);

        return true;

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
            API.getInstance().getRedisManager().setRedisString(PlayerDataValue.CONNECTED_BUNGEESERVER_REDIS.getString(apiPlayer), API.getInstance().getServerName());
        else {
            Server server = apiPlayer.getServer();
            if (server != null) server.removePlayerInServer(apiPlayer.getUUID());
            API.getInstance().getRedisManager().setRedisString(PlayerDataValue.CONNECTED_SPIGOTSERVER_REDIS.getString(apiPlayer), API.getInstance().getServerName());
        }
    }

    @Override
    public void removePlayerInServer(UUID uuid) {
        API.getInstance().getRedisManager().getRedissonClient().getList(ServerDataValue.SERVER_PLAYER_REDIS.getString(this)).remove(uuid.toString());
    }

    @Override
    public void sendShutdownOrder() {
        if (getServerName().equals(API.getInstance().getServerName())) {
            API.getInstance().getPluginEnabler().shutdownServer("Shutdown Order from: " + getServerName());
            return;
        }
        PMManager.sendRedissonPluginMessage(API.getInstance().getRedisManager().getRedissonClient(), "shutdownOrder", API.getInstance().getServerName());
    }

    @Override
    public boolean isHostServer() {
        return API.getInstance().getGameManager().getHost(getServerName()) != null;
    }

    @Override
    public boolean isGameServer() {
        return API.getInstance().getGame() != null;
    }

    @Override
    public Game getGame() {
        return API.getInstance().getGameManager().getGame(getServerName());
    }

    @Override
    public Host getHost() {
        return API.getInstance().getGameManager().getHost(getServerName());
    }

    @Override
    public List<Long> getTeamLinked() {
        return API.getInstance().getRedisManager().getRedisList(ServerDataValue.SERVER_LINK_TEAM_REDIS.getString(this));
    }

    @Override
    public RankList getReservedRank() {
        return RankList.getRank(API.getInstance().getRedisManager().getRedisLong(ServerDataValue.SERVER_NEEDRANK_REDIS.getString(this)));
    }

    @Override
    public void setReservedRank(RankList rankList) {
        Long power = rankList == null ? RankList.JOUEUR.getRankPower() : rankList.getRankPower();
        API.getInstance().getRedisManager().setRedisLong(ServerDataValue.SERVER_NEEDRANK_REDIS.getString(this), power);
    }

}
