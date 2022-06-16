/*
 *
 * Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 *
 */

package fr.redxil.core.common.game;

import fr.redxil.api.common.game.Game;
import fr.redxil.api.common.game.GameManager;
import fr.redxil.api.common.game.Host;
import fr.redxil.api.common.utils.DataReminder;
import fr.redxil.core.common.data.game.GameDataRedis;
import fr.xilitra.hiroshisav.enums.TypeGame;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class CGameManager implements GameManager {

    DataReminder<Map<Long, Long>> serverToGameMAP = DataReminder.generateMapReminder(GameDataRedis.MAP_GAME_REDIS.getString());
    DataReminder<Map<Long, Long>> serverToHostMAP = DataReminder.generateMapReminder(GameDataRedis.MAP_HOST_REDIS.getString());

    @Override
    public List<Game> getListGames() {
        return new ArrayList<>() {{
            for (Long serverName : getServerToGameIDMap().values())
                getGame(serverName).ifPresent(this::add);
        }};
    }

    @Override
    public boolean isGameExistByServerID(long s) {
        return getServerToGameIDMap().containsKey(s);
    }

    @Override
    public boolean isGameExist(long l) {
        return getServerToGameIDMap().containsValue(l);
    }

    @Override
    public Optional<Game> getGameByServerID(long serverID) {
        Long gameID = getServerToGameIDMap().get(serverID);
        if (gameID == null)
            return Optional.empty();
        return Optional.of(new CGame(gameID));
    }

    @Override
    public Optional<Game> getGame(long s) {
        if (!isGameExist(s)) return Optional.empty();
        return Optional.of(new CGame(s));
    }

    @Override
    public Optional<Game> createGame(long serverID, TypeGame gameEnum) {
        if (isGameExistByServerID(serverID))
            return Optional.empty();
        return Optional.of(new CGame(serverID, gameEnum));
    }


    @Override
    public List<Host> getListHost() {
        return new ArrayList<>() {{
            for (Long serverName : getServerToHostIDMap().values()) {
                getHost(serverName).ifPresent(this::add);
            }
        }};
    }

    @Override
    public boolean isHostExistByServerID(long s) {
        return getServerToHostIDMap().containsKey(s);
    }

    @Override
    public boolean isHostExist(long s) {
        return getServerToHostIDMap().containsValue(s);
    }

    @Override
    public Optional<Host> getHostByServerID(long s) {
        Long gameID = getServerToHostIDMap().get(s);
        if (gameID == null)
            return Optional.empty();
        return Optional.of(new CHost(gameID));
    }

    @Override
    public Optional<Host> getHost(long s) {
        if (!isHostExist(s)) return Optional.empty();
        return Optional.of(new CHost(s));
    }

    @Override
    public Optional<Host> createHost(long serverID, Long s1, TypeGame hostGame) {
        if (isHostExistByServerID(serverID))
            return Optional.empty();
        return Optional.of(new CHost(serverID, s1, hostGame));
    }

    @Override
    public Map<Long, Long> getServerToGameIDMap() {
        return serverToGameMAP.getData();
    }

    @Override
    public Map<Long, Long> getServerToHostIDMap() {
        return serverToHostMAP.getData();
    }

}
