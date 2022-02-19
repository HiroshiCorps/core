/* Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 */

package fr.redxil.core.common.player;

import fr.redline.pms.utils.IpInfo;
import fr.redxil.api.common.API;
import fr.redxil.api.common.player.APIOfflinePlayer;
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.api.common.player.APIPlayerManager;
import fr.redxil.api.common.player.data.LinkData;
import fr.redxil.core.common.data.PlayerDataValue;
import fr.redxil.core.common.player.sqlmodel.player.PlayerLinkModel;
import fr.redxil.core.common.player.sqlmodel.player.PlayerModel;
import fr.redxil.core.common.sql.SQLModels;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.logging.Level;

public class CPlayerManager implements APIPlayerManager {

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
        Map<String, Long> playerMap = API.getInstance().getRedisManager().getRedissonClient().getMap(PlayerDataValue.MAP_PLAYER_NAME.getString(null));
        if (!playerMap.containsKey(name)) {
            return null;
        }
        return new CPlayer(playerMap.get(name));
    }

    /**
     * Get the APIPlayer with this UUID (supported but take more query on redis, please use APIPlayerManager.getPlayer(memberID) if you can)
     *
     * @param uuid this need to be the UUID of the APIPlayer
     * @return APIPlayerModerator or null if player is not loaded or not a moderator
     */

    @Override
    public APIPlayer getPlayer(UUID uuid) {
        Map<String, Long> playerMap = API.getInstance().getRedisManager().getRedissonClient().getMap(PlayerDataValue.MAP_PLAYER_UUID.getString(null));
        if (!playerMap.containsKey(uuid.toString())) {
            return null;
        }
        return new CPlayer(playerMap.get(uuid.toString()));
    }

    /**
     * Get the APIPlayer with the MemberId
     *
     * @param id this need to be the MemberId of the APIPlayer
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
        API.getInstance().getPluginEnabler().printLog(Level.FINE, "OPPUUID - 1");

        APIPlayer apiPlayer = getPlayer(uuid);
        if (apiPlayer != null) return apiPlayer;

        API.getInstance().getPluginEnabler().printLog(Level.FINE, "OPPUUID - 2");

        PlayerModel playerModel = new SQLModels<>(PlayerModel.class).getFirst("WHERE " + PlayerDataValue.PLAYER_UUID_SQL.getString(null) + " = ?", uuid.toString());

        if (playerModel != null) {
            API.getInstance().getPluginEnabler().printLog(Level.FINE, "OPPUUID - 3");
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

        API.getInstance().getPluginEnabler().printLog(Level.FINE, "OPPNAME - 1");

        APIPlayer apiPlayer = getPlayer(name);
        if (apiPlayer != null) return apiPlayer;

        API.getInstance().getPluginEnabler().printLog(Level.FINE, "OPPNAME - 2");

        PlayerModel playerModel = new SQLModels<>(PlayerModel.class).getFirst("WHERE " + PlayerDataValue.PLAYER_NAME_SQL.getString(null) + " = ?", name);

        if (playerModel != null) {
            API.getInstance().getPluginEnabler().printLog(Level.FINE, "OPPNAME - 3");
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

        if (new SQLModels<>(PlayerModel.class).getFirst("WHERE " + PlayerDataValue.PLAYER_MEMBERID_SQL.getString(null) + " = ?", memberID) == null)
            return null;
        return new CPlayerOffline(memberID);
    }

    @Override
    public APIPlayer loadPlayer(String p, UUID uuid, IpInfo ipInfo) {
        if (isLoadedPlayer(uuid)) return getPlayer(uuid);
        return CPlayer.loadPlayer(p, uuid, ipInfo);
    }

    @Override
    public boolean isLoadedPlayer(String p) {
        return API.getInstance().getRedisManager().getRedissonClient().getMap(PlayerDataValue.MAP_PLAYER_NAME.getString(null)).containsKey(p);
    }

    @Override
    public boolean isLoadedPlayer(long l) {
        return API.getInstance().getRedisManager().getRedissonClient().getList(PlayerDataValue.LIST_PLAYER_ID.getString(null)).contains(l);
    }

    @Override
    public boolean isLoadedPlayer(UUID uuid) {
        return API.getInstance().getRedisManager().getRedissonClient().getMap(PlayerDataValue.MAP_PLAYER_UUID.getString(null)).containsKey(uuid.toString());
    }

    @Override
    public List<Long> getLoadedPlayer() {
        return API.getInstance().getRedisManager().getRedissonClient().getList(PlayerDataValue.LIST_PLAYER_ID.getString(null));
    }

    HashMap<String, BiConsumer<APIPlayer, LinkData>> linkMap = new HashMap<>();

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
