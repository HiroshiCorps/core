/* Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 */

package fr.redxil.core.common.game;

import fr.redxil.api.common.game.GameEnum;
import fr.redxil.api.common.game.GameState;
import fr.redxil.api.common.game.HostAccess;
import fr.redxil.api.common.game.Hosts;
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.api.common.redis.RedisManager;
import fr.redxil.core.common.CoreAPI;
import fr.redxil.core.common.data.GameDataValue;

import java.util.List;

public class CHost extends CGame implements Hosts {

    public CHost(long gameID) {
        super(gameID);
    }

    public static long initHost(String server, String author, GameEnum gameEnum) {

        long gameID = CGame.initGame(server, gameEnum);

        RedisManager redisManager = CoreAPI.get().getRedisManager();

        redisManager.getRedisMap(GameDataValue.HOSTMAP_SERVER_REDIS.getString(null)).put(server, gameID);
        redisManager.setRedisString(GameDataValue.HOST_AUTHOR_REDIS.getString(server, gameID), author);
        redisManager.setRedisString(GameDataValue.HOST_ACCESS_REDIS.getString(server, gameID), HostAccess.CLOSE.toString());

        return gameID;

    }

    @Override
    public String getAuthor() {
        return CoreAPI.get().getRedisManager().getRedisString(GameDataValue.HOST_AUTHOR_REDIS.getString(this));
    }

    @Override
    public HostAccess getHostAccess() {
        return HostAccess.getStatus(CoreAPI.get().getRedisManager().getRedisString(GameDataValue.HOST_ACCESS_REDIS.getString(this)));
    }

    @Override
    public void setHostAccess(HostAccess hostAccess) {
        CoreAPI.get().getRedisManager().setRedisString(GameDataValue.HOST_ACCESS_REDIS.getString(this), hostAccess.toString());
    }

    @Override
    public boolean canAccess(APIPlayer apiPlayer, boolean spec) {

        if (spec) {
            if (!apiPlayer.getRank().isModeratorRank()) {
                if (!getGame().isAllowPlSpec())
                    return false;
                if (getMaxPlayerSpec() <= getOutGameSpectators().size())
                    return false;
                return isAllowSpectator(apiPlayer.getName());
            }
            return true;
        }

        if (isGameState(GameState.STARTING, GameState.WAITING) && getPlayers().size() < getMaxPlayer()) {
            HostAccess hostAccess = getHostAccess();
            if (hostAccess.toString().equals(HostAccess.CLOSE.toString()))
                return false;
            if (hostAccess.toString().equals(HostAccess.FRIEND.toString()))
                return getAllowPlayer().contains(apiPlayer.getName()) || apiPlayer.hasFriend(CoreAPI.get().getPlayerManager().getPlayer(getAuthor()));
            return true;
        }

        return false;

    }

    @Override
    public List<String> getAllowPlayer() {
        return CoreAPI.get().getRedisManager().getRedisList(GameDataValue.HOST_ALLOWPLAYER_REDIS.getString(this));
    }

    @Override
    public List<String> getAllowSpectator() {
        return CoreAPI.get().getRedisManager().getRedisList(GameDataValue.HOST_ALLOWSPECTATOR_REDIS.getString(this));
    }

    @Override
    public boolean isAllowPlayer(String s) {
        return getAllowPlayer().contains(s);
    }

    @Override
    public boolean isAllowSpectator(String s) {
        return getAllowSpectator().contains(s);
    }

    @Override
    public Hosts getHost() {
        return this;
    }

}
