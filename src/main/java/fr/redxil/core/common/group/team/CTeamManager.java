/* Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 */

package fr.redxil.core.common.group.team;

import fr.redxil.api.common.API;
import fr.redxil.api.common.game.Game;
import fr.redxil.api.common.group.team.Team;
import fr.redxil.api.common.group.team.TeamManager;
import fr.redxil.api.common.message.Color;
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.core.common.data.TeamDataValue;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CTeamManager implements TeamManager {

    @Override
    public List<Team> getTeamList(Game game) {
        return new ArrayList<>() {{
            getTeamNameList(game).forEach((id) -> add(getTeam(game, id)));
        }};
    }

    @Override
    public List<String> getTeamNameList(Game game) {
        return new ArrayList<>() {{
            for (Object l : API.getInstance().getRedisManager().getRedisList(TeamDataValue.TEAM_LIST_REDIS.getString(game, null)))
                if (l instanceof String)
                    add((String) l);
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
    public List<Team> getTeamWithRemainingPlace(Game game, int i) {
        return getTeamWithRemainingPlace(getTeamList(game), i);
    }

    @Override
    public List<Team> getTeamWithRemainingPlace(Game game) {
        return getTeamWithRemainingPlace(getTeamList(game), 1);
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
    public List<Team> getTeamWithMinPlayer(Game game, int playerMin) {
        return getTeamWithMinPlayer(getTeamList(game), playerMin);
    }

    @Override
    public List<Team> getTeamWithMinPlayer(List<Team> teamList) {
        return getTeamWithMinPlayer(teamList, 1);
    }

    @Override
    public List<Team> getTeamWithMinPlayer(Game game) {
        return getTeamWithMinPlayer(getTeamList(game), 1);
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
    public List<Team> getTeamOrderByRemainingPlace(Game game, boolean remainingOnly) {
        if (remainingOnly)
            return getTeamOrderByRemainingPlace(getTeamWithRemainingPlace(game));
        else
            return getTeamOrderByRemainingPlace(getTeamList(game));
    }

    @Override
    public List<Team> getTeamOrderByRemainingPlace(Game game) {
        return getTeamOrderByRemainingPlace(getTeamList(game));
    }


    @Override
    public boolean hasTeam(Game game, APIPlayer player) {
        return hasTeam(game, player.getUUID());
    }

    @Override
    public boolean hasTeam(Game game, UUID playerUUID) {
        return API.getInstance().getRedisManager().getRedisMap(TeamDataValue.TEAM_LINK_MAP_REDIS.getString(game, null)).containsKey(playerUUID.toString());
    }

    @Override
    public boolean hasTeam(Game game) {
        return !API.getInstance().getRedisManager().getRedisMap(TeamDataValue.TEAM_LINK_MAP_REDIS.getString(game, null)).isEmpty();
    }

    @Override
    public boolean isTeamExist(Game game, String teamName) {
        return API.getInstance().getRedisManager().getRedisList(TeamDataValue.TEAM_LIST_REDIS.getString(game, null)).contains(teamName);
    }

    @Override
    public CTeam getTeam(Game game, String teamName) {

        if (!isTeamExist(game, teamName)) return null;
        return new CTeam(game.getGameID(), teamName);

    }

    @Override
    public CTeam createTeam(Game game, String name, int maxPlayer) {

        return CTeam.initTeam(game.getGameID(), name, maxPlayer);

    }

    @Override
    public CTeam createTeam(Game game, String name, int maxPlayer, Color color) {

        return CTeam.initTeam(game.getGameID(), name, maxPlayer, color);

    }

    @Override
    public CTeam getPlayerTeam(Game game, UUID uuid) {
        if (!hasTeam(game, uuid)) return null;
        return getTeam(game, (String) API.getInstance().getRedisManager().getRedisMap(TeamDataValue.TEAM_LINK_MAP_REDIS.getString(game, null)).get(uuid.toString()));
    }

    @Override
    public CTeam getPlayerTeam(Game game, APIPlayer player) {
        return getPlayerTeam(game, player.getUUID());
    }

    @Override
    public boolean areAllInTeams(Game game, List<UUID> playerList) {

        for (UUID player : playerList)
            if (getPlayerTeam(game, player) == null)
                return false;

        return true;
    }

    @Override
    public boolean attributeTeamToAll(Game game, List<UUID> playerList) {
        for (UUID player : playerList) {

            List<Team> teamList = getTeamOrderByRemainingPlace(getTeamWithMinPlayer(game));
            if (teamList.isEmpty()) return false;
            teamList.get(0).addPlayer(player);

        }

        return true;
    }

}
