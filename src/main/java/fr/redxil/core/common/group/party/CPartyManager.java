/* Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 */

package fr.redxil.core.common.group.party;

import fr.redxil.api.common.API;
import fr.redxil.api.common.group.party.Party;
import fr.redxil.api.common.group.party.PartyManager;
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.core.common.data.PartyDataRedis;

public class CPartyManager implements PartyManager {

    @Override
    public Party createParty(APIPlayer apiPlayer) {
        if (hasParty(apiPlayer)) return getPlayerParty(apiPlayer);
        return getParty(CParty.initParty(apiPlayer));
    }

    @Override
    public Party getPlayerParty(APIPlayer apiPlayer) {
        if (!hasParty(apiPlayer)) return null;
        return getParty((long) API.getInstance().getRedisManager().getRedisMap(PartyDataRedis.MAP_PLAYERPARTY_REDIS.getString()).get(apiPlayer.getMemberID()));
    }

    @Override
    public Party getParty(long l) {
        if (!isPartyExist(l)) return null;
        return new CParty(l);
    }

    @Override
    public boolean hasParty(APIPlayer apiPlayer) {
        return API.getInstance().getRedisManager().getRedisMap(PartyDataRedis.MAP_PLAYERPARTY_REDIS.getString()).containsKey(apiPlayer.getMemberID());
    }

    @Override
    public boolean isPartyExist(long l) {
        return API.getInstance().getRedisManager().getRedisMap(PartyDataRedis.MAP_PLAYERPARTY_REDIS.getString()).containsValue(l);
    }

}
