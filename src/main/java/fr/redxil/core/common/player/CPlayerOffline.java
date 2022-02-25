/*
 *
 * Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 *
 */

package fr.redxil.core.common.player;

import fr.redline.pms.utils.IpInfo;
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
import fr.redxil.core.common.data.link.LinkDataSql;
import fr.redxil.core.common.data.money.MoneyDataSql;
import fr.redxil.core.common.data.player.PlayerDataRedis;
import fr.redxil.core.common.data.player.PlayerDataSql;
import fr.redxil.core.common.data.utils.SQLColumns;
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
import java.util.logging.Level;

public class CPlayerOffline implements APIOfflinePlayer {

    List<SanctionInfo> sanctionModelList = null;
    List<Setting> settingsModelList = null;
    private final long memberID;
    private PlayerModel playerModel = null;
    private MoneyModel moneyModel = null;

    public CPlayerOffline(PlayerModel playerModel) {
        this.playerModel = playerModel;
        this.memberID = playerModel.getMemberID();
    }

    public CPlayerOffline(long memberID) {
        this.memberID = memberID;
    }


    private void initPlayerModel() {
        if (this.playerModel == null)
            this.playerModel = new SQLModels<>(PlayerModel.class).getFirst("WHERE " + PlayerDataSql.PLAYER_MEMBERID_SQL.getSQLColumns().toSQL() + " = ?", memberID);
    }

    private void initMoneyModel() {
        if (this.moneyModel == null)
            this.moneyModel = new SQLModels<>(MoneyModel.class).getFirst("WHERE " + MoneyDataSql.PLAYER_MEMBERID_SQL.getSQLColumns().toSQL() + " = ?", memberID);
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
        getMoneyModel().set(MoneyDataSql.PLAYER_SOLDE_SQL.getSQLColumns(), getMoneyModel().getSolde() + i);
    }

    @Override
    public boolean setSolde(long i) {

        if (i <= 0)
            return false;


        getMoneyModel().set(MoneyDataSql.PLAYER_SOLDE_SQL.getSQLColumns(), i);

        return true;

    }

    @Override
    public void addCoins(long i) {

        getMoneyModel().set(MoneyDataSql.PLAYER_COINS_SQL.getSQLColumns(), getMoneyModel().getCoins() + i);
    }

    @Override
    public boolean setCoins(long i) {
        if (i <= 0)
            return false;


        getMoneyModel().set(MoneyDataSql.PLAYER_COINS_SQL.getSQLColumns(), getMoneyModel().getCoins() + i);
        return true;
    }

    @Override
    public Rank getRank() {
        return getPlayerModel().getRank();
    }

    @Override
    public void setRank(Rank Rank) {
        getPlayerModel().set(PlayerDataSql.PLAYER_RANK_SQL.getSQLColumns(), Rank.getRankPower().intValue());
    }

    @Override
    public void setRank(Rank rank, Timestamp timestamp) {
        getPlayerModel().set(PlayerDataSql.PLAYER_RANK_SQL.getSQLColumns(), getRankPower().intValue());
        getPlayerModel().set(PlayerDataSql.PLAYER_RANK_TIME_SQL.getSQLColumns(), timestamp);
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
            getPlayerModel().set(PlayerDataSql.PLAYER_NAME_SQL.getSQLColumns(), s);
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
            getPlayerModel().set(PlayerDataSql.PLAYER_UUID_SQL.getSQLColumns(), uuid.toString());
    }

    @Override
    public long getMemberID() {
        return memberID;
    }

    @Override
    public boolean isConnected() {
        return API.getInstance().getRedisManager().getRedissonClient().getList(PlayerDataRedis.LIST_PLAYER_ID.getString()).contains(getMemberID());
    }

    @Override
    public void loadSettings() {
        this.settingsModelList = new ArrayList<>();
        this.settingsModelList.addAll(new SQLModels<>(SettingsModel.class).get("WHERE member_id = ? ORDER BY settings_name ASC", getMemberID()));
    }

    @Override
    public List<Setting> getSettings() {
        if (settingsModelList == null) loadSettings();
        return this.settingsModelList;
    }

    @Override
    public void removeSetting(String settingName) {
        new SQLModels<>(SettingsModel.class).delete("WHERE settings_name = ? AND player_id = ?", settingName, getMemberID());
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
                getMemberID(),
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
            this.addAll(new SQLModels<>(PlayerLinkModel.class).get("(" + pair.getOne() + ") AND " + getStringSQL(LinkDataSql.LINK_TYPE_SQL.getSQLColumns(), s.length) + " ORDER BY " + LinkDataSql.LINK_ID_SQL.getSQLColumns().toSQL() + " DESC", pair.getTwo().toArray(), s));
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
    public IpInfo getIP() {
        return IpInfo.fromString(getPlayerModel().getString(PlayerDataSql.PLAYER_IP_SQL.getSQLColumns()));
    }

    @Override
    public void setIP(IpInfo ipInfo) {
        getPlayerModel().set(PlayerDataSql.PLAYER_IP_SQL.getSQLColumns(), ipInfo.getIp());
    }

    @Override
    public void loadSanction() {
        this.sanctionModelList = new ArrayList<>();
        this.sanctionModelList.addAll(new SQLModels<>(SanctionModel.class).get("WHERE targetID = ? ORDER BY sanctionTS DESC", getMemberID()));
        API.getInstance().getPluginEnabler().printLog(Level.INFO, "Sanction: " + this.sanctionModelList.size());
    }

    @Override
    public SanctionInfo banPlayer(String reason, long time, APIPlayerModerator author) {

        if (isBan()) return null;

        SanctionModel sm = new SanctionModel(
                getMemberID(),
                author.getMemberID(),
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
                getMemberID(),
                author.getMemberID(),
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
                getMemberID(),
                author.getMemberID(),
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
        if (sanctionList.isEmpty()) {
            API.getInstance().getPluginEnabler().printLog(Level.INFO, "Pas de sanction");
            return null;
        }

        return sanctionList.get(0);
    }

    @Override
    public boolean unBan(APIPlayerModerator mod) {

        SanctionInfo sm = getLastSanction(SanctionType.BAN);
        if (sm != null && sm.isEffective()) {
            sm.setCanceller(mod.getMemberID());
            return true;
        }

        return false;

    }

    @Override
    public boolean unMute(APIPlayerModerator mod) {

        SanctionInfo sm = getLastSanction(SanctionType.MUTE);
        if (sm != null && sm.isEffective()) {
            sm.setCanceller(mod.getMemberID());
            return true;
        }

        return false;

    }

    public Pair<String, List<Object>> getWhereString(LinkUsage linkUsage, @Nullable APIOfflinePlayer player2) {
        int id1 = Long.valueOf(getMemberID()).intValue();
        Integer id2 = null;
        if (player2 != null)
            id2 = Long.valueOf(player2.getMemberID()).intValue();
        switch (linkUsage) {
            case FROM -> {
                String queries = LinkDataSql.TO_ID_SQL.getSQLColumns() + " = ?";
                if (id2 != null)
                    queries += " AND " + LinkDataSql.FROM_ID_SQL.getSQLColumns() + " = ?";
                Integer finalID = id2;
                return new Pair<>(queries, new ArrayList<>() {{
                    add(id1);
                    if (finalID != null)
                        add(finalID);
                }});
            }
            case TO -> {
                String queries = LinkDataSql.FROM_ID_SQL.getSQLColumns() + " = ?";
                if (id2 != null)
                    queries += " AND " + LinkDataSql.TO_ID_SQL.getSQLColumns() + " = ?";
                Integer finalID = id2;
                return new Pair<>(queries, new ArrayList<>() {{
                    add(id1);
                    if (finalID != null)
                        add(finalID);
                }});
            }
            case BOTH -> {
                Pair<String, List<Object>> one = getWhereString(LinkUsage.FROM, player2);
                Pair<String, List<Object>> two = getWhereString(LinkUsage.TO, player2);
                return new Pair<>("(" + one.getOne() + ") OR (" + two.getOne() + ")", new ArrayList<>(one.getTwo()) {{
                    add(two.getTwo());
                }});
            }
            default -> {
                return null;
            }
        }

    }

    public String getStringSQL(SQLColumns s, int size) {

        if (size == 1)
            return s.toSQL() + " = ?";
        StringBuilder stringBuilder = new StringBuilder("(" + s.toSQL() + " = ?");
        for (int i = size - 1; i != 0; i--) {
            stringBuilder.append(" ").append("OR").append(" ").append(s.toSQL()).append(" = ?");
        }
        return stringBuilder.append(")").toString();

    }

}
