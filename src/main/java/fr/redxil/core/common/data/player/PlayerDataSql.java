/*
 *
 * Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 *
 */

package fr.redxil.core.common.data.player;

import fr.redxil.core.common.data.utils.SQLColumns;

public enum PlayerDataSql {

    PLAYER_RANK_SQL("member", "member_rank"),
    PLAYER_RANK_TIME_SQL("member", "rank_limit"),


    PLAYER_MEMBERID_SQL("member", "member_id"),
    PLAYER_IP_SQL("member", "member_ip"),

    PLAYER_NAME_SQL("member", "member_name"),
    PLAYER_UUID_SQL("member", "member_uuid"),

    PLAYER_FC_SQL("member", "member_fc"),
    PLAYER_LC_SQL("member", "member_lc");

    final SQLColumns sqlColumns;

    PlayerDataSql(String table, String columns) {
        this.sqlColumns = new SQLColumns(table, columns);
    }

    public SQLColumns getSQLColumns() {
        return this.sqlColumns;
    }
}
