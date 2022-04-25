/*
 *
 * Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 *
 */

package fr.redxil.core.common.group.team;

import fr.redxil.api.common.API;
import fr.redxil.api.common.group.team.Team;
import fr.redxil.api.common.message.Color;
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.api.common.player.APIPlayerManager;
import fr.redxil.api.common.redis.RedisManager;
import fr.redxil.core.common.data.TeamDataValue;
import fr.redxil.core.common.data.utils.DataType;
import org.redisson.api.RMap;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CTeam implements Team {

    private final String teamName;
    private final long gameID;

    protected CTeam(long gameID, String teamName) {
        this.gameID = gameID;
        this.teamName = teamName;
    }

    public static CTeam initTeam(long gameID, String teamName, int maxPlayers) {

        RedisManager rm = API.getInstance().getRedisManager();

        rm.setRedisString(TeamDataValue.TEAM_DISPLAY_NAME_REDIS.getString(gameID, teamName), teamName);
        rm.setRedisLong(TeamDataValue.TEAM_MAXP_REDIS.getString(gameID, teamName), Integer.valueOf(maxPlayers).longValue());

        rm.setRedisString(TeamDataValue.TEAM_COLOR_REDIS.getString(gameID, teamName), Color.WHITE.getMOTD());
        rm.setRedisString(TeamDataValue.TEAM_CHAT_COLOR_REDIS.getString(gameID, teamName), Color.WHITE.getMOTD());

        rm.setRedisString(TeamDataValue.TEAM_PREFIX_REDIS.getString(gameID, teamName), "");
        rm.setRedisString(TeamDataValue.TEAM_SUFFIX_REDIS.getString(gameID, teamName), "");

        rm.setRedisBoolean(TeamDataValue.TEAM_CS_AV_REDIS.getString(gameID, teamName), false);
        rm.setRedisBoolean(TeamDataValue.TEAM_HIDE_OTHER_REDIS.getString(gameID, teamName), false);
        rm.setRedisBoolean(TeamDataValue.TEAM_FF_REDIS.getString(gameID, teamName), false);
        rm.setRedisBoolean(TeamDataValue.TEAM_COLISION_REDIS.getString(gameID, teamName), false);

        rm.getRedisList(TeamDataValue.TEAM_LIST_REDIS.getString(gameID, teamName)).add(teamName);

        return new CTeam(gameID, teamName);

    }

    public static CTeam initTeam(long gameID, String name, int maxPlayers, Color color) {

        CTeam team = CTeam.initTeam(gameID, name, maxPlayers);
        team.setChatColor(color);
        team.setColor(color);
        return team;

    }

    @Override
    public void deleteTeam() {
        setClientSideAvailable(false);
        for (UUID uuid : getListPlayerUUID())
            removePlayer(uuid);
        API.getInstance().getRedisManager().getRedisList(TeamDataValue.TEAM_LIST_REDIS.getString(getGameID(), getTeamName())).remove(getTeamName());
        TeamDataValue.clearRedisData(DataType.TEAM, getGameID(), getTeamName());
    }

    /*
     *
     * Scoreboard part
     *
     */
    @Override
    public boolean hisClientSideAvailable() {
        return API.getInstance().getRedisManager().getRedisBoolean(TeamDataValue.TEAM_CS_AV_REDIS.getString(getGameID(), getTeamName()));
    }

    @Override
    public void setClientSideAvailable(boolean value) {

        if (hisClientSideAvailable() == value) return;

        API.getInstance().getRedisManager().setRedisBoolean(TeamDataValue.TEAM_CS_AV_REDIS.getString(getGameID(), getTeamName()), value);

    }

    @Override
    public boolean getHideToOtherTeams() {
        return API.getInstance().getRedisManager().getRedisBoolean(TeamDataValue.TEAM_HIDE_OTHER_REDIS.getString(getGameID(), getTeamName()));
    }

    @Override
    public void setHideToOtherTeams(boolean value) {
        if (getHideToOtherTeams() == value) return;

        API.getInstance().getRedisManager().setRedisBoolean(TeamDataValue.TEAM_HIDE_OTHER_REDIS.getString(getGameID(), getTeamName()), value);
    }

    @Override
    public boolean getCollide() {
        return API.getInstance().getRedisManager().getRedisBoolean(TeamDataValue.TEAM_COLISION_REDIS.getString(getGameID(), getTeamName()));
    }

    @Override
    public void setCollide(boolean value) {
        if (getCollide() == value) return;

        API.getInstance().getRedisManager().setRedisBoolean(TeamDataValue.TEAM_COLISION_REDIS.getString(getGameID(), getTeamName()), value);
    }


    /*
     *
     * Team data part
     *
     */
    @Override
    public long getGameID() {
        return gameID;
    }

    @Override
    public String getTeamName() {
        return teamName;
    }

    @Override
    public String getDisplayName() {
        return API.getInstance().getRedisManager().getRedisString(TeamDataValue.TEAM_DISPLAY_NAME_REDIS.getString(getGameID(), getTeamName()));
    }

    @Override
    public void setDisplayName(String dspName) {
        API.getInstance().getRedisManager().setRedisString(TeamDataValue.TEAM_DISPLAY_NAME_REDIS.getString(getGameID(), getTeamName()), dspName);
    }

    @Override
    public String getPrefix() {
        return API.getInstance().getRedisManager().getRedisString(TeamDataValue.TEAM_PREFIX_REDIS.getString(getGameID(), getTeamName()));
    }

    @Override
    public void setPrefix(String prefix) {
        API.getInstance().getRedisManager().setRedisString(TeamDataValue.TEAM_PREFIX_REDIS.getString(getGameID(), getTeamName()), prefix);
    }

    @Override
    public String getSuffix() {
        return API.getInstance().getRedisManager().getRedisString(TeamDataValue.TEAM_SUFFIX_REDIS.getString(getGameID(), getTeamName()));
    }

    @Override
    public void setSuffix(String suffix) {
        API.getInstance().getRedisManager().setRedisString(TeamDataValue.TEAM_SUFFIX_REDIS.getString(getGameID(), getTeamName()), suffix);
    }

    @Override
    public Color getChatColor() {
        return Color.getByMOTD(API.getInstance().getRedisManager().getRedisString(TeamDataValue.TEAM_CHAT_COLOR_REDIS.getString(getGameID(), getTeamName())));
    }

    @Override
    public void setChatColor(Color chatColor) {
        API.getInstance().getRedisManager().setRedisString(TeamDataValue.TEAM_CHAT_COLOR_REDIS.getString(getGameID(), getTeamName()), chatColor.getMOTD());
    }

    @Override
    public Color getColor() {
        return Color.getByMOTD(API.getInstance().getRedisManager().getRedisString(TeamDataValue.TEAM_COLOR_REDIS.getString(getGameID(), getTeamName())));
    }

    @Override
    public void setColor(Color color) {
        API.getInstance().getRedisManager().setRedisString(TeamDataValue.TEAM_CHAT_COLOR_REDIS.getString(getGameID(), getTeamName()), color.getMOTD());
    }

    @Override
    public String getColoredName() {
        return getColor() + getTeamName();
    }

    @Override
    public boolean getFriendlyFire() {
        return API.getInstance().getRedisManager().getRedisBoolean(TeamDataValue.TEAM_FF_REDIS.getString(getGameID(), getTeamName()));
    }

    @Override
    public void setFriendlyFire(boolean value) {
        if (getFriendlyFire() == value) return;

        API.getInstance().getRedisManager().setRedisBoolean(TeamDataValue.TEAM_FF_REDIS.getString(getGameID(), getTeamName()), value);
    }

    /*
     *
     * APIPlayer part
     *
     */
    @Override
    public List<String> getListPlayerName() {
        return new ArrayList<>() {{
            getPlayers().forEach((apiPlayer) -> add(apiPlayer.getName()));
        }};
    }

    @Override
    public List<APIPlayer> getPlayers() {
        return new ArrayList<>() {{
            APIPlayerManager spm = API.getInstance().getPlayerManager();
            getListPlayerUUIDS().forEach(uuids -> add(spm.getPlayer(UUID.fromString(uuids))));
        }};
    }

    @Override
    public List<String> getListPlayerUUIDS() {
        return API.getInstance().getRedisManager().getRedisList(TeamDataValue.TEAM_PLAYERS_REDIS.getString(getGameID(), getTeamName()));
    }

    @Override
    public List<UUID> getListPlayerUUID() {
        return new ArrayList<>() {{
            getListPlayerUUIDS().forEach((string) -> add(UUID.fromString(string)));
        }};
    }

    @Override
    public boolean addPlayer(UUID player) {

        if (getRemainingPlace() == 0 || getListPlayerUUIDS().contains(player.toString()))
            return false;

        Team beforeTeam = API.getInstance().getTeamManager().getPlayerTeam(API.getInstance().getGameManager().getGame(getGameID()), player);
        if (beforeTeam != null && !beforeTeam.removePlayer(player))
            return false;

        getListPlayerUUIDS().add(player.toString());
        API.getInstance().getRedisManager().getRedisMap(TeamDataValue.TEAM_LINK_MAP_REDIS.getString(getGameID(), getTeamName())).put(player.toString(), getTeamName());
        return true;

    }

    @Override
    public boolean removePlayer(UUID player) {

        if (!getListPlayerUUIDS().contains(player.toString())) return false;

        getListPlayerUUIDS().remove(player.toString());
        API.getInstance().getRedisManager().getRedisMap(TeamDataValue.TEAM_LINK_MAP_REDIS.getString(getGameID(), getTeamName())).remove(player.toString(), getTeamName());

        return true;

    }

    @Override
    public int getSize() {
        return getListPlayerUUIDS().size();
    }

    @Override
    public boolean isEmpty() {
        return getListPlayerUUIDS().isEmpty();
    }

    @Override
    public boolean hasPlayer(APIPlayer player) {
        return getListPlayerUUIDS().contains(player.getUUID().toString());
    }

    @Override
    public int getMaxPlayers() {
        return Long.valueOf(API.getInstance().getRedisManager().getRedisLong(TeamDataValue.TEAM_MAXP_REDIS.getString(getGameID(), getTeamName()))).intValue();
    }

    @Override
    public void setMaxPlayers(int maxPlayers) {
        API.getInstance().getRedisManager().setRedisLong(TeamDataValue.TEAM_MAXP_REDIS.getString(getGameID(), getTeamName()), Integer.valueOf(maxPlayers).longValue());
    }

    @Override
    public int getRemainingPlace() {
        return Math.max(getMaxPlayers() - getSize(), 0);
    }

    @Override
    public boolean hasAttached(String key) {
        return getAttachedMap().containsKey(key);
    }

    @Override
    public void addAttach(String key, Object object) {
        removeAttach(key);
        getAttachedMap().put(key, object);
    }

    @Override
    public void removeAttach(String key) {
        getAttachedMap().remove(key);
    }

    @Override
    public Object getAttach(String key) {
        return getAttachedMap().get(key);
    }

    @Override
    public RMap<String, Object> getAttachedMap() {
        return API.getInstance().getRedisManager().getRedisMap(TeamDataValue.TEAM_ATTACHED_REDIS.getString(getGameID(), getTeamName()));
    }

}