/* Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 */

package fr.redxil.core.common.server;

import fr.redline.pms.connect.linker.pm.PMManager;
import fr.redline.pms.utils.IpInfo;
import fr.redxil.api.common.game.Games;
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.api.common.rank.RankList;
import fr.redxil.api.common.redis.RedisManager;
import fr.redxil.api.common.server.Server;
import fr.redxil.api.common.server.type.ServerAccess;
import fr.redxil.api.common.server.type.ServerStatus;
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

    public static Server initServer(ServerType serverType, String name, IpInfo serverIp) {

        int maxPlayer = CoreAPI.get().getPluginEnabler().getMaxPlayer();

        ServerModel model = new SQLModels<>(ServerModel.class).getOrInsert(new HashMap<String, Object>() {{
            put(ServerDataValue.SERVER_NAME_SQL.getString(null), name);
            put(ServerDataValue.SERVER_MAXP_SQL.getString(null), maxPlayer);
            put(ServerDataValue.SERVER_STATUS_SQL.getString(null), ServerStatus.ONLINE.toString());
            put(ServerDataValue.SERVER_TYPE_SQL.getString(null), serverType.name());
            put(ServerDataValue.SERVER_ACCESS_SQL.getString(null), serverType.getRelatedServerAccess().name());
            put(ServerDataValue.SERVER_NEEDRANK_SQL.getString(null), null);
            put(ServerDataValue.SERVER_IP_SQL.getString(null), serverIp.getIp());
            put(ServerDataValue.SERVER_PORT_SQL.getString(null), serverIp.getPort().toString());
        }}, "WHERE " + ServerDataValue.SERVER_NAME_SQL.getString(null) + " = ?", name);

        Long serverId = Integer.valueOf(model.getServerID()).longValue();

        model.set(ServerDataValue.SERVER_MAXP_SQL.getString(null), maxPlayer);
        model.set(ServerDataValue.SERVER_IP_SQL.getString(null), serverIp.getIp());
        model.set(ServerDataValue.SERVER_PORT_SQL.getString(null), serverIp.getPort().toString());
        model.set(ServerDataValue.SERVER_STATUS_SQL.getString(null), ServerStatus.ONLINE.toString());

        ServerDataValue.clearRedisData(DataType.SERVER, name, serverId);
        RedisManager redisManager = CoreAPI.get().getRedisManager();

        redisManager.getRedisMap(ServerDataValue.MAP_SERVER_REDIS.getString(null)).put(name, serverId);
        redisManager.setRedisString(ServerDataValue.SERVER_NAME_REDIS.getString(name, serverId), name);
        redisManager.setRedisString(ServerDataValue.SERVER_TYPE_REDIS.getString(name, serverId), model.getString(ServerDataValue.SERVER_TYPE_SQL.getString(null, null)));

        redisManager.setRedisLong(ServerDataValue.SERVER_MAXP_REDIS.getString(name, serverId), maxPlayer);
        redisManager.setRedisString(ServerDataValue.SERVER_STATUS_REDIS.getString(name, serverId), model.getString(ServerDataValue.SERVER_STATUS_SQL.getString(null, null)));

        redisManager.setRedisString(ServerDataValue.SERVER_TYPE_REDIS.getString(name, serverId), model.getString(ServerDataValue.SERVER_TYPE_SQL.getString(null)));
        redisManager.setRedisString(ServerDataValue.SERVER_ACCESS_REDIS.getString(name, serverId), model.getString(ServerDataValue.SERVER_ACCESS_SQL.getString(null)));
        redisManager.setRedisString(ServerDataValue.SERVER_NEEDRANK_REDIS.getString(name, serverId), model.getString(ServerDataValue.SERVER_NEEDRANK_SQL.getString(null)));

        redisManager.setRedisString(ServerDataValue.SERVER_IP_REDIS.getString(name, serverId), model.getString(ServerDataValue.SERVER_IP_SQL.getString(null, null)));
        redisManager.setRedisLong(ServerDataValue.SERVER_PORT_REDIS.getString(name, serverId), Long.parseLong(model.getString(ServerDataValue.SERVER_PORT_SQL.getString(null, null))));

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
    public boolean shutdown() {

        String name = getServerName();
        if (!CoreAPI.get().getPluginEnabler().getServerName().equals(name)) return false;

        long id = getServerId();

        //model.set(ServerDataValue.SERVER_TASKS_SQL.getString(null), getTasks().toString().toUpperCase());

        System.out.println("[Core] Clearing redis data");

        getTeamLinked().forEach((teamID) -> CoreAPI.get().getTeamManager().getTeam(teamID).deleteTeam());

        ServerModel model = new SQLModels<>(ServerModel.class).getFirst("WHERE " + ServerDataValue.SERVER_ID_SQL.getString(null) + " = ?", id);

        model.set(ServerDataValue.SERVER_STATUS_SQL.getString(name, id), ServerStatus.OFFLINE.name());
        model.set(ServerDataValue.SERVER_ACCESS_SQL.getString(name, id), getServerAccess().name());
        model.set(ServerDataValue.SERVER_NEEDRANK_SQL.getString(name, id), getReservedRank().getRankPower());
        model.set(ServerDataValue.SERVER_TYPE_SQL.getString(name, id), getServerType().name());

        ServerDataValue.clearRedisData(DataType.SERVER, name, id);

        CoreAPI.get().getRedisManager().getRedissonClient().getMap(ServerDataValue.MAP_SERVER_REDIS.getString(null)).remove(name);

        return true;

    }

    @Override
    public ServerStatus getServerStatus() {
        return ServerStatus.valueOf(CoreAPI.get().getRedisManager().getRedisString(ServerDataValue.SERVER_STATUS_REDIS.getString(this)));
    }

    @Override
    public void setServerStatus(ServerStatus serverStatus) {
        CoreAPI.get().getRedisManager().setRedisString(ServerDataValue.SERVER_STATUS_REDIS.getString(this), serverStatus.name());
    }

    @Override
    public ServerAccess getServerAccess() {
        return ServerAccess.valueOf(CoreAPI.get().getRedisManager().getRedisString(ServerDataValue.SERVER_ACCESS_REDIS.getString(this)));
    }

    @Override
    public void setServerAccess(ServerAccess serverAccess) {
        CoreAPI.get().getRedisManager().setRedisString(ServerDataValue.SERVER_ACCESS_REDIS.getString(this), serverAccess.name());
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

    @Override
    public RankList getReservedRank() {
        String value = CoreAPI.get().getRedisManager().getRedisString(ServerDataValue.SERVER_NEEDRANK_REDIS.getString(this));
        if (value == null)
            return null;
        return RankList.getRank(value);
    }

    @Override
    public void setReservedRank(RankList rankList) {
        String name = rankList == null ? null : rankList.getRankName();
        CoreAPI.get().getRedisManager().setRedisString(ServerDataValue.SERVER_NEEDRANK_REDIS.getString(this), name);
    }

}
