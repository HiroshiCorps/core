/*
 *
 * Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 *
 */

package fr.redxil.core.common.player.moderator;

import fr.redxil.api.common.API;
import fr.redxil.api.common.message.TextComponentBuilder;
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.api.common.player.moderators.APIPlayerModerator;
import fr.redxil.api.common.player.moderators.ModeratorManager;
import fr.redxil.api.common.utils.DataReminder;
import fr.redxil.core.common.CoreAPI;
import fr.redxil.core.common.data.moderator.ModeratorDataRedis;
import fr.redxil.core.common.player.CServerPlayer;

import java.util.*;

public class CModeratorManager implements ModeratorManager {

    DataReminder<Map<String, Long>> nameToLong = DataReminder.generateMapReminder(ModeratorDataRedis.MAP_MODERATOR_NAME.getString());
    DataReminder<Map<String, Long>> uuidToLong = DataReminder.generateMapReminder(ModeratorDataRedis.MAP_MODERATOR_UUID.getString());
    Map<Long, CPlayerModerator> map = new HashMap<>();

    @Override
    public Optional<APIPlayerModerator> loadModerator(long id, UUID uuid, String name) {
        if(CoreAPI.getInstance().getPlayerManager().getServerPlayer().getMemberID() == id)
            return Optional.empty();
        if (isLoaded(id)) return Optional.empty();
        if (isModerator(uuid)) {
            return Optional.of(new CPlayerModerator(id, uuid, name));
        }
        return Optional.empty();
    }

    /**
     * Get the moderator with this name / nick
     *
     * @param s This need to be the name of the player / nick supported but take more query on redis
     * @return APIPlayerModerator or null if player is not loaded or not a moderator
     */

    @Override
    public Optional<APIPlayerModerator> getModerator(String s) {
        if(API.getInstance().getPlayerManager().getServerPlayer().getName() == s)
            return Optional.of(new CServerModerator());
        Long result = uuidToLong.getData().get(s);
        if (result == null) return Optional.empty();
        if (API.getInstance().isOnlineMod())
            return Optional.of(new CPlayerModerator(result));
        return Optional.ofNullable(getMap().get(result));
    }

    /**
     * Get the moderator with this APIPlayer
     *
     * @param s This need to be the name of the player / nick supported but take more query on redis
     * @return APIPlayerModerator or null if player is not loaded or not a moderator
     */

    @Override
    public Optional<APIPlayerModerator> getModerator(APIPlayer s) {
        if(s instanceof CServerPlayer)
            return Optional.of(getServerModerator());
        return getModerator(s.getMemberID());
    }

    /**
     * Get the moderator with the MemberID
     *
     * @param result this need to be the MemberID of the APIPlayer
     * @return APIPlayerModerator or null if player is not loaded or not a moderator
     */

    @Override
    public Optional<APIPlayerModerator> getModerator(long result) {
        if(API.getInstance().getPlayerManager().getServerPlayer().getMemberID() == result)
            return Optional.of(new CServerModerator());
        if (isLoaded(result))
            return Optional.empty();
        if (API.getInstance().isOnlineMod())
            return Optional.of(new CPlayerModerator(result));
        return Optional.ofNullable(getMap().get(result));
    }

    /**
     * Get the moderator with this UUID, please prefer ModeratorManager.getModerator(MemberID) if you can
     *
     * @param uuid this need to be the UUID of the APIPlayer
     * @return APIPlayerModerator or null if player is not loaded or not a moderator
     */

    @Override
    public Optional<APIPlayerModerator> getModerator(UUID uuid) {
        if(API.getInstance().getPlayerManager().getServerPlayer().getUUID() == uuid)
            return Optional.of(new CServerModerator());
        Long result = uuidToLong.getData().get(uuid.toString());
        if (result == null) return Optional.empty();
        if (API.getInstance().isOnlineMod())
            return Optional.of(new CPlayerModerator(result));
        return Optional.ofNullable(getMap().get(result));
    }

    /**
     * Get a list of the connected moderator
     *
     * @return The MemberID of the connected Moderator
     */

    @Override
    public Collection<Long> getLoadedModerator() {
        return uuidToLong.getData().values();
    }

    @Override
    public Map<String, Long> getStringToLongModerator() {
        return nameToLong.getData();
    }

    @Override
    public Map<String, Long> getUUIDToLongModerator() {
        return uuidToLong.getData();
    }

    @Override
    public void sendToModerators(TextComponentBuilder textComponentBuilder) {

        textComponentBuilder.sendToIDS(getLoadedModerator());

    }

    /**
     * Check if a player is a server moderator
     *
     * @param uuid This need to be the UUID of the player
     * @return True if the player is a moderator
     */

    @Override
    public boolean isModerator(UUID uuid) {
        Optional<APIPlayer> apiPlayer = API.getInstance().getPlayerManager().getPlayer(uuid);
        if (apiPlayer.isEmpty()) return false;
        else return apiPlayer.get().getRank().isModeratorRank();
    }

    /**
     * Check if a player is a server moderator
     *
     * @param memberID this need to be the MemberID of the APIPlayer
     * @return True if the player is a moderator
     */

    @Override
    public boolean isModerator(long memberID) {
        Optional<APIPlayer> apiPlayer = API.getInstance().getPlayerManager().getPlayer(memberID);
        if (apiPlayer.isEmpty()) return false;
        else return apiPlayer.get().getRank().isModeratorRank();
    }

    /**
     * Check if a player is a server moderator
     *
     * @param name This need to be the name of the player / nick supported but take more query on redis
     * @return True if the player is a moderator
     */

    @Override
    public boolean isModerator(String name) {
        Optional<APIPlayer> apiPlayer = API.getInstance().getPlayerManager().getPlayer(name);
        if (apiPlayer.isEmpty()) return false;
        else return apiPlayer.get().getRank().isModeratorRank();
    }

    @Override
    public boolean isLoaded(long memberID) {
        return getStringToLongModerator().containsValue(memberID);
    }

    @Override
    public boolean isLoaded(UUID uuid) {
        return getUUIDToLongModerator().containsKey(uuid.toString());
    }

    @Override
    public boolean isLoaded(String s) {
        return getStringToLongModerator().containsKey(s);
    }

    public Map<Long, CPlayerModerator> getMap() {
        return map;
    }

    @Override
    public APIPlayerModerator getServerModerator() {
        return new CServerModerator();
    }


    
}
