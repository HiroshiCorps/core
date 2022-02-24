/*
 *
 * Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 *
 */

package fr.redxil.core.common.data.money;

import fr.redxil.core.common.data.utils.SQLColumns;

public enum MoneyDataSql {

    PLAYER_MEMBERID_SQL("money", "member_id"),
    PLAYER_COINS_SQL("money", "coins"),
    PLAYER_SOLDE_SQL("money", "solde");

    final SQLColumns sqlColumns;

    MoneyDataSql(String table, String columns) {
        this.sqlColumns = new SQLColumns(table, columns);
    }

    public SQLColumns getSQLColumns() {
        return this.sqlColumns;
    }

}
