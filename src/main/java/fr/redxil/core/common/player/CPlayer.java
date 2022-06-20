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
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.api.common.player.APIPlayerManager;
import fr.redxil.api.common.player.data.SanctionInfo;
import fr.redxil.api.common.player.moderators.APIPlayerModerator;
import fr.redxil.api.common.player.rank.Rank;
import fr.redxil.api.common.utils.SanctionType;
import fr.redxil.core.common.CoreAPI;
import fr.redxil.core.common.data.IDDataValue;
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
import fr.redxil.core.common.utils.DataReminder;
import fr.redxil.core.common.utils.IDGenerator;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.logging.Level;

public class CPlayer extends CPlayerOffline implements APIPlayer {

    DataReminder<Long> serverNameReminder = null;
    DataReminder<String> lastMSGReminder = null;
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
        super(IDGenerator.generateLONGID(IDDataValue.PLAYERID.getLocation()));

        initNameReminder();
        nameReminder.setData(name);
        initRealNameReminder();
        realNameReminder.setData(name);

        initUUIDReminder();
        uuidReminder.setData(uuid.toString());

        initBungeeReminder();
        bungeeReminder.setData(CoreAPI.getInstance().getServer().getServerID());

        setIP(ipInfo);

        if (CoreAPI.getInstance().isOnlineMod()) {
            this.playerModel = new SQLModels<>(PlayerModel.class).getOrInsert(new HashMap<>() {{
                this.put(PlayerDataSql.PLAYER_NAME_SQL.getSQLColumns(), name);
                this.put(PlayerDataSql.PLAYER_UUID_SQL.getSQLColumns(), uuid.toString());
                this.put(PlayerDataSql.PLAYER_RANK_SQL.getSQLColumns(), Rank.JOUEUR.getRankPower().intValue());
                this.put(PlayerDataSql.PLAYER_IP_SQL.getSQLColumns(), ipInfo.getIp());
            }}, "WHERE " + PlayerDataSql.PLAYER_UUID_SQL.getSQLColumns().toSQL() + " = ?", uuid.toString());

            this.memberID = playerModel.getMemberID();

            this.moneyModel = new SQLModels<>(MoneyModel.class).getOrInsert(new HashMap<>() {{
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

            CoreAPI.getInstance().getPlayerManager().getMap().put(memberID, this);
        }

        CoreAPI.getInstance().getPlayerManager().getNameToLongMap().put(name, memberID);
        CoreAPI.getInstance().getPlayerManager().getUUIDToLongMap().put(uuid.toString(), memberID);

        loadLink(this);

        CoreAPI.getInstance().getAPIEnabler().getLogger().log(Level.FINE, "Player Data creation finished");
    }

    static void loadLink(CPlayer cPlayer) {

        if (!CoreAPI.getInstance().isOnlineMod())
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
        Long playerServer = this.getServerID();
        if (playerServer.equals(CoreAPI.getInstance().getServerID()))
            CoreAPI.getInstance().getAPIEnabler().sendMessage(getUUID(), s);
        else
            CoreAPI.getInstance().getRedisManager().ifPresent(redis -> RedisPMManager.sendRedissonPluginMessage(redis.getRedissonClient(), "playerMessage", Long.valueOf(getMemberID()).toString() + "<msp>" + s));
    }

    @Override
    public void switchServer(long server) {
        CoreAPI.getInstance().getRedisManager().ifPresent(redis -> RedisPMManager.sendRedissonPluginMessage(redis.getRedissonClient(), "askSwitchServer", getName() + "<switchSplit>" + Long.valueOf(server).toString()));
    }

    @Override
    public void addSolde(long value) {
        setSolde(getSolde() + value);
    }

    @Override
    public void unloadPlayer() {
        if (!CoreAPI.getInstance().getAPIEnabler().isVelocity()) return;

        CoreAPI.getInstance().getModeratorManager().getModerator(getMemberID()).ifPresent(APIPlayerModerator::disconnectModerator);

        UUID uuid = getUUID();
        APIPlayerManager playerManager = CoreAPI.getInstance().getPlayerManager();

        playerManager.getUUIDToLongMap().remove(uuid.toString());
        playerManager.getNameToLongMap().remove(getName());
        playerManager.getLoadedPlayer().remove(getMemberID());

        if (CoreAPI.getInstance().isOnlineMod()) {

            getMoneyModel().set(new HashMap<>() {{
                put(MoneyDataSql.PLAYER_SOLDE_SQL.getSQLColumns(), getSolde());
                put(MoneyDataSql.PLAYER_COINS_SQL.getSQLColumns(), getCoins());
            }});

            getPlayerModel().set(PlayerDataSql.PLAYER_LC_SQL.getSQLColumns(), Timestamp.from(Instant.now()));

            MoneyDataRedis.clearRedisData(DataType.PLAYER, this.getMemberID());

            CoreAPI.getInstance().getRedisManager().ifPresent(rm -> rm.getRedisList("ip/" + getIP().getIp()).remove(getRealName()));

            PlayerDataRedis.clearRedisData(DataType.PLAYER, this.getMemberID());

        } else
            CoreAPI.getInstance().getPlayerManager().getMap().remove(memberID);

    }

    @Override
    public void addCoins(long value) {
        setCoins(getCoins() + value);
    }

    public void initSNReminder() {
        if (this.serverNameReminder == null)
            this.serverNameReminder = DataReminder.generateReminder(PlayerDataRedis.PLAYER_SPIGOT_REDIS.getString(this), null);
    }

    @Override
    public Long getServerID() {
        initSNReminder();
        return this.serverNameReminder.getData();
    }

    /// <!-------------------- Rank part --------------------!>

    @Override
    public void setServerID(long name) {
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
        return Rank.getRank(getRealRankPower()).orElse(null);
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
        setRank(getRealRank(), getRealRankTimeStamp().orElse(null));
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
        return Rank.getRank(getRankPower()).orElse(null);
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
        if (rank == Rank.SERVER)
            return;
        String timeStampString = timestamp == null ? null : timestamp.toString();
        super.setRank(rank, timestamp);
        initRealRankReminder();
        initRealRankTimeReminder();
        realRankReminder.setData(rank.getRankPower());
        realRankTimerReminder.setData(timeStampString);
    }

    @Override
    public Long getRealRankPower() {

        getRealRankTimeStamp().ifPresent(stamp -> {
            if (stamp.before(Timestamp.from(Instant.now()))) {
                setRealRank(Rank.JOUEUR);
            }
        });
        initRealRankReminder();
        return realRankReminder.getData();
    }

    @Override
    public Optional<Timestamp> getRankTimeStamp() {
        initRankTimeReminder();
        String timeStamp = rankTimerReminder.getData();
        if (timeStamp != null)
            return Optional.of(Timestamp.valueOf(timeStamp));
        return Optional.empty();
    }

    @Override
    public Optional<Timestamp> getRealRankTimeStamp() {
        initRealRankTimeReminder();
        String timeStamp = realRankTimerReminder.getData();
        if (timeStamp != null)
            return Optional.of(Timestamp.valueOf(timeStamp));
        return Optional.empty();
    }

    @Override
    public void setRank(Rank rank, Timestamp timestamp) {
        if (rank == Rank.SERVER)
            return;
        String timeStampString = timestamp == null ? null : timestamp.toString();
        initRankTimeReminder();
        initRankReminder();
        rankReminder.setData(rank.getRankPower());
        rankTimerReminder.setData(timeStampString);
        CoreAPI.getInstance().getRedisManager().ifPresent(redis -> RedisPMManager.sendRedissonPluginMessage(redis.getRedissonClient(), "rankChange", this.getUUID().toString()));
    }

    @Override
    public Long getRankPower() {
        getRankTimeStamp().ifPresent(stamp -> {
            if (stamp.before(Timestamp.from(Instant.now()))) {
                setRank(Rank.JOUEUR);
            }
        });
        initRankReminder();
        return rankReminder.getData();
    }

    @Override
    public boolean isConnected() {
        return CoreAPI.getInstance().getPlayerManager().isLoadedPlayer(getMemberID());
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
        if (s.contains(";"))
            return false;
        if (getRealName().equals(s) || !CoreAPI.getInstance().getPlayerManager().dataExist(s)) {
            String oldName = getName();
            CoreAPI.getInstance().getPlayerManager().getNameToLongMap().remove(oldName);
            initNameReminder();
            nameReminder.setData(s);
            CoreAPI.getInstance().getPlayerManager().getNameToLongMap().put(s, getMemberID());

            if (CoreAPI.getInstance().isOnlineMod()) {
                CoreAPI.getInstance().getRedisManager().ifPresent(redis -> {
                    RedisPMManager.sendRedissonPluginMessage(redis.getRedissonClient(), "nameChange", this.getUUID().toString());
                    IpInfo ipInfo = getIP();
                    if (ipInfo != null) {
                        if (oldName != null)
                            redis.getRedisList("ip/" + ipInfo).remove(oldName);
                        redis.getRedisList("ip/" + ipInfo).add(getName());
                    }
                });
            }

            return true;
        }
        return false;
    }

    public void initUUIDReminder() {
        if (uuidReminder == null)
            uuidReminder = DataReminder.generateReminder(PlayerDataRedis.PLAYER_UUID_REDIS.getString(this), null);
    }

    @Override
    public UUID getUUID() {
        initUUIDReminder();
        return UUID.fromString(uuidReminder.getData());
    }

    @Override
    public void setUUID(UUID uuid) {
        if (uuid != null) {
            CoreAPI.getInstance().getPlayerManager().getUUIDToLongMap().remove(getUUID().toString());
            initUUIDReminder();
            uuidReminder.setData(uuid.toString());
            CoreAPI.getInstance().getPlayerManager().getUUIDToLongMap().put(uuid.toString(), getMemberID());
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
        CoreAPI.getInstance().getRedisManager().ifPresent(redis -> {
            IpInfo lastIP = getIP();
            if (lastIP != null) {
                redis.getRedisList("ip/" + lastIP).remove(getName());
            }
            redis.getRedisList("ip/" + ipInfo).add(getName());
        });
        initIpReminder();
        ipReminder.setData(ipInfo.getIp());
        super.setIP(ipInfo);
    }

    public void initFreezeReminder() {
        if (this.freezeReminder == null)
            this.freezeReminder = DataReminder.generateReminder(PlayerDataRedis.PLAYER_FREEZE_REDIS.getString(this), 0L);
    }

    @Override
    public Optional<Long> getFreeze() {
        initFreezeReminder();
        return Optional.of(freezeReminder.getData());
    }

    @Override
    public boolean isFreeze() {
        return getFreeze().isPresent();
    }

    @Override
    public void setFreeze(Long s) {
        initFreezeReminder();
        freezeReminder.setData(s);
    }

    public void initTempDataReminder() {
        if (this.tempDataReminder == null)
            this.tempDataReminder = DataReminder.generateMapReminder(PlayerDataRedis.PLAYER_MAP_REDIS.getString(this));
    }


    @Override
    public void addTempData(String s, Object o) {
        initTempDataReminder();
        tempDataReminder.getData().put(s, o);
    }

    @Override
    public Optional<Object> removeTempData(String s) {
        initTempDataReminder();
        return Optional.ofNullable(tempDataReminder.getData().remove(s));
    }

    @Override
    public Optional<Object> getTempData(String s) {
        initTempDataReminder();
        return Optional.ofNullable(tempDataReminder.getData().get(s));
    }

    @Override
    public List<String> getTempDataKeyList() {
        return new ArrayList<>() {{
            initTempDataReminder();
            this.addAll(tempDataReminder.getData().keySet());
        }};
    }

    public void initLastMSG() {
        if (this.lastMSGReminder == null)
            this.lastMSGReminder = DataReminder.generateReminder(PlayerDataRedis.PLAYER_LASTMSG_REDIS.getString(this), null);
    }

    @Override
    public Optional<String> getLastMSGPlayer() {
        initLastMSG();
        return Optional.ofNullable(lastMSGReminder.getData());
    }

    @Override
    public void setLastMSGPlayer(String s) {
        initLastMSG();
        lastMSGReminder.setData(s);
    }

    @Override
    public Optional<SanctionInfo> kickPlayer(String reason, APIPlayerModerator author) {

        if (!CoreAPI.getInstance().isOnlineMod())
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


