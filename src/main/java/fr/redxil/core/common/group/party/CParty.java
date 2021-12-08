/* Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 */

package fr.redxil.core.common.group.party;

import fr.redxil.api.common.API;
import fr.redxil.api.common.group.party.Party;
import fr.redxil.api.common.group.party.PartyAccess;
import fr.redxil.api.common.group.party.PartyRank;
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.api.common.player.APIPlayerManager;
import fr.redxil.api.common.redis.RedisManager;
import fr.redxil.core.common.data.IDDataValue;
import fr.redxil.core.common.data.PartyDataValue;
import fr.redxil.core.common.data.utils.DataType;
import fr.redxil.core.common.redis.IDGenerator;
import org.redisson.api.RMap;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CParty implements Party {

    final long id;

    public CParty(Long id) {
        this.id = id;
    }

    public static long initParty(APIPlayer owner) {
        long partyID = IDGenerator.generateLONGID(IDDataValue.PARTY);
        RedisManager rm = API.getInstance().getRedisManager();
        rm.setRedisString(PartyDataValue.PARTY_ACCESS_REDIS.getString(partyID), PartyAccess.FRIEND.getAccessName());
        rm.setRedisLong(PartyDataValue.PARTY_OWNER_REDIS.getString(partyID), owner.getMemberId());
        rm.getRedisMap(PartyDataValue.PARTY_PLAYERRANKMAP_REDIS.getString(partyID)).put(owner.getMemberId(), PartyRank.OWNER.getRankName());
        rm.getRedisMap(PartyDataValue.MAP_PLAYERPARTY_REDIS.getString()).put(owner.getMemberId(), partyID);
        return partyID;
    }

    @Override
    public boolean joinParty(APIPlayer apiPlayer) {
        if (API.getInstance().getPartyManager().hasParty(apiPlayer)) return false;
        switch (getPartyAccess()) {
            case CLOSE: {
                return false;
            }
            case FRIEND: {
                if (!hisInvitePlayer(apiPlayer) && !getPartyOwner().hasFriend(apiPlayer))
                    return false;
            }
        }

        revokeInvite(apiPlayer);
        API.getInstance().getRedisManager().getRedisMap(PartyDataValue.PARTY_PLAYERRANKMAP_REDIS.getString(this)).put(apiPlayer.getMemberId(), PartyRank.PLAYER.getRankName());
        API.getInstance().getRedisManager().getRedisMap(PartyDataValue.MAP_PLAYERPARTY_REDIS.getString(this)).put(apiPlayer.getMemberId(), getPartyID());
        return true;
    }

    @Override
    public boolean quitParty(APIPlayer apiPlayer) {
        if (!hisInParty(apiPlayer))
            return false;
        if (isPartyOwner(apiPlayer))
            return deleteParty(apiPlayer);
        API.getInstance().getRedisManager().getRedisMap(PartyDataValue.PARTY_PLAYERRANKMAP_REDIS.getString(this)).remove(apiPlayer.getMemberId());
        API.getInstance().getRedisManager().getRedisMap(PartyDataValue.MAP_PLAYERPARTY_REDIS.getString(this)).remove(apiPlayer.getMemberId());
        return true;
    }

    @Override
    public boolean kickParty(APIPlayer kicker, APIPlayer apiPlayer1) {
        if (!hisInParty(kicker) || !hisInParty(apiPlayer1))
            return false;
        if (!getPartyRank(kicker).isOver(getPartyRank(apiPlayer1)))
            return false;

        API.getInstance().getRedisManager().getRedisMap(PartyDataValue.PARTY_PLAYERRANKMAP_REDIS.getString(this)).remove(apiPlayer1.getMemberId());
        API.getInstance().getRedisManager().getRedisMap(PartyDataValue.MAP_PLAYERPARTY_REDIS.getString(this)).remove(apiPlayer1.getMemberId());

        return true;
    }

    @Override
    public boolean setPartyRank(APIPlayer ranker, APIPlayer apiPlayer1, PartyRank partyRank) {
        if (!hisInParty(apiPlayer1) || !hisInParty(ranker)) return false;
        PartyRank rankerRank = getPartyRank(ranker);
        if (!rankerRank.isOver(getPartyRank(apiPlayer1)) || !rankerRank.isOver(partyRank))
            return false;

        RMap<Long, String> rankMap = API.getInstance().getRedisManager().getRedisMap(PartyDataValue.PARTY_PLAYERRANKMAP_REDIS.getString(this));
        rankMap.remove(apiPlayer1.getMemberId());
        rankMap.put(apiPlayer1.getMemberId(), partyRank.getRankName());

        return true;
    }

    @Override
    public PartyRank getPartyRank(APIPlayer apiPlayer) {
        if (!hisInParty(apiPlayer))
            return null;
        return PartyRank.getPartyRank((String) API.getInstance().getRedisManager().getRedisMap(PartyDataValue.PARTY_PLAYERRANKMAP_REDIS.getString(this)).get(apiPlayer.getMemberId()));
    }

    @Override
    public PartyAccess getPartyAccess() {
        return PartyAccess.getPartyAccess(API.getInstance().getRedisManager().getRedisString(PartyDataValue.PARTY_ACCESS_REDIS.getString(this)));
    }

    @Override
    public void setPartyAccess(PartyAccess partyAccess) {
        API.getInstance().getRedisManager().setRedisString(PartyDataValue.PARTY_ACCESS_REDIS.getString(this), partyAccess.getAccessName());
    }

    @Override
    public boolean hisInParty(APIPlayer apiPlayer) {
        return API.getInstance().getRedisManager().getRedisMap(PartyDataValue.PARTY_PLAYERRANKMAP_REDIS.getString(this)).containsKey(apiPlayer.getMemberId());
    }

    @Override
    public List<Long> getPlayerList() {
        return new ArrayList<Long>() {{
            for (Object playerName : API.getInstance().getRedisManager().getRedisMap(PartyDataValue.PARTY_PLAYERRANKMAP_REDIS.getString(getPartyID())).keySet()) {
                add((Long) playerName);
            }
        }};
    }

    @Override
    public List<APIPlayer> getAPIPlayerList() {
        APIPlayerManager spm = API.getInstance().getPlayerManager();
        return new ArrayList<APIPlayer>() {{
            for (Object playerName : API.getInstance().getRedisManager().getRedisMap(PartyDataValue.PARTY_PLAYERRANKMAP_REDIS.getString(getPartyID())).keySet()) {
                add(spm.getPlayer((Long) playerName));
            }
        }};
    }

    @Override
    public boolean deleteParty(APIPlayer apiPlayer) {
        PartyRank pr = getPartyRank(apiPlayer);
        if (!pr.equals(PartyRank.ADMIN)) return false;
        for (APIPlayer apiPlayers : getAPIPlayerList())
            quitParty(apiPlayers);
        PartyDataValue.clearRedisData(DataType.PLAYER, this);
        return true;
    }

    @Override
    public long getPartyID() {
        return id;
    }

    @Override
    public boolean invitePlayer(APIPlayer apiPlayer) {
        if (hisInvitePlayer(apiPlayer)) return false;
        if (API.getInstance().getPartyManager().hasParty(apiPlayer)) return false;


        API.getInstance().getRedisManager().getRedisList(PartyDataValue.PARTY_INVITE_REDIS.getString(this)).add(apiPlayer.getName());
        return true;
    }

    @Override
    public boolean revokeInvite(APIPlayer apiPlayer) {
        if (!hisInvitePlayer(apiPlayer)) return false;

        API.getInstance().getRedisManager().getRedisList(PartyDataValue.PARTY_INVITE_REDIS.getString(this)).remove(apiPlayer.getName());
        return true;
    }

    @Override
    public boolean hisInvitePlayer(APIPlayer apiPlayer) {
        return API.getInstance().getRedisManager().getRedisList(PartyDataValue.PARTY_INVITE_REDIS.getString(this)).contains(apiPlayer.getName());
    }

    @Override
    public boolean isPartyOwner(APIPlayer apiPlayer) {
        return apiPlayer.getName().equals(getPartyOwner().getName());
    }

    @Override
    public APIPlayer getPartyOwner() {
        return API.getInstance().getPlayerManager().getPlayer(API.getInstance().getRedisManager().getRedisString(PartyDataValue.PARTY_OWNER_REDIS.getString(this)));
    }

    @Override
    public Map<String, PartyRank> getRank() {
        return new HashMap<String, PartyRank>() {{
            for (Entry<Object, Object> entry : API.getInstance().getRedisManager().getRedisMap(PartyDataValue.PARTY_PLAYERRANKMAP_REDIS.getString(getPartyID())).entrySet()) {
                put((String) entry.getKey(), PartyRank.valueOf((String) entry.getValue()));
            }
        }};
    }

}
