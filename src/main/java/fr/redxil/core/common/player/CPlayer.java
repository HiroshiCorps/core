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
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.api.common.player.APIPlayerManager;
import fr.redxil.api.common.player.data.SanctionInfo;
import fr.redxil.api.common.player.moderators.APIPlayerModerator;
import fr.redxil.api.common.player.rank.Rank;
import fr.redxil.api.common.redis.RedisManager;
import fr.redxil.api.common.server.Server;
import fr.redxil.api.common.utils.SanctionType;
import fr.redxil.core.common.CoreAPI;
import fr.redxil.core.common.data.IDDataValue;
import fr.redxil.core.common.data.link.LinkDataSql;
import fr.redxil.core.common.data.money.MoneyDataRedis;
import fr.redxil.core.common.data.money.MoneyDataSql;
import fr.redxil.core.common.data.player.PlayerDataRedis;
import fr.redxil.core.common.data.player.PlayerDataSql;
import fr.redxil.core.common.data.utils.DataReminder;
import fr.redxil.core.common.data.utils.DataType;
import fr.redxil.core.common.player.sqlmodel.moderator.SanctionModel;
import fr.redxil.core.common.player.sqlmodel.player.MoneyModel;
import fr.redxil.core.common.player.sqlmodel.player.PlayerLinkModel;
import fr.redxil.core.common.player.sqlmodel.player.PlayerModel;
import fr.redxil.core.common.redis.IDGenerator;
import fr.redxil.core.common.sql.SQLModels;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.logging.Level;

public class CPlayer extends CPlayerOffline implements APIPlayer {

    DataReminder<String> serverNameReminder = null;
    DataReminder<Long> bungeeReminder = null;
    DataReminder<Long> soldeReminder = null;
    DataReminder<Long> coinsReminder = null;
    DataReminder<Long> realRankReminder = null;
    DataReminder<Long> rankReminder = null;
    DataReminder<String> realRankTimerReminder = null;
    DataReminder<String> rankTimerReminder = null;
    DataReminder<String> realNameReminder = null;
    DataReminder<String> nameReminder = null;
    DataReminder<String> uuidReminder = null;
    DataReminder<String> ipReminder = null;
    DataReminder<Long> freezeReminder = null;
    DataReminder<Map<String, Object>> tempDataReminder = null;


    public CPlayer(long memberID) {
        super(memberID);
    }

    public CPlayer(String name, UUID uuid, IpInfo ipInfo) {
        super(IDGenerator.generateLONGID(IDDataValue.PLAYERID));

        RedisManager redisManager = API.getInstance().getRedisManager();

        if (API.getInstance().isOnlineMod()) {
            PlayerModel playerModel = new SQLModels<>(PlayerModel.class).getOrInsert(new HashMap<>() {{
                this.put(PlayerDataSql.PLAYER_NAME_SQL.getSQLColumns(), name);
                this.put(PlayerDataSql.PLAYER_UUID_SQL.getSQLColumns(), uuid.toString());
                this.put(PlayerDataSql.PLAYER_RANK_SQL.getSQLColumns(), Rank.JOUEUR.getRankPower().intValue());
                this.put(PlayerDataSql.PLAYER_IP_SQL.getSQLColumns(), ipInfo.getIp());
            }}, "WHERE " + PlayerDataSql.PLAYER_UUID_SQL.getSQLColumns().toSQL() + " = ?", uuid.toString());

            this.memberID = playerModel.getMemberID();

            MoneyModel moneyModel = new SQLModels<>(MoneyModel.class).getOrInsert(new HashMap<>() {{
                this.put(MoneyDataSql.PLAYER_MEMBERID_SQL.getSQLColumns(), memberID);
                this.put(MoneyDataSql.PLAYER_SOLDE_SQL.getSQLColumns(), 0);
                this.put(MoneyDataSql.PLAYER_COINS_SQL.getSQLColumns(), 0);
            }}, "WHERE " + MoneyDataSql.PLAYER_MEMBERID_SQL.getSQLColumns().toSQL() + " = ?", Long.valueOf(memberID).intValue());

            PlayerDataRedis.clearRedisData(DataType.PLAYER, memberID);

            setCoins(moneyModel.getCoins());
            setSolde(moneyModel.getSolde());

            Timestamp timestamp = (Timestamp) playerModel.get(PlayerDataSql.PLAYER_RANK_TIME_SQL.getSQLColumns());

            String timeStampString = timestamp == null ? null : timestamp.toString();
            initRankTimeReminder();
            initRankReminder();
            rankReminder.setData(playerModel.getRank().getRankPower());
            rankTimerReminder.setData(timeStampString);

            initRealRankReminder();
            initRealRankTimeReminder();
            realRankReminder.setData(playerModel.getRank().getRankPower());
            realRankTimerReminder.setData(timeStampString);

        } else {
            initRankTimeReminder();
            initRankReminder();
            rankReminder.setData(Rank.JOUEUR.getRankPower());
            rankTimerReminder.setData(null);

            initRealRankReminder();
            initRealRankTimeReminder();
            realRankReminder.setData(Rank.JOUEUR.getRankPower());
            realRankTimerReminder.setData(null);

            setCoins(0L);
            setSolde(0L);
        }

        initNameReminder();
        nameReminder.setData(name);
        initRealNameReminder();
        realNameReminder.setData(name);

        initUUIDReminder();
        uuidReminder.setData(uuid.toString());

        initBungeeReminder();
        bungeeReminder.setData(API.getInstance().getServer().getServerID());
        redisManager.setRedisString(PlayerDataRedis.PLAYER_INPUT_REDIS.getString(memberID), null);

        setIP(ipInfo);

        redisManager.getRedisMap(PlayerDataRedis.MAP_PLAYER_NAME.getString(memberID)).put(name, memberID);
        redisManager.getRedisMap(PlayerDataRedis.MAP_PLAYER_UUID.getString(memberID)).put(uuid.toString(), memberID);

        redisManager.getRedisList(PlayerDataRedis.LIST_PLAYER_ID.getString(memberID)).add(memberID);

        loadLink(this);

        API.getInstance().getPluginEnabler().printLog(Level.FINE, "Player Data creation finished");
    }

    static void loadLink(CPlayer cPlayer) {

        if (!API.getInstance().isOnlineMod())
            return;

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
    public void sendMessage(String s) {
        String playerServer = this.getServerName();
        if (playerServer.equals(API.getInstance().getServerName()))
            API.getInstance().getPluginEnabler().sendMessage(getUUID(), s);
        else if (API.getInstance().isOnlineMod())
            RedisPMManager.sendRedissonPluginMessage(API.getInstance().getRedisManager().getRedissonClient(), "playerMessage", Long.valueOf(getMemberID()).toString() + "<msp>" + s);
    }

    @Override
    public Server getServer() {
        String serverName = getServerName();
        if (serverName == null) return null;
        return API.getInstance().getServerManager().getServer(serverName);
    }

    @Override
    public void switchServer(long server) {
        if (API.getInstance().isOnlineMod())
            RedisPMManager.sendRedissonPluginMessage(API.getInstance().getRedisManager().getRedissonClient(), "askSwitchServer", getName() + "<switchSplit>" + Long.valueOf(server).toString());
    }

    @Override
    public void addSolde(long value) {
        setSolde(getSolde() + value);
    }

    @Override
    public void unloadPlayer() {
        if (!API.getInstance().isVelocity()) return;

        API.getInstance().getModeratorManager().getModerator(getMemberID()).ifPresent(APIPlayerModerator::disconnectModerator);

        UUID uuid = getUUID();
        APIPlayerManager playerManager = API.getInstance().getPlayerManager();

        playerManager.getUUIDToLongMap().remove(uuid.toString());
        playerManager.getNameToLongMap().remove(getName());
        playerManager.getLoadedPlayer().remove(getMemberID());

        if (API.getInstance().isOnlineMod()) {

            RedisManager rm = API.getInstance().getRedisManager();

            getMoneyModel().set(new HashMap<>() {{
                put(MoneyDataSql.PLAYER_SOLDE_SQL.getSQLColumns(), getSolde());
                put(MoneyDataSql.PLAYER_COINS_SQL.getSQLColumns(), getCoins());
            }});

            getPlayerModel().set(PlayerDataSql.PLAYER_LC_SQL.getSQLColumns(), Timestamp.from(Instant.now()));

            MoneyDataRedis.clearRedisData(DataType.PLAYER, this.getMemberID());

            rm.getRedisList("ip/" + getIP().getIp()).remove(getRealName());

            PlayerDataRedis.clearRedisData(DataType.PLAYER, this.getMemberID());

        }

    }

    @Override
    public void addCoins(long value) {
        setCoins(getCoins() + value);
    }

    public void initSNReminder() {
        if (this.serverNameReminder == null)
            this.serverNameReminder = DataReminder.generateReminder(PlayerDataRedis.PLAYER_SPIGOT_REDIS.getString(this), "null");
    }

    @Override
    public String getServerName() {
        initSNReminder();
        return this.serverNameReminder.getData();
    }

    /// <!-------------------- Rank part --------------------!>

    @Override
    public void setServerName(String name) {
        initSNReminder();
        this.serverNameReminder.setData(name);
    }

    public void initBungeeReminder() {
        if (this.bungeeReminder == null)
            this.bungeeReminder = DataReminder.generateReminder(PlayerDataRedis.PLAYER_BUNGEE_REDIS.getString(this), 0L);
    }

    @Override
    public long getBungeeServerID() {
        initBungeeReminder();
        return this.bungeeReminder.getData();
    }

    @Override
    public Server getBungeeServer() {
        return API.getInstance().getServerManager().getServer(getBungeeServerID());
    }

    public void initSoldeReminder() {
        if (this.soldeReminder == null)
            this.soldeReminder = DataReminder.generateReminder(MoneyDataRedis.PLAYER_SOLDE_REDIS.getString(this), 0L);
    }

    @Override
    public boolean setSolde(long value) {

        if (value <= 0)
            return false;

        initSoldeReminder();
        this.soldeReminder.setData(value);

        return true;
    }

    @Override
    public long getSolde() {
        initSoldeReminder();
        return this.soldeReminder.getData();
    }

    public void initCoinsReminder() {
        if (this.coinsReminder == null)
            this.coinsReminder = DataReminder.generateReminder(MoneyDataRedis.PLAYER_COINS_REDIS.getString(this), 0L);
    }

    @Override
    public Rank getRealRank() {
        return Rank.getRank(getRealRankPower());
    }

    @Override
    public void setRealRank(Rank rank) {
        this.setRealRank(rank, null);
    }

    @Override
    public boolean setCoins(long value) {
        if (value <= 0)
            return false;

        initCoinsReminder();
        coinsReminder.setData(value);
        return true;
    }

    @Override
    public void restoreRealData() {
        setName(getRealName());
        setRank(getRealRank(), getRealRankTimeStamp());
    }

    @Override
    public long getCoins() {
        initCoinsReminder();
        return coinsReminder.getData();
    }

    public void initRealRankReminder() {
        if (this.realRankReminder == null)
            this.realRankReminder = DataReminder.generateReminder(PlayerDataRedis.PLAYER_REAL_RANK_REDIS.getString(getMemberID()), Rank.JOUEUR.getRankPower());
    }

    public void initRankReminder() {
        if (this.rankReminder == null)
            this.rankReminder = DataReminder.generateReminder(PlayerDataRedis.PLAYER_RANK_REDIS.getString(getMemberID()), Rank.JOUEUR.getRankPower());
    }


    @Override
    public Rank getRank() {
        return Rank.getRank(getRankPower());
    }

    @Override
    public void setRank(Rank rank) {
        this.setRank(rank, null);
    }

    public void initRealRankTimeReminder() {
        if (this.realRankTimerReminder == null)
            this.realRankTimerReminder = DataReminder.generateReminder(PlayerDataRedis.PLAYER_REAL_RANK_TIME_REDIS.getString(getMemberID()), null);
    }

    public void initRankTimeReminder() {
        if (this.rankTimerReminder == null)
            this.rankTimerReminder = DataReminder.generateReminder(PlayerDataRedis.PLAYER_RANK_TIME_REDIS.getString(getMemberID()), null);
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
    public void setRealRank(Rank rank, Timestamp timestamp) {
        String timeStampString = timestamp == null ? null : timestamp.toString();
        super.setRank(rank, timestamp);
        initRealRankReminder();
        initRealRankTimeReminder();
        realRankReminder.setData(rank.getRankPower());
        realRankTimerReminder.setData(timeStampString);
    }

    @Override
    public Long getRealRankPower() {

        Timestamp timeStamp = getRealRankTimeStamp();
        if (timeStamp != null) {
            if (timeStamp.before(Timestamp.from(Instant.now()))) {
                setRank(Rank.JOUEUR);
            }
        }
        initRealRankReminder();
        return realRankReminder.getData();
    }

    @Override
    public Timestamp getRankTimeStamp() {
        initRankTimeReminder();
        String timeStamp = API.getInstance().getRedisManager().getRedisString(PlayerDataRedis.PLAYER_RANK_TIME_REDIS.getString(this));
        if (timeStamp != null)
            return Timestamp.valueOf(timeStamp);
        return null;
    }

    @Override
    public Timestamp getRealRankTimeStamp() {
        initRealRankTimeReminder();
        String timeStamp = API.getInstance().getRedisManager().getRedisString(PlayerDataRedis.PLAYER_REAL_RANK_TIME_REDIS.getString(this));
        if (timeStamp != null)
            return Timestamp.valueOf(timeStamp);
        return null;
    }

    @Override
    public void setRank(Rank rank, Timestamp timestamp) {
        String timeStampString = timestamp == null ? null : timestamp.toString();
        initRankTimeReminder();
        initRankReminder();
        rankReminder.setData(rank.getRankPower());
        rankTimerReminder.setData(timeStampString);
        if (API.getInstance().isOnlineMod())
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
        initRankReminder();
        return rankReminder.getData();
    }

    @Override
    public boolean isConnected() {
        return API.getInstance().getRedisManager().getRedissonClient().getList(PlayerDataRedis.LIST_PLAYER_ID.getString(this)).contains(getMemberID());
    }

    public void initRealNameReminder() {
        if (this.realNameReminder == null)
            this.realNameReminder = DataReminder.generateReminder(PlayerDataRedis.PLAYER_REAL_NAME_REDIS.getString(getMemberID()), null);
    }

    public void initNameReminder() {
        if (this.nameReminder == null)
            this.nameReminder = DataReminder.generateReminder(PlayerDataRedis.PLAYER_NAME_REDIS.getString(getMemberID()), null);
    }

    @Override
    public boolean isNick() {
        return !getRealName().equals(getName());
    }

    @Override
    public boolean setRealName(String name) {
        if (super.setName(name)) {
            initRealNameReminder();
            realNameReminder.setData(name);
            setName(name);
            return true;
        }
        return false;
    }

    @Override
    public String getRealName() {
        initRealNameReminder();
        return realNameReminder.getData();
    }

    @Override
    public String getName() {
        initNameReminder();
        return nameReminder.getData();
    }

    @Override
    public boolean setName(String s) {
        if (s != null && (getRealName().equals(s) || !API.getInstance().getPlayerManager().dataExist(s))) {
            String oldName = getName();
            API.getInstance().getRedisManager().getRedisMap(PlayerDataRedis.MAP_PLAYER_NAME.getString(this)).remove(getName());
            initNameReminder();
            nameReminder.setData(s);
            API.getInstance().getRedisManager().getRedisMap(PlayerDataRedis.MAP_PLAYER_NAME.getString(this)).put(getName(), getMemberID());

            IpInfo ipInfo = getIP();
            if (ipInfo != null) {
                if (oldName != null)
                    API.getInstance().getRedisManager().getRedisList("ip/" + ipInfo).remove(getName());
                API.getInstance().getRedisManager().getRedisList("ip/" + ipInfo).add(getName());
            }

            if (API.getInstance().isOnlineMod())
                RedisPMManager.sendRedissonPluginMessage(API.getInstance().getRedisManager().getRedissonClient(), "nameChange", this.getUUID().toString());
            return true;
        }
        return false;
    }

    public void initUUIDReminder() {

    }

    @Override
    public UUID getUUID() {
        initUUIDReminder();
        return UUID.fromString(uuidReminder.getData());
    }

    @Override
    public void setUUID(UUID uuid) {
        if (uuid != null) {
            API.getInstance().getRedisManager().getRedisMap(PlayerDataRedis.MAP_PLAYER_UUID.getString(this)).remove(getUUID().toString());
            initUUIDReminder();
            uuidReminder.setData(uuid.toString());
            API.getInstance().getRedisManager().getRedisMap(PlayerDataRedis.MAP_PLAYER_UUID.getString(this)).put(uuid.toString(), getMemberID());
        }
    }

    public void initIpReminder() {
        if (this.ipReminder == null)
            this.ipReminder = DataReminder.generateReminder(PlayerDataRedis.PLAYER_IP_REDIS.getString(getMemberID()), "0.0.0.0:0000");
    }

    @Override
    public IpInfo getIP() {
        initIpReminder();
        return IpInfo.fromString(ipReminder.getData());
    }

    @Override
    public void setIP(IpInfo ipInfo) {
        IpInfo lastIP = getIP();
        if (lastIP != null) {
            API.getInstance().getRedisManager().getRedisList("ip/" + lastIP).remove(getName());
        }
        initIpReminder();
        ipReminder.setData(ipInfo.getIp());
        API.getInstance().getRedisManager().getRedisList("ip/" + ipInfo).add(getName());
        super.setIP(ipInfo);
    }

    public void initFreezeReminder() {
        if (this.freezeReminder == null)
            this.freezeReminder = DataReminder.generateReminder(PlayerDataRedis.PLAYER_FREEZE_REDIS.getString(this), 0L);
    }

    @Override
    public long getFreeze() {
        initFreezeReminder();
        return freezeReminder.getData();
    }

    @Override
    public boolean isFreeze() {
        return getFreeze() != 0L;
    }

    @Override
    public void setFreeze(long s) {
        initFreezeReminder();
        freezeReminder.setData(s);
    }

    public void initTempDataReminder() {
        if (this.tempDataReminder == null)
            this.tempDataReminder = DataReminder.generateReminder(PlayerDataRedis.PLAYER_MAP_REDIS.getString(this), new HashMap<>());
    }


    @Override
    public void addTempData(String s, Object o) {
        initTempDataReminder();
        tempDataReminder.getData().put(s, o);
    }

    @Override
    public Object removeTempData(String s) {
        initTempDataReminder();
        return tempDataReminder.getData().remove(s);
    }

    @Override
    public Object getTempData(String s) {
        initTempDataReminder();
        return tempDataReminder.getData().get(s);
    }

    @Override
    public List<String> getTempDataKeyList() {
        return new ArrayList<>() {{
            initTempDataReminder();
            this.addAll(tempDataReminder.getData().keySet());
        }};
    }

    @Override
    public Optional<SanctionInfo> kickPlayer(String reason, APIPlayerModerator author) {

        if (!API.getInstance().isOnlineMod())
            return Optional.empty();

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

}


