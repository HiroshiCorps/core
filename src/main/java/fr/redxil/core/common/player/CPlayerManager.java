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
import fr.redxil.api.common.utils.DataReminder;
import fr.redxil.core.common.data.player.PlayerDataRedis;
import fr.redxil.core.common.data.player.PlayerDataSql;
import fr.redxil.core.common.player.sqlmodel.player.PlayerLinkModel;
import fr.redxil.core.common.player.sqlmodel.player.PlayerModel;
import fr.redxil.core.common.sql.SQLModels;

import java.util.*;
import java.util.function.BiConsumer;

public class CPlayerManager implements APIPlayerManager {

    HashMap<String, BiConsumer<APIPlayer, LinkData>> linkMap = new HashMap<>();
    DataReminder<Map<String, Long>> nameToID = DataReminder.generateMapReminder(PlayerDataRedis.MAP_PLAYER_NAME.getString());
    DataReminder<Map<String, Long>> uuidToID = DataReminder.generateMapReminder(PlayerDataRedis.MAP_PLAYER_UUID.getString());
    Map<Long, CPlayer> playerMap = new HashMap<>();

    @Override
    public boolean dataExist(String s) {
        return isLoadedPlayer(s) || getOfflinePlayer(s).isPresent();
    }

    /**
     * Get the APIPlayer with this name / nick (supported but take more query on redis, please use APIPlayerManager.getPlayer(memberID) if you can)
     *
     * @param name This need to be the name of the player / nick
     * @return APIPlayer or null if the player is not loaded
     */
    @Override
    public Optional<APIPlayer> getPlayer(String name) {
        Long value = getNameToLongMap().get(name);
        if (value == null)
            return Optional.empty();
        if (!API.getInstance().isOnlineMod())
            return Optional.ofNullable(playerMap.get(value));
        return Optional.of(new CPlayer(value));
    }

    /**
     * Get the APIPlayer with this UUID (supported but take more query on redis, please use APIPlayerManager.getPlayer(memberID) if you can)
     *
     * @param uuid this need to be the UUID of the APIPlayer
     * @return APIPlayerModerator or null if player is not loaded or not a moderator
     */

    @Override
    public Optional<APIPlayer> getPlayer(UUID uuid) {
        Long value = getUUIDToLongMap().get(uuid.toString());
        if (value == null)
            return Optional.empty();
        if (!API.getInstance().isOnlineMod())
            return Optional.ofNullable(playerMap.get(value));
        return Optional.of(new CPlayer(value));
    }

    /**
     * Get the APIPlayer with the MemberID
     *
     * @param id this need to be the MemberID of the APIPlayer
     * @return APIPlayer or null if player is not loaded
     */

    @Override
    public Optional<APIPlayer> getPlayer(long id) {
        if (!isLoadedPlayer(id)) {
            return Optional.empty();
        }
        if (!API.getInstance().isOnlineMod())
            return Optional.ofNullable(playerMap.get(id));
        return Optional.of(new CPlayer(id));
    }

    /**
     * Get the APIPlayerOffline with the name
     *
     * @param uuid this need to be the name of the APIPlayer
     * @return APIPlayerOffline or null if the player never connected on the server
     */

    @Override
    public Optional<APIOfflinePlayer> getOfflinePlayer(UUID uuid) {

        Optional<APIPlayer> apiPlayer = getPlayer(uuid);
        if (apiPlayer.isPresent()) return Optional.of(apiPlayer.get());

        if (!API.getInstance().isOnlineMod())
            return Optional.empty();

        PlayerModel playerModel = new SQLModels<>(PlayerModel.class).getFirst("WHERE " + PlayerDataSql.PLAYER_UUID_SQL.getSQLColumns().toSQL() + " = ?", uuid.toString());

        if (playerModel != null) {
            return Optional.of(new CPlayerOffline(playerModel));
        }

        return Optional.empty();

    }

    /**
     * Get the APIPlayerOffline with the name
     *
     * @param name this need to be the name of the APIPlayer
     * @return APIPlayerOffline or null if the player never connected on the server
     */

    @Override
    public Optional<APIOfflinePlayer> getOfflinePlayer(String name) {

        Optional<APIPlayer> apiPlayer = getPlayer(name);
        if (apiPlayer.isPresent()) return Optional.of(apiPlayer.get());

        if (!API.getInstance().isOnlineMod())
            return Optional.empty();

        PlayerModel playerModel = new SQLModels<>(PlayerModel.class).getFirst("WHERE " + PlayerDataSql.PLAYER_NAME_SQL.getSQLColumns().toSQL() + " = ?", name);

        if (playerModel != null) {
            return Optional.of(new CPlayerOffline(playerModel));
        }
        return Optional.empty();
    }

    /**
     * Get the APIPlayerOffline with the name
     *
     * @param memberID this need to be the MemberID of the APIPlayer
     * @return APIPlayerOffline or null if the player never connected on the server
     */

    @Override
    public Optional<APIOfflinePlayer> getOfflinePlayer(long memberID) {
        Optional<APIPlayer> apiPlayer = getPlayer(memberID);
        if (apiPlayer.isPresent()) return Optional.of(apiPlayer.get());

        if (!API.getInstance().isOnlineMod())
            return Optional.empty();

        PlayerModel playerModel = new SQLModels<>(PlayerModel.class).getFirst("WHERE " + PlayerDataSql.PLAYER_MEMBERID_SQL.getSQLColumns().toSQL() + " = ?", memberID);
        if (playerModel == null)
            return Optional.empty();
        return Optional.of(new CPlayerOffline(playerModel));
    }

    @Override
    public Optional<APIPlayer> loadPlayer(String p, UUID uuid, IpInfo ipInfo) {
        if (isLoadedPlayer(uuid)) return Optional.empty();
        return Optional.of(new CPlayer(p, uuid, ipInfo));
    }

    @Override
    public boolean isLoadedPlayer(String p) {
        return getNameToLongMap().containsKey(p);
    }

    @Override
    public boolean isLoadedPlayer(long l) {
        return getNameToLongMap().containsValue(l);
    }

    @Override
    public boolean isLoadedPlayer(UUID uuid) {
        return getUUIDToLongMap().containsKey(uuid.toString());
    }

    @Override
    public Collection<Long> getLoadedPlayer() {
        return getNameToLongMap().values();
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
    public Optional<LinkData> getLink(int i) {
        return Optional.ofNullable(new SQLModels<>(PlayerLinkModel.class).get(i));
    }

    @Override
    public Map<String, Long> getNameToLongMap() {
        return nameToID.getData();
    }

    @Override
    public Map<String, Long> getUUIDToLongMap() {
        return uuidToID.getData();
    }

    public Map<Long, CPlayer> getMap() {
        return playerMap;
    }

}
