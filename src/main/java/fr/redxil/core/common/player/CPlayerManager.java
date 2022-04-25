/*
 *
 * Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 *
 */

package fr.redxil.core.common.player;

import fr.redline.pms.utils.IpInfo;
import fr.redxil.api.common.API;
import fr.redxil.api.common.player.APIOfflinePlayer;
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.api.common.player.APIPlayerManager;
import fr.redxil.api.common.player.data.LinkData;
import fr.redxil.core.common.data.player.PlayerDataRedis;
import fr.redxil.core.common.data.player.PlayerDataSql;
import fr.redxil.core.common.player.sqlmodel.player.PlayerLinkModel;
import fr.redxil.core.common.player.sqlmodel.player.PlayerModel;
import fr.redxil.core.common.sql.SQLModels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;

public class CPlayerManager implements APIPlayerManager {

    HashMap<String, BiConsumer<APIPlayer, LinkData>> linkMap = new HashMap<>();

    @Override
    public boolean dataExist(String s) {
        return isLoadedPlayer(s) || getOfflinePlayer(s) != null;
    }

    /**
     * Get the APIPlayer with this name / nick (supported but take more query on redis, please use APIPlayerManager.getPlayer(memberID) if you can)
     *
     * @param name This need to be the name of the player / nick
     * @return APIPlayer or null if the player is not loaded
     */
    public APIPlayer getPlayer(String name) {
        Object value = API.getInstance().getRedisManager().getRedissonClient().getMap(PlayerDataRedis.MAP_PLAYER_NAME.getString()).get(name);
        if (value instanceof Long)
            return new CPlayer((Long) value);
        else return null;
    }

    /**
     * Get the APIPlayer with this UUID (supported but take more query on redis, please use APIPlayerManager.getPlayer(memberID) if you can)
     *
     * @param uuid this need to be the UUID of the APIPlayer
     * @return APIPlayerModerator or null if player is not loaded or not a moderator
     */

    @Override
    public APIPlayer getPlayer(UUID uuid) {
        Object value = API.getInstance().getRedisManager().getRedissonClient().getMap(PlayerDataRedis.MAP_PLAYER_UUID.getString()).get(uuid.toString());
        if (value instanceof Long)
            return new CPlayer((Long) value);
        else return null;
    }

    /**
     * Get the APIPlayer with the MemberID
     *
     * @param id this need to be the MemberID of the APIPlayer
     * @return APIPlayer or null if player is not loaded
     */

    @Override
    public APIPlayer getPlayer(long id) {
        if (!isLoadedPlayer(id)) {
            return null;
        }
        return new CPlayer(id);
    }

    /**
     * Get the APIPlayerOffline with the name
     *
     * @param uuid this need to be the name of the APIPlayer
     * @return APIPlayerOffline or null if the player never connected on the server
     */

    @Override
    public APIOfflinePlayer getOfflinePlayer(UUID uuid) {

        APIPlayer apiPlayer = getPlayer(uuid);
        if (apiPlayer != null) return apiPlayer;

        PlayerModel playerModel = new SQLModels<>(PlayerModel.class).getFirst("WHERE " + PlayerDataSql.PLAYER_UUID_SQL.getSQLColumns().toSQL() + " = ?", uuid.toString());

        if (playerModel != null) {
            return new CPlayerOffline(playerModel);
        }

        return null;

    }

    /**
     * Get the APIPlayerOffline with the name
     *
     * @param name this need to be the name of the APIPlayer
     * @return APIPlayerOffline or null if the player never connected on the server
     */

    @Override
    public APIOfflinePlayer getOfflinePlayer(String name) {

        APIPlayer apiPlayer = getPlayer(name);
        if (apiPlayer != null) return apiPlayer;

        PlayerModel playerModel = new SQLModels<>(PlayerModel.class).getFirst("WHERE " + PlayerDataSql.PLAYER_NAME_SQL.getSQLColumns().toSQL() + " = ?", name);

        if (playerModel != null) {
            return new CPlayerOffline(playerModel);
        }
        return null;

    }

    /**
     * Get the APIPlayerOffline with the name
     *
     * @param memberID this need to be the MemberID of the APIPlayer
     * @return APIPlayerOffline or null if the player never connected on the server
     */

    @Override
    public APIOfflinePlayer getOfflinePlayer(long memberID) {
        APIPlayer apiPlayer = getPlayer(memberID);
        if (apiPlayer != null) return apiPlayer;

        if (new SQLModels<>(PlayerModel.class).getFirst("WHERE " + PlayerDataSql.PLAYER_MEMBERID_SQL.getSQLColumns().toSQL() + " = ?", memberID) == null)
            return null;
        return new CPlayerOffline(memberID);
    }

    @Override
    public APIPlayer loadPlayer(String p, UUID uuid, IpInfo ipInfo) {
        APIPlayer apiPlayer = getPlayer(uuid);
        if (apiPlayer != null) return apiPlayer;
        return CPlayer.loadPlayer(p, uuid, ipInfo);
    }

    @Override
    public boolean isLoadedPlayer(String p) {
        return API.getInstance().getRedisManager().getRedissonClient().getMap(PlayerDataRedis.MAP_PLAYER_NAME.getString()).containsKey(p);
    }

    @Override
    public boolean isLoadedPlayer(long l) {
        return API.getInstance().getRedisManager().getRedissonClient().getList(PlayerDataRedis.LIST_PLAYER_ID.getString()).contains(l);
    }

    @Override
    public boolean isLoadedPlayer(UUID uuid) {
        return API.getInstance().getRedisManager().getRedissonClient().getMap(PlayerDataRedis.MAP_PLAYER_UUID.getString()).containsKey(uuid.toString());
    }

    @Override
    public List<Long> getLoadedPlayer() {
        return API.getInstance().getRedisManager().getRedissonClient().getList(PlayerDataRedis.LIST_PLAYER_ID.getString());
    }

    @Override
    public void addLinkOnConnectAction(String s, BiConsumer<APIPlayer, LinkData> biConsumer) {
        if (hasLinkType(s))
            return;
        linkMap.put(s, biConsumer);
    }

    @Override
    public void removeLinkOnConnectAction(String s) {
        linkMap.remove(s);
    }

    @Override
    public BiConsumer<APIPlayer, LinkData> getLinkOnConnectAction(String s) {
        return linkMap.get(s);
    }

    @Override
    public List<String> getLinkTypeList() {
        return new ArrayList<>(linkMap.keySet());
    }

    @Override
    public boolean hasLinkType(String s) {
        return linkMap.containsKey(s);
    }

    @Override
    public LinkData getLink(int i) {
        return new SQLModels<>(PlayerLinkModel.class).get(i);
    }

}
