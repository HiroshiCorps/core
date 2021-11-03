/* Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 */

package fr.redxil.core.common.player;

import de.dytanic.cloudnet.driver.CloudNetDriver;
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

        RedisManager redisManager = CoreAPI.get().getRedisManager();

        PlayerModel PlayerModel = new SQLModels<>(PlayerModel.class).getOrInsert(new HashMap<String, Object>() {{
            this.put(PlayerDataValue.PLAYER_NAME_SQL.getString(null), name);
            this.put(PlayerDataValue.PLAYER_UUID_SQL.getString(null), uuid.toString());
            this.put(PlayerDataValue.PLAYER_RANK_SQL.getString(null), RankList.JOUEUR.getRankPower().toString());
        }}, "WHERE " + CoreAPI.getInstance().getServerAccessEnum().getPdv().getString() + " = ?", CoreAPI.getInstance().getDataForGetAndSet(name, uuid));

        long memberID = PlayerModel.getMemberId();

        PlayerFriendModel PlayerFriendModel = new SQLModels<>(PlayerFriendModel.class).getOrInsert(new HashMap<String, Object>() {{
            this.put(PlayerDataValue.PLAYER_MEMBERID_SQL.getString(null), memberID);
        }}, "WHERE " + PlayerDataValue.PLAYER_MEMBERID_SQL.getString(null) + " = ?", memberID);

        MoneyModel moneyModel = new SQLModels<>(MoneyModel.class).getOrInsert(new HashMap<String, Object>() {{
            this.put(PlayerDataValue.PLAYER_MEMBERID_SQL.getString(null), memberID);
            this.put(PlayerDataValue.PLAYER_SOLDE_SQL.getString(), 0);
            this.put(PlayerDataValue.PLAYER_COINS_SQL.getString(), 0);
        }}, "WHERE " + PlayerDataValue.PLAYER_MEMBERID_SQL.getString(null) + " = ?", memberID);

        PlayerDataValue.clearRedisData(DataType.PLAYER, name, memberID);

        redisManager.setRedisLong(PlayerDataValue.PLAYER_COINS_REDIS.getString(name, memberID), moneyModel.getCoins());
        redisManager.setRedisLong(PlayerDataValue.PLAYER_SOLDE_REDIS.getString(name, memberID), moneyModel.getSolde());
        redisManager.setRedisString(PlayerDataValue.PLAYER_NAME_REDIS.getString(name, memberID), name);
        redisManager.setRedisString(PlayerDataValue.PLAYER_UUID_REDIS.getString(name, memberID), uuid.toString());
        redisManager.setRedisString(PlayerDataValue.CONNECTED_BUNGEESERVER_REDIS.getString(name, memberID), CoreAPI.get().getServer().getServerName());
        redisManager.setRedisLong(PlayerDataValue.PLAYER_RANK_REDIS.getString(name, memberID), PlayerModel.getPowerRank());
        redisManager.setRedisString(PlayerDataValue.PLAYER_INPUT_REDIS.getString(name, memberID), null);
        redisManager.setRedisString(PlayerDataValue.PLAYER_IPINFO_REDIS.getString(name, memberID), ipInfo.toString());

        redisManager.setRedisList(PlayerDataValue.PLAYER_FRIENDLIST_REDIS.getString(name, memberID), PlayerFriendModel.getFriendList());
        redisManager.setRedisList(PlayerDataValue.PLAYER_BLACKLIST_REDIS.getString(name, memberID), PlayerFriendModel.getBlackList());
        redisManager.setRedisList(PlayerDataValue.PLAYER_FRIENDSENDEDLIST_REDIS.getString(name, memberID), PlayerFriendModel.getSendedList());
        redisManager.setRedisList(PlayerDataValue.PLAYER_FRIENDRECEIVEDLIST_REDIS.getString(name, memberID), PlayerFriendModel.getReceivedList());

        redisManager.getRedisList("ip/" + ipInfo.getIp()).add(name);

        redisManager.getRedisMap(PlayerDataValue.MAP_PLAYER_NAME.getString(name, memberID)).put(name, memberID);
        redisManager.getRedisMap(PlayerDataValue.MAP_PLAYER_UUID.getString(name, memberID)).put(uuid.toString(), memberID);

        redisManager.getRedisList(PlayerDataValue.LIST_PLAYER_ID.getString(name, memberID)).add(memberID);

        return new CPlayer(memberID);

    }

    @Override
    public void unloadPlayer() {
        if (!CoreAPI.get().isBungee()) return;

        String name = getName();

        APIPlayerModerator spm = CoreAPI.get().getModeratorManager().getModerator(getMemberId());
        if (spm != null)
            spm.disconnectModerator();


        Party party = getParty();
        if (party != null)
            party.quitParty(this);


        UUID uuid = getUUID();
        Team team = CoreAPI.get().getTeamManager().getPlayerTeam(uuid);
        if (team != null)
            team.removePlayer(uuid);

        RedisManager rm = CoreAPI.get().getRedisManager();

        rm.getRedisMap(PlayerDataValue.MAP_PLAYER_UUID.getString(this)).remove(uuid.toString());
        rm.getRedisMap(PlayerDataValue.MAP_PLAYER_NAME.getString(this)).remove(name);
        rm.getRedisList(PlayerDataValue.LIST_PLAYER_ID.getString(this)).remove(memberID);

        MoneyModel moneyModel = new SQLModels<>(MoneyModel.class).getFirst("WHERE " + PlayerDataValue.PLAYER_MEMBERID_SQL.getString(null) + " = ?", memberID);

        /// Money Part

        if (moneyModel != null) {
            moneyModel.set(PlayerDataValue.PLAYER_SOLDE_SQL.getString(), getSolde());
            moneyModel.set(PlayerDataValue.PLAYER_COINS_SQL.getString(), getCoins());
        }

        /// APIPlayer Part

        PlayerModel PlayerModel = new SQLModels<>(PlayerModel.class).getFirst("WHERE " + PlayerDataValue.PLAYER_MEMBERID_SQL.getString(null) + " = ?", memberID);
        if (PlayerModel != null)
            PlayerModel.set(PlayerDataValue.PLAYER_RANK_REDIS.getString(this), getRankPower().toString());

        /// Friend Part

        PlayerFriendModel PlayerFriendModel = new SQLModels<>(PlayerFriendModel.class).getFirst("WHERE " + PlayerDataValue.PLAYER_MEMBERID_SQL.getString(null) + " = ?", memberID);

        if (PlayerFriendModel != null) {
            PlayerFriendModel.setBlackList(getBlackList());
            PlayerFriendModel.setFriendList(getFriendList());
            PlayerFriendModel.setReceivedList(getFriendInviteReceived());
            PlayerFriendModel.setSendedList(getFriendInviteSended());
        }

        rm.getRedisList("ip/" + getIpInfo().getIp()).remove(name);

        CloudNetDriver.getInstance().getPermissionManagement().deleteUser(name);
        PlayerDataValue.clearRedisData(DataType.PLAYER, name, memberID);

    }

    @Override
    public Server getServer() {
        String serverName = CoreAPI.get().getRedisManager().getRedisString(PlayerDataValue.CONNECTED_SPIGOTSERVER_REDIS.getString(this));
        if (serverName == null) return null;
        return CoreAPI.get().getServerManager().getServer(serverName);
    }


    /// <!-------------------- Money part --------------------!>

    @Override
    public Server getBungeeServer() {
        String serverName = CoreAPI.get().getRedisManager().getRedisString(PlayerDataValue.CONNECTED_BUNGEESERVER_REDIS.getString(this));
        if (serverName == null) return null;
        return CoreAPI.get().getServerManager().getServer(serverName);
    }

    @Override
    public void switchServer(String server) {
        PMManager.sendRedissonPluginMessage(CoreAPI.get().getRedisManager().getRedissonClient(), "switchServer", getName() + "<switchSplit>" + server);
    }

    @Override
    public void addSolde(long value) {
        CoreAPI.get().getRedisManager().setRedisLong(PlayerDataValue.PLAYER_SOLDE_REDIS.getString(this), getSolde() + value);
    }

    @Override
    public boolean setSolde(long value) {

        if (value <= 0)
            return false;

        CoreAPI.get().getRedisManager().setRedisLong(PlayerDataValue.PLAYER_SOLDE_REDIS.getString(this), value);

        return true;
    }

    @Override
    public long getSolde() {
        return CoreAPI.get().getRedisManager().getRedisLong(PlayerDataValue.PLAYER_SOLDE_REDIS.getString(this));
    }

    @Override
    public void addCoins(long value) {
        CoreAPI.get().getRedisManager().setRedisLong(PlayerDataValue.PLAYER_COINS_REDIS.getString(this), getCoins() + value);
    }


    /// <!-------------------- Rank part --------------------!>

    @Override
    public boolean setCoins(long value) {
        if (value <= 0)
            return false;

        CoreAPI.get().getRedisManager().setRedisLong(PlayerDataValue.PLAYER_COINS_REDIS.getString(this), value);
        return true;
    }

    @Override
    public long getCoins() {
        return CoreAPI.get().getRedisManager().getRedisLong(PlayerDataValue.PLAYER_COINS_REDIS.getString(this));
    }

    @Override
    public RankList getRank() {
        return RankList.getRank(getRankPower());
    }

    @Override
    public void setRank(RankList rankList) {
        CoreAPI.get().getRedisManager().setRedisLong(PlayerDataValue.PLAYER_RANK_REDIS.getString(this), rankList.getRankPower());
        PMManager.sendRedissonPluginMessage(CoreAPI.get().getRedisManager().getRedissonClient(), "rankChange", this.getUUID().toString());
    }

    @Override
    public RankList getRank(boolean nickCare) {
        return RankList.getRank(getRankPower(nickCare));
    }

    @Override
    public Long getRankPower() {
        return CoreAPI.get().getRedisManager().getRedisLong(PlayerDataValue.PLAYER_RANK_REDIS.getString(this));
    }


    /// <!-------------------- String part --------------------!>

    @Override
    public Long getRankPower(boolean nickCare) {
        if (nickCare) {
            NickData nick = API.get().getNickGestion().getNickData(this);
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
        return CoreAPI.get().getRedisManager().getRedissonClient().getList(PlayerDataValue.LIST_PLAYER_ID.getString(this)).contains(Long.valueOf(memberID).toString());
    }

    @Override
    public String getName() {
        return CoreAPI.get().getRedisManager().getRedisString(PlayerDataValue.PLAYER_NAME_REDIS.getString(this));
    }

    @Override
    public void setName(String s) {
        if (s != null) {
            CoreAPI.get().getRedisManager().getRedisMap(PlayerDataValue.MAP_PLAYER_NAME.getString(this)).remove(getName());
            CoreAPI.get().getRedisManager().setRedisString(PlayerDataValue.PLAYER_NAME_REDIS.getString(this), s);
            CoreAPI.get().getRedisManager().getRedisMap(PlayerDataValue.MAP_PLAYER_NAME.getString(this)).put(getName(), memberID);
        }
    }

    @Override
    public String getName(boolean nickCare) {
        if (nickCare) {
            NickData nickData = API.get().getNickGestion().getNickData(this);
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
        return UUID.fromString(CoreAPI.get().getRedisManager().getRedisString(PlayerDataValue.PLAYER_UUID_REDIS.getString(this)));
    }

    @Override
    public void setUUID(UUID uuid) {
        if (uuid != null) {
            CoreAPI.get().getRedisManager().getRedisMap(PlayerDataValue.MAP_PLAYER_UUID.getString(this)).remove(getUUID().toString());
            CoreAPI.get().getRedisManager().setRedisString(PlayerDataValue.PLAYER_UUID_REDIS.getString(this), uuid.toString());
            CoreAPI.get().getRedisManager().getRedisMap(PlayerDataValue.MAP_PLAYER_UUID.getString(this)).put(uuid.toString(), memberID);
        }
    }

    @Override
    public IpInfo getIpInfo() {
        return IpInfo.fromString(CoreAPI.get().getRedisManager().getRedisString(PlayerDataValue.PLAYER_IPINFO_REDIS.getString(this)));
    }

    @Override
    public boolean isNick() {
        return CoreAPI.get().getNickGestion().hasNick(this);
    }

    @Override
    public boolean isLogin() {
        return CoreAPI.get().getRedisManager().getRedisLong(PlayerDataValue.PLAYER_HUBLOGGED_REDIS.getString(this)) == 1L;
    }

    @Override
    public boolean isFreeze() {
        return CoreAPI.get().getRedisManager().getRedisLong(PlayerDataValue.PLAYER_FREEZE_REDIS.getString(this)) != 0L;
    }

    @Override
    public boolean hasFriend(APIOfflinePlayer playerName) {
        return getFriendList().contains(playerName.getName());
    }

    @Override
    public List<String> getFriendInviteReceived() {

        return CoreAPI.get().getRedisManager().getRedissonClient().getList(PlayerDataValue.PLAYER_FRIENDRECEIVEDLIST_REDIS.getString(this));

    }

    @Override
    public boolean acceptFriendInvite(APIOfflinePlayer s) {
        if (!hasFriendReceived(s)) {
            TextComponentBuilder.createTextComponent(TextUtils.getPrefix("amis"))
                    .appendNewComponentBuilder("Impossible d'accepter la demande d'amis, elle à dûes être annulée").setColor(Color.RED).sendTo(this);
            return false;
        }

        getFriendInviteReceived().remove(s.getName());

        List<String> sList = getFriendList();
        if (hasFriend(s)) {
            TextComponentBuilder.createTextComponent(TextUtils.getPrefix("amis"))
                    .appendNewComponentBuilder("Une erreur est apparue, vous êtes déjà amis avec cette personne").setColor(Color.RED).sendTo(this);
            return false;
        }

        if (s.acceptFriendInviteReceived(this)) {

            sList.add(s.getName());
            TextComponentBuilder.createTextComponent(TextUtils.getPrefix("amis"))
                    .appendNewComponentBuilder("Demande d'amis de: " + s.getName(false) + " accepté").setColor(Color.GREEN).sendTo(this);
            return true;

        }

        TextComponentBuilder.createTextComponent(TextUtils.getPrefix("amis"))
                .appendNewComponentBuilder("Impossible d'accepter la demande d'amis, elle à dûes être annulée").setColor(Color.RED).sendTo(this);
        return false;
    }

    @Override
    public boolean refusedFriendInvite(APIOfflinePlayer s) {
        List<String> sList = getFriendInviteReceived();
        if (!sList.contains(s.getName())) {
            TextComponentBuilder.createTextComponent(TextUtils.getPrefix("amis"))
                    .appendNewComponentBuilder("Impossible de refuser la demande d'amis, elle à dûes être annulée").setColor(Color.RED).sendTo(this);
            return false;
        }
        TextComponentBuilder.createTextComponent(TextUtils.getPrefix("amis"))
                .appendNewComponentBuilder("Demande d'amis refusée").setColor(Color.GREEN).sendTo(this);
        sList.remove(s.getName());
        s.refusedFriendInviteReceived(this);
        return true;
    }

    @Override
    public List<String> getFriendInviteSended() {

        return CoreAPI.get().getRedisManager().getRedissonClient().getList(PlayerDataValue.PLAYER_FRIENDSENDEDLIST_REDIS.getString(this));

    }

    @Override
    public boolean revokeFriendInvite(APIOfflinePlayer s) {
        if (!getFriendInviteSended().contains(s.getName())) {
            TextComponentBuilder.createTextComponent(TextUtils.getPrefix("amis"))
                    .appendNewComponentBuilder("Impossible de révoquer la demande d'amis").setColor(Color.RED).sendTo(this);
            return false;
        }
        TextComponentBuilder.createTextComponent(TextUtils.getPrefix("amis"))
                .appendNewComponentBuilder("Demande d'amis révoquée").setColor(Color.GREEN).sendTo(this);
        getFriendInviteSended().remove(s.getName());
        s.friendInviteRevokeReceived(this);
        return true;
    }

    @Override
    public boolean sendFriendInvite(APIOfflinePlayer s) {

        if (getName().equals(s.getName())) {
            TextComponentBuilder.createTextComponent(TextUtils.getPrefix("amis"))
                    .appendNewComponentBuilder("Hmm très intéressant, quel est ta vie pour interargir avec toi-même ?").setColor(Color.RED).sendTo(this);
            return false;
        }

        if (hasFriend(s)) {
            TextComponentBuilder.createTextComponent(TextUtils.getPrefix("amis"))
                    .appendNewComponentBuilder("Hmm très intéressant, pourquoi demander en amis une personne que tu as déjà en amis?").setColor(Color.RED).sendTo(this);
            return false;
        }

        if (hasFriendReceived(s)) {
            if (acceptFriendInvite(s))
                return true;
        }

        if (hasFriendSend(s)) {
            TextComponentBuilder.createTextComponent(TextUtils.getPrefix("amis"))
                    .appendNewComponentBuilder("Hmm très intéressant, pourquoi à nouveau le demander en amis? Une fois ne te suffit donc pas").setColor(Color.RED).sendTo(this);
            return false;
        }

        if (s.friendInviteReceived(this)) {
            getFriendInviteSended().add(s.getName());
            TextComponentBuilder.createTextComponent(TextUtils.getPrefix("amis"))
                    .appendNewComponentBuilder("Demande d'amis envoyée à: " + s.getName(true)).setColor(Color.GREEN).sendTo(this);
            return true;
        }

        TextComponentBuilder.createTextComponent(TextUtils.getPrefix("amis"))
                .appendNewComponentBuilder("Erreur, impossible de demander en amis la personne").setColor(Color.RED).sendTo(this);
        return false;

    }

    @Override
    public List<String> getFriendList() {

        return CoreAPI.get().getRedisManager().getRedissonClient().getList(PlayerDataValue.PLAYER_FRIENDLIST_REDIS.getString(this));

    }

    @Override
    public void removeFriend(APIOfflinePlayer s) {

        getFriendList().remove(s.getName());
        TextComponentBuilder.createTextComponent(TextUtils.getPrefix("amis"))
                .appendNewComponentBuilder("Tu as retirée: " + s.getName(false) + " de tes amis").setColor(Color.GREEN).sendTo(this);
        s.removeFriendReceived(this);

    }

    @Override
    public void removeFriendReceived(APIOfflinePlayer s) {
        getFriendList().remove(s.getName());
        TextComponentBuilder.createTextComponent(TextUtils.getPrefix("amis"))
                .appendNewComponentBuilder("Le joueur: ").setColor(Color.WHITE).appendNewComponentBuilder(s.getName()).setColor(Color.RED).appendNewComponentBuilder(" vous à retirée de ces amis").setColor(Color.WHITE).sendTo(this);
    }

    @Override
    public List<String> getBlackList() {

        return CoreAPI.get().getRedisManager().getRedissonClient().getList(PlayerDataValue.PLAYER_BLACKLIST_REDIS.getString(this));

    }

    @Override
    public boolean isBlackList(APIOfflinePlayer playerName) {
        return getBlackList().contains(playerName.getName());
    }

    @Override
    public boolean friendInviteReceived(APIOfflinePlayer s) {
        if (isBlackList(s)) return false;
        getFriendInviteReceived().add(s.getName());
        TextComponentBuilder.createTextComponent(TextUtils.getPrefix("amis"))
                .appendNewComponentBuilder("Le joueur: ").setColor(Color.WHITE)
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
        TextComponentBuilder.createTextComponent(TextUtils.getPrefix("amis"))
                .appendNewComponentBuilder("Le joueur ").setColor(Color.WHITE)
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

        TextComponentBuilder.createTextComponent(TextUtils.getPrefix("amis"))
                .appendNewComponentBuilder("Le joueur ").setColor(Color.WHITE)
                .appendNewComponentBuilder(s.getName()).setColor(Color.GREEN)
                .appendNewComponentBuilder(" à accepté votre demande d'amis").setColor(Color.WHITE)
                .sendTo(this);

        return true;

    }

    @Override
    public void refusedFriendInviteReceived(APIOfflinePlayer s) {
        getFriendInviteSended().remove(s.getName());
        TextComponentBuilder.createTextComponent(TextUtils.getPrefix("amis"))
                .appendNewComponentBuilder("Le joueur ").setColor(Color.WHITE)
                .appendNewComponentBuilder(s.getName()).setColor(Color.RED)
                .appendNewComponentBuilder(" à refusé votre demande d'amis").setColor(Color.WHITE)
                .sendTo(this);
    }

    @Override
    public boolean addBlackList(APIOfflinePlayer s) {
        List<String> fList = getBlackList();
        if (!fList.contains(s.getName())) {
            fList.add(s.getName());
            TextComponentBuilder.createTextComponent(TextUtils.getPrefix("amis"))
                    .appendNewComponentBuilder("Le joueur " + s.getName() + " est maintenant dans votre blacklist").setColor(Color.GREEN)
                    .sendTo(this);
            return true;
        }
        TextComponentBuilder.createTextComponent(TextUtils.getPrefix("amis"))
                .appendNewComponentBuilder("Le joueur " + s.getName() + " est déjà dans votre blacklist").setColor(Color.RED)
                .sendTo(this);
        return false;
    }

    /* Settings */

    @Override
    public boolean removeBlackList(APIOfflinePlayer s) {

        if (getBlackList().remove(s.getName())) {
            TextComponentBuilder.createTextComponent(TextUtils.getPrefix("amis"))
                    .appendNewComponentBuilder("Le joueur " + s.getName() + " n'est plus dans votre blacklist").setColor(Color.GREEN)
                    .sendTo(this);
            return true;
        }

        TextComponentBuilder.createTextComponent(TextUtils.getPrefix("amis"))
                .appendNewComponentBuilder("Le joueur " + s.getName() + " n'est pas dans votre blacklist").setColor(Color.RED)
                .sendTo(this);

        return false;

    }

    @Override
    public Party getParty() {
        return CoreAPI.get().getPartyManager().getParty(this);
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


