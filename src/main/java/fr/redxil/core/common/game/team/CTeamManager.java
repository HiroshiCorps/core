/* Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 */

package fr.redxil.core.common.game.team;

import fr.redxil.api.common.API;
import fr.redxil.api.common.game.team.Team;
import fr.redxil.api.common.game.team.TeamManager;
import fr.redxil.api.common.message.Color;
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.core.common.data.TeamDataValue;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CTeamManager implements TeamManager {

    @Override
    public List<Team> getTeamList() {
        return new ArrayList<Team>() {{
            getTeamIDList().forEach((id) -> add(getTeam(id)));
        }};
    }

    @Override
    public List<Long> getTeamIDList() {
        return new ArrayList<Long>() {{
            for (Object l : API.getInstance().getRedisManager().getRedisMap(TeamDataValue.TEAM_LIST_REDIS.getLocation()).entrySet())
                if (l instanceof Long)
                    add((Long) l);
        }};
    }

    @Override
    public List<Team> getTeamWithRemainingPlace(List<Team> teamList, int remain) {
        List<Team> remainingPlace = new ArrayList<>();

        teamList.forEach((team -> {
            if (team.getRemainingPlace() >= remain) remainingPlace.add(team);
        }));

        return remainingPlace;
    }

    @Override
    public List<Team> getTeamWithRemainingPlace(List<Team> teamList) {
        return getTeamWithRemainingPlace(teamList, 1);
    }

    @Override
    public List<Team> getTeamWithRemainingPlace(int i) {
        return getTeamWithRemainingPlace(getTeamList(), i);
    }

    @Override
    public List<Team> getTeamWithRemainingPlace() {
        return getTeamWithRemainingPlace(getTeamList(), 1);
    }

    @Override
    public List<Team> getTeamWithMinPlayer(List<Team> teamList, int playerMin) {

        List<Team> teamChecked = new ArrayList<>();

        teamList.forEach(
                team -> {
                    if (team.getSize() >= playerMin) teamChecked.add(team);
                }
        );

        return teamChecked;

    }

    @Override
    public List<Team> getTeamWithMinPlayer(int playerMin) {
        return getTeamWithMinPlayer(getTeamList(), playerMin);
    }

    @Override
    public List<Team> getTeamWithMinPlayer(List<Team> teamList) {
        return getTeamWithMinPlayer(teamList, 1);
    }

    @Override
    public List<Team> getTeamWithMinPlayer() {
        return getTeamWithMinPlayer(getTeamList(), 1);
    }


    @Override
    public List<Team> getTeamOrderByRemainingPlace(List<Team> teamList2) {
        List<Team> teamList = new ArrayList<>(teamList2);

        for (int i = 0; i < teamList.size(); i++) {

            Team team = teamList.get(i);
            for (int i2 = i + 1; i2 < teamList.size(); i2++) {

                Team teamInCheck = teamList.get(i2);
                if (!(teamInCheck.getRemainingPlace() > team.getRemainingPlace())) continue;

                teamList.set(i, teamInCheck);
                teamList.set(i2, team);
                team = teamInCheck;

            }

        }

        return teamList;
    }

    @Override
    public List<Team> getTeamOrderByRemainingPlace(boolean remainingOnly) {
        if (remainingOnly)
            return getTeamOrderByRemainingPlace(getTeamWithRemainingPlace());
        else
            return getTeamOrderByRemainingPlace(getTeamList());
    }

    @Override
    public List<Team> getTeamOrderByRemainingPlace() {
        return getTeamOrderByRemainingPlace(getTeamList());
    }


    @Override
    public boolean hasTeam(APIPlayer player) {
        return hasTeam(player.getUUID());
    }

    @Override
    public boolean hasTeam(UUID playerUUID) {
        return API.getInstance().getRedisManager().getRedisMap(TeamDataValue.TEAM_LINK_MAP_REDIS.getLocation()).containsKey(playerUUID.toString());
    }

    @Override
    public boolean isTeamExist(long teamID) {
        return API.getInstance().getRedisManager().getRedisList(TeamDataValue.TEAM_LIST_REDIS.getLocation()).contains(teamID);
    }

    @Override
    public CTeam getTeam(long teamID) {

        if (!isTeamExist(teamID)) return null;
        return new CTeam(teamID);

    }

    @Override
    public CTeam createTeam(String name, int maxPlayer) {

        return CTeam.initTeam(name, maxPlayer);

    }

    @Override
    public CTeam createTeam(String name, int maxPlayer, Color color) {

        return CTeam.initTeam(name, maxPlayer, color);

    }

    @Override
    public CTeam getPlayerTeam(UUID uuid) {
        if (!hasTeam(uuid)) return null;
        return getTeam((long) API.getInstance().getRedisManager().getRedisMap(TeamDataValue.TEAM_LINK_MAP_REDIS.getLocation()).get(uuid.toString()));
    }

    @Override
    public CTeam getPlayerTeam(APIPlayer player) {
        return getPlayerTeam(player.getUUID());
    }

    @Override
    public boolean areAllInTeams(List<UUID> playerList) {

        for (UUID player : playerList)
            if (getPlayerTeam(player) == null)
                return false;

        return true;
    }

    @Override
    public boolean attributeTeamToAll(List<UUID> playerList) {
        for (UUID player : playerList) {

            List<Team> teamList = getTeamOrderByRemainingPlace(getTeamWithMinPlayer());
            if (teamList.isEmpty()) return false;
            teamList.get(0).addPlayer(player);

        }

        return true;
    }

}
