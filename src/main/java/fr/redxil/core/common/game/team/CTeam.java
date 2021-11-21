/* Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 */

package fr.redxil.core.common.game.team;

import fr.redline.pms.connect.linker.pm.PMManager;
import fr.redxil.api.common.API;
import fr.redxil.api.common.game.team.Team;
import fr.redxil.api.common.message.Color;
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.api.common.player.APIPlayerManager;
import fr.redxil.api.common.redis.RedisManager;
import fr.redxil.api.spigot.minigame.GameBuilder;
import fr.redxil.core.common.data.IDDataValue;
import fr.redxil.core.common.data.ServerDataValue;
import fr.redxil.core.common.data.TeamDataValue;
import fr.redxil.core.common.data.utils.DataType;
import fr.redxil.core.common.redis.IDGenerator;
import org.redisson.api.RMap;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CTeam implements Team {

    public final static String teamBalise = "<tb>";
    private final long teamID;
    private final String teamName;

    protected CTeam(long longID) {
        this.teamID = longID;
        this.teamName = API.getInstance().getRedisManager().getRedisString(TeamDataValue.TEAM_NAME_REDIS.getString(longID));
    }

    public static CTeam initTeam(String name, int maxPlayers) {

        long teamID = IDGenerator.generateLONGID(IDDataValue.TEAM);
        RedisManager rm = API.getInstance().getRedisManager();

        rm.setRedisString(TeamDataValue.TEAM_NAME_REDIS.getString(teamID), name);
        rm.setRedisString(TeamDataValue.TEAM_SERVER_REDIS.getString(teamID), API.getInstance().getServerName());
        rm.setRedisString(TeamDataValue.TEAM_DISPLAY_NAME_REDIS.getString(teamID), name);
        rm.setRedisLong(TeamDataValue.TEAM_MAXP_REDIS.getString(teamID), Integer.valueOf(maxPlayers).longValue());

        rm.setRedisString(TeamDataValue.TEAM_COLOR_REDIS.getString(teamID), Color.WHITE.getMOTD());
        rm.setRedisString(TeamDataValue.TEAM_CHAT_COLOR_REDIS.getString(teamID), Color.WHITE.getMOTD());

        rm.setRedisString(TeamDataValue.TEAM_PREFIX_REDIS.getString(teamID), "");
        rm.setRedisString(TeamDataValue.TEAM_SUFFIX_REDIS.getString(teamID), "");

        rm.setRedisBoolean(TeamDataValue.TEAM_CS_AV_REDIS.getString(teamID), false);
        rm.setRedisBoolean(TeamDataValue.TEAM_HIDE_OTHER_REDIS.getString(teamID), false);
        rm.setRedisBoolean(TeamDataValue.TEAM_FF_REDIS.getString(teamID), false);
        rm.setRedisBoolean(TeamDataValue.TEAM_COLISION_REDIS.getString(teamID), false);

        rm.getRedisList(TeamDataValue.TEAM_LIST_REDIS.getString(teamID)).add(teamID);

        rm.getRedisList(ServerDataValue.SERVER_LINK_TEAM_REDIS.getString(API.getInstance().getServer())).add(teamID);

        return new CTeam(teamID);

    }

    public static CTeam initTeam(String name, int maxPlayers, Color color) {

        CTeam team = CTeam.initTeam(name, maxPlayers);
        team.setChatColor(color);
        team.setColor(color);
        return team;

    }

    public void deleteTeam() {
        setClientSideAvailable(false);
        for (UUID uuid : getListPlayerUUID())
            removePlayer(uuid);
        API.getInstance().getRedisManager().getRedisList(TeamDataValue.TEAM_LIST_REDIS.getString(teamID)).remove(getName());
        TeamDataValue.clearRedisData(DataType.TEAM, getTeamID());
        API.getInstance().getRedisManager().getRedisList(ServerDataValue.SERVER_LINK_TEAM_REDIS.getString(API.getInstance().getServer())).remove(getTeamID());
    }

    /*
     *
     * Scoreboard part
     *
     */

    public boolean hisClientSideAvailable() {
        return API.getInstance().getRedisManager().getRedisBoolean(TeamDataValue.TEAM_CS_AV_REDIS.getString(getTeamID()));
    }

    public void setClientSideAvailable(boolean value) {

        if (hisClientSideAvailable() == value) return;

        API.getInstance().getRedisManager().setRedisBoolean(TeamDataValue.TEAM_CS_AV_REDIS.getString(getTeamID()), value);
        if (value)
            PMManager.sendRedissonPluginMessage(API.getInstance().getRedisManager().getRedissonClient(), "teamON", getTeamID());
        else
            PMManager.sendRedissonPluginMessage(API.getInstance().getRedisManager().getRedissonClient(), "teamOFF", getTeamID());

    }

    public boolean getHideToOtherTeams() {
        return API.getInstance().getRedisManager().getRedisBoolean(TeamDataValue.TEAM_HIDE_OTHER_REDIS.getString(getTeamID()));
    }

    public void setHideToOtherTeams(boolean value) {

        if (getHideToOtherTeams() == value) return;

        API.getInstance().getRedisManager().setRedisBoolean(TeamDataValue.TEAM_HIDE_OTHER_REDIS.getString(getTeamID()), value);
        PMManager.sendRedissonPluginMessage(API.getInstance().getRedisManager().getRedissonClient(), "opChange", getTeamID());
    }

    public boolean getCollide() {
        return API.getInstance().getRedisManager().getRedisBoolean(TeamDataValue.TEAM_COLISION_REDIS.getString(getTeamID()));
    }

    public void setCollide(boolean value) {

        if (getCollide() == value) return;

        API.getInstance().getRedisManager().setRedisBoolean(TeamDataValue.TEAM_COLISION_REDIS.getString(getTeamID()), value);
        PMManager.sendRedissonPluginMessage(API.getInstance().getRedisManager().getRedissonClient(), "opChange", getTeamID());
    }


    /*
     *
     * Team data part
     *
     */

    public long getTeamID() {
        return teamID;
    }

    public String getName() {
        return teamName;
    }


    public String getDisplayName() {
        return API.getInstance().getRedisManager().getRedisString(TeamDataValue.TEAM_DISPLAY_NAME_REDIS.getString(getTeamID()));
    }

    public void setDisplayName(String dspName) {
        API.getInstance().getRedisManager().setRedisString(TeamDataValue.TEAM_DISPLAY_NAME_REDIS.getString(getTeamID()), dspName);
        PMManager.sendRedissonPluginMessage(API.getInstance().getRedisManager().getRedissonClient(), "opChange", getTeamID());
    }


    public String getPrefix() {
        return API.getInstance().getRedisManager().getRedisString(TeamDataValue.TEAM_PREFIX_REDIS.getString(getTeamID()));
    }

    public void setPrefix(String prefix) {
        API.getInstance().getRedisManager().setRedisString(TeamDataValue.TEAM_PREFIX_REDIS.getString(getTeamID()), prefix);
        PMManager.sendRedissonPluginMessage(API.getInstance().getRedisManager().getRedissonClient(), "opChange", getTeamID());
    }

    public String getSuffix() {
        return API.getInstance().getRedisManager().getRedisString(TeamDataValue.TEAM_SUFFIX_REDIS.getString(getTeamID()));
    }

    public void setSuffix(String suffix) {
        API.getInstance().getRedisManager().setRedisString(TeamDataValue.TEAM_SUFFIX_REDIS.getString(getTeamID()), suffix);
        PMManager.sendRedissonPluginMessage(API.getInstance().getRedisManager().getRedissonClient(), "opChange", getTeamID());
    }


    public Color getChatColor() {
        return Color.getByMOTD(API.getInstance().getRedisManager().getRedisString(TeamDataValue.TEAM_CHAT_COLOR_REDIS.getString(getTeamID())));
    }

    public void setChatColor(Color chatColor) {
        API.getInstance().getRedisManager().setRedisString(TeamDataValue.TEAM_CHAT_COLOR_REDIS.getString(getTeamID()), chatColor.getMOTD());
    }

    public Color getColor() {
        return Color.getByMOTD(API.getInstance().getRedisManager().getRedisString(TeamDataValue.TEAM_COLOR_REDIS.getString(getTeamID())));
    }

    public void setColor(Color color) {
        API.getInstance().getRedisManager().setRedisString(TeamDataValue.TEAM_CHAT_COLOR_REDIS.getString(getTeamID()), color.getMOTD());
    }

    public String getColoredName() {
        return getColor() + getName();
    }

    public boolean getFriendlyFire() {
        return API.getInstance().getRedisManager().getRedisBoolean(TeamDataValue.TEAM_FF_REDIS.getString(getTeamID()));
    }

    public void setFriendlyFire(boolean value) {

        if (getFriendlyFire() == value) return;

        API.getInstance().getRedisManager().setRedisBoolean(TeamDataValue.TEAM_FF_REDIS.getString(getTeamID()), value);
        PMManager.sendRedissonPluginMessage(API.getInstance().getRedisManager().getRedissonClient(), "opChange", getTeamID());
    }

    /*
     *
     * APIPlayer part
     *
     */

    public List<String> getListPlayerName(boolean nickCare) {
        return new ArrayList<String>() {{
            getPlayers().forEach((apiPlayer) -> add(apiPlayer.getName(nickCare)));
        }};
    }

    public List<APIPlayer> getPlayers() {
        return new ArrayList<APIPlayer>() {{
            APIPlayerManager spm = API.getInstance().getPlayerManager();
            getListPlayerUUIDS().forEach(uuids -> add(spm.getPlayer(UUID.fromString(uuids))));
        }};
    }

    public List<String> getListPlayerUUIDS() {
        return API.getInstance().getRedisManager().getRedisList(TeamDataValue.TEAM_PLAYERS_REDIS.getString(getTeamID()));
    }

    public List<UUID> getListPlayerUUID() {
        return new ArrayList<UUID>() {{

            getListPlayerUUIDS().forEach((string) -> add(UUID.fromString(string)));

        }};
    }

    public boolean addPlayer(UUID player) {

        if (getRemainingPlace() == 0 || getListPlayerUUIDS().contains(player.toString()))
            return false;

        Team beforeTeam = GameBuilder.getGameBuilder().getTeamManager().getPlayerTeam(player);
        if (beforeTeam != null && !beforeTeam.removePlayer(player))
            return false;

        getListPlayerUUIDS().add(player.toString());
        API.getInstance().getRedisManager().getRedisMap(TeamDataValue.TEAM_LINK_MAP_REDIS.getString(teamID)).put(player.toString(), teamID);
        PMManager.sendRedissonPluginMessage(API.getInstance().getRedisManager().getRedissonClient(), "addp", getTeamID() + teamBalise + player);
        return true;

    }

    public boolean removePlayer(UUID player) {

        if (!getListPlayerUUIDS().contains(player.toString())) return false;

        getListPlayerUUIDS().remove(player.toString());
        API.getInstance().getRedisManager().getRedisMap(TeamDataValue.TEAM_LINK_MAP_REDIS.getString(teamID)).remove(player.toString());

        PMManager.sendRedissonPluginMessage(API.getInstance().getRedisManager().getRedissonClient(), "rmp", getTeamID() + teamBalise + player);
        return true;

    }

    public int getSize() {
        return getListPlayerUUIDS().size();
    }

    public boolean isEmpty() {
        return getListPlayerUUIDS().isEmpty();
    }

    public boolean hasPlayer(APIPlayer player) {
        return getListPlayerUUIDS().contains(player.getUUID().toString());
    }

    public int getMaxPlayers() {
        return Long.valueOf(API.getInstance().getRedisManager().getRedisLong(TeamDataValue.TEAM_MAXP_REDIS.getString(getTeamID()))).intValue();
    }

    public void setMaxPlayers(int maxPlayers) {
        API.getInstance().getRedisManager().setRedisLong(TeamDataValue.TEAM_MAXP_REDIS.getString(getTeamID()), Integer.valueOf(maxPlayers).longValue());
    }

    public int getRemainingPlace() {
        return Math.max(getMaxPlayers() - getSize(), 0);
    }

    public boolean hasAttached(String key) {
        return getAttachedMap().containsKey(key);
    }

    public void addAttach(String key, Object object) {
        removeAttach(key);
        getAttachedMap().put(key, object);
    }

    public void removeAttach(String key) {
        getAttachedMap().remove(key);
    }

    public Object getAttach(String key) {
        return getAttachedMap().get(key);
    }

    public RMap<String, Object> getAttachedMap() {
        return API.getInstance().getRedisManager().getRedisMap(TeamDataValue.TEAM_ATTACHED_REDIS.getString(getTeamID()));
    }

}