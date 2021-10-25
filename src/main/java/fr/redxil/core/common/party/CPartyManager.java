/* Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 */

package fr.redxil.core.common.party;

import fr.redxil.api.common.party.Party;
import fr.redxil.api.common.party.PartyManager;
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.core.common.CoreAPI;
import fr.redxil.core.common.data.PartyDataValue;

public class CPartyManager implements PartyManager {

    @Override
    public Party createParty(APIPlayer apiPlayer) {
        if (hasParty(apiPlayer)) return getParty(apiPlayer);
        return getParty(CParty.initParty(apiPlayer));
    }

    @Override
    public Party getParty(APIPlayer apiPlayer) {
        if (!hasParty(apiPlayer)) return null;
        return getParty((long) CoreAPI.get().getRedisManager().getRedisMap(PartyDataValue.PARTY_PLAYERMAP_REDIS.getString()).get(apiPlayer.getName()));
    }

    @Override
    public Party getParty(long l) {
        if (!hasParty(l)) return null;
        return new CParty(l);
    }

    @Override
    public boolean hasParty(APIPlayer apiPlayer) {
        return CoreAPI.get().getRedisManager().getRedisMap(PartyDataValue.PARTY_PLAYERMAP_REDIS.getString()).containsKey(apiPlayer.getName());
    }

    @Override
    public boolean hasParty(long l) {
        return CoreAPI.get().getRedisManager().getRedisMap(PartyDataValue.PARTY_PLAYERMAP_REDIS.getString()).containsValue(l);
    }

    @Override
    public boolean isOwner(APIPlayer apiPlayer) {
        Party party = getParty(apiPlayer);
        if (party == null) return false;
        return party.isPartyOwner(apiPlayer);
    }

}
