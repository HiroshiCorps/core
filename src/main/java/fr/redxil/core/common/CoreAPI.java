/* Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 */

package fr.redxil.core.common;

import fr.redline.pms.connect.linker.SocketGestion;
import fr.redline.pms.utils.GSONSaver;
import fr.redline.pms.utils.IpInfo;
import fr.redxil.api.common.API;
import fr.redxil.api.common.PluginEnabler;
import fr.redxil.api.common.game.Games;
import fr.redxil.api.common.game.GamesManager;
import fr.redxil.api.common.game.Hosts;
import fr.redxil.api.common.game.team.TeamManager;
import fr.redxil.api.common.party.PartyManager;
import fr.redxil.api.common.player.APIOfflinePlayer;
import fr.redxil.api.common.player.APIPlayerManager;
import fr.redxil.api.common.player.moderators.ModeratorManager;
import fr.redxil.api.common.player.nick.NickGestion;
import fr.redxil.api.common.redis.RedisManager;
import fr.redxil.api.common.server.Server;
import fr.redxil.api.common.server.ServerManager;
import fr.redxil.api.common.server.type.ServerType;
import fr.redxil.api.common.sql.SQLConnection;
import fr.redxil.core.common.data.PlayerDataValue;
import fr.redxil.core.common.game.CGameManager;
import fr.redxil.core.common.game.team.CTeamManager;
import fr.redxil.core.common.party.CPartyManager;
import fr.redxil.core.common.player.CNickGestion;
import fr.redxil.core.common.player.CPlayerManager;
import fr.redxil.core.common.player.moderator.CModeratorManager;
import fr.redxil.core.common.pmsListener.ShutdownOrderListener;
import fr.redxil.core.common.redis.CRedisManager;
import fr.redxil.core.common.server.CServerManager;
import fr.redxil.core.common.sql.CSQLConnection;

import java.io.File;
import java.util.UUID;

public class CoreAPI extends API {

    private final ServerAccessEnum sea;
    private CServerManager serverManager;
    private CPlayerManager apiPlayerManager;
    private CModeratorManager moderatorManager;
    private CNickGestion nickGestion;
    private CRedisManager manager;
    private CGameManager cGameManager;
    private SQLConnection sqlConnection;
    private PartyManager partyManager;
    private CTeamManager cTeamManager;

    public CoreAPI(PluginEnabler plugin, ServerAccessEnum sea) {
        super(plugin);

        this.sea = sea;

        File sqlUserFile = new File(plugin.getPluginDataFolder() + File.separator + "sqlCredential" + File.separator + "sqlUser.json");
        File sqlPassFile = new File(plugin.getPluginDataFolder() + File.separator + "sqlCredential" + File.separator + "sqlPass.json");
        File redisPassFile = new File(plugin.getPluginDataFolder() + File.separator + "redisCredential" + File.separator + "redisPass.json");
        File redisUserFile = new File(plugin.getPluginDataFolder() + File.separator + "redisCredential" + File.separator + "redisUser.json");

        String sqlUser = GSONSaver.loadGSON(sqlUserFile, String.class);
        String sqlPass = GSONSaver.loadGSON(sqlPassFile, String.class);
        String redisPass = GSONSaver.loadGSON(redisPassFile, String.class);
        String redisUser = GSONSaver.loadGSON(redisUserFile, String.class);

        if (sqlUser == null || sqlPass == null || redisPass == null) {

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

        this.sqlConnection = new CSQLConnection();
        this.sqlConnection.connect(new IpInfo("127.0.0.1", 3306), "hiroshi", sqlUser, sqlPass);
        this.manager = new CRedisManager("127.0.0.1", "6379", 0, redisUser.equals("null") ? null : redisUser, redisPass.equals("null") ? null : redisPass);

        this.serverManager = new CServerManager();
        this.apiPlayerManager = new CPlayerManager();
        this.moderatorManager = new CModeratorManager();
        this.nickGestion = new CNickGestion();
        this.cGameManager = new CGameManager();
        this.partyManager = new CPartyManager();
        this.cTeamManager = new CTeamManager();
        new ShutdownOrderListener();

        ServerType serverType;
        if (plugin.isBungee())
            serverType = ServerType.BUNGEE;
        else {

            Games games = getGame();
            if (games != null) {
                if (games instanceof Hosts)
                    serverType = ServerType.HOST;
                else serverType = ServerType.GAME;
            } else serverType = ServerType.HUB;

        }

        if (!getServerManager().isServerExist(plugin.getServerName()))
            this.serverManager.initServer(serverType, plugin.getServerName(), plugin.getServerIp());

        CoreAPI.setEnabled(true);

    }

    public static CoreAPI getInstance() {
        return (CoreAPI) API.get();
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
        if (this.serverManager == null) return null;
        return this.serverManager.getServer(getPluginEnabler().getServerName());
    }

    @Override
    public NickGestion getNickGestion() {
        return this.nickGestion;
    }

    @Override
    public void shutdown() {
        CoreAPI.setEnabled(false);
        SocketGestion.closeAllConnection();
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
    public ServerType getServerType() {
        return this.getServer().getServerType();
    }

    @Override
    public SQLConnection getSQLConnection() {
        return this.sqlConnection;
    }

    @Override
    public Hosts getHost() {
        if (isHostServer() && isSpigot())
            return getGamesManager().getHost(this.getPluginEnabler().getServerName());

        return null;
    }

    @Override
    public GamesManager getGamesManager() {
        return this.cGameManager;
    }

    @Override
    public boolean isHostServer() {
        Server server = this.getServer();
        if (server == null) return false;
        return server.getServerType() == ServerType.HOST;
    }

    @Override
    public Games getGame() {
        return getGamesManager().getGame(getPluginEnabler().getServerName());
    }

    @Override
    public boolean isGameServer() {
        return getGamesManager().isGameExist(getPluginEnabler().getServerName());
    }

    public ServerAccessEnum getServerAccessEnum() {
        return this.sea;
    }

    public String getDataForGetAndSet(APIOfflinePlayer aop) {
        return getServerAccessEnum() == ServerAccessEnum.CRACK ? aop.getName() : aop.getUUID().toString();
    }

    public String getDataForGetAndSet(String name, UUID uuid) {
        return getServerAccessEnum() == ServerAccessEnum.CRACK ? name : uuid.toString();
    }

    public enum ServerAccessEnum {

        PRENIUM(PlayerDataValue.PLAYER_UUID_SQL),
        CRACK(PlayerDataValue.PLAYER_NAME_SQL);

        final PlayerDataValue pdv;

        ServerAccessEnum(PlayerDataValue playerDataValue) {
            this.pdv = playerDataValue;
        }

        public PlayerDataValue getPdv() {
            return pdv;
        }
    }

}