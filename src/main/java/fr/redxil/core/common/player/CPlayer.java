/*
 *
 * Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 *
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
import fr.redxil.core.common.data.link.LinkDataSql;
import fr.redxil.core.common.data.money.MoneyDataRedis;
import fr.redxil.core.common.data.money.MoneyDataSql;
import fr.redxil.core.common.data.player.PlayerDataRedis;
import fr.redxil.core.common.data.player.PlayerDataSql;
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
            this.put(PlayerDataSql.PLAYER_NAME_SQL.getSQLColumns(), name);
            this.put(PlayerDataSql.PLAYER_UUID_SQL.getSQLColumns(), uuid.toString());
            this.put(PlayerDataSql.PLAYER_RANK_SQL.getSQLColumns(), Rank.JOUEUR.getRankPower().intValue());
            this.put(PlayerDataSql.PLAYER_IP_SQL.getSQLColumns(), ipInfo.getIp());
        }}, "WHERE " + PlayerDataSql.PLAYER_UUID_SQL.getSQLColumns().toSQL() + " = ?", uuid.toString());

        long memberID = playerModel.getMemberID();

        MoneyModel moneyModel = new SQLModels<>(MoneyModel.class).getOrInsert(new HashMap<>() {{
            this.put(MoneyDataSql.PLAYER_MEMBERID_SQL.getSQLColumns(), memberID);
            this.put(MoneyDataSql.PLAYER_SOLDE_SQL.getSQLColumns(), 0);
            this.put(MoneyDataSql.PLAYER_COINS_SQL.getSQLColumns(), 0);
        }}, "WHERE " + MoneyDataSql.PLAYER_MEMBERID_SQL.getSQLColumns().toSQL() + " = ?", Long.valueOf(memberID).intValue());

        PlayerDataRedis.clearRedisData(DataType.PLAYER, memberID);

        redisManager.setRedisLong(MoneyDataRedis.PLAYER_COINS_REDIS.getString(memberID), moneyModel.getCoins());
        redisManager.setRedisLong(MoneyDataRedis.PLAYER_SOLDE_REDIS.getString(memberID), moneyModel.getSolde());
        redisManager.setRedisString(PlayerDataRedis.PLAYER_NAME_REDIS.getString(memberID), name);
        redisManager.setRedisString(PlayerDataRedis.PLAYER_REAL_NAME_REDIS.getString(memberID), name);
        redisManager.setRedisString(PlayerDataRedis.PLAYER_UUID_REDIS.getString(memberID), uuid.toString());
        redisManager.setRedisString(PlayerDataRedis.PLAYER_BUNGEE_REDIS.getString(memberID), API.getInstance().getServer().getServerName());
        redisManager.setRedisLong(PlayerDataRedis.PLAYER_RANK_REDIS.getString(memberID), playerModel.getPowerRank());
        redisManager.setRedisLong(PlayerDataRedis.PLAYER_REAL_RANK_REDIS.getString(memberID), playerModel.getPowerRank());
        redisManager.setRedisString(PlayerDataRedis.PLAYER_INPUT_REDIS.getString(memberID), null);
        redisManager.setRedisString(PlayerDataRedis.PLAYER_IP_REDIS.getString(memberID), ipInfo.toString());

        Timestamp timestamp = (Timestamp) playerModel.get(PlayerDataSql.PLAYER_RANK_TIME_SQL.getSQLColumns());
        if (timestamp != null) {
            redisManager.setRedisString(PlayerDataRedis.PLAYER_RANK_TIME_REDIS.getString(memberID), timestamp.toString());
            redisManager.setRedisString(PlayerDataRedis.PLAYER_REAL_RANK_TIME_REDIS.getString(memberID), timestamp.toString());
        }

        redisManager.getRedisList("ip/" + ipInfo.getIp()).add(name);

        redisManager.getRedisMap(PlayerDataRedis.MAP_PLAYER_NAME.getString(memberID)).put(name, memberID);
        redisManager.getRedisMap(PlayerDataRedis.MAP_PLAYER_UUID.getString(memberID)).put(uuid.toString(), memberID);

        redisManager.getRedisList(PlayerDataRedis.LIST_PLAYER_ID.getString(memberID)).add(memberID);

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
            queryToAdd.append(LinkDataSql.LINK_TYPE_SQL.getSQLColumns().toSQL()).append(" = ").append(linkType);
        }

        List<PlayerLinkModel> playerLinkModelList = new SQLModels<>(PlayerLinkModel.class).get("SELECT * FROM link WHERE (" + LinkDataSql.FROM_ID_SQL.getSQLColumns().toSQL() + " = ? OR " + LinkDataSql.TO_ID_SQL.getSQLColumns().toSQL() + " = ?) AND (" + queryToAdd + ")", cPlayer.getMemberID(), cPlayer.getMemberID());
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

        rm.getRedisMap(PlayerDataRedis.MAP_PLAYER_UUID.getString(this)).remove(uuid.toString());
        rm.getRedisMap(PlayerDataRedis.MAP_PLAYER_NAME.getString(this)).remove(getName());
        rm.getRedisList(PlayerDataRedis.LIST_PLAYER_ID.getString(this)).remove(memberID);

        getMoneyModel().set(new HashMap<>() {{
            put(MoneyDataSql.PLAYER_SOLDE_SQL.getSQLColumns(), getSolde());
            put(MoneyDataSql.PLAYER_COINS_SQL.getSQLColumns(), getCoins());
        }});

        getPlayerModel().set(PlayerDataSql.PLAYER_LC_SQL.getSQLColumns(), Timestamp.from(Instant.now()));

        MoneyDataRedis.clearRedisData(DataType.PLAYER, this.getMemberID());

        rm.getRedisList("ip/" + getIP().getIp()).remove(getRealName());

        PlayerDataRedis.clearRedisData(DataType.PLAYER, this.getMemberID());

    }

    @Override
    public Server getServer() {
        String serverName = API.getInstance().getRedisManager().getRedisString(PlayerDataRedis.PLAYER_SPIGOT_REDIS.getString(this));
        if (serverName == null) return null;
        return API.getInstance().getServerManager().getServer(serverName);
    }


    /// <!-------------------- Money part --------------------!>

    @Override
    public Server getBungeeServer() {
        String serverName = API.getInstance().getRedisManager().getRedisString(PlayerDataRedis.PLAYER_BUNGEE_REDIS.getString(this));
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

        API.getInstance().getRedisManager().setRedisLong(MoneyDataRedis.PLAYER_SOLDE_REDIS.getString(this), value);

        return true;
    }

    @Override
    public long getSolde() {
        return API.getInstance().getRedisManager().getRedisLong(MoneyDataRedis.PLAYER_SOLDE_REDIS.getString(this));
    }

    @Override
    public void addCoins(long value) {
        setCoins(getCoins() + value);
    }

    @Override
    public boolean setCoins(long value) {
        if (value <= 0)
            return false;

        API.getInstance().getRedisManager().setRedisLong(MoneyDataRedis.PLAYER_COINS_REDIS.getString(this), value);
        return true;
    }

    @Override
    public long getCoins() {
        return API.getInstance().getRedisManager().getRedisLong(MoneyDataRedis.PLAYER_COINS_REDIS.getString(this));
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
        API.getInstance().getRedisManager().setRedisLong(PlayerDataRedis.PLAYER_REAL_RANK_REDIS.getString(this), rank.getRankPower());
        API.getInstance().getRedisManager().setRedisString(PlayerDataRedis.PLAYER_REAL_RANK_TIME_REDIS.getString(this), timeStampString);
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
        return API.getInstance().getRedisManager().getRedisLong(PlayerDataRedis.PLAYER_REAL_RANK_REDIS.getString(this));
    }

    @Override
    public Timestamp getRankTimeStamp() {
        String timeStamp = API.getInstance().getRedisManager().getRedisString(PlayerDataRedis.PLAYER_RANK_TIME_REDIS.getString(this));
        if (timeStamp != null)
            return Timestamp.valueOf(timeStamp);
        return null;
    }

    @Override
    public Timestamp getRealRankTimeStamp() {
        String timeStamp = API.getInstance().getRedisManager().getRedisString(PlayerDataRedis.PLAYER_REAL_RANK_TIME_REDIS.getString(this));
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
        API.getInstance().getRedisManager().setRedisLong(PlayerDataRedis.PLAYER_RANK_REDIS.getString(this), rank.getRankPower());
        API.getInstance().getRedisManager().setRedisString(PlayerDataRedis.PLAYER_RANK_TIME_REDIS.getString(this), timeStampString);
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
        return API.getInstance().getRedisManager().getRedisLong(PlayerDataRedis.PLAYER_RANK_REDIS.getString(this));
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
        return API.getInstance().getRedisManager().getRedissonClient().getList(PlayerDataRedis.LIST_PLAYER_ID.getString(this)).contains(memberID);
    }


    @Override
    public boolean setRealName(String name) {
        if (super.setName(name)) {
            API.getInstance().getRedisManager().getRedisString(PlayerDataRedis.PLAYER_REAL_NAME_REDIS.getString(this));
            return true;
        }
        return false;
    }

    @Override
    public String getRealName() {
        return API.getInstance().getRedisManager().getRedisString(PlayerDataRedis.PLAYER_REAL_NAME_REDIS.getString(this));
    }

    @Override
    public String getName() {
        return API.getInstance().getRedisManager().getRedisString(PlayerDataRedis.PLAYER_NAME_REDIS.getString(this));
    }

    @Override
    public boolean setName(String s) {
        if (s != null && (getRealName().equals(s) || !API.getInstance().getPlayerManager().dataExist(s))) {
            API.getInstance().getRedisManager().getRedisMap(PlayerDataRedis.MAP_PLAYER_NAME.getString(this)).remove(getName());
            API.getInstance().getRedisManager().setRedisString(PlayerDataRedis.PLAYER_NAME_REDIS.getString(this), s);
            API.getInstance().getRedisManager().getRedisMap(PlayerDataRedis.MAP_PLAYER_NAME.getString(this)).put(getName(), memberID);

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
        return UUID.fromString(API.getInstance().getRedisManager().getRedisString(PlayerDataRedis.PLAYER_UUID_REDIS.getString(this)));
    }

    @Override
    public void setUUID(UUID uuid) {
        if (uuid != null) {
            API.getInstance().getRedisManager().getRedisMap(PlayerDataRedis.MAP_PLAYER_UUID.getString(this)).remove(getUUID().toString());
            API.getInstance().getRedisManager().setRedisString(PlayerDataRedis.PLAYER_UUID_REDIS.getString(this), uuid.toString());
            API.getInstance().getRedisManager().getRedisMap(PlayerDataRedis.MAP_PLAYER_UUID.getString(this)).put(uuid.toString(), memberID);
        }
    }

    @Override
    public IpInfo getIP() {
        return IpInfo.fromString(API.getInstance().getRedisManager().getRedisString(PlayerDataRedis.PLAYER_IP_REDIS.getString(this)));
    }

    @Override
    public void setIP(IpInfo ipInfo) {
        API.getInstance().getRedisManager().setRedisString(PlayerDataRedis.PLAYER_IP_REDIS.getString(this), ipInfo.getIp());
        super.setIP(ipInfo);
    }

    @Override
    public boolean isLogin() {
        return API.getInstance().getRedisManager().getRedisLong(PlayerDataRedis.PLAYER_HUBLOGGED_REDIS.getString(this)) == 1L;
    }

    @Override
    public boolean isFreeze() {
        return API.getInstance().getRedisManager().getRedisLong(PlayerDataRedis.PLAYER_FREEZE_REDIS.getString(this)) != 0L;
    }

    @Override
    public Party getParty() {
        return API.getInstance().getPartyManager().getPlayerParty(this);
    }

    @Override
    public void addTempData(String s, Object o) {
        API.getInstance().getRedisManager().getRedisMap(PlayerDataRedis.PLAYER_MAP_REDIS.getString(this)).put(s, o);
    }

    @Override
    public Object removeTempData(String s) {
        return API.getInstance().getRedisManager().getRedisMap(PlayerDataRedis.PLAYER_MAP_REDIS.getString(this)).remove(s);
    }

    @Override
    public Object getTempData(String s) {
        return API.getInstance().getRedisManager().getRedisMap(PlayerDataRedis.PLAYER_MAP_REDIS.getString(this)).get(s);
    }

    @Override
    public List<String> getTempDataKeyList() {
        return new ArrayList<>() {{
            API.getInstance().getRedisManager().getRedisMap(PlayerDataRedis.PLAYER_MAP_REDIS.getString(getMemberID())).keySet().forEach((object) -> add((String) object));
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


