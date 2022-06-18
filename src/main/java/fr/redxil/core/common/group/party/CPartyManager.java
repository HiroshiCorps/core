/*
 *
 * Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 *
 */

package fr.redxil.core.common.group.party;

import fr.redxil.api.common.group.party.Party;
import fr.redxil.api.common.group.party.PartyManager;
import fr.redxil.core.common.data.game.PartyDataRedis;
import fr.redxil.core.common.utils.DataReminder;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class CPartyManager implements PartyManager {

    DataReminder<Map<String, Long>> nameToParty = DataReminder.generateMapReminder(PartyDataRedis.MAP_PLAYERPARTY_REDIS.getString());

    @Override
    public Optional<Party> createParty(UUID apiPlayer) {
        if (hasParty(apiPlayer))
            return Optional.empty();
        return Optional.of(new CParty(apiPlayer));
    }

    @Override
    public Optional<Party> getPlayerParty(UUID apiPlayer) {
        Long partyID = getUUIDToPartyIDMap().get(apiPlayer.toString());
        if (partyID == null) return Optional.empty();
        return Optional.of(new CParty(partyID));
    }

    @Override
    public Optional<Party> getParty(long l) {
        if (!isPartyExist(l)) return Optional.empty();
        return Optional.of(new CParty(l));
    }

    @Override
    public boolean hasParty(UUID apiPlayer) {
        return getUUIDToPartyIDMap().containsKey(apiPlayer.toString());
    }

    @Override
    public boolean isPartyExist(long l) {
        return getUUIDToPartyIDMap().containsValue(l);
    }

    @Override
    public Map<String, Long> getUUIDToPartyIDMap() {
        return nameToParty.getData();
    }

}
