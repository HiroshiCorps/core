/* Copyright (C) Hiroshi - Ibrahim - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Ibrahim for Hiroshi, braimsou@gmail.com - contact@hiroshimc.net - 2021
 */

package fr.redxil.core.paper.scoreboard;

import fr.redxil.api.paper.scoreboard.util.EntryCache;
import net.minecraft.server.v1_12_R1.ScoreboardScore;

public class BoardPosition extends ScoreboardScore {

    private final int score;

    public BoardPosition(BoardObjective objective, int index) {
        super(BoardObjective.scoreboard, objective, EntryCache.getFakeName(index));

        this.score = objective.lineSize() - index;
    }

    @Override
    public int getScore() {
        return this.score;
    }
}
