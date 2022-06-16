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
import fr.redxil.api.common.game.utils.GameState;
import fr.redxil.api.common.group.team.Team;
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.api.common.server.PlayerState;
import fr.redxil.api.common.utils.DataReminder;
import fr.redxil.api.common.utils.id.IDGenerator;
import fr.redxil.core.common.data.IDDataValue;
import fr.redxil.core.common.data.game.GameDataRedis;
import fr.redxil.core.common.data.game.TeamDataValue;
import fr.redxil.core.common.data.utils.DataType;
import fr.xilitra.hiroshisav.enums.ServerType;
import fr.xilitra.hiroshisav.enums.TypeGame;

import javax.annotation.Nullable;
import java.util.*;
import java.util.logging.Level;

public class CGame implements Game {

    final long gameID;
    private DataReminder<Long> serverReminder = null, maxPlayerReminder = null, maxPlayerSpecReminder = null, minPlayerReminder = null;
    private DataReminder<List<UUID>> specListReminder = null, inCoReminder = null, coReminder = null, modSpecReminder = null;
    private DataReminder<Map<UUID, String>> playerStateReminder = null;
    private DataReminder<Map<String, Object>> settingsReminder = null;
    private DataReminder<String> gameStateReminder = null, gameReminder = null, worldNameReminder;

    public CGame(long id) {
        this.gameID = id;
    }

    public CGame(long serverID, TypeGame gameEnum) {

        this.gameID = IDGenerator.generateLONGID(IDDataValue.GAME.getLocation());

        initServerReminder();
        serverReminder.setData(serverID);

        initGameReminder();
        gameReminder.setData(gameEnum.getConfigName());

        setGameState(GameState.WAITING);
        setMinPlayer(gameEnum.getDefaultMinP());
        setMaxPlayer(gameEnum.getDefaultMaxP());
        setMaxPlayerSpec(gameEnum.getDefaultMaxNPSpec());
        setWorldName("None");

        GameDataRedis.clearRedisData(DataType.SERVER, this.gameID);

        API.getInstance().getGameManager().getServerToGameIDMap().put(serverID, this.gameID);

    }

    @Override
    public void clearData() {

        API.getInstance().getAPIEnabler().printLog(Level.FINE, "[Host] Clearing redis data");

        TeamDataValue.clearRedisData(DataType.TEAM, this);

        Long serverID = getServerID();
        long gameID = getGameID();

        GameDataRedis.clearRedisData(DataType.SERVER, gameID);

        API.getInstance().getGameManager().getServerToGameIDMap().remove(serverID);

    }

    @Override
    public boolean canAccess(UUID uuid, boolean b) {
        if (b) {

            Optional<APIPlayer> apiPlayer = API.getInstance().getPlayerManager().getPlayer(uuid);
            if (apiPlayer.isEmpty())
                return false;

            if (apiPlayer.get().getRank().isModeratorRank())
                return true;

            if (!getGame().isAllowPlSpec())
                return false;

            return getMaxPlayerSpec() > getPlayerList(PlayerState.MODSPECTATE, PlayerState.SPECTATE).size();

        }

        return isGameState(GameState.WAITING) && getPlayerList().size() < getMaxPlayer();
    }

    /*
     * Game Part
     */

    @Override
    public boolean joinGame(UUID uuid, boolean b) {
        if(isRegistered(uuid))
            return false;
        if (!canAccess(uuid, b)) return false;
        setPlayerState(uuid, b ? PlayerState.SPECTATE : PlayerState.INCONNECT);
        long serverID = getServerID();
        API.getInstance().getServerManager().getServer(serverID).ifPresent(server -> server.setAllowedConnect(uuid, true));
        API.getInstance().getPlayerManager().getPlayer(uuid).ifPresent(player -> player.switchServer(serverID));
        return true;
    }

    @Override
    public boolean quitGame(UUID uuid){
        if(!isRegistered(uuid))
            return false;

        setPlayerState(uuid, null);

        API.getInstance().getServerManager().getServer(getServerID()).ifPresent(server -> server.setAllowedConnect(uuid, false));
        API.getInstance().getPlayerManager().getPlayer(uuid).ifPresent(player -> {
            if(player.getServerID() == getServerID()) {
                API.getInstance().getServerManager().getConnectableServer(player, ServerType.HUB).ifPresent(targetServer -> player.switchServer(targetServer.getServerID()));
            }
        });

        return true;

    }

    public void initServerReminder() {
        if (this.serverReminder == null)
            this.serverReminder = DataReminder.generateReminder(GameDataRedis.GAME_SERVER_REDIS.getString(gameID), null);
    }

    @Override
    public long getServerID() {
        initServerReminder();
        return this.serverReminder.getData();
    }

    public void initMinPlayerReminder() {
        if (this.minPlayerReminder == null)
            this.minPlayerReminder = DataReminder.generateReminder(GameDataRedis.GAME_MINP_REDIS.getString(gameID), null);
    }

    @Override
    public int getMinPlayer() {
        initMinPlayerReminder();
        return this.minPlayerReminder.getData().intValue();
    }

    @Override
    public void setMinPlayer(int i) {
        initMinPlayerReminder();
        this.minPlayerReminder.setData(Integer.valueOf(i).longValue());
    }

    public void initMaxPlayerReminder() {
        if (this.maxPlayerReminder == null)
            this.maxPlayerReminder = DataReminder.generateReminder(GameDataRedis.GAME_MAXP_REDIS.getString(gameID), null);
    }

    @Override
    public int getMaxPlayer() {
        initMaxPlayerReminder();
        return this.maxPlayerReminder.getData().intValue();
    }

    @Override
    public void setMaxPlayer(int i) {
        initMaxPlayerReminder();
        this.maxPlayerReminder.setData(Integer.valueOf(i).longValue());
    }

    public void initMaxPlayerSpecReminder() {
        if (this.maxPlayerSpecReminder == null)
            this.maxPlayerSpecReminder = DataReminder.generateReminder(GameDataRedis.GAME_MAXPLSPEC_REDIS.getString(gameID), null);
    }

    @Override
    public int getMaxPlayerSpec() {
        initMaxPlayerSpecReminder();
        return this.maxPlayerSpecReminder.getData().intValue();
    }

    @Override
    public void setMaxPlayerSpec(int i) {
        initMaxPlayerSpecReminder();
        this.maxPlayerSpecReminder.setData(Integer.valueOf(i).longValue());
    }

    public void initCoListReminder() {
        if (coReminder == null)
            this.coReminder = DataReminder.generateListReminder(GameDataRedis.GAME_INPLAYER_REDIS.getString(this));
    }

    public void initModSpecReminder() {
        if (modSpecReminder == null)
            this.modSpecReminder = DataReminder.generateListReminder(GameDataRedis.GAME_SPEC_MODERATOR_REDIS.getString(this));
    }

    public void initSpecReminder() {
        if (specListReminder == null)
            this.specListReminder = DataReminder.generateListReminder(GameDataRedis.GAME_SPEC_PLAYER_REDIS.getString(this));
    }

    public void initInCoListReminder() {
        if (inCoReminder == null)
            this.inCoReminder = DataReminder.generateListReminder(GameDataRedis.GAME_INCOPLAYER_REDIS.getString(this));
    }


    @Override
    public List<UUID> getPlayerList(PlayerState... playerStates) {
        List<UUID> playerList = new ArrayList<>();
        for(PlayerState playerState : playerStates){
            switch (playerState){
                case SPECTATE -> {
                    initSpecReminder();
                    playerList.addAll(this.specListReminder.getData());
                }
                case CONNECTED -> {
                    initCoListReminder();
                    playerList.addAll(coReminder.getData());
                }
                case INCONNECT -> {
                    initInCoListReminder();
                    playerList.addAll(inCoReminder.getData());
                }
                case MODSPECTATE -> {
                    initModSpecReminder();
                    playerList.addAll(modSpecReminder.getData());
                }
            }
        }
        return playerList;
    }

    public void initPlStateReminder() {
        if (this.playerStateReminder == null)
            this.playerStateReminder = DataReminder.generateMapReminder(GameDataRedis.MAP_STATE_REDIS.getString(this));
    }

    @Override
    public PlayerState getPlayerState(UUID player){
        initPlStateReminder();
        return PlayerState.valueOf(this.playerStateReminder.getData().get(player));
    }

    @Override
    public void setPlayerState(UUID player, @Nullable PlayerState playerState){
        PlayerState oldState = getPlayerState(player);
        if(oldState != null){
            switch (oldState){
                case SPECTATE -> {
                    initSpecReminder();
                    this.specListReminder.getData().remove(player);
                }
                case CONNECTED -> {
                    initCoListReminder();
                    coReminder.getData().remove(player);
                }
                case INCONNECT -> {
                    initInCoListReminder();
                    inCoReminder.getData().remove(player);
                }
                case MODSPECTATE -> {
                    initModSpecReminder();
                    modSpecReminder.getData().remove(player);
                }
            }
        }

        initPlStateReminder();
        if(playerState != null){
            playerStateReminder.getData().put(player, playerState.name());
            switch (playerState){
                case SPECTATE -> {
                    initSpecReminder();
                    this.specListReminder.getData().add(player);
                }
                case CONNECTED -> {
                    initCoListReminder();
                    coReminder.getData().add(player);
                }
                case INCONNECT -> {
                    initInCoListReminder();
                    inCoReminder.getData().add(player);
                }
                case MODSPECTATE -> {
                    initModSpecReminder();
                    modSpecReminder.getData().add(player);
                }
            }
        }else playerStateReminder.getData().remove(player);
    }

    @Override
    public void removeSettings(String s) {
        getSettingsMap().remove(s);
    }

    @Override
    public void setSettings(String s, Object o) {
        Map<String, Object> map = getSettingsMap();
        map.remove(s);
        map.put(s, o);
    }

    public void initSettingsMapReminder() {
        if (this.settingsReminder == null)
            this.settingsReminder = DataReminder.generateMapReminder(GameDataRedis.GAME_SETTINGS_REDIS.getString(gameID));
    }

    @Override
    public Map<String, Object> getSettingsMap() {
        initSettingsMapReminder();
        return this.settingsReminder.getData();
    }

    public void initGameStateReminder() {
        if (this.gameStateReminder == null)
            this.gameStateReminder = DataReminder.generateReminder(GameDataRedis.GAME_GAMESTATE_REDIS.getString(this), GameState.WAITING.toString());
    }

    @Override
    public GameState getGameState() {
        initGameStateReminder();
        return GameState.valueOf(this.gameStateReminder.getData());
    }

    @Override
    public void setGameState(GameState gameState) {
        initGameStateReminder();
        this.gameStateReminder.setData(gameState.getName());
    }

    @Override
    public boolean isGameState(GameState... gameState) {
        GameState actState = getGameState();
        for (GameState gs : gameState)
            if (gs.getName().equals(actState.getName()))
                return true;
        return false;
    }

    public void initGameReminder() {
        if (this.gameReminder == null)
            this.gameReminder = DataReminder.generateReminder(GameDataRedis.GAME_GAME_REDIS.getString(this), TypeGame.UHC12P.getConfigName());
    }

    @Override
    public TypeGame getGame() {
        initGameReminder();
        return TypeGame.getBYConfigString(gameReminder.getData()).orElse(null);
    }

    public void initWorldNameReminder() {
        if (this.worldNameReminder == null)
            this.worldNameReminder = DataReminder.generateReminder(GameDataRedis.GAME_MAP_REDIS.getString(this), "null");
    }

    @Override
    public String getWorldName() {
        initWorldNameReminder();
        return this.worldNameReminder.getData();
    }

    @Override
    public void setWorldName(String map) {
        initWorldNameReminder();
        this.worldNameReminder.setData(map);
    }

    @Override
    public long getGameID() {
        return gameID;
    }

    @Override
    public void forceStart(UUID uuid) {
        API.getInstance().getRedisManager().ifPresent(
                redis -> RedisPMManager.sendRedissonPluginMessage(redis.getRedissonClient(), "forceSTART", uuid.toString())
        );
    }

    @Override
    public boolean forceEnd(UUID uuid, String reason) {
        if (reason.contains("<split>")) return false;
        API.getInstance().getRedisManager().ifPresent(
                redis -> RedisPMManager.sendRedissonPluginMessage(redis.getRedissonClient(), "forceEND", uuid.toString() + "<split>" + reason)
        );
        return true;
    }

    @Override
    public boolean forceWin(UUID uuid, Team team, String reason) {
        if (reason.contains("<split>")) return false;
        API.getInstance().getRedisManager().ifPresent(
                redis -> RedisPMManager.sendRedissonPluginMessage(redis.getRedissonClient(), "forceWIN", uuid.toString() + "<split>" + team.getTeamName() + "<split>" + reason)
        );
        return true;
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
    public boolean isRegistered(UUID playerName) {
        return getPlayerState(playerName) != null;
    }

    @Override
    public boolean isPlayer(UUID playerName) {
        PlayerState playerState = getPlayerState(playerName);
        if(playerState == null)
            return false;
        return playerState == PlayerState.CONNECTED || playerState == PlayerState.INCONNECT;
    }

    @Override
    public boolean isSpectator(UUID playerName) {
        PlayerState playerState = getPlayerState(playerName);
        if(playerState == null)
            return false;
        return playerState == PlayerState.SPECTATE || playerState == PlayerState.MODSPECTATE;
    }

}
