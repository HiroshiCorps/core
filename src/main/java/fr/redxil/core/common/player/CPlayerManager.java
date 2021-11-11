/* Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 */

package fr.redxil.core.common.player;

import fr.redline.pms.utils.IpInfo;
import fr.redxil.api.common.player.APIOfflinePlayer;
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.api.common.player.APIPlayerManager;
import fr.redxil.core.common.CoreAPI;
import fr.redxil.core.common.data.PlayerDataValue;
import fr.redxil.core.common.sql.SQLModels;
import fr.redxil.core.common.sql.player.PlayerModel;

import java.util.List;
import java.util.UUID;

public class CPlayerManager implements APIPlayerManager {

    /**
     * Get the APIPlayer with this name / nick (supported but take more query on redis, please use APIPlayerManager.getPlayer(memberID) if you can)
     *
     * @param name This need to be the name of the player / nick
     * @return APIPlayer or null if the player is not loaded
     */

    public APIPlayer getPlayer(String name) {
        if (!isLoadedPlayer(name)) {
            return CoreAPI.get().getNickGestion().getAPIPlayer(name);
        }
        return getPlayer((long) CoreAPI.get().getRedisManager().getRedissonClient().getMap(PlayerDataValue.MAP_PLAYER_NAME.getString(null)).get(name));
    }

    /**
     * Get the APIPlayer with this UUID (supported but take more query on redis, please use APIPlayerManager.getPlayer(memberID) if you can)
     *
     * @param uuid this need to be the UUID of the APIPlayer
     * @return APIPlayerModerator or null if player is not loaded or not a moderator
     */

    @Override
    public APIPlayer getPlayer(UUID uuid) {
        if (!isLoadedPlayer(uuid)) {
            return null;
        }
        return getPlayer((long) CoreAPI.get().getRedisManager().getRedissonClient().getMap(PlayerDataValue.MAP_PLAYER_UUID.getString(null)).get(uuid.toString()));
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
        APIPlayer apiPlayer = getPlayer(uuid);
        if (apiPlayer != null) return apiPlayer;

        if (new SQLModels<>(PlayerModel.class).getFirst("WHERE " + PlayerDataValue.PLAYER_UUID_SQL.getString(null) + " = ?", uuid) == null)
            return CoreAPI.get().getNickGestion().getAPIOfflinePlayer(uuid.toString());
        return new CPlayerOffline(uuid);
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

        if (new SQLModels<>(PlayerModel.class).getFirst("WHERE " + PlayerDataValue.PLAYER_NAME_SQL.getString(null) + " = ?", name) == null)
            return CoreAPI.get().getNickGestion().getAPIOfflinePlayer(name);
        return new CPlayerOffline(name);
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
        return CoreAPI.get().getRedisManager().getRedissonClient().getMap(PlayerDataValue.MAP_PLAYER_NAME.getString(null)).containsKey(p);
    }

    @Override
    public boolean isLoadedPlayer(long l) {
        return CoreAPI.get().getRedisManager().getRedissonClient().getList(PlayerDataValue.LIST_PLAYER_ID.getString(null)).contains(l);
    }

    @Override
    public boolean isLoadedPlayer(UUID uuid) {
        return CoreAPI.get().getRedisManager().getRedissonClient().getMap(PlayerDataValue.MAP_PLAYER_UUID.getString(null)).containsKey(uuid.toString());
    }

    @Override
    public List<Long> getLoadedPlayer() {
        return CoreAPI.get().getRedisManager().getRedissonClient().getList(PlayerDataValue.LIST_PLAYER_ID.getString(null));
    }

}
