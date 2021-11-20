/* Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 */

package fr.redxil.core.common.player;

import fr.redxil.api.common.API;
import fr.redxil.api.common.player.APIOfflinePlayer;
import fr.redxil.api.common.player.data.SanctionInfo;
import fr.redxil.api.common.player.data.Setting;
import fr.redxil.api.common.player.moderators.APIPlayerModerator;
import fr.redxil.api.common.player.nick.NickData;
import fr.redxil.api.common.rank.RankList;
import fr.redxil.api.common.utils.SanctionType;
import fr.redxil.core.common.CoreAPI;
import fr.redxil.core.common.data.MoneyDataValue;
import fr.redxil.core.common.data.PlayerDataValue;
import fr.redxil.core.common.sql.SQLModels;
import fr.redxil.core.common.sql.money.MoneyModel;
import fr.redxil.core.common.sql.player.PlayerFriendModel;
import fr.redxil.core.common.sql.player.PlayerModel;
import fr.redxil.core.common.sql.player.SettingsModel;
import fr.redxil.core.common.sql.sanction.SanctionModel;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CPlayerOffline implements APIOfflinePlayer {

    List<SanctionInfo> sanctionModelList = null;
    List<Setting> settingsModelList = null;
    private long memberID;
    private PlayerModel model = null;
    private MoneyModel moneyModel;
    private PlayerFriendModel friendModel = null;

    public CPlayerOffline(String name) {

        this.model = new SQLModels<>(PlayerModel.class).getFirst("WHERE " + PlayerDataValue.PLAYER_NAME_SQL.getString(null) + " = ?", name);
        initPlayer(this.model.getMemberId());

    }

    public CPlayerOffline(UUID uuid) {

        this.model = new SQLModels<>(PlayerModel.class).getFirst("WHERE " + PlayerDataValue.PLAYER_UUID_SQL.getString(null) + " = ?", uuid.toString());
        initPlayer(this.model.getMemberId());

    }


    public CPlayerOffline(long memberID) {
        initPlayer(memberID);
    }

    public void initPlayer(long memberID) {

        this.memberID = memberID;
        if (this.model == null)
            this.model = new SQLModels<>(PlayerModel.class).getFirst("WHERE " + PlayerDataValue.PLAYER_MEMBERID_SQL.getString(null) + " = ?", memberID);

        this.moneyModel = new SQLModels<>(MoneyModel.class).getFirst("WHERE " + PlayerDataValue.PLAYER_MEMBERID_SQL.getString(null) + " = ?", memberID);
        this.friendModel = new SQLModels<>(PlayerFriendModel.class).getFirst("WHERE " + PlayerDataValue.PLAYER_MEMBERID_SQL.getString(null) + " = ?", memberID);

    }

    @Override
    public void addSolde(long i) {
        moneyModel.set(MoneyDataValue.PLAYER_SOLDE_SQL.getString(), moneyModel.getSolde() + i);
    }

    @Override
    public boolean setSolde(long i) {

        if (i <= 0)
            return false;

        moneyModel.set(MoneyDataValue.PLAYER_SOLDE_SQL.getString(), i);

        return true;
    }

    @Override
    public void addCoins(long i) {
        moneyModel.set(MoneyDataValue.PLAYER_COINS_SQL.getString(), moneyModel.getCoins() + i);
    }

    @Override
    public boolean setCoins(long i) {
        if (i <= 0)
            return false;

        moneyModel.set(MoneyDataValue.PLAYER_COINS_SQL.getString(), moneyModel.getCoins() + i);
        return true;
    }

    @Override
    public RankList getRank() {
        return model.getRank();
    }

    @Override
    public void setRank(RankList rankList) {
        model.set(PlayerDataValue.PLAYER_RANK_SQL.getString(null), rankList.getRankPower().intValue());
    }

    @Override
    public RankList getRank(boolean nickCare) {
        return RankList.getRank(getRankPower(nickCare));
    }

    @Override
    public Long getRankPower() {
        return getRank().getRankPower();
    }

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
    public boolean hasPermission(long l) {
        return getRank().getRankPower() >= l;
    }

    @Override
    public long getSolde() {
        return moneyModel.getSolde();
    }

    @Override
    public long getCoins() {
        return moneyModel.getCoins();
    }

    @Override
    public String getName() {
        return model.getName();
    }

    @Override
    public void setName(String s) {
        if (s != null || CoreAPI.getInstance().getServerAccessEnum() == CoreAPI.ServerAccessEnum.PRENIUM)
            model.set(PlayerDataValue.PLAYER_NAME_SQL.getString(), s);
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
    public UUID getUUID() {
        return model.getUUID();
    }

    @Override
    public void setUUID(UUID uuid) {
        if (uuid != null)
            model.set(PlayerDataValue.PLAYER_UUID_SQL.getString(), uuid.toString());
        else if (CoreAPI.getInstance().getServerAccessEnum() == CoreAPI.ServerAccessEnum.CRACK)
            model.set(PlayerDataValue.PLAYER_UUID_SQL.getString(), null);
    }

    @Override
    public long getMemberId() {
        return memberID;
    }

    @Override
    public boolean isConnected() {
        return API.getInstance().getRedisManager().getRedissonClient().getList(PlayerDataValue.LIST_PLAYER_ID.getString(null)).contains(getMemberId());
    }

    @Override
    public boolean isNick() {
        return API.getInstance().getNickGestion().hasNick(this);
    }

    @Override
    public List<String> getFriendInviteReceived() {
        return friendModel.getReceivedList();
    }

    @Override
    public boolean friendInviteReceived(APIOfflinePlayer s) {

        if (isBlackList(s)) return false;
        List<String> fList = friendModel.getReceivedList();
        if (fList.contains(s.getName())) return false;

        fList.add(s.getName());
        friendModel.setReceivedList(fList);
        return true;

    }

    @Override
    public List<String> getFriendInviteSended() {
        return friendModel.getSendedList();
    }

    @Override
    public List<String> getFriendList() {
        return friendModel.getFriendList();
    }

    @Override
    public void removeFriendReceived(APIOfflinePlayer s) {

        List<String> fList = friendModel.getFriendList();
        if (fList.remove(s.getName()))
            friendModel.setFriendList(fList);

    }

    @Override
    public boolean acceptFriendInviteReceived(APIOfflinePlayer s) {
        if (!hasFriendSend(s)) return false;

        List<String> fList = getFriendInviteSended();
        fList.remove(s.getName());
        friendModel.setSendedList(fList);

        List<String> sList = getFriendList();
        sList.add(s.getName());
        friendModel.setFriendList(fList);

        return true;
    }

    @Override
    public List<String> getBlackList() {
        return friendModel.getBlackList();
    }

    @Override
    public boolean isBlackList(APIOfflinePlayer playerName) {
        return getBlackList().contains(playerName.getName());
    }

    @Override
    public boolean hasFriend(APIOfflinePlayer playerName) {
        return getFriendList().contains(playerName.getName());
    }

    @Override
    public boolean hasFriendSend(APIOfflinePlayer playerName) {
        return getFriendInviteSended().contains(playerName.getName());
    }

    @Override
    public void friendInviteRevokeReceived(APIOfflinePlayer s) {
        List<String> fList = getFriendInviteReceived();
        if (fList.remove(s.getName()))
            friendModel.setReceivedList(fList);
    }

    @Override
    public boolean hasFriendReceived(APIOfflinePlayer playerName) {
        return getFriendInviteReceived().contains(playerName.getName());
    }

    @Override
    public void refusedFriendInviteReceived(APIOfflinePlayer s) {
        List<String> fList = getFriendInviteSended();
        if (fList.remove(s.getName()))
            friendModel.setSendedList(fList);
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

}
