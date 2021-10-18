/* Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 */

package fr.redxil.core.common.moderator;

import fr.redxil.api.common.data.ModeratorDataValue;
import fr.redxil.api.common.data.PlayerDataValue;
import fr.redxil.api.common.data.utils.DataType;
import fr.redxil.api.common.moderators.APIPlayerModerator;
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.api.common.redis.RedisManager;
import fr.redxil.core.common.CoreAPI;
import fr.redxil.core.common.sql.SQLModel;
import fr.redxil.core.common.sql.SQLModels;
import org.redisson.api.RList;

import java.util.HashMap;
import java.util.UUID;

public class CPlayerModerator implements APIPlayerModerator {

    private final long memberID;

    public CPlayerModerator(long memberId) {
        this.memberID = memberId;
        new SQLModels<>(ModeratorModel.class).getFirst("WHERE " + ModeratorDataValue.MODERATOR_MEMBERID_SQL.getString(null) + " = ?", memberId);
    }

    protected static APIPlayerModerator initModerator(APIPlayer apiPlayer) {

        Long memberID = apiPlayer.getMemberId();

        if (!CoreAPI.get().getModeratorManager().isModerator(memberID)) return null;

        ModeratorModel model = new SQLModels<>(ModeratorModel.class).getOrInsert(new HashMap<String, Object>() {{
            this.put(ModeratorDataValue.MODERATOR_MEMBERID_SQL.getString(null, null), memberID);
            this.put(ModeratorDataValue.MODERATOR_MOD_SQL.getString(null, null), Boolean.valueOf(false).toString());
            this.put(ModeratorDataValue.MODERATOR_VANISH_SQL.getString(null, null), Boolean.valueOf(false).toString());
            this.put(ModeratorDataValue.MODERATOR_CIBLE_SQL.getString(null), null);
        }}, "WHERE " + PlayerDataValue.PLAYER_MEMBERID_SQL.getString(null) + " = ?", memberID);

        RedisManager rm = CoreAPI.get().getRedisManager();

        rm.setRedisString(ModeratorDataValue.MODERATOR_NAME_REDIS.getString(null, memberID), apiPlayer.getName());
        rm.setRedisString(ModeratorDataValue.MODERATOR_MOD_REDIS.getString(null, memberID), model.getString(ModeratorDataValue.MODERATOR_MOD_SQL.getString(null, null)));
        rm.setRedisString(ModeratorDataValue.MODERATOR_VANISH_REDIS.getString(null, memberID), model.getString(ModeratorDataValue.MODERATOR_VANISH_SQL.getString(null, null)));
        rm.setRedisString(ModeratorDataValue.MODERATOR_CIBLE_REDIS.getString(null, memberID), model.getString(ModeratorDataValue.MODERATOR_CIBLE_SQL.getString(null, null)));
        rm.setRedisString(ModeratorDataValue.MODERATOR_UUID_REDIS.getString(null, memberID), apiPlayer.getUUID().toString());

        RList<Long> idList = rm.getRedisList(ModeratorDataValue.LIST_MODERATOR.getString(null));
        if (!idList.contains(memberID))
            idList.add(memberID);

        return new CPlayerModerator(memberID);

    }

    @Override
    public boolean hasCible() {
        return getCible() != null;
    }

    @Override
    public String getCible() {
        return CoreAPI.get().getRedisManager().getRedisString(ModeratorDataValue.MODERATOR_CIBLE_REDIS.getString(this));
    }

    @Override
    public void setCible(String s) {
        CoreAPI.get().getRedisManager().setRedisString(ModeratorDataValue.MODERATOR_CIBLE_REDIS.getString(this), s);
    }

    @Override
    public boolean isVanish() {
        String bool = CoreAPI.get().getRedisManager().getRedisString(ModeratorDataValue.MODERATOR_VANISH_REDIS.getString(this));
        if (bool == null)
            return false;
        else
            return Boolean.parseBoolean(bool);
    }

    @Override
    public String getName() {
        return CoreAPI.get().getRedisManager().getRedisString(ModeratorDataValue.MODERATOR_NAME_REDIS.getString(this));
    }

    @Override
    public boolean isModeratorMod() {
        String bool = CoreAPI.get().getRedisManager().getRedisString(ModeratorDataValue.MODERATOR_MOD_REDIS.getString(this));
        if (bool == null)
            return false;
        else
            return Boolean.parseBoolean(bool);
    }

    @Override
    public APIPlayer getAPIPlayer() {
        return CoreAPI.get().getPlayerManager().getPlayer(getMemberId());
    }

    /**
     * @return This function return the MemberId of the moderator
     */

    @Override
    public long getMemberId() {
        return memberID;
    }

    @Override
    public void disconnectModerator() {
        if (!CoreAPI.get().isBungee()) return;

        long memberID = this.memberID;

        ModeratorModel model = new SQLModels<>(ModeratorModel.class).getFirst("WHERE " + PlayerDataValue.PLAYER_MEMBERID_SQL.getString(null) + " = ?", memberID);

        model.set(ModeratorDataValue.MODERATOR_MOD_SQL.getString(null, null), Boolean.valueOf(this.isModeratorMod()).toString());
        model.set(ModeratorDataValue.MODERATOR_VANISH_SQL.getString(null, null), Boolean.valueOf(this.isVanish()).toString());
        model.set(ModeratorDataValue.MODERATOR_CIBLE_SQL.getString(null, null), this.getCible());

        ModeratorDataValue.clearRedisData(DataType.PLAYER, this);

        CoreAPI.get().getRedisManager().getRedisList(ModeratorDataValue.LIST_MODERATOR.getString(null)).remove(memberID);
    }

    @Override
    public boolean isConnected() {
        return getAPIPlayer() != null;
    }

    /**
     * @return This function return the current UUID of the moderator
     */

    @Override
    public UUID getUUID() {
        String uuid = CoreAPI.get().getRedisManager().getRedisString(ModeratorDataValue.MODERATOR_UUID_REDIS.getString(this));
        if (uuid == null) return null;
        return UUID.fromString(uuid);
    }

    public static class ModeratorModel extends SQLModel {

        public ModeratorModel() {
            super("moderator", ModeratorDataValue.MODERATOR_MEMBERID_SQL.getString(null));
        }

    }

}