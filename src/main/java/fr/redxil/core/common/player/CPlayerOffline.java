/* Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 */

package fr.redxil.core.common.player;

import fr.redxil.api.common.API;
import fr.redxil.api.common.player.APIOfflinePlayer;
import fr.redxil.api.common.player.data.LinkData;
import fr.redxil.api.common.player.data.LinkUsage;
import fr.redxil.api.common.player.data.SanctionInfo;
import fr.redxil.api.common.player.data.Setting;
import fr.redxil.api.common.player.moderators.APIPlayerModerator;
import fr.redxil.api.common.player.rank.Rank;
import fr.redxil.api.common.utils.Pair;
import fr.redxil.api.common.utils.SanctionType;
import fr.redxil.core.common.data.LinkDataValue;
import fr.redxil.core.common.data.MoneyDataValue;
import fr.redxil.core.common.data.PlayerDataValue;
import fr.redxil.core.common.player.sqlmodel.moderator.SanctionModel;
import fr.redxil.core.common.player.sqlmodel.player.MoneyModel;
import fr.redxil.core.common.player.sqlmodel.player.PlayerLinkModel;
import fr.redxil.core.common.player.sqlmodel.player.PlayerModel;
import fr.redxil.core.common.player.sqlmodel.player.SettingsModel;
import fr.redxil.core.common.sql.SQLModels;

import javax.annotation.Nullable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CPlayerOffline implements APIOfflinePlayer {

    List<SanctionInfo> sanctionModelList = null;
    List<Setting> settingsModelList = null;
    private final long memberID;
    private PlayerModel playerModel = null;
    private MoneyModel moneyModel = null;

    public CPlayerOffline(String name) {

        playerModel = new SQLModels<>(PlayerModel.class).getFirst("WHERE " + PlayerDataValue.PLAYER_NAME_SQL.getString(null) + " = ?", name);
        this.memberID = this.getPlayerModel().getMemberId();

    }

    public CPlayerOffline(UUID uuid) {

        playerModel = new SQLModels<>(PlayerModel.class).getFirst("WHERE " + PlayerDataValue.PLAYER_UUID_SQL.getString(null) + " = ?", uuid.toString());
        this.memberID = this.getPlayerModel().getMemberId();

    }


    public CPlayerOffline(long memberID) {
        this.memberID = memberID;
    }


    private void initPlayerModel() {
        if (this.getPlayerModel() == null)
            this.playerModel = new SQLModels<>(PlayerModel.class).getFirst("WHERE " + PlayerDataValue.PLAYER_MEMBERID_SQL.getString(null) + " = ?", memberID);
    }

    private void initMoneyModel() {
        if (this.getMoneyModel() == null)
            this.moneyModel = new SQLModels<>(MoneyModel.class).getFirst("WHERE " + PlayerDataValue.PLAYER_MEMBERID_SQL.getString(null) + " = ?", memberID);
    }

    public PlayerModel getPlayerModel() {
        if (playerModel == null)
            initPlayerModel();
        return playerModel;
    }

    public MoneyModel getMoneyModel() {
        if (moneyModel == null)
            initMoneyModel();
        return moneyModel;
    }

    @Override
    public void addSolde(long i) {

        getMoneyModel().set(MoneyDataValue.PLAYER_SOLDE_SQL.getString(), getMoneyModel().getSolde() + i);
    }

    @Override
    public boolean setSolde(long i) {

        if (i <= 0)
            return false;


        getMoneyModel().set(MoneyDataValue.PLAYER_SOLDE_SQL.getString(), i);

        return true;

    }

    @Override
    public void addCoins(long i) {

        getMoneyModel().set(MoneyDataValue.PLAYER_COINS_SQL.getString(), getMoneyModel().getCoins() + i);
    }

    @Override
    public boolean setCoins(long i) {
        if (i <= 0)
            return false;


        getMoneyModel().set(MoneyDataValue.PLAYER_COINS_SQL.getString(), getMoneyModel().getCoins() + i);
        return true;
    }

    @Override
    public Rank getRank() {
        return getPlayerModel().getRank();
    }

    @Override
    public void setRank(Rank Rank) {
        getPlayerModel().set(PlayerDataValue.PLAYER_RANK_SQL.getString(null), Rank.getRankPower().intValue());
    }

    @Override
    public void setRank(Rank rank, Timestamp timestamp) {
        getPlayerModel().set(PlayerDataValue.PLAYER_RANK_REDIS.getString(this), getRankPower().intValue());
        getPlayerModel().set(PlayerDataValue.PLAYER_RANK_TIME_SQL.getString(this), timestamp);
    }

    @Override
    public Long getRankPower() {
        return getRank().getRankPower();
    }

    @Override
    public boolean hasPermission(long l) {
        return getRank().getRankPower() >= l;
    }

    @Override
    public long getSolde() {

        return getMoneyModel().getSolde();
    }

    @Override
    public long getCoins() {

        return getMoneyModel().getCoins();
    }

    @Override
    public String getName() {
        return getPlayerModel().getName();
    }

    @Override
    public boolean setName(String s) {
        if (s != null) {
            getPlayerModel().set(PlayerDataValue.PLAYER_NAME_SQL.getString(), s);
            return true;
        }
        return false;
    }

    @Override
    public UUID getUUID() {
        return getPlayerModel().getUUID();
    }

    @Override
    public void setUUID(UUID uuid) {
        if (uuid != null)
            getPlayerModel().set(PlayerDataValue.PLAYER_UUID_SQL.getString(), uuid.toString());
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
    public boolean hasLinkWith(LinkUsage linkUsage, @Nullable APIOfflinePlayer apiOfflinePlayer, String... strings) {
        return getLink(linkUsage, apiOfflinePlayer, strings) != null;
    }

    @Override
    public List<LinkData> getLinks(LinkUsage linkUsage, @Nullable APIOfflinePlayer apiOfflinePlayer, String... s) {
        Pair<String, List<Object>> pair = getWhereString(linkUsage, apiOfflinePlayer);
        pair.getTwo().add(s);
        return new ArrayList<>() {{
            this.addAll(new SQLModels<>(PlayerLinkModel.class).get("(" + pair.getOne() + ") AND " + getStringSQL(LinkDataValue.LINK_TYPE_SQL.getString(), s.length) + " ORDER BY " + LinkDataValue.LINK_ID_SQL.getString() + " DESC", pair.getTwo().toArray(), s));
        }};
    }

    @Override
    public LinkData getLink(LinkUsage linkUsage, @Nullable APIOfflinePlayer apiOfflinePlayer, String... s) {
        return getLinks(linkUsage, apiOfflinePlayer, s).get(0);
    }

    @Override
    public LinkData createLink(APIOfflinePlayer apiOfflinePlayer, String s) {

        PlayerLinkModel linkData = new PlayerLinkModel(this, apiOfflinePlayer, s);

        new SQLModels<>(PlayerLinkModel.class).insert(linkData);

        return getLink(LinkUsage.TO, apiOfflinePlayer, s);

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

    public Pair<String, List<Object>> getWhereString(LinkUsage linkUsage, @Nullable APIOfflinePlayer player2) {
        int id1 = Long.valueOf(getMemberId()).intValue();
        Integer id2 = null;
        if (player2 != null)
            id2 = Long.valueOf(player2.getMemberId()).intValue();
        switch (linkUsage) {
            case FROM: {
                String queries = LinkDataValue.TO_ID_SQL.getString() + " = ?";
                if (id2 != null)
                    queries += " AND " + LinkDataValue.FROM_ID_SQL.getString() + " = ?";
                Integer finalId = id2;
                return new Pair<>(queries, new ArrayList<>() {{
                    add(id1);
                    if (finalId != null)
                        add(finalId);
                }});
            }
            case TO: {
                String queries = LinkDataValue.FROM_ID_SQL.getString() + " = ?";
                if (id2 != null)
                    queries += " AND " + LinkDataValue.TO_ID_SQL.getString() + " = ?";
                Integer finalId = id2;
                return new Pair<>(queries, new ArrayList<>() {{
                    add(id1);
                    if (finalId != null)
                        add(finalId);
                }});
            }
            case BOTH: {
                Pair<String, List<Object>> one = getWhereString(LinkUsage.FROM, player2);
                Pair<String, List<Object>> two = getWhereString(LinkUsage.TO, player2);
                return new Pair<>("(" + one.getOne() + ") OR (" + two.getOne() + ")", new ArrayList<>(one.getTwo()) {{
                    add(two.getTwo());
                }});
            }
            default: {
                return null;
            }
        }

    }

    public String getStringSQL(String s, int size) {

        if (size == 1)
            return s + " = ?";
        StringBuilder stringBuilder = new StringBuilder("(" + s + " = ?");
        for (int i = size - 1; i != 0; i--) {
            stringBuilder.append(" ").append("OR").append(" ").append(s).append(" = ?");
        }
        return stringBuilder.append(")").toString();

    }

}
