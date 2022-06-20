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
import fr.redxil.api.common.group.party.PartyAccess;
import fr.redxil.api.common.group.party.PartyRank;
import fr.redxil.core.common.CoreAPI;
import fr.redxil.core.common.data.IDDataValue;
import fr.redxil.core.common.data.game.PartyDataRedis;
import fr.redxil.core.common.data.utils.DataType;
import fr.redxil.core.common.utils.DataReminder;
import fr.redxil.core.common.utils.IDGenerator;

import java.util.*;

public class CParty implements Party {

    final long id;
    DataReminder<String> accessReminder = null;
    DataReminder<UUID> ownerReminder = null;
    DataReminder<Map<UUID, String>> rankMapReminder = null;
    DataReminder<List<UUID>> inviteReminder = null;

    public CParty(Long id) {
        this.id = id;
    }

    public CParty(UUID owner) {
        this.id = IDGenerator.generateLONGID(IDDataValue.PARTY.getLocation());
        setPartyAccess(PartyAccess.INVITE);
        initOwnerReminder();
        ownerReminder.setData(owner);
        setPartyRank(owner, PartyRank.OWNER);
        CoreAPI.getInstance().getPartyManager().getUUIDToPartyIDMap().put(owner.toString(), getPartyID());
    }

    @Override
    public boolean joinParty(UUID apiPlayer) {
        if (CoreAPI.getInstance().getPartyManager().hasParty(apiPlayer)) return false;
        switch (getPartyAccess()) {
            case CLOSE: {
                return false;
            }
            case INVITE: {
                if (!hisInvitePlayer(apiPlayer))
                    return false;
            }
            default: {
                break;
            }
        }

        revokeInvite(apiPlayer);
        setPartyRank(apiPlayer, PartyRank.PLAYER);
        CoreAPI.getInstance().getPartyManager().getUUIDToPartyIDMap().put(apiPlayer.toString(), getPartyID());
        return true;
    }

    @Override
    public boolean quitParty(UUID apiPlayer) {
        if (!hisInParty(apiPlayer))
            return false;
        if (isPartyOwner(apiPlayer))
            return deleteParty(apiPlayer);
        revokeInvite(apiPlayer);
        CoreAPI.getInstance().getPartyManager().getUUIDToPartyIDMap().remove(apiPlayer.toString());
        return true;
    }

    public void initRankMapReminder() {
        if (rankMapReminder == null)
            rankMapReminder = DataReminder.generateMapReminder(PartyDataRedis.PARTY_PLAYERRANKMAP_REDIS.getString(this));
    }

    @Override
    public boolean kickParty(UUID kicker, UUID apiPlayer1) {
        if (!hisInParty(kicker) || !hisInParty(apiPlayer1))
            return false;
        if (!getPartyRank(kicker).orElse(PartyRank.PLAYER).isOver(getPartyRank(apiPlayer1).orElse(PartyRank.ADMIN)))
            return false;
        initRankMapReminder();
        rankMapReminder.getData().remove(apiPlayer1);
        CoreAPI.getInstance().getPartyManager().getUUIDToPartyIDMap().remove(apiPlayer1.toString());

        return true;
    }

    @Override
    public boolean setPartyRank(UUID apiPlayer1, PartyRank partyRank) {
        if (!hisInParty(apiPlayer1)) return false;

        initRankMapReminder();
        rankMapReminder.getData().replace(apiPlayer1, partyRank.getRankName());

        return true;
    }

    @Override
    public Optional<PartyRank> getPartyRank(UUID apiPlayer) {
        initRankMapReminder();
        return PartyRank.getPartyRank(rankMapReminder.getData().get(apiPlayer));
    }

    public void initPartyAccess() {
        if (accessReminder == null)
            accessReminder = DataReminder.generateReminder(PartyDataRedis.PARTY_ACCESS_REDIS.getString(this), PartyAccess.INVITE.getAccessName());
    }

    @Override
    public PartyAccess getPartyAccess() {
        initPartyAccess();
        return PartyAccess.getPartyAccess(accessReminder.getData()).orElse(null);
    }

    @Override
    public void setPartyAccess(PartyAccess partyAccess) {
        accessReminder.setData(partyAccess.getAccessName());
    }

    @Override
    public boolean hisInParty(UUID apiPlayer) {
        initRankMapReminder();
        return rankMapReminder.getData().containsKey(apiPlayer);
    }

    @Override
    public List<UUID> getPlayerList() {
        initRankMapReminder();
        return new ArrayList<>() {{
            addAll(rankMapReminder.getData().keySet());
        }};
    }

    @Override
    public boolean deleteParty(UUID apiPlayer) {
        Optional<PartyRank> pr = getPartyRank(apiPlayer);
        if (pr.isEmpty() || !pr.get().equals(PartyRank.ADMIN)) return false;
        for (UUID apiPlayers : getPlayerList())
            quitParty(apiPlayers);
        PartyDataRedis.clearRedisData(DataType.PLAYER, getPartyID());
        return true;
    }

    @Override
    public long getPartyID() {
        return id;
    }

    public void initInviteReminder() {
        if (inviteReminder == null)
            inviteReminder = DataReminder.generateListReminder(PartyDataRedis.PARTY_INVITE_REDIS.getString(this));
    }

    @Override
    public boolean invitePlayer(UUID apiPlayer) {
        if (hisInvitePlayer(apiPlayer)) return false;
        initInviteReminder();
        inviteReminder.getData().add(apiPlayer);
        return true;
    }

    @Override
    public boolean revokeInvite(UUID apiPlayer) {
        if (!hisInvitePlayer(apiPlayer)) return false;
        initInviteReminder();
        inviteReminder.getData().remove(apiPlayer);
        return true;
    }

    @Override
    public boolean hisInvitePlayer(UUID apiPlayer) {
        initInviteReminder();
        return inviteReminder.getData().contains(apiPlayer);
    }

    public void initOwnerReminder() {
        if (ownerReminder == null)
            ownerReminder = DataReminder.generateReminder(PartyDataRedis.PARTY_OWNER_REDIS.getString(this), null);
    }

    @Override
    public boolean isPartyOwner(UUID apiPlayer) {
        return apiPlayer == getPartyOwner();
    }

    @Override
    public UUID getPartyOwner() {
        return ownerReminder.getData();
    }

    @Override
    public Map<UUID, PartyRank> getRank() {
        initRankMapReminder();
        return new HashMap<>() {{
            for (Entry<UUID, String> entry : rankMapReminder.getData().entrySet()) {
                put(entry.getKey(), PartyRank.getPartyRank(entry.getValue()).orElse(PartyRank.PLAYER));
            }
        }};
    }

}
