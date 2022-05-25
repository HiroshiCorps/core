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
import fr.redxil.core.common.data.player.PlayerDataSql;
import fr.redxil.core.common.player.sqlmodel.moderator.SanctionModel;
import fr.redxil.core.common.player.sqlmodel.player.MoneyModel;
import fr.redxil.core.common.player.sqlmodel.player.PlayerLinkModel;
import fr.redxil.core.common.player.sqlmodel.player.PlayerModel;
import fr.redxil.core.common.player.sqlmodel.player.SettingsModel;
import fr.redxil.core.common.sql.SQLModels;
import fr.redxil.core.common.sql.utils.SQLColumns;

import javax.annotation.Nullable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;

public class CPlayerOffline implements APIOfflinePlayer {

    protected long memberID;
    List<SanctionInfo> sanctionModelList = null;
    List<Setting> settingsModelList = null;
    protected PlayerModel playerModel = null;
    protected MoneyModel moneyModel = null;

    public CPlayerOffline(PlayerModel playerModel) {
        this.playerModel = playerModel;
        this.memberID = playerModel.getMemberID();
    }

    public CPlayerOffline(long memberID) {
        this.memberID = memberID;
    }


    private void initPlayerModel() {
        if (this.playerModel == null && API.getInstance().isOnlineMod())
            this.playerModel = new SQLModels<>(PlayerModel.class).getFirst("WHERE " + PlayerDataSql.PLAYER_MEMBERID_SQL.getSQLColumns().toSQL() + " = ?", memberID);
    }

    private void initMoneyModel() {
        if (this.moneyModel == null && API.getInstance().isOnlineMod())
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
        setSolde(getSolde() + i);
    }

    @Override
    public boolean setSolde(long i) {

        if (i <= 0)
            return false;

        MoneyModel moneyModel1 = getMoneyModel();
        if (moneyModel1 != null)
            moneyModel1.set(MoneyDataSql.PLAYER_SOLDE_SQL.getSQLColumns(), i);

        return true;

    }

    @Override
    public void addCoins(long i) {
        setCoins(getCoins() + i);
    }

    @Override
    public boolean setCoins(long i) {
        if (i <= 0)
            return false;

        MoneyModel moneyModel1 = getMoneyModel();
        if (moneyModel1 != null)
            moneyModel1.set(MoneyDataSql.PLAYER_COINS_SQL.getSQLColumns(), i);
        return true;
    }

    @Override
    public Rank getRank() {
        PlayerModel playerModel1 = getPlayerModel();
        if (playerModel1 != null)
            return playerModel1.getRank();
        return Rank.JOUEUR;
    }

    @Override
    public void setRank(Rank Rank) {
        PlayerModel playerModel1 = getPlayerModel();
        if (playerModel1 != null)
            playerModel1.set(PlayerDataSql.PLAYER_RANK_SQL.getSQLColumns(), Rank.getRankPower().intValue());
    }

    @Override
    public void setRank(Rank rank, Timestamp timestamp) {
        PlayerModel playerModel1 = getPlayerModel();
        if (playerModel1 != null) {
            playerModel1.set(PlayerDataSql.PLAYER_RANK_SQL.getSQLColumns(), getRankPower().intValue());
            playerModel1.set(PlayerDataSql.PLAYER_RANK_TIME_SQL.getSQLColumns(), timestamp);
        }
    }

    @Override
    public Timestamp getRankTimeStamp() {
        PlayerModel playerModel1 = getPlayerModel();
        if (playerModel1 != null)
            return (Timestamp) playerModel1.get(PlayerDataSql.PLAYER_RANK_TIME_SQL.getSQLColumns());
        else return null;
    }

    @Override
    public Long getRankPower() {
        Rank rank = getRank();
        if (rank == null)
            return Rank.JOUEUR.getRankPower();
        return rank.getRankPower();
    }

    @Override
    public boolean hasPermission(long l) {
        return getRank().getRankPower() >= l;
    }

    @Override
    public long getSolde() {
        MoneyModel moneyModel1 = getMoneyModel();
        if (moneyModel1 != null)
            return moneyModel1.getSolde();
        return 0L;
    }

    @Override
    public long getCoins() {
        MoneyModel moneyModel1 = getMoneyModel();
        if (moneyModel1 != null)
            return moneyModel1.getCoins();
        return 0L;
    }

    @Override
    public String getName() {
        return getPlayerModel().getName();
    }

    @Override
    public boolean setName(String s) {
        if (s != null) {
            PlayerModel playerModel1 = getPlayerModel();
            if (playerModel1 != null)
                playerModel1.set(PlayerDataSql.PLAYER_NAME_SQL.getSQLColumns(), s);
            else return false;
            return true;
        }
        return false;
    }

    @Override
    public UUID getUUID() {
        PlayerModel playerModel1 = getPlayerModel();
        if (playerModel1 != null)
            return playerModel1.getUUID();
        return null;
    }

    @Override
    public void setUUID(UUID uuid) {
        if (uuid == null)
            return;
        PlayerModel playerModel1 = getPlayerModel();
        if (playerModel1 != null)
            playerModel1.set(PlayerDataSql.PLAYER_UUID_SQL.getSQLColumns(), uuid.toString());
    }

    @Override
    public long getMemberID() {
        return memberID;
    }

    @Override
    public boolean isConnected() {
        return API.getInstance().getPlayerManager().getLoadedPlayer().contains(getMemberID());
    }

    @Override
    public void loadSettings() {
        this.settingsModelList = new ArrayList<>();
        if (API.getInstance().isOnlineMod())
            this.settingsModelList.addAll(new SQLModels<>(SettingsModel.class).get("WHERE member_id = ? ORDER BY settings_name ASC", getMemberID()));
    }

    @Override
    public List<Setting> getSettings() {
        if (settingsModelList == null) loadSettings();
        return this.settingsModelList;
    }

    @Override
    public void removeSetting(String settingName) {
        if (API.getInstance().isOnlineMod())
            new SQLModels<>(SettingsModel.class).delete("WHERE settings_name = ? AND player_id = ?", settingName, getMemberID());
        loadSettings();
    }

    @Override
    public Optional<Setting> createSetting(String settingName, String settingValue) {

        if (!API.getInstance().isOnlineMod())
            return Optional.empty();

        Optional<Setting> base = getSetting(settingName);
        if (base.isPresent()) {
            base.get().setValue(settingValue);
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
    public Optional<Setting> getSetting(String settingsName) {
        for (Setting sm : getSettings())
            if (settingsName.equals(sm.getName()))
                return Optional.of(sm);
        return Optional.empty();
    }


    @Override
    public boolean hasLinkWith(LinkUsage linkUsage, @Nullable APIOfflinePlayer apiOfflinePlayer, String... strings) {
        return getLink(linkUsage, apiOfflinePlayer, strings).isPresent();
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
    public Optional<LinkData> getLink(LinkUsage linkUsage, @Nullable APIOfflinePlayer apiOfflinePlayer, String... s) {
        List<LinkData> linkList = getLinks(linkUsage, apiOfflinePlayer, s);
        if (linkList.isEmpty())
            return Optional.empty();
        return Optional.of(linkList.get(0));
    }

    @Override
    public Optional<LinkData> createLink(APIOfflinePlayer apiOfflinePlayer, String s) {

        if (!API.getInstance().isOnlineMod())
            return Optional.empty();

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
        PlayerModel playerModel1 = getPlayerModel();
        if (playerModel1 != null)
            playerModel1.set(PlayerDataSql.PLAYER_IP_SQL.getSQLColumns(), ipInfo.getIp());
    }

    @Override
    public void loadSanction() {
        this.sanctionModelList = new ArrayList<>();
        if (!API.getInstance().isOnlineMod())
            return;
        this.sanctionModelList.addAll(new SQLModels<>(SanctionModel.class).get("WHERE targetID = ? ORDER BY sanctionTS DESC", getMemberID()));
        API.getInstance().getPluginEnabler().printLog(Level.INFO, "Sanction: " + this.sanctionModelList.size());
    }

    @Override
    public Optional<SanctionInfo> banPlayer(String reason, long time, APIPlayerModerator author) {

        if (isBan() || !API.getInstance().isOnlineMod()) return Optional.empty();

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
    public Optional<SanctionInfo> mutePlayer(String reason, long time, APIPlayerModerator author) {

        if (isMute() || !API.getInstance().isOnlineMod()) return Optional.empty();

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
    public Optional<SanctionInfo> warnPlayer(String reason, APIPlayerModerator author) {

        if (!API.getInstance().isOnlineMod())
            return Optional.empty();

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
        Optional<SanctionInfo> sm = getLastSanction(SanctionType.MUTE);
        if (sm.isEmpty()) return false;
        return sm.get().isEffective();
    }

    @Override
    public boolean isBan() {
        Optional<SanctionInfo> sm = getLastSanction(SanctionType.BAN);
        if (sm.isEmpty()) return false;
        return sm.get().isEffective();
    }

    @Override
    public Optional<SanctionInfo> getLastSanction(SanctionType sanctionType) {
        List<SanctionInfo> sanctionList = getSanction(sanctionType);
        if (sanctionList.isEmpty()) {
            API.getInstance().getPluginEnabler().printLog(Level.INFO, "Pas de sanction");
            return Optional.empty();
        }

        return Optional.of(sanctionList.get(0));
    }

    @Override
    public boolean unBan(APIPlayerModerator mod) {

        Optional<SanctionInfo> sm = getLastSanction(SanctionType.BAN);
        if (sm.isPresent() && sm.get().isEffective()) {
            sm.get().setCanceller(mod.getMemberID());
            return true;
        }

        return false;

    }

    @Override
    public boolean unMute(APIPlayerModerator mod) {

        Optional<SanctionInfo> sm = getLastSanction(SanctionType.MUTE);
        if (sm.isPresent() && sm.get().isEffective()) {
            sm.get().setCanceller(mod.getMemberID());
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
