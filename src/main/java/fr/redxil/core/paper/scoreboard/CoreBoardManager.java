/* Copyright (C) Hiroshi - Ibrahim - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Ibrahim for Hiroshi, braimsou@gmail.com - contact@hiroshimc.net - 2021
 */

package fr.redxil.core.paper.scoreboard;

import fr.redxil.api.paper.Paper;
import fr.redxil.api.paper.scoreboard.BoardManager;
import fr.redxil.api.paper.scoreboard.BoardProvider;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;

public class CoreBoardManager implements BoardManager {

    private final Plugin plugin = Paper.getInstance();
    private final Map<Player, BoardObjective> boardMap = new HashMap<>();
    private final Map<BoardProvider, BoardObjective> objectiveMap = new HashMap<>();

    @Override
    public void setProvider(Player player, BoardProvider provider) {
        this.removeProvider(player);
        BoardObjective objective = this.getOrCreateObjective(provider);
        this.boardMap.put(player, objective);
        objective.apply(player);
    }

    @Override
    public BoardProvider getProvider(Player player) {
        BoardObjective objective = this.boardMap.get(player);
        if (objective == null) return null;
        return objective.getProvider();
    }

    @Override
    public void removeProvider(Player player) {
        BoardObjective objective = this.boardMap.remove(player);
        if (objective == null) return;
        objective.remove(player);
    }

    @Override
    public void updateLine(BoardProvider provider, int line) {
        this.getOrCreateObjective(provider).updateLine(line);
    }

    @Override
    public void updateTitle(BoardProvider provider) {
        this.getOrCreateObjective(provider).updateLine(-1);
    }

    private BoardObjective getOrCreateObjective(BoardProvider provider) {
        return this.objectiveMap.computeIfAbsent(provider, p -> new BoardObjective(this.plugin, p));
    }
}
