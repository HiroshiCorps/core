/* Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 */

package fr.redxil.core.common.game;

import fr.redxil.api.common.API;
import fr.redxil.api.common.game.Game;
import fr.redxil.api.common.game.Host;
import fr.redxil.api.common.game.utils.GameEnum;
import fr.redxil.api.common.game.utils.GameState;
import fr.redxil.api.common.game.utils.HostAccess;
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.api.common.player.data.LinkUsage;
import fr.redxil.api.common.redis.RedisManager;
import fr.redxil.core.common.data.GameDataValue;

import java.util.List;
import java.util.UUID;

public class CHost extends CGame implements Host {

    public CHost(long gameID) {
        super(gameID, false);
    }

    public static Host initHost(String author, GameEnum gameEnum) {

        Game game = CGame.initGame(gameEnum);

        long gameID = game.getGameID();

        RedisManager redisManager = API.getInstance().getRedisManager();

        redisManager.setRedisString(GameDataValue.HOST_AUTHOR_REDIS.getString(gameID), author);
        redisManager.setRedisString(GameDataValue.HOST_ACCESS_REDIS.getString(gameID), HostAccess.CLOSE.toString());
        redisManager.getRedisList(GameDataValue.LIST_HOST_REDIS.getString()).add(gameID);

        return new CHost(gameID);

    }

    @Override
    public UUID getAuthor() {
        return UUID.fromString(API.getInstance().getRedisManager().getRedisString(GameDataValue.HOST_AUTHOR_REDIS.getString(this)));
    }

    @Override
    public HostAccess getHostAccess() {
        return HostAccess.getStatus(API.getInstance().getRedisManager().getRedisString(GameDataValue.HOST_ACCESS_REDIS.getString(this)));
    }

    @Override
    public void setHostAccess(HostAccess hostAccess) {
        API.getInstance().getRedisManager().setRedisString(GameDataValue.HOST_ACCESS_REDIS.getString(this), hostAccess.toString());
    }

    @Override
    public boolean canAccess(APIPlayer apiPlayer, boolean spec) {

        if (spec) {

            if (apiPlayer.getRank().isModeratorRank())
                return true;

            if (!getGame().isAllowPlSpec())
                return false;

            if (getMaxPlayerSpec() <= getPlayerSpectators().size())
                return false;

            return isAllowSpectator(apiPlayer.getUUID());

        }

        if (isGameState(GameState.WAITING) && getPlayers().size() < getMaxPlayer()) {
            HostAccess hostAccess = getHostAccess();
            if (hostAccess == HostAccess.CLOSE)
                return false;
            if (hostAccess.toString().equals(HostAccess.FRIEND.toString()))
                return getAllowPlayer().contains(apiPlayer.getUUID()) || apiPlayer.getLink(LinkUsage.BOTH, API.getInstance().getPlayerManager().getPlayer(getAuthor()), "friend") != null;
            return true;
        }

        return false;

    }

    @Override
    public List<UUID> getAllowPlayer() {
        return API.getInstance().getRedisManager().getRedisList(GameDataValue.HOST_ALLOWPLAYER_REDIS.getString(this));
    }

    @Override
    public List<UUID> getAllowSpectator() {
        return API.getInstance().getRedisManager().getRedisList(GameDataValue.HOST_ALLOWSPECTATOR_REDIS.getString(this));
    }

    @Override
    public boolean isAllowPlayer(UUID s) {
        return getAllowPlayer().contains(s);
    }

    @Override
    public boolean isAllowSpectator(UUID s) {
        return getAllowSpectator().contains(s);
    }

    @Override
    public Host getHost() {
        return this;
    }

}
