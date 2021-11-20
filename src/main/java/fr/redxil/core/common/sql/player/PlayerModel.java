/*
 *  Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 *  * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 *  * Proprietary and confidential
 *  * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 *
 */

package fr.redxil.core.common.sql.player;

import fr.redxil.api.common.rank.RankList;
import fr.redxil.core.common.data.PlayerDataValue;
import fr.redxil.core.common.sql.SQLModel;

import java.util.UUID;

public class PlayerModel extends SQLModel {

    public PlayerModel() {
        super("member", PlayerDataValue.PLAYER_MEMBERID_SQL.getString());
    }

    public long getPowerRank() {
        return Integer.valueOf(this.getInt(PlayerDataValue.PLAYER_RANK_SQL.getString(null))).longValue();
    }

    public RankList getRank() {
        return RankList.getRank(getPowerRank());
    }

    public int getMemberId() {
        return this.getInt(PlayerDataValue.PLAYER_MEMBERID_SQL.getString(null));
    }

    public String getName() {
        return this.getString(PlayerDataValue.PLAYER_NAME_SQL.getString());
    }

    public UUID getUUID() {
        String uuidString = this.getString(PlayerDataValue.PLAYER_UUID_SQL.getString(null));
        return uuidString != null ? UUID.fromString(uuidString) : null;
    }

}
