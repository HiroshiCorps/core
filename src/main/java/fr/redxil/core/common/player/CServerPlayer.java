package fr.redxil.core.common.player;

import fr.redline.pms.utils.IpInfo;
import fr.redxil.api.common.player.APIOfflinePlayer;
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.api.common.player.data.LinkData;
import fr.redxil.api.common.player.data.LinkUsage;
import fr.redxil.api.common.player.data.SanctionInfo;
import fr.redxil.api.common.player.data.Setting;
import fr.redxil.api.common.player.moderators.APIPlayerModerator;
import fr.redxil.api.common.player.rank.Rank;
import fr.redxil.api.common.utils.SanctionType;
import fr.redxil.core.common.CoreAPI;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CServerPlayer implements APIPlayer {

    @Override
    public void sendMessage(String s) {
        System.out.println(s);
    }

    @Override
    public void switchServer(long server) {
    }

    @Override
    public void addSolde(long value) {
    }

    @Override
    public void unloadPlayer() {
    }

    @Override
    public void addCoins(long value) {
    }

    @Override
    public Long getServerID() {
        return CoreAPI.getInstance().getServerID();
    }

    /// <!-------------------- Rank part --------------------!>

    @Override
    public void setServerID(long serverID) {
    }

    @Override
    public long getBungeeServerID() {
        return 0L;
    }

    @Override
    public boolean setSolde(long value) {
        return true;
    }

    @Override
    public long getSolde() {
        return 9999L;
    }

    @Override
    public Rank getRealRank() {
        return Rank.SERVER;
    }

    @Override
    public void setRealRank(Rank rank) {
    }

    @Override
    public boolean setCoins(long value) {
        return true;
    }

    @Override
    public void restoreRealData() {
    }

    @Override
    public long getCoins() {
        return 9999L;
    }


    @Override
    public Rank getRank() {
        return Rank.SERVER;
    }

    @Override
    public void setRank(Rank rank) {
    }


    /// <!-------------------- String part --------------------!>

    @Override
    public boolean hasPermission(long power) {
        return true;
    }


    /// <!-------------------- APIPlayer part --------------------!>

    @Override
    public String getTabString() {
        return getRank().getTabString() + getName();
    }

    @Override
    public String getChatString() {
        Rank rank = getRank();
        return rank.getChatRankString() + getName() + rank.getChatSeparator() + "";
    }

    @Override
    public void setRealRank(Rank rank, Timestamp timestamp) {
    }

    @Override
    public Long getRealRankPower() {
        return Rank.SERVER.getRankPower();
    }

    @Override
    public Optional<Timestamp> getRankTimeStamp() {
        return Optional.empty();
    }

    @Override
    public Optional<Timestamp> getRealRankTimeStamp() {
        return Optional.empty();
    }

    @Override
    public void setRank(Rank rank, Timestamp timestamp) {
    }

    @Override
    public Long getRankPower() {
        return Rank.SERVER.getRankPower();
    }

    @Override
    public boolean isConnected() {
        return true;
    }

    @Override
    public boolean isNick() {
        return false;
    }

    @Override
    public boolean setRealName(String name) {
        return false;
    }

    @Override
    public String getRealName() {
        return "Server;";
    }

    @Override
    public String getName() {
        return "Server;";
    }

    @Override
    public boolean setName(String s) {
        return false;
    }

    @Override
    public UUID getUUID() {
        return UUID.fromString("a12345678-b123-1234-a123-1234567891011");
    }

    @Override
    public void setUUID(UUID uuid) {
    }

    @Override
    public IpInfo getIP() {
        return IpInfo.fromString("0.0.0.0:0000");
    }

    @Override
    public void setIP(IpInfo ipInfo) {
    }

    @Override
    public Optional<Long> getFreeze() {
        return Optional.empty();
    }

    @Override
    public boolean isFreeze() {
        return getFreeze().isPresent();
    }

    @Override
    public void setFreeze(Long s) {
    }


    @Override
    public void addTempData(String s, Object o) {
    }

    @Override
    public Optional<Object> removeTempData(String s) {
        return Optional.empty();
    }

    @Override
    public Optional<Object> getTempData(String s) {
        return Optional.empty();
    }

    @Override
    public List<String> getTempDataKeyList() {
        return new ArrayList<>();
    }

    @Override
    public Optional<String> getLastMSGPlayer() {
        return Optional.empty();
    }

    @Override
    public void setLastMSGPlayer(String s) {
    }

    @Override
    public Optional<SanctionInfo> kickPlayer(String reason, APIPlayerModerator author) {

        return Optional.empty();

    }

    @Override
    public Optional<SanctionInfo> banPlayer(String arg0, Timestamp arg1, APIPlayerModerator arg2) {
        return Optional.empty();
    }

    @Override
    public Optional<LinkData> createLink(APIOfflinePlayer arg0, String arg1) {
        return Optional.empty();
    }

    @Override
    public Optional<Setting> createSetting(String arg0, String arg1) {
        return Optional.empty();
    }

    @Override
    public Optional<SanctionInfo> getLastSanction(SanctionType arg0) {
        return Optional.empty();
    }

    @Override
    public Optional<LinkData> getLink(LinkUsage arg0, APIOfflinePlayer arg1, String... arg2) {
        return Optional.empty();
    }

    @Override
    public List<LinkData> getLinks(LinkUsage arg0, APIOfflinePlayer arg1, String... arg2) {
        return new ArrayList<>();
    }

    @Override
    public long getMemberID() {
        return -5L;
    }

    @Override
    public List<SanctionInfo> getSanction() {
        return new ArrayList<>();
    }

    @Override
    public List<SanctionInfo> getSanction(SanctionType arg0) {
        return new ArrayList<>();
    }

    @Override
    public Optional<Setting> getSetting(String arg0) {
        return Optional.empty();
    }

    @Override
    public List<Setting> getSettings() {
        return new ArrayList<>();
    }

    @Override
    public boolean hasLinkWith(LinkUsage arg0, APIOfflinePlayer arg1, String... arg2) {
        return false;
    }

    @Override
    public boolean isBan() {
        return false;
    }

    @Override
    public boolean isMute() {
        return false;
    }

    @Override
    public void loadSanction() {
    }

    @Override
    public void loadSettings() {
    }

    @Override
    public Optional<SanctionInfo> mutePlayer(String arg0, Timestamp arg1, APIPlayerModerator arg2) {
        return Optional.empty();
    }

    @Override
    public void removeSetting(String arg0) {
    }

    @Override
    public boolean unBan(APIPlayerModerator arg0) {
        return false;
    }

    @Override
    public boolean unMute(APIPlayerModerator arg0) {
        return false;
    }

    @Override
    public Optional<SanctionInfo> warnPlayer(String arg0, APIPlayerModerator arg1) {
        return Optional.empty();
    }

}