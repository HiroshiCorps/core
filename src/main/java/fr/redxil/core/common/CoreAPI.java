/* Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 */

package fr.redxil.core.common;

import fr.redline.pms.utils.GSONSaver;
import fr.redline.pms.utils.IpInfo;
import fr.redxil.api.common.API;
import fr.redxil.api.common.PluginEnabler;
import fr.redxil.api.common.game.Game;
import fr.redxil.api.common.game.GameManager;
import fr.redxil.api.common.game.Host;
import fr.redxil.api.common.group.party.PartyManager;
import fr.redxil.api.common.group.team.TeamManager;
import fr.redxil.api.common.player.APIPlayerManager;
import fr.redxil.api.common.player.moderators.ModeratorManager;
import fr.redxil.api.common.redis.RedisManager;
import fr.redxil.api.common.server.Server;
import fr.redxil.api.common.server.ServerManager;
import fr.redxil.api.common.server.type.ServerType;
import fr.redxil.api.common.sql.SQLConnection;
import fr.redxil.core.common.game.CGameManager;
import fr.redxil.core.common.group.party.CPartyManager;
import fr.redxil.core.common.group.team.CTeamManager;
import fr.redxil.core.common.player.CPlayerManager;
import fr.redxil.core.common.player.moderator.CModeratorManager;
import fr.redxil.core.common.pmsListener.ShutdownOrderListener;
import fr.redxil.core.common.redis.CRedisManager;
import fr.redxil.core.common.server.CServerManager;
import fr.redxil.core.common.sql.CSQLConnection;

import java.io.File;

public class CoreAPI extends API {

    private final String serverName;
    private final CServerManager serverManager;
    private final CPlayerManager apiPlayerManager;
    private final CModeratorManager moderatorManager;
    private CRedisManager manager;
    private final CGameManager cGameManager;
    private final SQLConnection sqlConnection;
    private final PartyManager partyManager;
    private final CTeamManager cTeamManager;

    public CoreAPI(PluginEnabler plugin) {
        super(plugin);

        this.serverManager = new CServerManager();
        this.apiPlayerManager = new CPlayerManager();
        this.moderatorManager = new CModeratorManager();
        this.cGameManager = new CGameManager();
        this.partyManager = new CPartyManager();
        this.cTeamManager = new CTeamManager();
        this.sqlConnection = new CSQLConnection();

        File serverNameFile = new File(plugin.getPluginDataFolder() + File.separator + "servername.json");
        File sqlUserFile = new File(plugin.getPluginDataFolder() + File.separator + "sqlCredential" + File.separator + "sqlUser.json");
        File sqlPassFile = new File(plugin.getPluginDataFolder() + File.separator + "sqlCredential" + File.separator + "sqlPass.json");
        File redisPassFile = new File(plugin.getPluginDataFolder() + File.separator + "redisCredential" + File.separator + "redisPass.json");
        File redisUserFile = new File(plugin.getPluginDataFolder() + File.separator + "redisCredential" + File.separator + "redisUser.json");

        this.serverName = GSONSaver.loadGSON(serverNameFile, String.class);
        String sqlUser = GSONSaver.loadGSON(sqlUserFile, String.class);
        String sqlPass = GSONSaver.loadGSON(sqlPassFile, String.class);
        String redisPass = GSONSaver.loadGSON(redisPassFile, String.class);
        String redisUser = GSONSaver.loadGSON(redisUserFile, String.class);

        if (serverName == null || sqlUser == null || sqlPass == null || redisPass == null) {

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

            CoreAPI.setEnabled(false);
            return;

        }

        this.sqlConnection.connect(new IpInfo("127.0.0.1", 3306), "hiroshi", sqlUser, sqlPass);
        this.manager = new CRedisManager("127.0.0.1", "6379", 0, redisUser.equals("null") ? null : redisUser, redisPass.equals("null") ? null : redisPass);

        new ShutdownOrderListener();

        ServerType serverType;
        if (plugin.isVelocity())
            serverType = ServerType.VELOCITY;
        else {

            Game games = getGame();
            if (games != null) {
                if (games instanceof Host)
                    serverType = ServerType.HOST;
                else serverType = ServerType.GAME;
            } else serverType = ServerType.HUB;

        }

        if (!getServerManager().isServerExist(getServerName()))
            this.serverManager.initServer(serverType, getServerName(), plugin.getServerIp());

        CoreAPI.setEnabled(true);

    }

    public static CoreAPI getInstance() {
        return (CoreAPI) API.getInstance();
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
    public PartyManager getPartyManager() {
        return this.partyManager;
    }

    @Override
    public Server getServer() {
        return this.serverManager.getServer(getServerName());
    }

    @Override
    public void shutdown() {
        CoreAPI.setEnabled(false);
        Server server = getServer();
        if (server != null)
            server.shutdown();

        RedisManager rm = getRedisManager();
        if (rm != null)
            rm.closeConnection();
    }

    @Override
    public TeamManager getTeamManager() {
        return cTeamManager;
    }

    @Override
    public String getServerName() {
        return serverName;
    }

    @Override
    public ServerType getServerType() {
        return this.getServer().getServerType();
    }

    @Override
    public SQLConnection getSQLConnection() {
        return this.sqlConnection;
    }

    @Override
    public Host getHost() {
        return getGameManager().getHost(getServerName());
    }

    @Override
    public GameManager getGameManager() {
        return this.cGameManager;
    }

    @Override
    public boolean isHostServer() {
        return getHost() != null;
    }

    @Override
    public Game getGame() {
        return getGameManager().getGame(getServerName());
    }

    @Override
    public boolean isGameServer() {
        return getGameManager().isGameExist(getServerName());
    }

}