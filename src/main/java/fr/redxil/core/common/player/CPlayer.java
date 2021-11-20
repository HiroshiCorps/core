/* Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 */

package fr.redxil.core.common.player;

import fr.redline.pms.connect.linker.pm.PMManager;
import fr.redline.pms.utils.IpInfo;
import fr.redxil.api.common.API;
import fr.redxil.api.common.game.team.Team;
import fr.redxil.api.common.message.Color;
import fr.redxil.api.common.message.TextComponentBuilder;
import fr.redxil.api.common.party.Party;
import fr.redxil.api.common.player.APIOfflinePlayer;
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.api.common.player.data.SanctionInfo;
import fr.redxil.api.common.player.data.Setting;
import fr.redxil.api.common.player.moderators.APIPlayerModerator;
import fr.redxil.api.common.player.nick.NickData;
import fr.redxil.api.common.rank.RankList;
import fr.redxil.api.common.redis.RedisManager;
import fr.redxil.api.common.server.Server;
import fr.redxil.api.common.utils.SanctionType;
import fr.redxil.core.common.CoreAPI;
import fr.redxil.core.common.data.FriendDataValue;
import fr.redxil.core.common.data.MoneyDataValue;
import fr.redxil.core.common.data.PlayerDataValue;
import fr.redxil.core.common.data.utils.DataType;
import fr.redxil.core.common.sql.SQLModels;
import fr.redxil.core.common.sql.money.MoneyModel;
import fr.redxil.core.common.sql.player.PlayerFriendModel;
import fr.redxil.core.common.sql.player.PlayerModel;
import fr.redxil.core.common.sql.player.SettingsModel;
import fr.redxil.core.common.sql.sanction.SanctionModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class CPlayer implements APIPlayer {

    //model variable
    private final long memberID;
    List<SanctionInfo> sanctionModelList = null;
    List<Setting> settingsModelList = null;

    public CPlayer(long memberID) {
        this.memberID = memberID;
    }


    /// <!-------------------- Server part --------------------!>

    protected static APIPlayer loadPlayer(String name, UUID uuid, IpInfo ipInfo) {

        RedisManager redisManager = API.getInstance().getRedisManager();

        PlayerModel playerModel = new SQLModels<>(PlayerModel.class).getOrInsert(new HashMap<String, Object>() {{
            this.put(PlayerDataValue.PLAYER_NAME_SQL.getString(null), name);
            this.put(PlayerDataValue.PLAYER_UUID_SQL.getString(null), uuid.toString());
            this.put(PlayerDataValue.PLAYER_RANK_SQL.getString(null), RankList.JOUEUR.getRankPower().intValue());
        }}, "WHERE " + CoreAPI.getInstance().getPlayerManager().getPlayerIdentifierColumn() + " = ?", CoreAPI.getInstance().getPlayerManager().getIdentifierString(name, uuid));

        long memberID = playerModel.getMemberId();

        PlayerFriendModel PlayerFriendModel = new SQLModels<>(PlayerFriendModel.class).getOrInsert(new HashMap<String, Object>() {{
            this.put(PlayerDataValue.PLAYER_MEMBERID_SQL.getString(null), memberID);
        }}, "WHERE " + PlayerDataValue.PLAYER_MEMBERID_SQL.getString(null) + " = ?", memberID);

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

        redisManager.setRedisList(FriendDataValue.PLAYER_FRIENDLIST_REDIS.getString(name, memberID), PlayerFriendModel.getFriendList());
        redisManager.setRedisList(FriendDataValue.PLAYER_BLACKLIST_REDIS.getString(name, memberID), PlayerFriendModel.getBlackList());
        redisManager.setRedisList(FriendDataValue.PLAYER_FRIENDSENDEDLIST_REDIS.getString(name, memberID), PlayerFriendModel.getSendedList());
        redisManager.setRedisList(FriendDataValue.PLAYER_FRIENDRECEIVEDLIST_REDIS.getString(name, memberID), PlayerFriendModel.getReceivedList());

        redisManager.getRedisList("ip/" + ipInfo.getIp()).add(name);

        redisManager.getRedisMap(PlayerDataValue.MAP_PLAYER_NAME.getString(name, memberID)).put(name, memberID);
        redisManager.getRedisMap(PlayerDataValue.MAP_PLAYER_UUID.getString(name, memberID)).put(uuid.toString(), memberID);

        redisManager.getRedisList(PlayerDataValue.LIST_PLAYER_ID.getString(name, memberID)).add(memberID);

        return new CPlayer(memberID);

    }

    @Override
    public void unloadPlayer() {
        if (!API.getInstance().isBungee()) return;

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

        /// Friend Part

        PlayerFriendModel PlayerFriendModel = new SQLModels<>(PlayerFriendModel.class).getFirst("WHERE " + PlayerDataValue.PLAYER_MEMBERID_SQL.getString(null) + " = ?", memberID);

        if (PlayerFriendModel != null) {
            PlayerFriendModel.setBlackList(getBlackList());
            PlayerFriendModel.setFriendList(getFriendList());
            PlayerFriendModel.setReceivedList(getFriendInviteReceived());
            PlayerFriendModel.setSendedList(getFriendInviteSended());
        }

        FriendDataValue.clearRedisData(DataType.PLAYER, name, memberID);

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
    public boolean hasFriend(APIOfflinePlayer playerName) {
        return getFriendList().contains(playerName.getName());
    }

    @Override
    public List<String> getFriendInviteReceived() {

        return API.getInstance().getRedisManager().getRedissonClient().getList(FriendDataValue.PLAYER_FRIENDRECEIVEDLIST_REDIS.getString(this));

    }

    @Override
    public boolean acceptFriendInvite(APIOfflinePlayer s) {
        if (!hasFriendReceived(s)) {
            TextComponentBuilder.createTextComponent("Impossible d'accepter la demande d'amis, elle à dûes être annulée").setColor(Color.RED).sendTo(this);
            return false;
        }

        getFriendInviteReceived().remove(s.getName());

        List<String> sList = getFriendList();
        if (hasFriend(s)) {
            TextComponentBuilder.createTextComponent("Une erreur est apparue, vous êtes déjà amis avec cette personne").setColor(Color.RED).sendTo(this);
            return false;
        }

        if (s.acceptFriendInviteReceived(this)) {

            sList.add(s.getName());
            TextComponentBuilder.createTextComponent("Demande d'amis de: " + s.getName(false) + " accepté").setColor(Color.GREEN).sendTo(this);
            return true;

        }

        TextComponentBuilder.createTextComponent("Impossible d'accepter la demande d'amis, elle à dûes être annulée").setColor(Color.RED).sendTo(this);
        return false;
    }

    @Override
    public boolean refusedFriendInvite(APIOfflinePlayer s) {
        List<String> sList = getFriendInviteReceived();
        if (!sList.contains(s.getName())) {
            TextComponentBuilder.createTextComponent("Impossible de refuser la demande d'amis, elle à dûes être annulée").setColor(Color.RED).sendTo(this);
            return false;
        }
        TextComponentBuilder.createTextComponent("Demande d'amis refusée").setColor(Color.GREEN).sendTo(this);
        sList.remove(s.getName());
        s.refusedFriendInviteReceived(this);
        return true;
    }

    @Override
    public List<String> getFriendInviteSended() {

        return API.getInstance().getRedisManager().getRedissonClient().getList(FriendDataValue.PLAYER_FRIENDSENDEDLIST_REDIS.getString(this));

    }

    @Override
    public boolean revokeFriendInvite(APIOfflinePlayer s) {
        if (!getFriendInviteSended().contains(s.getName())) {
            TextComponentBuilder.createTextComponent("Impossible de révoquer la demande d'amis").setColor(Color.RED).sendTo(this);
            return false;
        }
        TextComponentBuilder.createTextComponent("Demande d'amis révoquée").setColor(Color.GREEN).sendTo(this);
        getFriendInviteSended().remove(s.getName());
        s.friendInviteRevokeReceived(this);
        return true;
    }

    @Override
    public boolean sendFriendInvite(APIOfflinePlayer s) {

        if (getName().equals(s.getName())) {
            TextComponentBuilder.createTextComponent("Hmm très intéressant, quel est ta vie pour interargir avec toi-même ?").setColor(Color.RED).sendTo(this);
            return false;
        }

        if (hasFriend(s)) {
            TextComponentBuilder.createTextComponent("Hmm très intéressant, pourquoi demander en amis une personne que tu as déjà en amis?").setColor(Color.RED).sendTo(this);
            return false;
        }

        if (hasFriendReceived(s)) {
            if (acceptFriendInvite(s))
                return true;
        }

        if (hasFriendSend(s)) {
            TextComponentBuilder.createTextComponent("Hmm très intéressant, pourquoi à nouveau le demander en amis? Une fois ne te suffit donc pas").setColor(Color.RED).sendTo(this);
            return false;
        }

        if (s.friendInviteReceived(this)) {
            getFriendInviteSended().add(s.getName());
            TextComponentBuilder.createTextComponent("Demande d'amis envoyée à: " + s.getName(true)).setColor(Color.GREEN).sendTo(this);
            return true;
        }

        TextComponentBuilder.createTextComponent("Erreur, impossible de demander en amis la personne").setColor(Color.RED).sendTo(this);
        return false;

    }

    @Override
    public List<String> getFriendList() {

        return API.getInstance().getRedisManager().getRedissonClient().getList(FriendDataValue.PLAYER_FRIENDLIST_REDIS.getString(this));

    }

    @Override
    public void removeFriend(APIOfflinePlayer s) {

        getFriendList().remove(s.getName());
        TextComponentBuilder.createTextComponent("Tu as retirée: " + s.getName(false) + " de tes amis").setColor(Color.GREEN).sendTo(this);
        s.removeFriendReceived(this);

    }

    @Override
    public void removeFriendReceived(APIOfflinePlayer s) {
        getFriendList().remove(s.getName());
        TextComponentBuilder.createTextComponent("Le joueur: ").setColor(Color.WHITE).appendNewComponentBuilder(s.getName()).setColor(Color.RED).appendNewComponentBuilder(" vous à retirée de ces amis").setColor(Color.WHITE).sendTo(this);
    }

    @Override
    public List<String> getBlackList() {

        return API.getInstance().getRedisManager().getRedissonClient().getList(FriendDataValue.PLAYER_BLACKLIST_REDIS.getString(this));

    }

    @Override
    public boolean isBlackList(APIOfflinePlayer playerName) {
        return getBlackList().contains(playerName.getName());
    }

    @Override
    public boolean friendInviteReceived(APIOfflinePlayer s) {
        if (isBlackList(s)) return false;
        getFriendInviteReceived().add(s.getName());
        TextComponentBuilder.createTextComponent("Le joueur: ").setColor(Color.WHITE)
                .appendNewComponentBuilder(s.getName()).setColor(Color.RED)
                .appendNewComponentBuilder(" vous à envoyée une demande d'amis\n").setColor(Color.WHITE)
                .appendNewComponentBuilder("Vous pouvez la ").setColor(Color.WHITE)
                .appendNewComponentBuilder("refuser").setColor(Color.RED).setOnClickExecCommand("/friend refuse " + s.getName()).setHover("friend refuse " + s.getName())
                .appendNewComponentBuilder(" ou alors l'").setColor(Color.WHITE).setOnClickExecCommand("/friend accept " + s.getName()).setHover("friend accept " + s.getName())
                .appendNewComponentBuilder("accepter").setColor(Color.GREEN)
                .sendTo(this);
        return true;
    }

    @Override
    public boolean hasFriendReceived(APIOfflinePlayer playerName) {
        return getFriendInviteReceived().contains(playerName.getName());
    }

    @Override
    public boolean hasFriendSend(APIOfflinePlayer playerName) {
        return getFriendInviteSended().contains(playerName.getName());
    }

    @Override
    public void friendInviteRevokeReceived(APIOfflinePlayer s) {
        getFriendInviteReceived().remove(s.getName());
        TextComponentBuilder.createTextComponent("Le joueur ").setColor(Color.WHITE)
                .appendNewComponentBuilder(s.getName()).setColor(Color.RED)
                .appendNewComponentBuilder(" à annulé sa demande d'amis").setColor(Color.WHITE)
                .sendTo(this);
    }

    @Override
    public boolean acceptFriendInviteReceived(APIOfflinePlayer s) {

        List<String> fList = getFriendInviteSended();
        if (!fList.contains(s.getName())) return false;

        fList.remove(s.getName());
        getFriendList().add(s.getName());

        TextComponentBuilder.createTextComponent("Le joueur ").setColor(Color.WHITE)
                .appendNewComponentBuilder(s.getName()).setColor(Color.GREEN)
                .appendNewComponentBuilder(" à accepté votre demande d'amis").setColor(Color.WHITE)
                .sendTo(this);

        return true;

    }

    @Override
    public void refusedFriendInviteReceived(APIOfflinePlayer s) {
        getFriendInviteSended().remove(s.getName());
        TextComponentBuilder.createTextComponent("Le joueur ").setColor(Color.WHITE)
                .appendNewComponentBuilder(s.getName()).setColor(Color.RED)
                .appendNewComponentBuilder(" à refusé votre demande d'amis").setColor(Color.WHITE)
                .sendTo(this);
    }

    @Override
    public boolean addBlackList(APIOfflinePlayer s) {
        List<String> fList = getBlackList();
        if (!fList.contains(s.getName())) {
            fList.add(s.getName());
            TextComponentBuilder.createTextComponent("Le joueur " + s.getName() + " est maintenant dans votre blacklist").setColor(Color.GREEN)
                    .sendTo(this);
            return true;
        }
        TextComponentBuilder.createTextComponent("Le joueur " + s.getName() + " est déjà dans votre blacklist").setColor(Color.RED)
                .sendTo(this);
        return false;
    }

    /* Settings */

    @Override
    public boolean removeBlackList(APIOfflinePlayer s) {

        if (getBlackList().remove(s.getName())) {
            TextComponentBuilder.createTextComponent("Le joueur " + s.getName() + " n'est plus dans votre blacklist").setColor(Color.GREEN)
                    .sendTo(this);
            return true;
        }

        TextComponentBuilder.createTextComponent("Le joueur " + s.getName() + " n'est pas dans votre blacklist").setColor(Color.RED)
                .sendTo(this);

        return false;

    }

    @Override
    public Party getParty() {
        return API.getInstance().getPartyManager().getParty(this);
    }

    @Override
    public void loadSettings() {
        this.settingsModelList = new ArrayList<>();
        this.settingsModelList.addAll(new SQLModels<>(SettingsModel.class).get("WHERE member_id = ? ORDER BY settings_name ASC", getMemberId()));
    }

    @Override
    public List<Setting> getSettings() {
        if (settingsModelList == null) loadSettings();
        return this.settingsModelList;
    }

    @Override
    public void removeSetting(String settingName) {
        new SQLModels<>(SettingsModel.class).delete("WHERE settings_name = ? AND player_id = ?", settingName, getMemberId());
        loadSettings();
    }

    @Override
    public Setting createSetting(String settingName, String settingValue) {

        Setting base = getSetting(settingName);
        if (base != null) {
            base.setValue(settingValue);
            return base;
        }

        SettingsModel sm = new SettingsModel(
                getMemberId(),
                settingName,
                settingValue
        );

        new SQLModels<>(SettingsModel.class).insert(sm);

        loadSettings();

        return getSetting(settingName);

    }

    @Override
    public Setting getSetting(String settingsName) {
        for (Setting sm : getSettings())
            if (settingsName.equals(sm.getName()))
                return sm;
        return null;
    }

    @Override
    public void loadSanction() {
        this.sanctionModelList = new ArrayList<>();
        this.sanctionModelList.addAll(new SQLModels<>(SanctionModel.class).get("WHERE targetID = ? ORDER BY sanctionTS DESC", getMemberId()));
    }

    @Override
    public SanctionInfo banPlayer(String reason, long time, APIPlayerModerator author) {

        if (isBan()) return null;

        SanctionModel sm = new SanctionModel(
                getMemberId(),
                author.getMemberId(),
                SanctionType.BAN,
                reason,
                time
        );

        new SQLModels<>(SanctionModel.class).insert(sm);

        loadSanction();

        return getLastSanction(SanctionType.BAN);

    }

    @Override
    public SanctionInfo mutePlayer(String reason, long time, APIPlayerModerator author) {

        if (isMute()) return null;

        SanctionModel sm = new SanctionModel(
                getMemberId(),
                author.getMemberId(),
                SanctionType.MUTE,
                reason,
                time
        );

        new SQLModels<>(SanctionModel.class).insert(sm);

        loadSanction();

        return getLastSanction(SanctionType.MUTE);

    }

    @Override
    public SanctionInfo warnPlayer(String reason, APIPlayerModerator author) {

        SanctionModel sm = new SanctionModel(
                getMemberId(),
                author.getMemberId(),
                SanctionType.WARN,
                reason
        );

        new SQLModels<>(SanctionModel.class).insert(sm);

        loadSanction();

        return getLastSanction(SanctionType.WARN);

    }

    @Override
    public List<SanctionInfo> getSanction() {
        if (sanctionModelList == null) loadSanction();
        return sanctionModelList;
    }

    @Override
    public List<SanctionInfo> getSanction(SanctionType sanctionType) {
        List<SanctionInfo> corrList = new ArrayList<>();
        for (SanctionInfo sm : getSanction()) {
            if (sm.getSanctionType().getID() == sanctionType.getID())
                corrList.add(sm);
        }
        return corrList;
    }

    @Override
    public boolean isMute() {
        SanctionInfo sm = getLastSanction(SanctionType.MUTE);
        if (sm == null) return false;
        return sm.isEffective();
    }

    @Override
    public boolean isBan() {
        SanctionInfo sm = getLastSanction(SanctionType.BAN);
        if (sm == null) return false;
        return sm.isEffective();
    }

    @Override
    public SanctionInfo getLastSanction(SanctionType sanctionType) {
        List<SanctionInfo> sanctionList = getSanction(sanctionType);
        if (sanctionList.isEmpty()) return null;

        return sanctionList.get(0);
    }

    @Override
    public boolean unBan(APIPlayerModerator mod) {

        SanctionInfo sm = getLastSanction(SanctionType.BAN);
        if (sm != null && sm.isEffective()) {
            sm.setCanceller(mod.getMemberId());
            return true;
        }

        return false;

    }

    @Override
    public boolean unMute(APIPlayerModerator mod) {

        SanctionInfo sm = getLastSanction(SanctionType.MUTE);
        if (sm != null && sm.isEffective()) {
            sm.setCanceller(mod.getMemberId());
            return true;
        }

        return false;

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

}


