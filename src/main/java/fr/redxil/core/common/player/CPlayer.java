/* Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 */

package fr.redxil.core.common.player;

import fr.redline.pms.connect.linker.pm.PMManager;
import fr.redline.pms.utils.IpInfo;
import fr.redxil.api.common.API;
import fr.redxil.api.common.group.party.Party;
import fr.redxil.api.common.group.team.Team;
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.api.common.player.APIPlayerManager;
import fr.redxil.api.common.player.data.SanctionInfo;
import fr.redxil.api.common.player.moderators.APIPlayerModerator;
import fr.redxil.api.common.player.nick.NickData;
import fr.redxil.api.common.rank.RankList;
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

        PlayerModel playerModel = new SQLModels<>(PlayerModel.class).getOrInsert(new HashMap<String, Object>() {{
            this.put(PlayerDataValue.PLAYER_NAME_SQL.getString(null), name);
            this.put(PlayerDataValue.PLAYER_UUID_SQL.getString(null), uuid.toString());
            this.put(PlayerDataValue.PLAYER_RANK_SQL.getString(null), RankList.JOUEUR.getRankPower().intValue());
        }}, "WHERE " + CoreAPI.getInstance().getPlayerManager().getPlayerIdentifierColumn() + " = ?", CoreAPI.getInstance().getPlayerManager().getIdentifierString(name, uuid));

        long memberID = playerModel.getMemberId();

        MoneyModel moneyModel = new SQLModels<>(MoneyModel.class).getOrInsert(new HashMap<String, Object>() {{
            this.put(PlayerDataValue.PLAYER_MEMBERID_SQL.getString(null), memberID);
            this.put(MoneyDataValue.PLAYER_SOLDE_SQL.getString(), 0);
            this.put(MoneyDataValue.PLAYER_COINS_SQL.getString(), 0);
        }}, "WHERE " + PlayerDataValue.PLAYER_MEMBERID_SQL.getString(null) + " = ?", memberID);

        PlayerDataValue.clearRedisData(DataType.PLAYER, name, memberID);

        redisManager.setRedisLong(MoneyDataValue.PLAYER_COINS_REDIS.getString(name, memberID), moneyModel.getCoins());
        redisManager.setRedisLong(MoneyDataValue.PLAYER_SOLDE_REDIS.getString(name, memberID), moneyModel.getSolde());
        redisManager.setRedisString(PlayerDataValue.PLAYER_NAME_REDIS.getString(name, memberID), name);
        redisManager.setRedisString(PlayerDataValue.PLAYER_UUID_REDIS.getString(name, memberID), uuid.toString());
        redisManager.setRedisString(PlayerDataValue.CONNECTED_BUNGEESERVER_REDIS.getString(name, memberID), API.getInstance().getServer().getServerName());
        redisManager.setRedisLong(PlayerDataValue.PLAYER_RANK_REDIS.getString(name, memberID), playerModel.getPowerRank());
        redisManager.setRedisString(PlayerDataValue.PLAYER_INPUT_REDIS.getString(name, memberID), null);
        redisManager.setRedisString(PlayerDataValue.PLAYER_IPINFO_REDIS.getString(name, memberID), ipInfo.toString());

        redisManager.getRedisList("ip/" + ipInfo.getIp()).add(name);

        redisManager.getRedisMap(PlayerDataValue.MAP_PLAYER_NAME.getString(name, memberID)).put(name, memberID);
        redisManager.getRedisMap(PlayerDataValue.MAP_PLAYER_UUID.getString(name, memberID)).put(uuid.toString(), memberID);

        redisManager.getRedisList(PlayerDataValue.LIST_PLAYER_ID.getString(name, memberID)).add(memberID);

        if (CoreAPI.getInstance().getServerAccessEnum() == CoreAPI.ServerAccessEnum.PRENIUM)
            redisManager.setRedisLong(PlayerDataValue.PLAYER_HUBLOGGED_REDIS.getString(name, memberID), 1L);

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

        List<PlayerLinkModel> playerLinkModelList = new SQLModels<>(PlayerLinkModel.class).get("SELECT * FROM link WHERE (" + LinkDataValue.FROM_ID_SQL.getString() + " = ? OR " + LinkDataValue.TO_ID_SQL.getString() + " = ?) AND (" + queryToAdd + ")", cPlayer.getMemberId(), cPlayer.getMemberId());
        for (PlayerLinkModel playerLinkModel : playerLinkModelList)
            cpm.getLinkOnConnectAction(playerLinkModel.getLinkType()).accept(cPlayer, playerLinkModel);
    }


    @Override
    public void unloadPlayer() {
        if (!API.getInstance().isVelocity()) return;

        String name = getName();

        APIPlayerModerator spm = API.getInstance().getModeratorManager().getModerator(getMemberId());
        if (spm != null)
            spm.disconnectModerator();


        Party party = getParty();
        if (party != null)
            party.quitParty(this);


        UUID uuid = getUUID();
        Team team = API.getInstance().getTeamManager().getPlayerTeam(uuid);
        if (team != null)
            team.removePlayer(uuid);

        RedisManager rm = API.getInstance().getRedisManager();

        rm.getRedisMap(PlayerDataValue.MAP_PLAYER_UUID.getString(this)).remove(uuid.toString());
        rm.getRedisMap(PlayerDataValue.MAP_PLAYER_NAME.getString(this)).remove(name);
        rm.getRedisList(PlayerDataValue.LIST_PLAYER_ID.getString(this)).remove(memberID);

        MoneyModel moneyModel = new SQLModels<>(MoneyModel.class).getFirst("WHERE " + PlayerDataValue.PLAYER_MEMBERID_SQL.getString(null) + " = ?", memberID);

        /// Money Part

        if (moneyModel != null) {
            moneyModel.set(MoneyDataValue.PLAYER_SOLDE_SQL.getString(), getSolde());
            moneyModel.set(MoneyDataValue.PLAYER_COINS_SQL.getString(), getCoins());
        }

        MoneyDataValue.clearRedisData(DataType.PLAYER, name, memberID);

        LinkDataValue.clearRedisData(DataType.PLAYER, name, memberID);

        rm.getRedisList("ip/" + getIpInfo().getIp()).remove(name);

        PlayerDataValue.clearRedisData(DataType.PLAYER, name, memberID);

    }

    @Override
    public Server getServer() {
        String serverName = API.getInstance().getRedisManager().getRedisString(PlayerDataValue.CONNECTED_SPIGOTSERVER_REDIS.getString(this));
        if (serverName == null) return null;
        return API.getInstance().getServerManager().getServer(serverName);
    }


    /// <!-------------------- Money part --------------------!>

    @Override
    public Server getBungeeServer() {
        String serverName = API.getInstance().getRedisManager().getRedisString(PlayerDataValue.CONNECTED_BUNGEESERVER_REDIS.getString(this));
        if (serverName == null) return null;
        return API.getInstance().getServerManager().getServer(serverName);
    }

    @Override
    public void switchServer(String server) {
        PMManager.sendRedissonPluginMessage(API.getInstance().getRedisManager().getRedissonClient(), "switchServer", getName() + "<switchSplit>" + server);
    }

    @Override
    public void addSolde(long value) {
        API.getInstance().getRedisManager().setRedisLong(MoneyDataValue.PLAYER_SOLDE_REDIS.getString(this), getSolde() + value);
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
        API.getInstance().getRedisManager().setRedisLong(MoneyDataValue.PLAYER_COINS_REDIS.getString(this), getCoins() + value);
    }


    /// <!-------------------- Rank part --------------------!>

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

    @Override
    public RankList getRank() {
        return RankList.getRank(getRankPower());
    }

    @Override
    public void setRank(RankList rankList) {
        PlayerModel playerModel = new SQLModels<>(PlayerModel.class).getFirst("WHERE " + PlayerDataValue.PLAYER_MEMBERID_SQL.getString(null) + " = ?", memberID);
        if (playerModel != null)
            playerModel.set(PlayerDataValue.PLAYER_RANK_REDIS.getString(this), getRankPower().intValue());
        API.getInstance().getRedisManager().setRedisLong(PlayerDataValue.PLAYER_RANK_REDIS.getString(this), rankList.getRankPower());
        PMManager.sendRedissonPluginMessage(API.getInstance().getRedisManager().getRedissonClient(), "rankChange", this.getUUID().toString());
    }

    @Override
    public RankList getRank(boolean nickCare) {
        return RankList.getRank(getRankPower(nickCare));
    }

    @Override
    public Long getRankPower() {
        return API.getInstance().getRedisManager().getRedisLong(PlayerDataValue.PLAYER_RANK_REDIS.getString(this));
    }


    /// <!-------------------- String part --------------------!>

    @Override
    public Long getRankPower(boolean nickCare) {
        if (nickCare) {
            NickData nick = API.getInstance().getNickGestion().getNickData(this);
            if (nick != null)
                return nick.getRank().getRankPower();
        }
        return getRankPower();
    }

    @Override
    public boolean hasPermission(long power) {
        return getRankPower() >= power;
    }


    /// <!-------------------- APIPlayer part --------------------!>

    @Override
    public String getTabString() {
        return getRank(true).getTabString() + getName(true);
    }

    @Override
    public String getChatString() {
        RankList rankList = getRank(true);
        return rankList.getChatRankString() + getName(true) + rankList.getChatSeparator() + "";
    }

    @Override
    public boolean isConnected() {
        return API.getInstance().getRedisManager().getRedissonClient().getList(PlayerDataValue.LIST_PLAYER_ID.getString(this)).contains(Long.valueOf(memberID).toString());
    }

    @Override
    public String getName() {
        return API.getInstance().getRedisManager().getRedisString(PlayerDataValue.PLAYER_NAME_REDIS.getString(this));
    }

    @Override
    public void setName(String s) {
        if (s != null) {
            API.getInstance().getRedisManager().getRedisMap(PlayerDataValue.MAP_PLAYER_NAME.getString(this)).remove(getName());
            API.getInstance().getRedisManager().setRedisString(PlayerDataValue.PLAYER_NAME_REDIS.getString(this), s);
            API.getInstance().getRedisManager().getRedisMap(PlayerDataValue.MAP_PLAYER_NAME.getString(this)).put(getName(), memberID);
        }
    }

    @Override
    public String getName(boolean nickCare) {
        if (nickCare) {
            NickData nickData = API.getInstance().getNickGestion().getNickData(this);
            if (nickData != null) return nickData.getName();
        }
        return getName();
    }

    @Override
    public long getMemberId() {
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
    public IpInfo getIpInfo() {
        return IpInfo.fromString(API.getInstance().getRedisManager().getRedisString(PlayerDataValue.PLAYER_IPINFO_REDIS.getString(this)));
    }

    @Override
    public boolean isNick() {
        return API.getInstance().getNickGestion().hasNick(this);
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
    public boolean hasTeam() {
        return API.getInstance().getTeamManager().hasTeam(this);
    }

    @Override
    public Team getTeam() {
        return API.getInstance().getTeamManager().getPlayerTeam(this);
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
        return new ArrayList<String>() {{
            API.getInstance().getRedisManager().getRedisMap(PlayerDataValue.PLAYER_MAP_REDIS.getString(getName(), getMemberId())).keySet().forEach((object) -> add((String) object));
        }};
    }

    @Override
    public SanctionInfo kickPlayer(String reason, APIPlayerModerator author) {

        SanctionModel sm = new SanctionModel(
                getMemberId(),
                author.getMemberId(),
                SanctionType.KICK,
                reason
        );

        new SQLModels<>(SanctionModel.class).insert(sm);

        loadSanction();

        return getLastSanction(SanctionType.KICK);

    }

    @Override
    public boolean hasParty() {
        return CoreAPI.getInstance().getPartyManager().getParty(getMemberId()) != null;
    }

}


