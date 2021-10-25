/* Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 */

package fr.redxil.core.common.game;

import fr.redxil.core.common.data.GameDataValue;
import fr.redxil.api.common.game.GameEnum;
import fr.redxil.api.common.game.Games;
import fr.redxil.api.common.game.GamesManager;
import fr.redxil.api.common.game.Hosts;
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.core.common.CoreAPI;

import java.util.ArrayList;
import java.util.List;

public class CGameManager implements GamesManager {

    @Override
    public List<Games> getListGames() {
        return new ArrayList<Games>() {{
            for (Object serverName : CoreAPI.get().getRedisManager().getRedissonClient().getMap(GameDataValue.GAMEMAP_SERVER_REDIS.getString(null)).values())
                add(getGame(Long.parseLong((String) serverName)));
        }};
    }

    @Override
    public boolean isGameExist(String s) {
        return CoreAPI.get().getRedisManager().getRedisMap(GameDataValue.GAMEMAP_SERVER_REDIS.getString(null)).containsKey(s);
    }

    @Override
    public boolean isGameExist(long l) {
        return CoreAPI.get().getRedisManager().getRedisMap(GameDataValue.GAMEMAP_SERVER_REDIS.getString(null)).containsValue(l);
    }

    @Override
    public Games getGame(String s) {
        if (!isGameExist(s)) return null;
        if (isHostExist(s)) getHost(s);
        return new CGame(s);
    }

    @Override
    public Games getGame(long s) {
        if (!isGameExist(s)) return null;
        if (isHostExist(s)) return getHost(s);
        return new CGame(s);
    }

    @Override
    public Games initGameServer(String s, GameEnum gameEnum) {
        Games games = getGame(s);
        if (games != null) return games;
        return getGame(CGame.initGame(s, gameEnum));
    }

    @Override
    public List<Hosts> getListHosts() {
        return new ArrayList<Hosts>() {{
            for (Object serverName : CoreAPI.get().getRedisManager().getRedissonClient().getMap(GameDataValue.HOSTMAP_SERVER_REDIS.getString(null)).values())
                add(getHost(Long.parseLong((String) serverName)));
        }};
    }

    @Override
    public boolean isHostExist(String s) {
        if (s == null) return false;
        return CoreAPI.get().getRedisManager().getRedissonClient().getMap(GameDataValue.HOSTMAP_SERVER_REDIS.getString(null)).containsKey(s);
    }

    @Override
    public boolean isHostExist(long s) {
        return CoreAPI.get().getRedisManager().getRedissonClient().getMap(GameDataValue.HOSTMAP_SERVER_REDIS.getString(null)).containsValue(s);
    }

    @Override
    public Hosts getHost(String s) {
        if (!isHostExist(s)) return null;
        return new CHost((long) CoreAPI.get().getRedisManager().getRedissonClient().getMap(GameDataValue.HOSTMAP_SERVER_REDIS.getString(null)).get(s));
    }

    @Override
    public Hosts getHost(long s) {
        if (!isHostExist(s)) return null;
        return new CHost(s);
    }

    @Override
    public Hosts initHostServer(String s, APIPlayer s1, GameEnum hostGame) {
        if (s == null || s1 == null || hostGame == null) return null;
        if (isHostExist(s))
            return getHost(s);
        return getHost(CHost.initHost(s, s1.getName(), hostGame));
    }

}
