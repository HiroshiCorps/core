/* Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 */

package fr.redxil.core.common.moderator;

import fr.redxil.api.common.data.ModeratorDataValue;
import fr.redxil.api.common.message.TextComponentBuilder;
import fr.redxil.api.common.moderators.APIPlayerModerator;
import fr.redxil.api.common.moderators.ModeratorManager;
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.core.common.CoreAPI;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class CModeratorManager implements ModeratorManager {

    @Override
    public APIPlayerModerator loadModerator(APIPlayer apiPlayer) {
        if (isLoaded(apiPlayer.getMemberId())) return getModerator(apiPlayer);
        return CPlayerModerator.initModerator(apiPlayer);
    }

    /**
     * Get the moderator with this name / nick
     *
     * @param s This need to be the name of the player / nick supported but take more query on redis
     * @return APIPlayerModerator or null if player is not loaded or not a moderator
     */

    @Override
    public APIPlayerModerator getModerator(String s) {
        APIPlayer apiPlayer = CoreAPI.get().getPlayerManager().getPlayer(s);
        if (apiPlayer == null) return null;
        return getModerator(apiPlayer);
    }

    /**
     * Get the moderator with the APIPlayer
     *
     * @param APIPlayer Of course, the APIPlayer
     * @return APIPlayerModerator or null if player is not loaded or not a moderator
     */

    @Override
    public APIPlayerModerator getModerator(APIPlayer apiPlayer) {
        return getModerator(apiPlayer.getMemberId());
    }

    /**
     * Get the moderator with the MemberId
     *
     * @param l this need to be the MemberId of the APIPlayer
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
        APIPlayer apiPlayer = CoreAPI.get().getPlayerManager().getPlayer(uuid);
        if (apiPlayer == null) return null;
        return getModerator(apiPlayer.getMemberId());
    }

    /**
     * Get a list of the connected moderator
     *
     * @return The MemberId of the connected Moderator
     */

    @Override
    public Collection<Long> getLoadedModerator() {
        List<Long> moderatorString = CoreAPI.get().getRedisManager().getRedissonClient().getList(ModeratorDataValue.LIST_MODERATOR.getString(null));
        return new ArrayList<Long>() {{
            for (long longS : moderatorString)
                add(longS);
        }};
    }

    @Override
    public void sendToModerators(TextComponentBuilder textComponentBuilder) {

        textComponentBuilder.sendToIDS(getLoadedModerator());

    }

    /**
     * Check if a player is a server moderator
     *
     * @param player Of course the APIPlayer
     * @return True if the player is a moderator
     */

    @Override
    public boolean isModerator(APIPlayer player) {
        return player.getRank().isModeratorRank();
    }

    /**
     * Check if a player is a server moderator
     *
     * @param uuid This need to be the UUID of the player
     * @return True if the player is a moderator
     */

    @Override
    public boolean isModerator(UUID uuid) {
        APIPlayer apiPlayer = CoreAPI.get().getPlayerManager().getPlayer(uuid);
        if (apiPlayer == null) return false;
        return isModerator(apiPlayer);
    }

    /**
     * Check if a player is a server moderator
     *
     * @param memberId this need to be the MemberId of the APIPlayer
     * @return True if the player is a moderator
     */

    @Override
    public boolean isModerator(long memberId) {
        APIPlayer apiPlayer = CoreAPI.get().getPlayerManager().getPlayer(memberId);
        if (apiPlayer == null) return false;
        return isModerator(apiPlayer);
    }

    /**
     * Check if a player is a server moderator
     *
     * @param name This need to be the name of the player / nick supported but take more query on redis
     * @return True if the player is a moderator
     */

    @Override
    public boolean isModerator(String name) {
        APIPlayer apiPlayer = CoreAPI.get().getPlayerManager().getPlayer(name);
        if (apiPlayer == null) return false;
        return isModerator(apiPlayer);
    }

    @Override
    public boolean isLoaded(long memberId) {
        return CoreAPI.get().getRedisManager().getRedissonClient().getList(ModeratorDataValue.LIST_MODERATOR.getString(null)).contains(memberId);
    }

}
