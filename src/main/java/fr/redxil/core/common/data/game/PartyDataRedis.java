/*
 *
 * Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 *
 */

package fr.redxil.core.common.data.game;

import fr.redxil.api.common.API;
import fr.redxil.api.common.group.party.Party;
import fr.redxil.core.common.data.utils.DataType;
import org.redisson.api.RedissonClient;

public enum PartyDataRedis {

    PARTY_PLAYERRANKMAP_REDIS(DataType.PLAYER, "party/<gameID>/playermap", true),
    PARTY_ACCESS_REDIS(DataType.PLAYER, "party/<gameID>/access", true),
    PARTY_INVITE_REDIS(DataType.PLAYER, "party/<gameID>/invite", true),
    PARTY_OWNER_REDIS(DataType.PLAYER, "party/<gameID>/owner", true),

    MAP_PLAYERPARTY_REDIS(DataType.GLOBAL, "party/linkmap", false);

    final DataType dataType;
    final String location;
    final boolean needID;

    PartyDataRedis(DataType dataType, String location, boolean needID) {
        this.dataType = dataType;
        this.location = location;
        this.needID = needID;
    }

    public static void clearRedisData(DataType dataType, Long playerID) {

        API.getInstance().getRedisManager().ifPresent(redis -> {
            RedissonClient redissonClient = redis.getRedissonClient();
            for (PartyDataRedis mdv : values())
                if ((dataType == null || mdv.isDataType(dataType)))
                    if (mdv.hasNeedInfo(playerID))
                        redissonClient.getBucket(mdv.getString(playerID)).delete();
        });

    }

    public String getString() {
        if (!hasNeedInfo(null)) return null;
        return location;
    }

    public String getString(Party apiPlayer) {
        return getString(apiPlayer.getPartyID());
    }

    public String getString(Long playerID) {
        String location = this.location;

        if (needID) {
            if (playerID == null) return null;
            location = location.replace("<gameID>", playerID.toString());
        }

        return location;
    }

    public boolean hasNeedInfo(Long memberID) {
        return !isNeedID() || memberID != null;
    }

    public boolean isNeedID() {
        return needID;
    }

    public boolean isDataType(DataType dataType) {
        return this.dataType.equals(dataType);
    }

}
