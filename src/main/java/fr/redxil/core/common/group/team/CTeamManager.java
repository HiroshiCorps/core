/*
 *
 * Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 *
 */

package fr.redxil.core.common.group.team;

import fr.redxil.api.common.utils.DataReminder;
import fr.redxil.api.common.group.team.Team;
import fr.redxil.api.common.group.team.TeamManager;
import fr.redxil.core.common.data.game.TeamDataValue;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class CTeamManager implements TeamManager {

    final Long gameID;
    final DataReminder<List<String>> teamReminder;
    final DataReminder<Map<UUID, String>> uuidReminder;

    public CTeamManager(Long gameID) {
        this.gameID = gameID;
        this.teamReminder = DataReminder.generateListReminder(TeamDataValue.TEAM_LIST_REDIS.getString(gameID, null));
        this.uuidReminder = DataReminder.generateMapReminder(TeamDataValue.TEAM_LINK_MAP_REDIS.getString(gameID, null));
    }

    @Override
    public List<String> getTeamList() {
        return this.teamReminder.getData();
    }

    @Override
    public boolean hasTeam(UUID playerUUID) {
        return getUUIDToTeamMap().containsKey(playerUUID);
    }

    @Override
    public boolean hasTeam() {
        return !getUUIDToTeamMap().isEmpty();
    }

    @Override
    public boolean isTeamExist(String teamName) {
        return getTeamList().contains(teamName);
    }

    @Override
    public Optional<Team> getTeam(String teamName) {

        if (!isTeamExist(teamName)) return Optional.empty();
        return Optional.of(new CTeam(gameID, teamName));

    }

    @Override
    public Optional<Team> createTeam(String name, int maxPlayer) {

        if (isTeamExist(name))
            return Optional.empty();
        return Optional.of(new CTeam(gameID, name, maxPlayer));

    }

    @Override
    public Optional<Team> getPlayerTeam(UUID uuid) {
        String name = getUUIDToTeamMap().get(uuid);
        if (name == null) return Optional.empty();
        return Optional.of(new CTeam(gameID, name));
    }

    @Override
    public boolean areAllInTeams(List<UUID> playerList) {

        for (UUID player : playerList)
            if (getPlayerTeam(player).isEmpty())
                return false;

        return true;
    }

    @Override
    public Map<UUID, String> getUUIDToTeamMap() {
        return uuidReminder.getData();
    }

}
