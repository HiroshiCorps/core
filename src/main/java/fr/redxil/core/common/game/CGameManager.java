/* Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 */

package fr.redxil.core.common.game;

import fr.redxil.api.common.API;
import fr.redxil.api.common.game.Game;
import fr.redxil.api.common.game.GameManager;
import fr.redxil.api.common.game.Host;
import fr.redxil.api.common.game.error.GameCreateError;
import fr.redxil.api.common.game.utils.GameEnum;
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.core.common.data.GameDataValue;

import java.util.ArrayList;
import java.util.List;

public class CGameManager implements GameManager {

    @Override
    public List<Game> getListGames() {
        return new ArrayList<>() {{
            for (Object serverName : API.getInstance().getRedisManager().getRedissonClient().getMap(GameDataValue.LIST_GAME_REDIS.getString()).values())
                add(getGame((long) serverName));
        }};
    }

    @Override
    public boolean isGameExistByServerID(long s) {
        return API.getInstance().getRedisManager().getRedisMap(GameDataValue.MAP_SERVER_REDIS.getString()).containsKey(s);
    }

    @Override
    public boolean isGameExist(long l) {
        return API.getInstance().getRedisManager().getRedisList(GameDataValue.LIST_GAME_REDIS.getString()).contains(l);
    }

    @Override
    public Game getGameByServerID(long serverID) {
        if (!isGameExistByServerID(serverID)) return null;
        return new CGame(serverID, true);
    }

    @Override
    public Game getGame(long s) {
        if (!isGameExist(s)) return null;
        return new CGame(s, false);
    }

    @Override
    public Game initGameServer(GameEnum gameEnum) {
        return CGame.initGame(gameEnum);
    }

    @Override
    public List<Host> getListHost() {
        return new ArrayList<>() {{
            for (Object serverName : API.getInstance().getRedisManager().getRedissonClient().getList(GameDataValue.LIST_HOST_REDIS.getString()).readAll())
                add(getHost(Long.parseLong((String) serverName)));
        }};
    }

    @Override
    public boolean isHostExistByServerID(long s) {
        Game game = getGameByServerID(s);
        if(game == null)
            return false;
        return API.getInstance().getRedisManager().getRedissonClient().getList(GameDataValue.LIST_HOST_REDIS.getString()).contains(game.getGameID());
    }

    @Override
    public boolean isHostExist(long s) {
        return API.getInstance().getRedisManager().getRedissonClient().getList(GameDataValue.LIST_HOST_REDIS.getString()).contains(s);
    }

    @Override
    public Host getHostByServerID(long s) {
        Game game = getGameByServerID(s);
        if(game == null)
            return null;
        if(!isHostExist(game.getGameID()))
            return null;
        return new CHost(game.getGameID());
    }

    @Override
    public Host getHost(long s) {
        if (!isHostExist(s)) return null;
        return new CHost(s);
    }

    @Override
    public Host initHostServer(APIPlayer s1, GameEnum hostGame) {
        return CHost.initHost(s1.getName(), hostGame);
    }

}
