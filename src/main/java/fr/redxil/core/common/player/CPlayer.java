/* Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 */

package fr.redxil.core.common.player;

import fr.redline.pms.pm.RedisPMManager;
import fr.redline.pms.utils.IpInfo;
import fr.redxil.api.common.API;
import fr.redxil.api.common.group.party.Party;
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.api.common.player.APIPlayerManager;
import fr.redxil.api.common.player.data.SanctionInfo;
import fr.redxil.api.common.player.moderators.APIPlayerModerator;
import fr.redxil.api.common.player.rank.Rank;
import fr.redxil.api.common.redis.RedisManager;
import fr.redxil.api.common.server.Server;
import fr.redxil.api.common.utils.SanctionType;
import fr.redxil.core.common.CoreAPI;
import fr.redxil.core.common.data.LinkDataValue;
import fr.redxil.core.common.data.MoneyDataValue;
import fr.redxil.core.common.data.PlayerDataValue;
import fr.redxil.core.common.data.utils.DataType;
import fr.redxil.core.common.player.sqlmodel.moderator.SanctionModel;
import fr.redxil.core.common.player.sqlmodel.player.MoneyModel;
import fr.redxil.core.common.player.sqlmodel.player.PlayerLinkModel;
import fr.redxil.core.common.player.sqlmodel.player.PlayerModel;
import fr.redxil.core.common.sql.SQLModels;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class CPlayer extends CPlayerOffline implements APIPlayer {

    //model variable
    private final long memberID;

    public CPlayer(long memberID) {
        super(memberID);
        this.memberID = memberID;
    }


    /// <!-------------------- Server part --------------------!>

    protected static APIPlayer loadPlayer(String name, UUID uuid, IpInfo ipInfo) {

        API.getInstance().getPluginEnabler().printLog(Level.INFO, "Creating player Data");

        RedisManager redisManager = API.getInstance().getRedisManager();

        PlayerModel playerModel = new SQLModels<>(PlayerModel.class).getOrInsert(new HashMap<>() {{
            this.put(PlayerDataValue.PLAYER_NAME_SQL.getString(), name);
            this.put(PlayerDataValue.PLAYER_UUID_SQL.getString(), uuid.toString());
            this.put(PlayerDataValue.PLAYER_RANK_SQL.getString(), Rank.JOUEUR.getRankPower().intValue());
            this.put(PlayerDataValue.PLAYER_IP_SQL.getString(), ipInfo.getIp());
        }}, "WHERE " + PlayerDataValue.PLAYER_UUID_SQL.getString() + " = ?", uuid.toString());

        long memberID = playerModel.getMemberID();

        MoneyModel moneyModel = new SQLModels<>(MoneyModel.class).getOrInsert(new HashMap<>() {{
            this.put(PlayerDataValue.PLAYER_MEMBERID_SQL.getString(), memberID);
            this.put(MoneyDataValue.PLAYER_SOLDE_SQL.getString(), 0);
            this.put(MoneyDataValue.PLAYER_COINS_SQL.getString(), 0);
        }}, "WHERE " + PlayerDataValue.PLAYER_MEMBERID_SQL.getString() + " = ?", Long.valueOf(memberID).intValue());

        PlayerDataValue.clearRedisData(DataType.PLAYER, memberID);

        redisManager.setRedisLong(MoneyDataValue.PLAYER_COINS_REDIS.getString(memberID), moneyModel.getCoins());
        redisManager.setRedisLong(MoneyDataValue.PLAYER_SOLDE_REDIS.getString(memberID), moneyModel.getSolde());
        redisManager.setRedisString(PlayerDataValue.PLAYER_NAME_REDIS.getString(memberID), name);
        redisManager.setRedisString(PlayerDataValue.PLAYER_REAL_NAME_REDIS.getString(memberID), name);
        redisManager.setRedisString(PlayerDataValue.PLAYER_UUID_REDIS.getString(memberID), uuid.toString());
        redisManager.setRedisString(PlayerDataValue.PLAYER_BUNGEE_REDIS.getString(memberID), API.getInstance().getServer().getServerName());
        redisManager.setRedisLong(PlayerDataValue.PLAYER_RANK_REDIS.getString(memberID), playerModel.getPowerRank());
        redisManager.setRedisLong(PlayerDataValue.PLAYER_REAL_RANK_REDIS.getString(memberID), playerModel.getPowerRank());
        redisManager.setRedisString(PlayerDataValue.PLAYER_INPUT_REDIS.getString(memberID), null);
        redisManager.setRedisString(PlayerDataValue.PLAYER_IP_REDIS.getString(memberID), ipInfo.toString());

        Timestamp timestamp = (Timestamp) playerModel.get(PlayerDataValue.PLAYER_RANK_TIME_SQL.getString());
        if (timestamp != null) {
            redisManager.setRedisString(PlayerDataValue.PLAYER_RANK_TIME_REDIS.getString(memberID), timestamp.toString());
            redisManager.setRedisString(PlayerDataValue.PLAYER_REAL_RANK_TIME_REDIS.getString(memberID), timestamp.toString());
        }

        redisManager.getRedisList("ip/" + ipInfo.getIp()).add(name);

        redisManager.getRedisMap(PlayerDataValue.MAP_PLAYER_NAME.getString(memberID)).put(name, memberID);
        redisManager.getRedisMap(PlayerDataValue.MAP_PLAYER_UUID.getString(memberID)).put(uuid.toString(), memberID);

        redisManager.getRedisList(PlayerDataValue.LIST_PLAYER_ID.getString(memberID)).add(memberID);

        CPlayer newPlayer = new CPlayer(memberID);

        loadLink(newPlayer);

        API.getInstance().getPluginEnabler().printLog(Level.FINE, "Player Data creation finished");

        return newPlayer;

    }

    static void loadLink(CPlayer cPlayer) {
        APIPlayerManager cpm = CoreAPI.getInstance().getPlayerManager();
        List<String> linkTypeList = cpm.getLinkTypeList();
        if (linkTypeList.isEmpty())
            return;

        StringBuilder queryToAdd = new StringBuilder();
        for (String linkType : linkTypeList) {
            if (queryToAdd.length() != 0)
                queryToAdd.append(" OR ");
            queryToAdd.append(LinkDataValue.LINK_TYPE_SQL.getString()).append(" = ").append(linkType);
        }

        List<PlayerLinkModel> playerLinkModelList = new SQLModels<>(PlayerLinkModel.class).get("SELECT * FROM link WHERE (" + LinkDataValue.FROM_ID_SQL.getString() + " = ? OR " + LinkDataValue.TO_ID_SQL.getString() + " = ?) AND (" + queryToAdd + ")", cPlayer.getMemberID(), cPlayer.getMemberID());
        for (PlayerLinkModel playerLinkModel : playerLinkModelList)
            cpm.getLinkOnConnectAction(playerLinkModel.getLinkType()).accept(cPlayer, playerLinkModel);
    }


    @Override
    public void unloadPlayer() {
        if (!API.getInstance().isVelocity()) return;

        APIPlayerModerator spm = API.getInstance().getModeratorManager().getModerator(getMemberID());
        if (spm != null)
            spm.disconnectModerator();


        Party party = getParty();
        if (party != null)
            party.quitParty(this);


        UUID uuid = getUUID();

        RedisManager rm = API.getInstance().getRedisManager();

        rm.getRedisMap(PlayerDataValue.MAP_PLAYER_UUID.getString(this)).remove(uuid.toString());
        rm.getRedisMap(PlayerDataValue.MAP_PLAYER_NAME.getString(this)).remove(getName());
        rm.getRedisList(PlayerDataValue.LIST_PLAYER_ID.getString(this)).remove(memberID);

        super.setSolde(this.getSolde());
        super.setCoins(this.getCoins());

        MoneyDataValue.clearRedisData(DataType.PLAYER, this.getMemberID());

        LinkDataValue.clearRedisData(DataType.PLAYER, this.getMemberID());

        rm.getRedisList("ip/" + getIP().getIp()).remove(getRealName());

        PlayerDataValue.clearRedisData(DataType.PLAYER, this.getMemberID());

    }

    @Override
    public Server getServer() {
        String serverName = API.getInstance().getRedisManager().getRedisString(PlayerDataValue.PLAYER_SPIGOT_REDIS.getString(this));
        if (serverName == null) return null;
        return API.getInstance().getServerManager().getServer(serverName);
    }


    /// <!-------------------- Money part --------------------!>

    @Override
    public Server getBungeeServer() {
        String serverName = API.getInstance().getRedisManager().getRedisString(PlayerDataValue.PLAYER_BUNGEE_REDIS.getString(this));
        if (serverName == null) return null;
        return API.getInstance().getServerManager().getServer(serverName);
    }

    @Override
    public void switchServer(long server) {
        RedisPMManager.sendRedissonPluginMessage(API.getInstance().getRedisManager().getRedissonClient(), "switchServer", getName() + "<switchSplit>" + Long.valueOf(server).toString());
    }

    @Override
    public void addSolde(long value) {
        setSolde(getSolde() + value);
    }

    @Override
    public boolean setSolde(long value) {

        if (value <= 0)
            return false;

        API.getInstance().getRedisManager().setRedisLong(MoneyDataValue.PLAYER_SOLDE_REDIS.getString(this), value);

        return true;
    }

    @Override
    public long getSolde() {
        return API.getInstance().getRedisManager().getRedisLong(MoneyDataValue.PLAYER_SOLDE_REDIS.getString(this));
    }

    @Override
    public void addCoins(long value) {
        setCoins(getCoins() + value);
    }

    @Override
    public boolean setCoins(long value) {
        if (value <= 0)
            return false;

        API.getInstance().getRedisManager().setRedisLong(MoneyDataValue.PLAYER_COINS_REDIS.getString(this), value);
        return true;
    }

    @Override
    public long getCoins() {
        return API.getInstance().getRedisManager().getRedisLong(MoneyDataValue.PLAYER_COINS_REDIS.getString(this));
    }

    /// <!-------------------- Rank part --------------------!>

    @Override
    public Rank getRealRank() {
        return Rank.getRank(getRealRankPower());
    }

    @Override
    public void setRealRank(Rank rank) {
        this.setRealRank(rank, null);
    }

    @Override
    public void setRealRank(Rank rank, Timestamp timestamp) {
        String timeStampString = timestamp == null ? null : timestamp.toString();
        super.setRank(rank, timestamp);
        API.getInstance().getRedisManager().setRedisLong(PlayerDataValue.PLAYER_REAL_RANK_REDIS.getString(this), rank.getRankPower());
        API.getInstance().getRedisManager().setRedisString(PlayerDataValue.PLAYER_REAL_RANK_TIME_REDIS.getString(this), timeStampString);
    }

    @Override
    public void restoreRealData() {
        setName(getRealName());
        setRank(getRealRank(), getRealRankTimeStamp());
    }

    @Override
    public Long getRealRankPower() {
        Timestamp timeStamp = getRealRankTimeStamp();
        if (timeStamp != null) {
            if (timeStamp.before(Timestamp.from(Instant.now()))) {
                setRank(Rank.JOUEUR);
            }
        }
        return API.getInstance().getRedisManager().getRedisLong(PlayerDataValue.PLAYER_REAL_RANK_REDIS.getString(this));
    }

    @Override
    public Timestamp getRankTimeStamp() {
        String timeStamp = API.getInstance().getRedisManager().getRedisString(PlayerDataValue.PLAYER_RANK_TIME_REDIS.getString(this));
        if (timeStamp != null)
            return Timestamp.valueOf(timeStamp);
        return null;
    }

    @Override
    public Timestamp getRealRankTimeStamp() {
        String timeStamp = API.getInstance().getRedisManager().getRedisString(PlayerDataValue.PLAYER_REAL_RANK_TIME_REDIS.getString(this));
        if (timeStamp != null)
            return Timestamp.valueOf(timeStamp);
        return null;
    }


    @Override
    public Rank getRank() {
        return Rank.getRank(getRankPower());
    }

    @Override
    public void setRank(Rank rank) {
        this.setRank(rank, null);
    }

    @Override
    public void setRank(Rank rank, Timestamp timestamp) {
        String timeStampString = timestamp == null ? null : timestamp.toString();
        API.getInstance().getRedisManager().setRedisLong(PlayerDataValue.PLAYER_RANK_REDIS.getString(this), rank.getRankPower());
        API.getInstance().getRedisManager().setRedisString(PlayerDataValue.PLAYER_RANK_TIME_REDIS.getString(this), timeStampString);
        RedisPMManager.sendRedissonPluginMessage(API.getInstance().getRedisManager().getRedissonClient(), "rankChange", this.getUUID().toString());

    }

    @Override
    public Long getRankPower() {
        Timestamp timeStamp = getRankTimeStamp();
        if (timeStamp != null) {
            if (timeStamp.before(Timestamp.from(Instant.now()))) {
                setRank(Rank.JOUEUR);
            }
        }
        return API.getInstance().getRedisManager().getRedisLong(PlayerDataValue.PLAYER_RANK_REDIS.getString(this));
    }


    /// <!-------------------- String part --------------------!>

    @Override
    public boolean hasPermission(long power) {
        return getRankPower() >= power;
    }


    /// <!-------------------- APIPlayer part --------------------!>

    @Override
    public String getTabString() {
        return getRank().getTabString() + getName();
    }

    @Override
    public String getChatString() {
        Rank Rank = getRank();
        return Rank.getChatRankString() + getName() + Rank.getChatSeparator() + "";
    }

    @Override
    public boolean isConnected() {
        return API.getInstance().getRedisManager().getRedissonClient().getList(PlayerDataValue.LIST_PLAYER_ID.getString(this)).contains(Long.valueOf(memberID).toString());
    }


    @Override
    public boolean setRealName(String name) {
        if (super.setName(name)) {
            API.getInstance().getRedisManager().getRedisString(PlayerDataValue.PLAYER_REAL_NAME_REDIS.getString(this));
            return true;
        }
        return false;
    }

    @Override
    public String getRealName() {
        return API.getInstance().getRedisManager().getRedisString(PlayerDataValue.PLAYER_REAL_NAME_REDIS.getString(this));
    }

    @Override
    public String getName() {
        return API.getInstance().getRedisManager().getRedisString(PlayerDataValue.PLAYER_NAME_REDIS.getString(this));
    }

    @Override
    public boolean setName(String s) {
        if (s != null && (getRealName().equals(s) || !API.getInstance().getPlayerManager().dataExist(s))) {
            API.getInstance().getRedisManager().getRedisMap(PlayerDataValue.MAP_PLAYER_NAME.getString(this)).remove(getName());
            API.getInstance().getRedisManager().setRedisString(PlayerDataValue.PLAYER_NAME_REDIS.getString(this), s);
            API.getInstance().getRedisManager().getRedisMap(PlayerDataValue.MAP_PLAYER_NAME.getString(this)).put(getName(), memberID);

            RedisPMManager.sendRedissonPluginMessage(API.getInstance().getRedisManager().getRedissonClient(), "nameChange", this.getUUID().toString());
            return true;
        }
        return false;
    }

    @Override
    public boolean isNick() {
        return !getRealName().equals(getName());
    }

    @Override
    public long getMemberID() {
        return memberID;
    }

    @Override
    public UUID getUUID() {
        return UUID.fromString(API.getInstance().getRedisManager().getRedisString(PlayerDataValue.PLAYER_UUID_REDIS.getString(this)));
    }

    @Override
    public void setUUID(UUID uuid) {
        if (uuid != null) {
            API.getInstance().getRedisManager().getRedisMap(PlayerDataValue.MAP_PLAYER_UUID.getString(this)).remove(getUUID().toString());
            API.getInstance().getRedisManager().setRedisString(PlayerDataValue.PLAYER_UUID_REDIS.getString(this), uuid.toString());
            API.getInstance().getRedisManager().getRedisMap(PlayerDataValue.MAP_PLAYER_UUID.getString(this)).put(uuid.toString(), memberID);
        }
    }

    @Override
    public IpInfo getIP() {
        return IpInfo.fromString(API.getInstance().getRedisManager().getRedisString(PlayerDataValue.PLAYER_IP_REDIS.getString(this)));
    }

    @Override
    public void setIP(IpInfo ipInfo) {
        API.getInstance().getRedisManager().setRedisString(PlayerDataValue.PLAYER_IP_REDIS.getString(this), ipInfo.getIp());
        super.setIP(ipInfo);
    }

    @Override
    public boolean isLogin() {
        return API.getInstance().getRedisManager().getRedisLong(PlayerDataValue.PLAYER_HUBLOGGED_REDIS.getString(this)) == 1L;
    }

    @Override
    public boolean isFreeze() {
        return API.getInstance().getRedisManager().getRedisLong(PlayerDataValue.PLAYER_FREEZE_REDIS.getString(this)) != 0L;
    }

    @Override
    public Party getParty() {
        return API.getInstance().getPartyManager().getPlayerParty(this);
    }

    @Override
    public void addTempData(String s, Object o) {
        API.getInstance().getRedisManager().getRedisMap(PlayerDataValue.PLAYER_MAP_REDIS.getString(this)).put(s, o);
    }

    @Override
    public Object removeTempData(String s) {
        return API.getInstance().getRedisManager().getRedisMap(PlayerDataValue.PLAYER_MAP_REDIS.getString(this)).remove(s);
    }

    @Override
    public Object getTempData(String s) {
        return API.getInstance().getRedisManager().getRedisMap(PlayerDataValue.PLAYER_MAP_REDIS.getString(this)).get(s);
    }

    @Override
    public List<String> getTempDataKeyList() {
        return new ArrayList<>() {{
            API.getInstance().getRedisManager().getRedisMap(PlayerDataValue.PLAYER_MAP_REDIS.getString(getMemberID())).keySet().forEach((object) -> add((String) object));
        }};
    }

    @Override
    public SanctionInfo kickPlayer(String reason, APIPlayerModerator author) {

        SanctionModel sm = new SanctionModel(
                getMemberID(),
                author.getMemberID(),
                SanctionType.KICK,
                reason
        );

        new SQLModels<>(SanctionModel.class).insert(sm);

        loadSanction();

        return getLastSanction(SanctionType.KICK);

    }

    @Override
    public boolean hasParty() {
        return CoreAPI.getInstance().getPartyManager().getParty(getMemberID()) != null;
    }

}


