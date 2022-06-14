/*
 *
 * Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 *
 */

package fr.redxil.core.common;

import fr.redline.pms.pm.RedisPMManager;
import fr.redline.pms.utils.GSONSaver;
import fr.redline.pms.utils.IpInfo;
import fr.redxil.api.common.API;
import fr.redxil.api.common.PluginEnabler;
import fr.redxil.api.common.player.rank.Rank;
import fr.redxil.api.common.redis.RedisManager;
import fr.redxil.api.common.server.Server;
import fr.redxil.api.common.server.ServerCreator;
import fr.redxil.api.common.server.type.ServerAccess;
import fr.redxil.api.common.server.type.ServerStatus;
import fr.xilitra.hiroshisav.enums.ServerType;
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
    private Long serverID;
    private final Boolean onlineMod;
    private final CServerManager serverManager;
    private final CPlayerManager apiPlayerManager;
    private final CModeratorManager moderatorManager;
    private CSQLConnection sqlConnection;
    private Server server;
    private final PluginEnabler pluginEnabler;

    private final boolean velocity;
    private CRedisManager manager;

    public CoreAPI(PluginEnabler plugin) {
        CoreAPI.instance = this;

        this.pluginEnabler = plugin;

        this.velocity = plugin.getServerType() == ServerType.VELOCITY;

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

        File serverAccessIP = new File(plugin.getPluginDataFolder() + File.separator + "serverip.json");

        this.serverName = GSONSaver.loadGSON(serverNameFile, String.class);
        this.onlineMod = GSONSaver.loadGSON(onlineModFile, Boolean.class);
        this.serverID = GSONSaver.loadGSON(serverIDFile, Long.class);

        String sqlUser = GSONSaver.loadGSON(sqlUserFile, String.class);
        String sqlPass = GSONSaver.loadGSON(sqlPassFile, String.class);
        String sqlIp = GSONSaver.loadGSON(sqlIpFile, String.class);

        String redisPass = GSONSaver.loadGSON(redisPassFile, String.class);
        String redisUser = GSONSaver.loadGSON(redisUserFile, String.class);
        String redisIp = GSONSaver.loadGSON(redisIpFile, String.class);

        IpInfo ipInfo;
        String gsonIP = GSONSaver.loadGSON(serverAccessIP, String.class);
        if(gsonIP == null)
            ipInfo = plugin.getServerIp();
        else ipInfo = new IpInfo(gsonIP);

        if (ipInfo == null || onlineMod == null || serverName == null || sqlUser == null || sqlPass == null || redisPass == null || sqlIp == null || redisIp == null) {

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

            assert sqlIp != null;
            this.sqlConnection.connect(new IpInfo(sqlIp), "hiroshi", sqlUser, sqlPass);
            assert redisIp != null && redisPass != null;
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

        Optional<Server> server;
        if (isOnlineMod() && serverID != null)
            server = this.serverManager.loadServer(serverID, serverName);
        else{
            server = this.serverManager.createServer(new ServerCreator() {
                @Override
                public ServerType getServerType() {
                    return plugin.getServerType();
                }

                @Override
                public String getServerName() {
                    return serverName;
                }

                @Override
                public String getServerMap() {
                    return plugin.getServerMap().orElse(null);
                }

                @Override
                public IpInfo getIpInfo() {
                    return ipInfo;
                }

                @Override
                public Integer getMaxPlayer() {
                    return plugin.getMaxPlayer();
                }

                @Override
                public ServerStatus getServerStatus() {
                    return ServerStatus.ONLINE;
                }

                @Override
                public boolean needGenerate() {
                    return false;
                }

                @Override
                public ServerAccess getServerAccess() {
                    if(plugin.getServerType() == ServerType.VELOCITY || plugin.getServerType() == ServerType.HUB)
                        return ServerAccess.OPEN;
                    else return ServerAccess.LIMITED;
                }

                @Override
                public Rank getRankAccess() {
                    return Rank.JOUEUR;
                }
            });

        }

        if (server.isEmpty()) {
            plugin.onAPILoadFail();
            return;
        }else{
            this.server = server.get();
            this.serverID = this.server.getServerID();
        }

        GSONSaver.writeGSON(serverIDFile, Long.valueOf(this.server.getServerID()).toString());

        plugin.printLog(Level.INFO, "Server id: " + this.server.getServerID());
        API.instance = this;
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
    public PluginEnabler getPluginEnabler() {
        return pluginEnabler;
    }

    @Override
    public void shutdown() {

        if (!getPluginEnabler().isPluginEnabled())
            return;

        getPluginEnabler().onAPIDisabled();

        API.getInstance().getRedisManager().ifPresent(redis ->
                RedisPMManager.sendRedissonPluginMessage(redis.getRedissonClient(), "onAPIDisabled", API.getInstance().getServerID()));

        Server server = getServer();
        if (server != null)
            server.shutdown();

        getRedisManager().ifPresent(RedisManager::closeConnection);

        getSQLConnection().ifPresent(SQLConnection::closeConnection);

    }

    @Override
    public long getServerID() {
        return this.serverID;
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
    public boolean isVelocity() {
        return velocity;
    }

    @Override
    public Optional<SQLConnection> getSQLConnection() {
        return Optional.ofNullable(this.sqlConnection);
    }

}