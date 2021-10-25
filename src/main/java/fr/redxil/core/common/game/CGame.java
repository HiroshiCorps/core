/* Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 */

package fr.redxil.core.common.game;

import fr.redline.pms.connect.linker.pm.PMManager;
import fr.redxil.api.common.API;
import fr.redxil.api.common.game.GameEnum;
import fr.redxil.api.common.game.GameState;
import fr.redxil.api.common.game.Games;
import fr.redxil.api.common.game.Hosts;
import fr.redxil.api.common.moderators.APIPlayerModerator;
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.api.common.redis.RedisManager;
import fr.redxil.api.common.team.Team;
import fr.redxil.api.spigot.minigame.GameBuilder;
import fr.redxil.core.common.CoreAPI;
import fr.redxil.core.common.data.GameDataValue;
import fr.redxil.core.common.data.IDDataValue;
import fr.redxil.core.common.data.utils.DataType;
import fr.redxil.core.common.redis.IDGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CGame implements Games {

    final String server;
    final long gameID;

    public CGame(long gameID) {
        this.server = CoreAPI.get().getRedisManager().getRedisString(GameDataValue.GAME_SERVER_REDIS.getString(null, gameID));
        this.gameID = gameID;
    }

    public CGame(String serverName) {
        this.server = serverName;
        this.gameID = (long) CoreAPI.get().getRedisManager().getRedisMap(GameDataValue.GAMEMAP_SERVER_REDIS.getString(null)).get(serverName);
    }

    public static long initGame(String server, GameEnum gameEnum) {

        long gameID = IDGenerator.generateLONGID(IDDataValue.GAME);

        RedisManager redisManager = CoreAPI.get().getRedisManager();

        GameDataValue.clearRedisData(DataType.SERVER, server, gameID);

        redisManager.getRedisMap(GameDataValue.GAMEMAP_SERVER_REDIS.getString(null)).put(server, gameID);

        redisManager.setRedisString(GameDataValue.GAME_GAME_REDIS.getString(server, gameID), gameEnum.toString());
        redisManager.setRedisBoolean(GameDataValue.GAME_TEAM_REDIS.getString(server, gameID), !GameBuilder.getGameBuilder().getTeamManager().getTeamList().isEmpty());
        redisManager.setRedisString(GameDataValue.GAME_GAMESTATE_REDIS.getString(server, gameID), GameState.WAITING.getName());
        redisManager.setRedisString(GameDataValue.GAME_SERVER_REDIS.getString(server, gameID), server);
        redisManager.setRedisLong(GameDataValue.GAME_MINP_REDIS.getString(server, gameID), Integer.valueOf(gameEnum.getDefaultMinP()).longValue());
        redisManager.setRedisLong(GameDataValue.GAME_MAXP_REDIS.getString(server, gameID), Integer.valueOf(gameEnum.getDefaultMaxP()).longValue());
        redisManager.setRedisLong(GameDataValue.GAME_MAXPLSPEC_REDIS.getString(server, gameID), Integer.valueOf(gameEnum.getDefaultMaxNPSpec()).longValue());

        return gameID;

    }

    @Override
    public void stop() {
        String name = getServerName();
        long id = getGameID();

        System.out.println("[Host] Clearing redis data");

        boolean host = isHostLinked();

        GameDataValue.clearRedisData(DataType.SERVER, name, id);

        CoreAPI.get().getRedisManager().getRedissonClient().getMap(GameDataValue.GAMEMAP_SERVER_REDIS.getString(null)).remove(name);
        if (host)
            CoreAPI.get().getRedisManager().getRedissonClient().getMap(GameDataValue.HOSTMAP_SERVER_REDIS.getString(null)).remove(name);
    }

    @Override
    public boolean canAccess(APIPlayer apiPlayer, boolean b) {

        if (b) {
            if (!apiPlayer.getRank().isModeratorRank()) {
                if (!getGame().isAllowPlSpec())
                    return false;
                return getMaxPlayerSpec() > getOutGameSpectators().size();
            }
            return true;
        }

        return isGameState(GameState.STARTING, GameState.WAITING) && getPlayers().size() < getMaxPlayer();
    }

    @Override
    public boolean joinGame(APIPlayer apiPlayer, boolean b) {
        if (!canAccess(apiPlayer, b)) return false;

        if (b) {
            if (!apiPlayer.getRank().isModeratorRank())
                getOutGameSpectators().add(apiPlayer.getName());
            else
                getInGameSpectators().add(apiPlayer.getName());
            apiPlayer.switchServer(getServerName());
        } else {
            getPlayers().add(apiPlayer.getName());
            apiPlayer.switchServer(getServerName());
        }

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
        return Long.valueOf(CoreAPI.get().getRedisManager().getRedisLong(GameDataValue.GAME_MINP_REDIS.getString(this))).intValue();
    }

    @Override
    public void setMinPlayer(int i) {
        CoreAPI.get().getRedisManager().setRedisLong(GameDataValue.GAME_MINP_REDIS.getString(this), Integer.valueOf(i).longValue());
    }

    @Override
    public int getMaxPlayer() {
        return Long.valueOf(CoreAPI.get().getRedisManager().getRedisLong(GameDataValue.GAME_MAXP_REDIS.getString(this))).intValue();
    }

    @Override
    public void setMaxPlayer(int i) {
        CoreAPI.get().getRedisManager().setRedisLong(GameDataValue.GAME_MAXP_REDIS.getString(this), Integer.valueOf(i).longValue());
    }

    @Override
    public int getMaxPlayerSpec() {
        return Long.valueOf(CoreAPI.get().getRedisManager().getRedisLong(GameDataValue.GAME_MAXPLSPEC_REDIS.getString(this))).intValue();
    }

    @Override
    public void setMaxPlayerSpec(int i) {
        CoreAPI.get().getRedisManager().setRedisLong(GameDataValue.GAME_MAXPLSPEC_REDIS.getString(this), Integer.valueOf(i).longValue());
    }

    @Override
    public List<String> getPlayers() {
        return CoreAPI.get().getRedisManager().getRedisList(GameDataValue.GAME_PLAYER_REDIS.getString(this));
    }

    @Override
    public List<String> getInGameSpectators() {
        return CoreAPI.get().getRedisManager().getRedisList(GameDataValue.GAME_SPEC_INGAME_REDIS.getString(this));
    }

    @Override
    public List<String> getOutGameSpectators() {
        return CoreAPI.get().getRedisManager().getRedisList(GameDataValue.GAME_SPEC_OUTGAME_REDIS.getString(this));
    }

    @Override
    public List<String> getInConnectPlayer() {
        return CoreAPI.get().getRedisManager().getRedisList(GameDataValue.GAME_INCOPLAYER_REDIS.getString(this));
    }

    @Override
    public boolean setSpectator(String s, boolean b) {
        if (b == isSpectator(s)) return false;
        if (b) {
            getPlayers().remove(s);
            getInGameSpectators().add(s);
        } else {
            if (getOutGameSpectators().contains(s)) return false;
            getPlayers().add(s);
            getInGameSpectators().remove(s);
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
        return CoreAPI.get().getRedisManager().getRedisMap(GameDataValue.GAME_SETTINGS_REDIS.getString(this));
    }

    @Override
    public boolean isHostLinked() {
        return CoreAPI.get().getGamesManager().isHostExist(getServerName());
    }

    @Override
    public Hosts getHost() {
        return CoreAPI.get().getGamesManager().getHost(getServerName());
    }

    @Override
    public GameState getGameState() {
        return GameState.getState(CoreAPI.get().getRedisManager().getRedisString(GameDataValue.GAME_GAMESTATE_REDIS.getString(this)));
    }

    @Override
    public void setGameState(GameState gameState) {
        CoreAPI.get().getRedisManager().setRedisString(GameDataValue.GAME_GAMESTATE_REDIS.getString(this), gameState.getName());
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
        return GameEnum.getStatus(CoreAPI.get().getRedisManager().getRedisString(GameDataValue.GAME_GAME_REDIS.getString(this)));
    }

    @Override
    public boolean hasTeam() {
        return CoreAPI.get().getRedisManager().getRedisBoolean(GameDataValue.GAME_TEAM_REDIS.getString(this));
    }

    @Override
    public void setHasTeam(boolean b) {
        CoreAPI.get().getRedisManager().setRedisBoolean(GameDataValue.GAME_TEAM_REDIS.getString(this), b);
    }

    @Override
    public long getGameID() {
        return gameID;
    }

    @Override
    public List<String> getSpectators() {
        List<String> specList = new ArrayList<>(getInGameSpectators());
        specList.addAll(getOutGameSpectators());
        return specList;
    }

    @Override
    public void forceStart(APIPlayerModerator APIPlayer) {
        PMManager.sendRedissonPluginMessage(API.get().getRedisManager().getRedissonClient(), "forceSTART", APIPlayer.getName());
    }

    @Override
    public void forceStopStart(APIPlayerModerator APIPlayer) {
        PMManager.sendRedissonPluginMessage(API.get().getRedisManager().getRedissonClient(), "forceSTOPSTART", APIPlayer.getName());
    }

    @Override
    public boolean forceEnd(APIPlayerModerator APIPlayer, String reason) {
        if (reason.contains("<split>")) return false;
        PMManager.sendRedissonPluginMessage(API.get().getRedisManager().getRedissonClient(), "forceEND", APIPlayer.getName() + "<split>" + reason);
        return true;
    }

    @Override
    public boolean forceWin(APIPlayerModerator APIPlayer, Team team, String reason) {
        if (reason.contains("<split>")) return false;
        PMManager.sendRedissonPluginMessage(API.get().getRedisManager().getRedissonClient(), "forceWIN", APIPlayer.getName() + "<split>" + team.getName() + "<split>" + reason);
        return true;
    }

    @Override
    public boolean isInConnectPlayer(String playerName) {
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
    public boolean isPlayer(String playerName) {
        return getPlayers().contains(playerName);
    }

    @Override
    public boolean isSpectator(String playerName) {
        return getSpectators().contains(playerName);
    }

}
