/*
 *
 * Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 *
 */

package fr.redxil.core.common.data;

import fr.redxil.core.common.data.utils.SQLColumns;

public enum SanctionDataSql {

    SANCTION_ID("sanction", "sanctionID"),
    SANCTION_TARGET("sanction", "targetID"),
    SANCTION_AUTHOR("sanction", "authorID"),
    SANCTION_TYPE("sanction", "sanctionType"),
    SANCTION_REASON("sanction", "reason"),
    SANCTION_CANCELLER("sanction", "cancellerID"),
    SANCTION_DATE("sanction", "sanctionTS"),
    SANCTION_END("sanction", "endTS");

    final SQLColumns sqlColumns;

    SanctionDataSql(String table, String columns) {
        this.sqlColumns = new SQLColumns(table, columns);
    }

    public SQLColumns getSQLColumns() {
        return this.sqlColumns;
    }
}
