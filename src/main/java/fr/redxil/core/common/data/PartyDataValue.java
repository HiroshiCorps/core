/*
 *
 * Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 *
 */

package fr.redxil.core.common.data;

import fr.redxil.api.common.API;
import fr.redxil.api.common.group.party.Party;
import fr.redxil.core.common.data.utils.DataBaseType;
import fr.redxil.core.common.data.utils.DataType;
import org.redisson.api.RedissonClient;

public enum PartyDataValue {

    PARTY_PLAYERRANKMAP_REDIS(DataBaseType.REDIS, DataType.PLAYER, "party/<gameID>/playermap", true),
    PARTY_ACCESS_REDIS(DataBaseType.REDIS, DataType.PLAYER, "party/<gameID>/access", true),
    PARTY_INVITE_REDIS(DataBaseType.REDIS, DataType.PLAYER, "party/<gameID>/invite", true),
    PARTY_OWNER_REDIS(DataBaseType.REDIS, DataType.PLAYER, "party/<gameID>/owner", true),

    MAP_PLAYERPARTY_REDIS(DataBaseType.REDIS, DataType.GLOBAL, "party/linkmap", false);

    final DataType dataType;
    final DataBaseType dataBaseType;
    final String location;
    final boolean needID;

    PartyDataValue(DataBaseType dataBaseType, DataType dataType, String location, boolean needID) {
        this.dataBaseType = dataBaseType;
        this.dataType = dataType;
        this.location = location;
        this.needID = needID;
    }

    public static void clearRedisData(DataType dataType, Long partyID) {

        RedissonClient redissonClient = API.getInstance().getRedisManager().getRedissonClient();

        for (PartyDataValue mdv : values())
            if ((dataType == null || mdv.isDataType(dataType)) && mdv.isDataBase(DataBaseType.REDIS))
                if (mdv.isArgNeeded() && mdv.hasNeedInfo(partyID))
                    redissonClient.getBucket(mdv.getString(partyID)).delete();
                else if (!mdv.isArgNeeded() && partyID == null) redissonClient.getBucket(mdv.getString()).delete();

    }

    public String getString() {
        if(!hasNeedInfo(null))
            return null;
        return this.location;
    }

    public String getString(Party party) {
        return getString(party.getPartyID());
    }

    public String getString(Long partyID) {
        String location = this.location;

        if (needID) {
            if (partyID == null) return null;
            location = location.replace("<hostID>", partyID.toString());
        }

        return location;
    }

    public boolean hasNeedInfo(Long memberID) {
        return !isNeedID() || memberID != null;
    }

    public boolean isArgNeeded() {
        return isNeedID();
    }

    public boolean isNeedID() {
        return needID;
    }

    public boolean isDataBase(DataBaseType dataBaseType) {
        return this.dataBaseType.sqlBase.equals(dataBaseType.sqlBase);
    }

    public boolean isDataType(DataType dataType) {
        return this.dataType.equals(dataType);
    }

}
