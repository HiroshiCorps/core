/*
 *
 * Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 *
 */

package fr.redxil.core.common.game;

import fr.redxil.api.common.API;
import fr.redxil.api.common.game.Host;
import fr.redxil.api.common.game.utils.GameState;
import fr.redxil.api.common.game.utils.HostAccess;
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.api.common.server.PlayerState;
import fr.redxil.api.common.utils.DataReminder;
import fr.redxil.core.common.data.game.GameDataRedis;
import fr.xilitra.hiroshisav.enums.TypeGame;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CHost extends CGame implements Host {

    DataReminder<Long> authorReminder = null;
    DataReminder<String> accessReminder = null;
    DataReminder<List<UUID>> apReminder = null;
    DataReminder<List<UUID>> asReminder = null;

    public CHost(long gameID) {
        super(gameID);
    }

    public CHost(long serverID, Long author, TypeGame gameEnum) {
        super(serverID, gameEnum);

        initAuthorReminder();
        authorReminder.setData(author);
        setHostAccess(HostAccess.CLOSE);
        API.getInstance().getGameManager().getServerToHostIDMap().put(serverID, gameID);

    }

    public void initAuthorReminder() {
        if (authorReminder == null)
            authorReminder = DataReminder.generateReminder(GameDataRedis.HOST_AUTHOR_REDIS.getString(this), null);
    }

    @Override
    public Long getAuthor() {
        initAuthorReminder();
        return authorReminder.getData();
    }

    public void initAccessReminder() {
        if (accessReminder == null)
            accessReminder = DataReminder.generateReminder(GameDataRedis.HOST_ACCESS_REDIS.getString(this), HostAccess.FRIEND.toString());
    }

    @Override
    public HostAccess getHostAccess() {
        initAccessReminder();
        return HostAccess.getStatus(accessReminder.getData());
    }

    @Override
    public void setHostAccess(HostAccess hostAccess) {
        initAccessReminder();
        accessReminder.setData(hostAccess.toString());
    }

    @Override
    public boolean canAccess(UUID uuid, boolean spec) {
        if (spec) {

            Optional<APIPlayer> apiPlayer = API.getInstance().getPlayerManager().getPlayer(uuid);
            if (apiPlayer.isEmpty())
                return false;

            if (apiPlayer.get().getRank().isModeratorRank())
                return true;

            if (!isAllowSpectator(uuid))
                return false;

            return getMaxPlayerSpec() > getPlayerList(PlayerState.MODSPECTATE, PlayerState.SPECTATE).size();

        }

        return isGameState(GameState.WAITING) && isAllowPlayer(uuid) && getPlayerList(PlayerState.INCONNECT, PlayerState.CONNECTED).size() < getMaxPlayer();
    }

    @Override
    public List<UUID> getAllowPlayer() {
        if (apReminder == null)
            apReminder = DataReminder.generateListReminder(GameDataRedis.HOST_ALLOWPLAYER_REDIS.getString(this));
        return apReminder.getData();
    }

    @Override
    public List<UUID> getAllowSpectator() {
        if (asReminder == null)
            asReminder = DataReminder.generateListReminder(GameDataRedis.HOST_ALLOWSPECTATOR_REDIS.getString(this));
        return asReminder.getData();
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
    public void clearData() {
        long serverID = getServerID();

        API.getInstance().getGameManager().getServerToHostIDMap().remove(serverID);

        super.clearData();
    }

}
