/*
 *
 * Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 *
 */

package fr.redxil.core.common.game;

import fr.redline.pms.pm.RedisPMManager;
import fr.redxil.api.common.API;
import fr.redxil.api.common.game.Game;
import fr.redxil.api.common.game.Host;
import fr.redxil.api.common.game.utils.GameEnum;
import fr.redxil.api.common.game.utils.GameState;
import fr.redxil.api.common.group.team.Team;
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.api.common.player.moderators.APIPlayerModerator;
import fr.redxil.api.common.redis.RedisManager;
import fr.redxil.core.common.data.GameDataRedis;
import fr.redxil.core.common.data.IDDataValue;
import fr.redxil.core.common.data.TeamDataValue;
import fr.redxil.core.common.data.utils.DataType;
import fr.redxil.core.common.redis.IDGenerator;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class CGame implements Game {

    final long gameID;

    public CGame(long id, boolean serverID) {
        this.gameID = serverID ? (long) API.getInstance().getRedisManager().getRedisMap(GameDataRedis.MAP_SERVER_REDIS.getString()).get(id) : id;
    }

    public static Game createGame(long serverID, GameEnum gameEnum) {

        long gameID = IDGenerator.generateLONGID(IDDataValue.GAME);

        RedisManager redisManager = API.getInstance().getRedisManager();

        GameDataRedis.clearRedisData(DataType.SERVER, gameID);

        redisManager.getRedisMap(GameDataRedis.MAP_SERVER_REDIS.getString(gameID)).put(serverID, gameID);
        redisManager.setRedisLong(GameDataRedis.GAME_SERVER_REDIS.getString(gameID), serverID);
        redisManager.setRedisString(GameDataRedis.GAME_GAME_REDIS.getString(gameID), gameEnum.toString());
        redisManager.setRedisString(GameDataRedis.GAME_GAMESTATE_REDIS.getString(gameID), GameState.WAITING.getName());
        redisManager.setRedisLong(GameDataRedis.GAME_MINP_REDIS.getString(gameID), Integer.valueOf(gameEnum.getDefaultMinP()).longValue());
        redisManager.setRedisLong(GameDataRedis.GAME_MAXP_REDIS.getString(gameID), Integer.valueOf(gameEnum.getDefaultMaxP()).longValue());
        redisManager.setRedisLong(GameDataRedis.GAME_MAXPLSPEC_REDIS.getString(gameID), Integer.valueOf(gameEnum.getDefaultMaxNPSpec()).longValue());
        redisManager.setRedisString(GameDataRedis.GAME_SUBGAME_REDIS.getString(gameID), gameEnum.name());
        redisManager.setRedisString(GameDataRedis.GAME_MAP_REDIS.getString(gameID), "None");
        redisManager.getRedisList(GameDataRedis.LIST_GAME_REDIS.getString()).add(gameID);
        return new CGame(gameID, false);

    }

    @Override
    public void clearData() {

        API.getInstance().getPluginEnabler().printLog(Level.FINE, "[Host] Clearing redis data");

        TeamDataValue.clearRedisData(DataType.TEAM, this);

        Long serverID = getServerID();
        long gameID = getGameID();

        GameDataRedis.clearRedisData(DataType.SERVER, gameID);

        API.getInstance().getRedisManager().getRedissonClient().getMap(GameDataRedis.MAP_SERVER_REDIS.getString()).remove(serverID);
        API.getInstance().getRedisManager().getRedissonClient().getList(GameDataRedis.LIST_GAME_REDIS.getString()).remove(gameID);
        API.getInstance().getRedisManager().getRedissonClient().getList(GameDataRedis.LIST_HOST_REDIS.getString()).remove(gameID);
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

        return isGameState(GameState.WAITING) && getConnectedPlayers().size() < getMaxPlayer();
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
            getConnectedPlayers().add(apiPlayer.getUUID());
        }
        apiPlayer.switchServer(getServerID());

        return true;
    }

    /*
     * Game Part
     */

    @Override
    public long getServerID() {
        return API.getInstance().getRedisManager().getRedisLong(GameDataRedis.GAME_SERVER_REDIS.getString(gameID));
    }

    @Override
    public int getMinPlayer() {
        return Long.valueOf(API.getInstance().getRedisManager().getRedisLong(GameDataRedis.GAME_MINP_REDIS.getString(this))).intValue();
    }

    @Override
    public void setMinPlayer(int i) {
        API.getInstance().getRedisManager().setRedisLong(GameDataRedis.GAME_MINP_REDIS.getString(this), Integer.valueOf(i).longValue());
    }

    @Override
    public int getMaxPlayer() {
        return Long.valueOf(API.getInstance().getRedisManager().getRedisLong(GameDataRedis.GAME_MAXP_REDIS.getString(this))).intValue();
    }

    @Override
    public void setMaxPlayer(int i) {
        API.getInstance().getRedisManager().setRedisLong(GameDataRedis.GAME_MAXP_REDIS.getString(this), Integer.valueOf(i).longValue());
    }

    @Override
    public int getMaxPlayerSpec() {
        return Long.valueOf(API.getInstance().getRedisManager().getRedisLong(GameDataRedis.GAME_MAXPLSPEC_REDIS.getString(this))).intValue();
    }

    @Override
    public void setMaxPlayerSpec(int i) {
        API.getInstance().getRedisManager().setRedisLong(GameDataRedis.GAME_MAXPLSPEC_REDIS.getString(this), Integer.valueOf(i).longValue());
    }

    @Override
    public List<UUID> getConnectedPlayers() {
        return API.getInstance().getRedisManager().getRedisList(GameDataRedis.GAME_PLAYER_REDIS.getString(this));
    }

    @Override
    public List<UUID> getPlayerSpectators() {
        return API.getInstance().getRedisManager().getRedisList(GameDataRedis.GAME_SPEC_PLAYER_REDIS.getString(this));
    }

    @Override
    public List<UUID> getModeratorSpectators() {
        return API.getInstance().getRedisManager().getRedisList(GameDataRedis.GAME_SPEC_MODERATOR_REDIS.getString(this));
    }

    @Override
    public List<UUID> getInConnectPlayer() {
        return API.getInstance().getRedisManager().getRedisList(GameDataRedis.GAME_INCOPLAYER_REDIS.getString(this));
    }

    @Override
    public boolean setSpectator(UUID s, boolean b) {
        if (b == isSpectator(s)) return false;
        if (b) {
            getConnectedPlayers().remove(s);
            getPlayerSpectators().add(s);
        } else {
            if (getModeratorSpectators().contains(s)) return false;
            getConnectedPlayers().add(s);
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
        return API.getInstance().getRedisManager().getRedisMap(GameDataRedis.GAME_SETTINGS_REDIS.getString(this));
    }

    @Override
    public boolean isHostLinked() {
        return API.getInstance().getGameManager().isHostExist(getGameID());
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
        return API.getInstance().getGameManager().getHost(getGameID());
    }

    @Override
    public GameState getGameState() {
        return GameState.getState(API.getInstance().getRedisManager().getRedisString(GameDataRedis.GAME_GAMESTATE_REDIS.getString(this)));
    }

    @Override
    public void setGameState(GameState gameState) {
        API.getInstance().getRedisManager().setRedisString(GameDataRedis.GAME_GAMESTATE_REDIS.getString(this), gameState.getName());
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
        return GameEnum.getStatus(API.getInstance().getRedisManager().getRedisString(GameDataRedis.GAME_GAME_REDIS.getString(this)));
    }

    @Override
    public String getSubGames() {
        return API.getInstance().getRedisManager().getRedisString(GameDataRedis.GAME_SUBGAME_REDIS.getString(this));
    }

    @Override
    public void setSubGames(String s) {
        API.getInstance().getRedisManager().setRedisString(GameDataRedis.GAME_SUBGAME_REDIS.getString(this), s);
    }

    @Override
    public String getWorldName() {
        return API.getInstance().getRedisManager().getRedisString(GameDataRedis.GAME_MAP_REDIS.getString(this));
    }

    @Override
    public void setWorldName(String map) {
        API.getInstance().getRedisManager().setRedisString(GameDataRedis.GAME_MAP_REDIS.getString(this), map);
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
    public boolean forceEnd(APIPlayerModerator APIPlayer, String reason) {
        if (reason.contains("<split>")) return false;
        RedisPMManager.sendRedissonPluginMessage(API.getInstance().getRedisManager().getRedissonClient(), "forceEND", APIPlayer.getName() + "<split>" + reason);
        return true;
    }

    @Override
    public boolean forceWin(APIPlayerModerator APIPlayer, Team team, String reason) {
        if (reason.contains("<split>")) return false;
        RedisPMManager.sendRedissonPluginMessage(API.getInstance().getRedisManager().getRedissonClient(), "forceWIN", APIPlayer.getName() + "<split>" + team.getTeamName() + "<split>" + reason);
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
        return getConnectedPlayers().contains(playerName);
    }

    @Override
    public boolean isSpectator(UUID playerName) {
        return getModeratorSpectators().contains(playerName) || getPlayerSpectators().contains(playerName);
    }

}
