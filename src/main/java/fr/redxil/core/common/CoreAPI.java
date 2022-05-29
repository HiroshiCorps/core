/*
 *
 * Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 *
 */

package fr.redxil.core.common;

import fr.redline.pms.utils.GSONSaver;
import fr.redline.pms.utils.IpInfo;
import fr.redxil.api.common.API;
import fr.redxil.api.common.PluginEnabler;
import fr.redxil.api.common.redis.RedisManager;
import fr.redxil.api.common.server.Server;
import fr.redxil.api.common.server.type.ServerType;
import fr.redxil.api.common.sql.SQLConnection;
import fr.redxil.core.common.player.CPlayerManager;
import fr.redxil.core.common.player.moderator.CModeratorManager;
import fr.redxil.core.common.redis.CRedisManager;
import fr.redxil.core.common.server.CServerManager;
import fr.redxil.core.common.sql.CSQLConnection;

import java.io.File;
import java.util.Optional;
import java.util.logging.Level;

public class CoreAPI extends API {

    private static CoreAPI instance;
    private final String serverName;
    private final Boolean onlineMod;
    private final CServerManager serverManager;
    private final CPlayerManager apiPlayerManager;
    private final CModeratorManager moderatorManager;
    private CSQLConnection sqlConnection;
    private Server server;
    private CRedisManager manager;

    public CoreAPI(PluginEnabler plugin) {
        super(plugin);

        CoreAPI.instance = this;

        this.serverManager = new CServerManager();
        this.apiPlayerManager = new CPlayerManager();
        this.moderatorManager = new CModeratorManager();
        this.sqlConnection = new CSQLConnection();

        File onlineModFile = new File(plugin.getPluginDataFolder() + File.separator + "onlinemod.json");
        File serverIDFile = new File(plugin.getPluginDataFolder() + File.separator + "serverid.json");
        File serverNameFile = new File(plugin.getPluginDataFolder() + File.separator + "servername.json");

        File sqlUserFile = new File(plugin.getPluginDataFolder() + File.separator + "service" + File.separator + "sql" + File.separator + "user.json");
        File sqlPassFile = new File(plugin.getPluginDataFolder() + File.separator + "service" + File.separator + "sql" + File.separator + "pass.json");
        File sqlIpFile = new File(plugin.getPluginDataFolder() + File.separator + "service" + File.separator + "sql" + File.separator + "ip.json");

        File redisPassFile = new File(plugin.getPluginDataFolder() + File.separator + "service" + File.separator + "redis" + File.separator + "pass.json");
        File redisUserFile = new File(plugin.getPluginDataFolder() + File.separator + "service" + File.separator + "redis" + File.separator + "user.json");
        File redisIpFile = new File(plugin.getPluginDataFolder() + File.separator + "service" + File.separator + "redis" + File.separator + "ip.json");

        this.serverName = GSONSaver.loadGSON(serverNameFile, String.class);
        this.onlineMod = GSONSaver.loadGSON(onlineModFile, Boolean.class);
        String serverID = GSONSaver.loadGSON(serverIDFile, String.class);

        String sqlUser = GSONSaver.loadGSON(sqlUserFile, String.class);
        String sqlPass = GSONSaver.loadGSON(sqlPassFile, String.class);
        String sqlIp = GSONSaver.loadGSON(sqlIpFile, String.class);

        String redisPass = GSONSaver.loadGSON(redisPassFile, String.class);
        String redisUser = GSONSaver.loadGSON(redisUserFile, String.class);
        String redisIp = GSONSaver.loadGSON(redisIpFile, String.class);

        if (onlineMod == null || serverName == null || sqlUser == null || sqlPass == null || redisPass == null || sqlIp == null || redisIp == null) {

            if (serverName == null) {
                GSONSaver.writeGSON(serverNameFile, "servername");
                return;
            }

            if (onlineMod == null) {
                GSONSaver.writeGSON(onlineModFile, false);
                return;
            }

            if (isOnlineMod()) {

                if (sqlUser == null)
                    GSONSaver.writeGSON(sqlUserFile, "userhere");

                if (sqlPass == null)
                    GSONSaver.writeGSON(sqlPassFile, "passhere");

                if (redisPass == null)
                    GSONSaver.writeGSON(redisPassFile, "passhere");

                if (redisUser == null)
                    GSONSaver.writeGSON(redisUserFile, "userhere");

                if (redisIp == null)
                    GSONSaver.writeGSON(redisIpFile, "127.0.0.1:6379");

                if (sqlIp == null)
                    GSONSaver.writeGSON(sqlIpFile, "127.0.0.1:3306");

                return;

            }

        }

        if (isOnlineMod()) {

            plugin.printLog(Level.INFO, "Connecting to db");

            this.sqlConnection.connect(new IpInfo(sqlIp), "hiroshi", sqlUser, sqlPass);
            this.manager = new CRedisManager(new IpInfo(redisIp), 0, redisUser.equals("null") ? null : redisUser, redisPass.equals("null") ? null : redisPass);

            if (!dataConnected()) {
                plugin.printLog(Level.SEVERE, "DataBase not connected");
                plugin.onAPILoadFail();
                return;
            }

        } else {
            this.sqlConnection = null;
            this.manager = null;
        }

        ServerType serverType = plugin.isVelocity() ? ServerType.VELOCITY : ServerType.HUB;
        Long serverLong = serverID != null ? Long.parseLong(serverID) : null;

        Optional<Server> server;
        if (isOnlineMod() && serverID != null)
            server = this.serverManager.getServer(serverID);
        else
            server = this.serverManager.createServer(serverType, serverLong, serverName, plugin.getServerIp(), plugin.getMaxPlayer());

        if (server.isEmpty()) {
            plugin.onAPILoadFail();
            return;
        }

        this.server = server.get();
        GSONSaver.writeGSON(serverIDFile, Long.valueOf(this.server.getServerID()).toString());

        plugin.printLog(Level.INFO, "Server id: " + this.server.getServerID());
        plugin.onAPIEnabled();

    }

    public static CoreAPI getInstance() {
        return instance;
    }

    @Override
    public Optional<RedisManager> getRedisManager() {
        return Optional.ofNullable(this.manager);
    }

    @Override
    public CPlayerManager getPlayerManager() {
        return this.apiPlayerManager;
    }

    @Override
    public CServerManager getServerManager() {
        return this.serverManager;
    }

    @Override
    public Server getServer() {
        return this.server;
    }

    @Override
    public String getServerName() {
        return this.serverName;
    }

    @Override
    public void shutdown() {

        if (!getPluginEnabler().isPluginEnabled())
            return;

        getPluginEnabler().onAPIDisabled();

        if (getPluginEnabler().isPluginEnabled())
            return;

        Server server = getServer();
        if (server != null)
            server.shutdown();

        getRedisManager().ifPresent(RedisManager::closeConnection);

        getSQLConnection().ifPresent(SQLConnection::closeConnection);

    }

    @Override
    public long getServerID() {
        return this.server.getServerID();
    }

    @Override
    public boolean dataConnected() {
        Optional<SQLConnection> sql = getSQLConnection();
        if (sql.isPresent())
            if (!sql.get().isConnected())
                return false;
        Optional<RedisManager> redis = getRedisManager();
        return redis.map(redisManager -> !redisManager.getRedissonClient().isShutdown() && !redisManager.getRedissonClient().isShuttingDown()).orElse(true);
    }

    @Override
    public CModeratorManager getModeratorManager() {
        return this.moderatorManager;
    }

    @Override
    public ServerType getServerType() {
        return this.getServer().getServerType();
    }

    @Override
    public boolean isOnlineMod() {
        return onlineMod;
    }

    @Override
    public Optional<SQLConnection> getSQLConnection() {
        return Optional.ofNullable(this.sqlConnection);
    }

}