/*
 *  Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 *  * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 *  * Proprietary and confidential
 *  * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 *
 */

package fr.redxil.core.common.player.sqlmodel.player;

import fr.redxil.api.common.player.rank.Rank;
import fr.redxil.core.common.data.player.PlayerDataSql;
import fr.redxil.core.common.sql.SQLModel;

import java.util.UUID;

public class PlayerModel extends SQLModel {

    public PlayerModel() {
        super("member", PlayerDataSql.PLAYER_MEMBERID_SQL.getSQLColumns());
    }

    public long getPowerRank() {
        return Integer.valueOf(this.getInt(PlayerDataSql.PLAYER_RANK_SQL.getSQLColumns())).longValue();
    }

    public Rank getRank() {
        return Rank.getRank(getPowerRank());
    }

    public int getMemberID() {
        return this.getInt(PlayerDataSql.PLAYER_MEMBERID_SQL.getSQLColumns());
    }

    public String getName() {
        return this.getString(PlayerDataSql.PLAYER_NAME_SQL.getSQLColumns());
    }

    public UUID getUUID() {
        String uuidString = this.getString(PlayerDataSql.PLAYER_UUID_SQL.getSQLColumns());
        return uuidString != null ? UUID.fromString(uuidString) : null;
    }

}
