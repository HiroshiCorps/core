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
            for (Object serverName : API.getInstance().getRedisManager().getRedissonClient().getMap(GameDataValue.GAMEMAP_SERVER_REDIS.getString(null)).values())
                add(getGame(Long.parseLong((String) serverName)));
        }};
    }

    @Override
    public boolean isGameExist(String s) {
        return API.getInstance().getRedisManager().getRedisMap(GameDataValue.GAMEMAP_SERVER_REDIS.getString(null)).containsKey(s);
    }

    @Override
    public boolean isGameExist(long l) {
        return API.getInstance().getRedisManager().getRedisMap(GameDataValue.GAMEMAP_SERVER_REDIS.getString(null)).containsValue(l);
    }

    @Override
    public Game getGame(String s) {
        if (!isGameExist(s)) return null;
        if (isHostExist(s)) getHost(s);
        return new CGame(s);
    }

    @Override
    public Game getGame(long s) {
        if (!isGameExist(s)) return null;
        if (isHostExist(s)) return getHost(s);
        return new CGame(s);
    }

    @Override
    public Game initGameServer(String s, GameEnum gameEnum) {
        Game games = getGame(s);
        if (games != null) return games;
        return CGame.initGame(s, gameEnum);
    }

    @Override
    public List<Host> getListHost() {
        return new ArrayList<>() {{
            for (Object serverName : API.getInstance().getRedisManager().getRedissonClient().getMap(GameDataValue.HOSTMAP_SERVER_REDIS.getString(null)).values())
                add(getHost(Long.parseLong((String) serverName)));
        }};
    }

    @Override
    public boolean isHostExist(String s) {
        if (s == null) return false;
        return API.getInstance().getRedisManager().getRedissonClient().getMap(GameDataValue.HOSTMAP_SERVER_REDIS.getString(null)).containsKey(s);
    }

    @Override
    public boolean isHostExist(long s) {
        return API.getInstance().getRedisManager().getRedissonClient().getMap(GameDataValue.HOSTMAP_SERVER_REDIS.getString(null)).containsValue(s);
    }

    @Override
    public Host getHost(String s) {
        if (!isHostExist(s)) return null;
        return new CHost((long) API.getInstance().getRedisManager().getRedissonClient().getMap(GameDataValue.HOSTMAP_SERVER_REDIS.getString(null)).get(s));
    }

    @Override
    public Host getHost(long s) {
        if (!isHostExist(s)) return null;
        return new CHost(s);
    }

    @Override
    public Host initHostServer(String s, APIPlayer s1, GameEnum hostGame) throws GameCreateError {
        if (isHostExist(s))
            throw new GameCreateError("Host name already used");
        return CHost.initHost(s, s1.getName(), hostGame);
    }

}
