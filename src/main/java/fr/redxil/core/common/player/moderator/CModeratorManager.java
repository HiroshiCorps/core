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
import fr.redxil.core.common.data.moderator.ModeratorDataRedis;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class CModeratorManager implements ModeratorManager {

    List<Long> loadedModerator = API.getInstance().getRedisManager().getRedissonClient().getList(ModeratorDataRedis.LIST_MODERATOR.getString());

    @Override
    public APIPlayerModerator loadModerator(long id, UUID uuid, String name) {
        APIPlayerModerator pm = getModerator(id);
        if (pm != null) return pm;
        return CPlayerModerator.initModerator(id, uuid, name);
    }

    /**
     * Get the moderator with this name / nick
     *
     * @param s This need to be the name of the player / nick supported but take more query on redis
     * @return APIPlayerModerator or null if player is not loaded or not a moderator
     */

    @Override
    public APIPlayerModerator getModerator(String s) {
        APIPlayer apiPlayer = API.getInstance().getPlayerManager().getPlayer(s);
        if (apiPlayer == null) return null;
        return getModerator(apiPlayer.getMemberID());
    }

    /**
     * Get the moderator with the MemberID
     *
     * @param l this need to be the MemberID of the APIPlayer
     * @return APIPlayerModerator or null if player is not loaded or not a moderator
     */

    @Override
    public APIPlayerModerator getModerator(long l) {
        if (!isLoaded(l)) return null;
        return new CPlayerModerator(l);
    }

    /**
     * Get the moderator with this UUID, please prefer ModeratorManager.getModerator(MemberID) if you can
     *
     * @param uuid this need to be the UUID of the APIPlayer
     * @return APIPlayerModerator or null if player is not loaded or not a moderator
     */

    @Override
    public APIPlayerModerator getModerator(UUID uuid) {
        APIPlayer apiPlayer = API.getInstance().getPlayerManager().getPlayer(uuid);
        if (apiPlayer == null) return null;
        return getModerator(apiPlayer.getMemberID());
    }

    /**
     * Get a list of the connected moderator
     *
     * @return The MemberID of the connected Moderator
     */

    @Override
    public Collection<Long> getLoadedModerator() {
        return loadedModerator;
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
        APIPlayer apiPlayer = API.getInstance().getPlayerManager().getPlayer(uuid);
        if (apiPlayer == null) return false;
        return apiPlayer.getRank().isModeratorRank();
    }

    /**
     * Check if a player is a server moderator
     *
     * @param memberID this need to be the MemberID of the APIPlayer
     * @return True if the player is a moderator
     */

    @Override
    public boolean isModerator(long memberID) {
        APIPlayer apiPlayer = API.getInstance().getPlayerManager().getPlayer(memberID);
        if (apiPlayer == null) return false;
        return apiPlayer.getRank().isModeratorRank();
    }

    /**
     * Check if a player is a server moderator
     *
     * @param name This need to be the name of the player / nick supported but take more query on redis
     * @return True if the player is a moderator
     */

    @Override
    public boolean isModerator(String name) {
        APIPlayer apiPlayer = API.getInstance().getPlayerManager().getPlayer(name);
        if (apiPlayer == null) return false;
        return apiPlayer.getRank().isModeratorRank();
    }

    @Override
    public boolean isLoaded(long memberID) {
        return getLoadedModerator().contains(memberID);
    }

}
