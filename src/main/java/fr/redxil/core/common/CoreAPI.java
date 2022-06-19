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
import fr.redxil.api.common.APIEnabler;
import fr.redxil.api.common.APILoadError;
import fr.redxil.api.common.APIPhaseInit;
import fr.redxil.api.common.game.GameManager;
import fr.redxil.api.common.group.party.PartyManager;
import fr.redxil.api.common.group.team.TeamManager;
import fr.redxil.api.common.server.Server;
import fr.redxil.api.common.server.type.ServerStatus;
import fr.redxil.core.common.game.CGameManager;
import fr.redxil.core.common.group.party.CPartyManager;
import fr.redxil.core.common.group.team.CTeamManager;
import fr.redxil.core.common.player.CPlayerManager;
import fr.redxil.core.common.player.moderator.CModeratorManager;
import fr.redxil.core.common.redis.RedisManager;
import fr.redxil.core.common.server.CServerManager;
import fr.redxil.core.common.sql.SQLConnection;
import fr.xilitra.hiroshisav.enums.ServerType;

import java.io.File;
import java.util.HashMap;
import java.util.Optional;
import java.util.logging.Level;

public class CoreAPI extends API {

    private static CoreAPI instance;
    private String serverName;
    private IpInfo connectIpInfo;
    private Boolean onlineMod;
    private CServerManager serverManager;
    private CPlayerManager apiPlayerManager;
    private CModeratorManager moderatorManager;
    private PartyManager partyManager;
    private GameManager gameManager;
    private APIEnabler apiEnabler;
    private final HashMap<Long, TeamManager> mapManager = new HashMap<>();
    private Long serverID;
    private SQLConnection sqlConnection = null;
    private Server server;
    private RedisManager manager = null;

    public CoreAPI() {

        CoreAPI.instance = this;
        API.instance = this;

    }

    @Override
    public IpInfo getConnectIpInfo() {
        return connectIpInfo;
    }

    public static CoreAPI getInstance() {
        return instance;
    }

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
    public APIEnabler getAPIEnabler() {
        return apiEnabler;
    }

    @Override
    public void shutdown() {

        if (!getAPIEnabler().isPluginEnabled())
            return;

        API.enabled = false;

        getAPIEnabler().onAPIDisabled();

        CoreAPI.getInstance().getRedisManager().ifPresent(redis ->
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
    public CModeratorManager getModeratorManager() {
        return this.moderatorManager;
    }

    @Override
    public boolean isOnlineMod() {
        return onlineMod;
    }

    @Override
    public boolean isVelocity() {
        return getAPIEnabler().getServerInfo().getServerType() == ServerType.VELOCITY;
    }

    @Override
    public PartyManager getPartyManager() {
        return partyManager;
    }

    @Override
    public GameManager getGameManager() {
        return gameManager;
    }

    @Override
    public TeamManager getTeamManager(Long aLong) {
        if (isOnlineMod())
            return new CTeamManager(aLong);
        else {
            if (mapManager.containsKey(aLong))
                return mapManager.get(aLong);
            else {
                TeamManager teamManager = new CTeamManager(aLong);
                mapManager.put(aLong, teamManager);
                return teamManager;
            }
        }
    }

    public void loadServerInfo() {
        File onlineModFile = new File(getAPIEnabler().getPluginDataFolder() + File.separator + "onlinemod.json");
        File serverIDFile = new File(getAPIEnabler().getPluginDataFolder() + File.separator + "serverid.json");
        File serverNameFile = new File(getAPIEnabler().getPluginDataFolder() + File.separator + "servername.json");

        File serverAccessIP = new File(getAPIEnabler().getPluginDataFolder() + File.separator + "serverip.json");

        this.serverName = GSONSaver.loadGSON(serverNameFile, String.class);
        this.onlineMod = GSONSaver.loadGSON(onlineModFile, Boolean.class);
        this.serverID = GSONSaver.loadGSON(serverIDFile, Long.class);

        String gsonIP = GSONSaver.loadGSON(serverAccessIP, String.class);
        if (gsonIP != null)
            this.connectIpInfo = new IpInfo(gsonIP);
        else this.connectIpInfo = null;

        if (connectIpInfo == null || onlineMod == null || serverName == null) {

            if (connectIpInfo == null)
                GSONSaver.writeGSON(serverAccessIP, "127.0.0.1:25565");

            if (serverName == null)
                GSONSaver.writeGSON(serverNameFile, "servername");

            if (onlineMod == null)
                GSONSaver.writeGSON(onlineModFile, false);

            getAPIEnabler().onAPILoadFail(APIPhaseInit.PART_1, APILoadError.SERVER_INFO_MISSING);
            return;

        }
        getAPIEnabler().onAPIInitPhaseEnded(APIPhaseInit.PART_1);
    }

    public void loadDB() {
        if (!isOnlineMod())
            return;

        File sqlUserFile = new File(getAPIEnabler().getPluginDataFolder() + File.separator + "service" + File.separator + "sql" + File.separator + "user.json");
        File sqlPassFile = new File(getAPIEnabler().getPluginDataFolder() + File.separator + "service" + File.separator + "sql" + File.separator + "pass.json");
        File sqlIpFile = new File(getAPIEnabler().getPluginDataFolder() + File.separator + "service" + File.separator + "sql" + File.separator + "ip.json");

        File redisPassFile = new File(getAPIEnabler().getPluginDataFolder() + File.separator + "service" + File.separator + "redis" + File.separator + "pass.json");
        File redisUserFile = new File(getAPIEnabler().getPluginDataFolder() + File.separator + "service" + File.separator + "redis" + File.separator + "user.json");
        File redisIpFile = new File(getAPIEnabler().getPluginDataFolder() + File.separator + "service" + File.separator + "redis" + File.separator + "ip.json");

        String sqlUser = GSONSaver.loadGSON(sqlUserFile, String.class);
        String sqlPass = GSONSaver.loadGSON(sqlPassFile, String.class);
        String sqlIp = GSONSaver.loadGSON(sqlIpFile, String.class);

        String redisPass = GSONSaver.loadGSON(redisPassFile, String.class);
        String redisUser = GSONSaver.loadGSON(redisUserFile, String.class);
        String redisIp = GSONSaver.loadGSON(redisIpFile, String.class);

        if (sqlUser == null || sqlPass == null || sqlIp == null) {

            if (sqlUser == null) {
                GSONSaver.writeGSON(sqlUserFile, "userhere");
            }

            if (sqlPass == null) {
                GSONSaver.writeGSON(sqlPassFile, "passhere");
            }

            if (sqlIp == null) {
                GSONSaver.writeGSON(sqlIpFile, "127.0.0.1:3306");
            }

            getAPIEnabler().onAPILoadFail(APIPhaseInit.PART_2, APILoadError.SQL_INFO_MISSING);
            return;

        }

        if (redisPass == null || redisIp == null || redisUser == null) {

            if (redisPass == null)
                GSONSaver.writeGSON(redisPassFile, "passhere");

            if (redisUser == null)
                GSONSaver.writeGSON(redisUserFile, "userhere");

            if (redisIp == null)
                GSONSaver.writeGSON(redisIpFile, "127.0.0.1:6379");

            getAPIEnabler().onAPILoadFail(APIPhaseInit.PART_2, APILoadError.REDIS_INFO_MISSING);
            return;

        }

        getAPIEnabler().printLog(Level.INFO, "Connecting to db");

        this.sqlConnection = new SQLConnection();
        this.sqlConnection.connect(new IpInfo(sqlIp), "hiroshi", sqlUser, sqlPass);
        if (!this.sqlConnection.isConnected()) {
            this.sqlConnection = null;
            getAPIEnabler().onAPILoadFail(APIPhaseInit.PART_2, APILoadError.SQL_CONNECT_ERROR);
            return;
        }

        this.manager = new RedisManager(new IpInfo(redisIp), 0, redisUser.equals("null") ? null : redisUser, redisPass.equals("null") ? null : redisPass);
        if (this.manager.getRedissonClient().isShutdown() || this.manager.getRedissonClient().isShuttingDown()) {
            this.manager = null;
            this.sqlConnection.closeConnection();
            getAPIEnabler().onAPILoadFail(APIPhaseInit.PART_2, APILoadError.REDIS_CONNECT_ERROR);
            return;
        }

        this.serverManager = new CServerManager();
        this.apiPlayerManager = new CPlayerManager();
        this.moderatorManager = new CModeratorManager();
        this.partyManager = new CPartyManager();
        this.gameManager = new CGameManager();

        Optional<Server> server;
        if (serverID != null) {
            server = this.serverManager.loadServer(serverID);
            server.ifPresent(apiServer -> {
                apiServer.setServerStatus(ServerStatus.ONLINE);
                apiServer.setServerIP(getConnectIpInfo());
            });
        } else
            server = this.serverManager.createServer(getAPIEnabler().getServerInfo());

        if (server.isEmpty()) {
            if (serverID == null)
                getAPIEnabler().onAPILoadFail(APIPhaseInit.PART_2, APILoadError.CREATE_SERVER_ERROR);
            else getAPIEnabler().onAPILoadFail(APIPhaseInit.PART_2, APILoadError.LOAD_SERVER_ERROR);
            return;
        } else {
            this.server = server.get();
            this.serverID = this.server.getServerID();
        }

        GSONSaver.writeGSON(new File(getAPIEnabler().getPluginDataFolder() + File.separator + "serverid.json"), Long.valueOf(this.server.getServerID()).toString());

        getAPIEnabler().printLog(Level.INFO, "Server id: " + this.server.getServerID());

        CoreAPI.getInstance().getRedisManager().ifPresent(redis ->
                RedisPMManager.sendRedissonPluginMessage(redis.getRedissonClient(), "onAPIEnabled", this.getServerID()));

        API.enabled = true;
        getAPIEnabler().onAPIInitPhaseEnded(APIPhaseInit.PART_2);
        getAPIEnabler().onAPIEnabled();

    }

    @Override
    public void initPhase(APIPhaseInit apiPhaseInit, APIEnabler apiEnabler) {
        this.apiEnabler = apiEnabler;
        if (apiPhaseInit == APIPhaseInit.PART_2) {
            if (isOnlineMod())
                loadDB();
            else {
                this.serverManager = new CServerManager();
                this.apiPlayerManager = new CPlayerManager();
                this.moderatorManager = new CModeratorManager();
                this.partyManager = new CPartyManager();
                this.gameManager = new CGameManager();
                API.enabled = true;
                getAPIEnabler().onAPIInitPhaseEnded(APIPhaseInit.PART_2);
                getAPIEnabler().onAPIEnabled();
            }
        } else loadServerInfo();
    }


    public Optional<SQLConnection> getSQLConnection() {
        return Optional.ofNullable(this.sqlConnection);
    }

}