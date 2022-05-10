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
import fr.redxil.api.common.player.APIPlayerManager;
import fr.redxil.api.common.player.moderators.ModeratorManager;
import fr.redxil.api.common.redis.RedisManager;
import fr.redxil.api.common.server.Server;
import fr.redxil.api.common.server.ServerManager;
import fr.redxil.api.common.server.type.ServerType;
import fr.redxil.api.common.sql.SQLConnection;
import fr.redxil.core.common.player.CPlayerManager;
import fr.redxil.core.common.player.moderator.CModeratorManager;
import fr.redxil.core.common.redis.CRedisManager;
import fr.redxil.core.common.server.CServerManager;
import fr.redxil.core.common.sql.CSQLConnection;

import java.io.File;
import java.util.logging.Level;

public class CoreAPI extends API {

    private final String serverName;
    private final CServerManager serverManager;
    private final CPlayerManager apiPlayerManager;
    private final CModeratorManager moderatorManager;
    private final SQLConnection sqlConnection;
    private Server server;
    private CRedisManager manager;

    public CoreAPI(PluginEnabler plugin) {
        super(plugin);

        this.serverManager = new CServerManager();
        this.apiPlayerManager = new CPlayerManager();
        this.moderatorManager = new CModeratorManager();
        this.sqlConnection = new CSQLConnection();

        File serverIDFile = new File(plugin.getPluginDataFolder() + File.separator + "serverid.json");
        File serverNameFile = new File(plugin.getPluginDataFolder() + File.separator + "servername.json");

        File sqlUserFile = new File(plugin.getPluginDataFolder() + File.separator + "service" + File.separator + "sql" + File.separator + "user.json");
        File sqlPassFile = new File(plugin.getPluginDataFolder() + File.separator + "service" + File.separator + "sql" + File.separator + "pass.json");
        File sqlIpFile = new File(plugin.getPluginDataFolder() + File.separator + "service" + File.separator + "sql" + File.separator + "ip.json");

        File redisPassFile = new File(plugin.getPluginDataFolder() + File.separator + "service" + File.separator + "redis" + File.separator + "pass.json");
        File redisUserFile = new File(plugin.getPluginDataFolder() + File.separator + "service" + File.separator + "redis" + File.separator + "user.json");
        File redisIpFile = new File(plugin.getPluginDataFolder() + File.separator + "service" + File.separator + "redis" + File.separator + "ip.json");

        this.serverName = GSONSaver.loadGSON(serverNameFile, String.class);
        String serverID = GSONSaver.loadGSON(serverIDFile, String.class);

        String sqlUser = GSONSaver.loadGSON(sqlUserFile, String.class);
        String sqlPass = GSONSaver.loadGSON(sqlPassFile, String.class);
        String sqlIp = GSONSaver.loadGSON(sqlIpFile, String.class);

        String redisPass = GSONSaver.loadGSON(redisPassFile, String.class);
        String redisUser = GSONSaver.loadGSON(redisUserFile, String.class);
        String redisIp = GSONSaver.loadGSON(redisIpFile, String.class);

        if (serverName == null || sqlUser == null || sqlPass == null || redisPass == null || sqlIp == null || redisIp == null) {

            if (serverName == null)
                GSONSaver.writeGSON(serverNameFile, "servername");

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

        plugin.printLog(Level.INFO, "Connecting to db");

        this.sqlConnection.connect(new IpInfo(sqlIp), "hiroshi", sqlUser, sqlPass);
        this.manager = new CRedisManager(new IpInfo(redisIp), 0, redisUser.equals("null") ? null : redisUser, redisPass.equals("null") ? null : redisPass);

        if (!dataConnected()) {
            plugin.printLog(Level.SEVERE, "DataBase not connected");
            plugin.onAPILoadFail();
            return;
        }

        ServerType serverType = plugin.isVelocity() ? ServerType.VELOCITY : ServerType.HUB;

        if (serverID == null) {
            plugin.printLog(Level.FINE, "Generating new Server on db");
            this.server = this.serverManager.createServer(serverType, serverName, plugin.getServerIp(), plugin.getMaxPlayer());
            if (this.server == null) {
                plugin.printLog(Level.FINE, "Error on generating server");
                plugin.onAPILoadFail();
                return;
            }
            GSONSaver.writeGSON(serverIDFile, Long.valueOf(this.server.getServerID()).toString());
        } else {
            plugin.printLog(Level.FINE, "Loading server with ID: " + serverID);
            this.server = getServerManager().loadServer(serverType, Long.parseLong(serverID), plugin.getServerIp());
            if (this.server == null) {
                plugin.printLog(Level.FINE, "Error on generating server");
                plugin.onAPILoadFail();
                return;
            }
            this.server.setServerName(serverName);
        }

        plugin.printLog(Level.INFO, "Server id: " + this.server.getServerID());
        plugin.onAPIEnabled();

    }

    @Override
    public CRedisManager getRedisManager() {
        return this.manager;
    }

    @Override
    public APIPlayerManager getPlayerManager() {
        return this.apiPlayerManager;
    }

    @Override
    public ServerManager getServerManager() {
        return this.serverManager;
    }

    @Override
    public ModeratorManager getModeratorManager() {
        return this.moderatorManager;
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

        RedisManager rm = getRedisManager();
        if (rm != null)
            rm.closeConnection();

        getSQLConnection().closeConnection();

    }

    @Override
    public long getServerID() {
        return this.server.getServerID();
    }

    @Override
    public boolean dataConnected() {
        return this.sqlConnection.isConnected() && !this.getRedisManager().getRedissonClient().isShuttingDown() && !this.getRedisManager().getRedissonClient().isShutdown();
    }

    @Override
    public ServerType getServerType() {
        return this.getServer().getServerType();
    }

    @Override
    public SQLConnection getSQLConnection() {
        return this.sqlConnection;
    }

}