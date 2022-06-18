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
import fr.redxil.api.common.server.Server;
import fr.redxil.api.common.server.creator.ServerInfo;
import fr.redxil.api.common.server.type.ServerAccess;
import fr.redxil.api.common.server.type.ServerStatus;
import fr.redxil.api.common.utils.DataReminder;
import fr.redxil.api.common.utils.id.IDGenerator;
import fr.redxil.core.common.CoreAPI;
import fr.redxil.core.common.data.IDDataValue;
import fr.redxil.core.common.data.server.ServerDataRedis;
import fr.redxil.core.common.data.server.ServerDataSql;
import fr.redxil.core.common.data.utils.DataType;
import fr.redxil.core.common.sql.SQLModels;
import fr.xilitra.hiroshisav.enums.ServerType;

import java.util.*;
import java.util.logging.Level;

public class CServer implements Server {

    DataReminder<Long> maxPlayerReminder = null;
    DataReminder<String> serverNameReminder = null;
    DataReminder<List<String>> playerListReminder = null;
    DataReminder<String> ipReminder = null;
    DataReminder<Long> portReminder = null;
    DataReminder<String> statusReminder = null;
    DataReminder<String> accessReminder = null;
    DataReminder<String> typeReminder = null;
    DataReminder<Long> reservedReminder = null;
    DataReminder<List<String>> allowReminder = null;
    private long serverID;

    public CServer(long serverID) {
        this.serverID = serverID;
    }

    public CServer(ServerInfo serverCreator) {
        ServerModel serverModel = null;

        if (API.getInstance().isOnlineMod())
            serverModel = new SQLModels<>(ServerModel.class).getOrInsert(new HashMap<>() {{
                put(ServerDataSql.SERVER_NAME_SQL.getSQLColumns(), serverCreator.getServerName());
                put(ServerDataSql.SERVER_MAXP_SQL.getSQLColumns(), serverCreator.getMaxPlayer());
                put(ServerDataSql.SERVER_STATUS_SQL.getSQLColumns(), serverCreator.getServerStatus().toString());
                put(ServerDataSql.SERVER_TYPE_SQL.getSQLColumns(), serverCreator.getServerType().toString());
                put(ServerDataSql.SERVER_ACCESS_SQL.getSQLColumns(), serverCreator.getServerAccess().toString());
                put(ServerDataSql.SERVER_NEEDRANK_SQL.getSQLColumns(), serverCreator.getRankAccess().getRankPower().intValue());
                put(ServerDataSql.SERVER_IP_SQL.getSQLColumns(), serverCreator.getIpInfo().getIp());
                put(ServerDataSql.SERVER_PORT_SQL.getSQLColumns(), serverCreator.getIpInfo().getPort().toString());
            }}, "WHERE " + ServerDataSql.SERVER_NAME_SQL.getSQLColumns().toSQL() + " = ?", serverCreator.getServerName());

        initServer(serverCreator.getServerType(), serverCreator.getServerAccess(), serverCreator.getRankAccess(), (serverModel == null ? null : Integer.valueOf(serverModel.getServerID()).longValue()), serverCreator.getServerName(), serverCreator.getIpInfo(), serverCreator.getMaxPlayer());

    }

    public CServer(Long serverID) {
        ServerModel serverModel = new SQLModels<>(ServerModel.class).getFirst("WHERE " + ServerDataSql.SERVER_ID_SQL.getSQLColumns().toSQL() + " = ?", serverID);
        initServer(serverModel.getServerType(), ServerAccess.valueOf(serverModel.getString(ServerDataSql.SERVER_ACCESS_SQL.getSQLColumns())), Rank.getRank(serverModel.getInt(ServerDataSql.SERVER_NEEDRANK_SQL.getSQLColumns())), serverID, serverModel.getServerName(), new IpInfo(serverModel.getString(ServerDataSql.SERVER_IP_SQL.getSQLColumns()), Integer.valueOf(serverModel.getString(ServerDataSql.SERVER_PORT_SQL.getSQLColumns()))), serverModel.getMaxPlayers());
    }

    private void initServer(ServerType serverType, ServerAccess serverAccess, Rank rank, Long serverID, String serverName, IpInfo ipInfo, int maxPlayer) {
        this.serverID = Objects.requireNonNullElseGet(serverID, () -> IDGenerator.generateLONGID(IDDataValue.SERVERID.getLocation()));

        ServerDataRedis.clearRedisData(DataType.SERVER, serverID);

        initServerName();
        API.getInstance().getServerManager().getNameToLongMap().put(serverName, serverID);
        serverNameReminder.setData(serverName);

        initTypeReminder();
        typeReminder.setData(serverType.toString());

        initMaxPlayer();
        maxPlayerReminder.setData(Integer.valueOf(maxPlayer).longValue());

        setServerStatus(ServerStatus.ONLINE);

        setServerAccess(serverAccess);
        setReservedRank(rank);

        setServerIP(ipInfo);
    }

    public void initMaxPlayer() {
        if (maxPlayerReminder == null)
            maxPlayerReminder = DataReminder.generateReminder(ServerDataRedis.SERVER_MAXP_REDIS.getString(this), null);
    }

    @Override
    public Optional<Integer> getMaxPlayers() {
        initMaxPlayer();
        Long maxPlayer = maxPlayerReminder.getData();
        return Optional.ofNullable(maxPlayer == null ? null : maxPlayer.intValue());
    }

    @Override
    public boolean isOnline() {
        return API.getInstance().getServerManager().isServerExist(serverID);
    }

    public void initServerName() {
        if (serverNameReminder == null)
            serverNameReminder = DataReminder.generateReminder(ServerDataRedis.SERVER_NAME_REDIS.getString(this), null);
    }

    @Override
    public Optional<String> getServerName() {
        initServerName();
        return Optional.ofNullable(serverNameReminder.getData());
    }

    public void initPlayerList() {
        if (playerListReminder == null)
            playerListReminder = DataReminder.generateListReminder(ServerDataRedis.SERVER_PLAYER_REDIS.getString(this));
    }

    @Override
    public Collection<UUID> getPlayerList() {

        initPlayerList();

        List<UUID> playerList = new ArrayList<>();

        playerListReminder.getData().forEach((uuidString) -> playerList.add(UUID.fromString(uuidString)));

        return playerList;

    }

    @Override
    public int getConnectedPlayer() {
        initPlayerList();
        return playerListReminder.getData().size();
    }

    public void initServerIP() {
        if (ipReminder == null)
            ipReminder = DataReminder.generateReminder(ServerDataRedis.SERVER_IP_REDIS.getString(this), "127.0.0.1");
        if (portReminder == null)
            portReminder = DataReminder.generateReminder(ServerDataRedis.SERVER_PORT_REDIS.getString(this), 0L);
    }

    @Override
    public IpInfo getServerIP() {
        initServerIP();
        return new IpInfo(
                ipReminder.getData(),
                portReminder.getData().intValue()
        );
    }

    @Override
    public void setServerIP(IpInfo ipInfo) {
        initServerIP();
        ipReminder.setData(ipInfo.getIp());
        portReminder.setData(ipInfo.getPort().longValue());
    }

    @Override
    public void shutdown() {

        Optional<String> serverName = getServerName();
        if (API.getInstance().getServerID() != serverID) return;

        long id = serverID;

        API.getInstance().getAPIEnabler().printLog(Level.INFO, "[Core] Clearing redis data");

        if (API.getInstance().isOnlineMod()) {

            ServerModel model = new SQLModels<>(ServerModel.class).getFirst("WHERE " + ServerDataSql.SERVER_ID_SQL.getSQLColumns().toSQL() + " = ?", id);

            model.set(
                    new HashMap<>() {{
                        put(ServerDataSql.SERVER_STATUS_SQL.getSQLColumns(), ServerStatus.OFFLINE.toString());
                        put(ServerDataSql.SERVER_ACCESS_SQL.getSQLColumns(), getServerAccess().toString());
                        put(ServerDataSql.SERVER_NEEDRANK_SQL.getSQLColumns(), getReservedRank().orElse(Rank.JOUEUR).getRankPower().intValue());
                        put(ServerDataSql.SERVER_TYPE_SQL.getSQLColumns(), getServerType().toString());
                        serverName.ifPresentOrElse((name) -> put(ServerDataSql.SERVER_NAME_SQL.getSQLColumns(), name), () -> put(ServerDataSql.SERVER_NAME_SQL.getSQLColumns(), null));
                    }}
            );

            ServerDataRedis.clearRedisData(DataType.SERVER, id);

        } else CoreAPI.getInstance().getServerManager().getMap().remove(id);

        serverName.ifPresent(s -> API.getInstance().getServerManager().getNameToLongMap().remove(s));

    }

    @Override
    public void setPlayerConnected(UUID uuid, boolean b) {
        initPlayerList();
        List<String> listPlayer = playerListReminder.getData();
        String uuidString = uuid.toString();
        if (listPlayer.contains(uuidString) != b)
            if (b)
                listPlayer.add(uuidString);
            else listPlayer.remove(uuidString);
    }

    public void initStatusReminder() {
        if (statusReminder == null)
            this.statusReminder = DataReminder.generateReminder(ServerDataRedis.SERVER_STATUS_REDIS.getString(this), ServerStatus.OFFLINE.toString());
    }

    @Override
    public ServerStatus getServerStatus() {
        initStatusReminder();
        return ServerStatus.valueOf(statusReminder.getData());
    }

    @Override
    public void setServerStatus(ServerStatus serverStatus) {
        initStatusReminder();
        statusReminder.setData(serverStatus.toString());
    }

    public void initAccessReminder() {
        if (accessReminder == null)
            this.accessReminder = DataReminder.generateReminder(ServerDataRedis.SERVER_ACCESS_REDIS.getString(this), ServerAccess.OPEN.toString());
    }

    @Override
    public ServerAccess getServerAccess() {
        initAccessReminder();
        return ServerAccess.valueOf(accessReminder.getData());
    }

    @Override
    public void setServerAccess(ServerAccess serverAccess) {
        initAccessReminder();
        accessReminder.setData(serverAccess.toString());
    }

    @Override
    public long getServerID() {
        return serverID;
    }

    public void initTypeReminder() {
        if (typeReminder == null)
            this.typeReminder = DataReminder.generateReminder(ServerDataRedis.SERVER_TYPE_REDIS.getString(this), ServerType.HUB.toString());
    }

    @Override
    public ServerType getServerType() {
        initTypeReminder();
        return ServerType.valueOf(typeReminder.getData());
    }

    public void initReservedReminder() {
        if (reservedReminder == null)
            this.reservedReminder = DataReminder.generateReminder(ServerDataRedis.SERVER_NEEDRANK_REDIS.getString(this), null);
    }

    @Override
    public Optional<Rank> getReservedRank() {
        initReservedReminder();
        Long power = reservedReminder.getData();
        if (power == null)
            return Optional.empty();
        return Optional.ofNullable(Rank.getRank(power));
    }

    @Override
    public void setReservedRank(Rank rank) {
        Long power = rank == null ? null : rank.getRankPower();
        initReservedReminder();
        reservedReminder.setData(power);
    }

    public void initAllowReminder() {
        if (allowReminder == null)
            allowReminder = DataReminder.generateListReminder(ServerDataRedis.SERVER_ALLOW_PLAYER_REDIS.getString(this));
    }

    @Override
    public void setAllowedConnect(UUID uuid, boolean b) {
        initAllowReminder();
        List<String> listPlayer = allowReminder.getData();
        String uuidString = uuid.toString();
        if (listPlayer.contains(uuidString) != b)
            if (b)
                listPlayer.add(uuidString);
            else listPlayer.remove(uuidString);
    }

    @Override
    public boolean getAllowedConnect(UUID uuid) {
        return allowReminder.getData().contains(uuid.toString());
    }

}
