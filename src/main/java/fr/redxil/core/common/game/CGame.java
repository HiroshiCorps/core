/* Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 */

package fr.redxil.core.common.game;

import fr.redline.pms.pm.RedisPMManager;
import fr.redxil.api.common.API;
import fr.redxil.api.common.game.Game;
import fr.redxil.api.common.game.GameEnum;
import fr.redxil.api.common.game.GameState;
import fr.redxil.api.common.game.Host;
import fr.redxil.api.common.group.team.Team;
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.api.common.player.moderators.APIPlayerModerator;
import fr.redxil.api.common.redis.RedisManager;
import fr.redxil.api.paper.minigame.GameBuilder;
import fr.redxil.core.common.data.GameDataValue;
import fr.redxil.core.common.data.IDDataValue;
import fr.redxil.core.common.data.utils.DataType;
import fr.redxil.core.common.redis.IDGenerator;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class CGame implements Game {

    final String server;
    final long gameID;

    public CGame(long gameID) {
        this.server = API.getInstance().getRedisManager().getRedisString(GameDataValue.GAME_SERVER_REDIS.getString(null, gameID));
        this.gameID = gameID;
    }

    public CGame(String serverName) {
        this.server = serverName;
        this.gameID = (long) API.getInstance().getRedisManager().getRedisMap(GameDataValue.GAMEMAP_SERVER_REDIS.getString(null)).get(serverName);
    }

    public static Game initGame(String server, GameEnum gameEnum) {

        long gameID = IDGenerator.generateLONGID(IDDataValue.GAME);

        RedisManager redisManager = API.getInstance().getRedisManager();

        GameDataValue.clearRedisData(DataType.SERVER, server, gameID);

        redisManager.getRedisMap(GameDataValue.GAMEMAP_SERVER_REDIS.getString(null)).put(server, gameID);

        redisManager.setRedisString(GameDataValue.GAME_GAME_REDIS.getString(server, gameID), gameEnum.toString());
        redisManager.setRedisBoolean(GameDataValue.GAME_TEAM_REDIS.getString(server, gameID), !GameBuilder.getGameBuilder().getTeamManager().getTeamList().isEmpty());
        redisManager.setRedisString(GameDataValue.GAME_GAMESTATE_REDIS.getString(server, gameID), GameState.WAITING.getName());
        redisManager.setRedisString(GameDataValue.GAME_SERVER_REDIS.getString(server, gameID), server);
        redisManager.setRedisLong(GameDataValue.GAME_MINP_REDIS.getString(server, gameID), Integer.valueOf(gameEnum.getDefaultMinP()).longValue());
        redisManager.setRedisLong(GameDataValue.GAME_MAXP_REDIS.getString(server, gameID), Integer.valueOf(gameEnum.getDefaultMaxP()).longValue());
        redisManager.setRedisLong(GameDataValue.GAME_MAXPLSPEC_REDIS.getString(server, gameID), Integer.valueOf(gameEnum.getDefaultMaxNPSpec()).longValue());
        redisManager.setRedisString(GameDataValue.GAME_SUBGAME_REDIS.getString(server, gameID), gameEnum.name());
        redisManager.setRedisString(GameDataValue.GAME_MAP_REDIS.getString(server, gameID), "None");
        return new CGame(gameID);

    }

    @Override
    public void stop() {
        String name = getServerName();
        long id = getGameID();

        API.getInstance().getPluginEnabler().printLog(Level.FINE, "[Host] Clearing redis data");

        boolean host = isHostLinked();

        GameDataValue.clearRedisData(DataType.SERVER, name, id);

        API.getInstance().getRedisManager().getRedissonClient().getMap(GameDataValue.GAMEMAP_SERVER_REDIS.getString(null)).remove(name);
        if (host)
            API.getInstance().getRedisManager().getRedissonClient().getMap(GameDataValue.HOSTMAP_SERVER_REDIS.getString(null)).remove(name);
    }

    @Override
    public boolean canAccess(APIPlayer apiPlayer, boolean b) {

        if (b) {

            if (apiPlayer.getRank().isModeratorRank())
                return true;

            if (!getGame().isAllowPlSpec())
                return false;

            return getMaxPlayerSpec() > getPlayerSpectators().size();

        }

        return isGameState(GameState.WAITING) && getPlayers().size() < getMaxPlayer();
    }

    @Override
    public boolean joinGame(APIPlayer apiPlayer, boolean b) {
        if (!canAccess(apiPlayer, b)) return false;

        if (b) {
            if (apiPlayer.getRank().isModeratorRank())
                getModeratorSpectators().add(apiPlayer.getUUID());
            else
                getPlayerSpectators().add(apiPlayer.getUUID());
        } else {
            getPlayers().add(apiPlayer.getUUID());
        }
        apiPlayer.switchServer(getServerName());

        return true;
    }

    /*
     * Game Part
     */

    @Override
    public String getServerName() {
        return server;
    }

    @Override
    public int getMinPlayer() {
        return Long.valueOf(API.getInstance().getRedisManager().getRedisLong(GameDataValue.GAME_MINP_REDIS.getString(this))).intValue();
    }

    @Override
    public void setMinPlayer(int i) {
        API.getInstance().getRedisManager().setRedisLong(GameDataValue.GAME_MINP_REDIS.getString(this), Integer.valueOf(i).longValue());
    }

    @Override
    public int getMaxPlayer() {
        return Long.valueOf(API.getInstance().getRedisManager().getRedisLong(GameDataValue.GAME_MAXP_REDIS.getString(this))).intValue();
    }

    @Override
    public void setMaxPlayer(int i) {
        API.getInstance().getRedisManager().setRedisLong(GameDataValue.GAME_MAXP_REDIS.getString(this), Integer.valueOf(i).longValue());
    }

    @Override
    public int getMaxPlayerSpec() {
        return Long.valueOf(API.getInstance().getRedisManager().getRedisLong(GameDataValue.GAME_MAXPLSPEC_REDIS.getString(this))).intValue();
    }

    @Override
    public void setMaxPlayerSpec(int i) {
        API.getInstance().getRedisManager().setRedisLong(GameDataValue.GAME_MAXPLSPEC_REDIS.getString(this), Integer.valueOf(i).longValue());
    }

    @Override
    public List<UUID> getPlayers() {
        return API.getInstance().getRedisManager().getRedisList(GameDataValue.GAME_PLAYER_REDIS.getString(this));
    }

    @Override
    public List<UUID> getPlayerSpectators() {
        return API.getInstance().getRedisManager().getRedisList(GameDataValue.GAME_SPEC_PLAYER_REDIS.getString(this));
    }

    @Override
    public List<UUID> getModeratorSpectators() {
        return API.getInstance().getRedisManager().getRedisList(GameDataValue.GAME_SPEC_MODERATOR_REDIS.getString(this));
    }

    @Override
    public List<UUID> getInConnectPlayer() {
        return API.getInstance().getRedisManager().getRedisList(GameDataValue.GAME_INCOPLAYER_REDIS.getString(this));
    }

    @Override
    public boolean setSpectator(UUID s, boolean b) {
        if (b == isSpectator(s)) return false;
        if (b) {
            getPlayers().remove(s);
            getPlayerSpectators().add(s);
        } else {
            if (getModeratorSpectators().contains(s)) return false;
            getPlayers().add(s);
            getPlayerSpectators().remove(s);
        }
        return true;
    }

    @Override
    public void removeSettings(String s) {
        getSettingsMap().remove(s);
    }

    @Override
    public void setSettings(String s, Object o) {
        getSettingsMap().remove(s);
        getSettingsMap().put(s, o);
    }

    @Override
    public Map<String, Object> getSettingsMap() {
        return API.getInstance().getRedisManager().getRedisMap(GameDataValue.GAME_SETTINGS_REDIS.getString(this));
    }

    @Override
    public boolean isHostLinked() {
        return API.getInstance().getGameManager().isHostExist(getServerName());
    }

    @Override
    public boolean isAllowConnectServer(UUID name) {
        boolean authorized = isPlayer(name) || isSpectator(name);
        if (!authorized) {
            APIPlayer apiPlayer = API.getInstance().getPlayerManager().getPlayer(name);
            if (apiPlayer == null)
                return false;
            if (apiPlayer.getRank().isModeratorRank()) {
                joinGame(apiPlayer, true);
                return true;
            }
        }

        return authorized;
    }

    @Override
    public Host getHost() {
        return API.getInstance().getGameManager().getHost(getServerName());
    }

    @Override
    public GameState getGameState() {
        return GameState.getState(API.getInstance().getRedisManager().getRedisString(GameDataValue.GAME_GAMESTATE_REDIS.getString(this)));
    }

    @Override
    public void setGameState(GameState gameState) {
        API.getInstance().getRedisManager().setRedisString(GameDataValue.GAME_GAMESTATE_REDIS.getString(this), gameState.getName());
    }

    @Override
    public boolean isGameState(GameState... gameState) {
        GameState actState = getGameState();
        for (GameState gs : gameState)
            if (gs.getName().equals(actState.getName()))
                return true;
        return false;
    }

    @Override
    public GameEnum getGame() {
        return GameEnum.getStatus(API.getInstance().getRedisManager().getRedisString(GameDataValue.GAME_GAME_REDIS.getString(this)));
    }

    @Override
    public String getSubGames() {
        return API.getInstance().getRedisManager().getRedisString(GameDataValue.GAME_SUBGAME_REDIS.getString(this));
    }

    @Override
    public void setSubGames(String s) {
        API.getInstance().getRedisManager().setRedisString(GameDataValue.GAME_SUBGAME_REDIS.getString(this), s);
    }

    @Override
    public String getMap() {
        return API.getInstance().getRedisManager().getRedisString(GameDataValue.GAME_MAP_REDIS.getString(this));
    }

    @Override
    public void setMap(String map) {
        API.getInstance().getRedisManager().setRedisString(GameDataValue.GAME_MAP_REDIS.getString(this), map);
    }

    @Override
    public boolean hasTeam() {
        return API.getInstance().getRedisManager().getRedisBoolean(GameDataValue.GAME_TEAM_REDIS.getString(this));
    }

    @Override
    public void setHasTeam(boolean b) {
        API.getInstance().getRedisManager().setRedisBoolean(GameDataValue.GAME_TEAM_REDIS.getString(this), b);
    }

    @Override
    public long getGameID() {
        return gameID;
    }

    @Override
    public void forceStart(APIPlayerModerator APIPlayer) {
        RedisPMManager.sendRedissonPluginMessage(API.getInstance().getRedisManager().getRedissonClient(), "forceSTART", APIPlayer.getName());
    }

    @Override
    public void forceStopStart(APIPlayerModerator APIPlayer) {
        RedisPMManager.sendRedissonPluginMessage(API.getInstance().getRedisManager().getRedissonClient(), "forceSTOPSTART", APIPlayer.getName());
    }

    @Override
    public boolean forceEnd(APIPlayerModerator APIPlayer, String reason) {
        if (reason.contains("<split>")) return false;
        RedisPMManager.sendRedissonPluginMessage(API.getInstance().getRedisManager().getRedissonClient(), "forceEND", APIPlayer.getName() + "<split>" + reason);
        return true;
    }

    @Override
    public boolean forceWin(APIPlayerModerator APIPlayer, Team team, String reason) {
        if (reason.contains("<split>")) return false;
        RedisPMManager.sendRedissonPluginMessage(API.getInstance().getRedisManager().getRedissonClient(), "forceWIN", APIPlayer.getName() + "<split>" + team.getName() + "<split>" + reason);
        return true;
    }

    @Override
    public boolean isInConnectPlayer(UUID playerName) {
        return getInConnectPlayer().contains(playerName);
    }

    @Override
    public Object getSettings(String key) {
        return getSettingsMap().get(key);
    }

    @Override
    public boolean hasSettings(String key) {
        return getSettingsMap().containsKey(key);
    }

    @Override
    public boolean isPlayer(UUID playerName) {
        return getPlayers().contains(playerName);
    }

    @Override
    public boolean isSpectator(UUID playerName) {
        return getModeratorSpectators().contains(playerName) || getPlayerSpectators().contains(playerName);
    }

}
