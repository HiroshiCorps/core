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
import fr.redxil.api.common.group.team.TeamManager;
import fr.redxil.api.common.utils.DataReminder;
import fr.redxil.core.common.data.game.TeamDataValue;
import fr.redxil.core.common.data.utils.DataType;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class CTeam implements Team {

    private final String teamName;
    private final long serverID;
    DataReminder<String> csavReminder = null;
    DataReminder<String> hoReminder = null;
    DataReminder<String> coReminder = null;

    /*
     *
     * Scoreboard part
     *
     */
    DataReminder<String> dnReminder = null;
    DataReminder<String> prReminder = null;
    DataReminder<String> suReminder = null;
    DataReminder<String> ffReminder = null;
    DataReminder<List<UUID>> playerReminder = null;
    DataReminder<Long> mpReminder = null;
    DataReminder<Map<String, Object>> attachMapReminder = null;

    protected CTeam(long serverID, String teamName) {
        this.serverID = serverID;
        this.teamName = teamName;
    }

    public CTeam(Long serverID, String teamName, int maxPlayers) {

        this.serverID = serverID;
        this.teamName = teamName;

        setDisplayName(teamName);
        setMaxPlayers(maxPlayers);

        setPrefix("");
        setSuffix("");

        setClientSideAvailable(false);
        setHideToOtherTeams(false);
        setFriendlyFire(false);
        setCollide(false);

        API.getInstance().getTeamManager(serverID).getTeamList().add(teamName);

    }

    @Override
    public void deleteTeam() {
        setClientSideAvailable(false);
        for (UUID uuid : getListPlayerUUID())
            removePlayer(uuid);
        API.getInstance().getTeamManager(getServerID()).getTeamList().remove(getTeamName());
        TeamDataValue.clearRedisData(DataType.TEAM, getServerID(), getTeamName());
    }

    public DataReminder<String> initCSAVReminder() {
        if (csavReminder == null)
            csavReminder = DataReminder.generateReminder(TeamDataValue.TEAM_CS_AV_REDIS.getString(this), Boolean.FALSE.toString());
        return csavReminder;
    }

    @Override
    public boolean hisClientSideAvailable() {
        return Boolean.getBoolean(initCSAVReminder().getData());
    }

    @Override
    public void setClientSideAvailable(boolean value) {

        initCSAVReminder().setData(Boolean.valueOf(value).toString());

    }

    public DataReminder<String> initHOReminder() {
        if (hoReminder == null)
            hoReminder = DataReminder.generateReminder(TeamDataValue.TEAM_HIDE_OTHER_REDIS.getString(this), Boolean.FALSE.toString());
        return hoReminder;
    }

    @Override
    public boolean getHideToOtherTeams() {
        return Boolean.getBoolean(initHOReminder().getData());
    }

    @Override
    public void setHideToOtherTeams(boolean value) {
        initHOReminder().setData(Boolean.valueOf(value).toString());
    }

    public DataReminder<String> initCOReminder() {
        if (coReminder == null)
            coReminder = DataReminder.generateReminder(TeamDataValue.TEAM_COLISION_REDIS.getString(this), Boolean.FALSE.toString());
        return coReminder;
    }

    @Override
    public boolean getCollide() {
        return Boolean.getBoolean(initCOReminder().getData());
    }

    @Override
    public void setCollide(boolean value) {
        initCOReminder().setData(Boolean.valueOf(value).toString());
    }

    /*
     *
     * Team data part
     *
     */
    @Override
    public long getServerID() {
        return serverID;
    }

    @Override
    public String getTeamName() {
        return teamName;
    }

    public DataReminder<String> initDNReminder() {
        if (dnReminder == null)
            dnReminder = DataReminder.generateReminder(TeamDataValue.TEAM_DISPLAY_NAME_REDIS.getString(this), "");
        return dnReminder;
    }

    @Override
    public String getDisplayName() {
        return initDNReminder().getData();
    }

    @Override
    public void setDisplayName(String dspName) {
        initDNReminder().setData(dspName);
    }

    public DataReminder<String> initPRReminder() {
        if (prReminder == null)
            prReminder = DataReminder.generateReminder(TeamDataValue.TEAM_PREFIX_REDIS.getString(this), "");
        return prReminder;
    }

    @Override
    public String getPrefix() {
        return initPRReminder().getData();
    }

    @Override
    public void setPrefix(String prefix) {
        initPRReminder().setData(prefix);
    }

    public DataReminder<String> initSUReminder() {
        if (suReminder == null)
            suReminder = DataReminder.generateReminder(TeamDataValue.TEAM_SUFFIX_REDIS.getString(this), "");
        return suReminder;
    }

    @Override
    public String getSuffix() {
        return initSUReminder().getData();
    }

    @Override
    public void setSuffix(String prefix) {
        initSUReminder().setData(prefix);
    }

    public DataReminder<String> initFFReminder() {
        if (ffReminder == null)
            ffReminder = DataReminder.generateReminder(TeamDataValue.TEAM_FF_REDIS.getString(this), Boolean.FALSE.toString());
        return ffReminder;
    }

    @Override
    public boolean getFriendlyFire() {
        return Boolean.getBoolean(initFFReminder().getData());
    }

    @Override
    public void setFriendlyFire(boolean value) {
        initFFReminder().setData(Boolean.valueOf(value).toString());
    }

    public DataReminder<List<UUID>> initPlayerReminder() {
        if (playerReminder == null)
            playerReminder = DataReminder.generateListReminder(TeamDataValue.TEAM_PLAYERS_REDIS.getString(this));
        return playerReminder;
    }

    @Override
    public List<UUID> getListPlayerUUID() {
        return initPlayerReminder().getData();
    }

    @Override
    public boolean addPlayer(UUID player) {

        if (getRemainingPlace() == 0 || getListPlayerUUID().contains(player))
            return false;

        TeamManager teamManager = API.getInstance().getTeamManager(getServerID());

        Optional<Team> beforeTeam = teamManager.getPlayerTeam(player);
        if (beforeTeam.isPresent() && !beforeTeam.get().removePlayer(player))
            return false;

        getListPlayerUUID().add(player);
        teamManager.getUUIDToTeamMap().put(player, getTeamName());
        return true;

    }

    @Override
    public boolean removePlayer(UUID player) {

        getListPlayerUUID().remove(player);
        API.getInstance().getTeamManager(getServerID()).getUUIDToTeamMap().remove(player, getTeamName());

        return true;

    }

    @Override
    public int getSize() {
        return getListPlayerUUID().size();
    }

    @Override
    public boolean isEmpty() {
        return getListPlayerUUID().isEmpty();
    }

    @Override
    public boolean hasPlayer(UUID player) {
        return getListPlayerUUID().contains(player);
    }

    public DataReminder<Long> initMPReminder() {
        if (mpReminder == null)
            mpReminder = DataReminder.generateReminder(TeamDataValue.TEAM_MAXP_REDIS.getString(this), 5L);
        return mpReminder;
    }

    @Override
    public int getMaxPlayers() {
        return initMPReminder().getData().intValue();
    }

    @Override
    public void setMaxPlayers(int maxPlayers) {
        initMPReminder().setData(Integer.valueOf(maxPlayers).longValue());
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
    public Map<String, Object> getAttachedMap() {
        if (attachMapReminder == null)
            attachMapReminder = DataReminder.generateMapReminder(TeamDataValue.TEAM_ATTACHED_REDIS.getString(this));
        return attachMapReminder.getData();
    }

}