/*
 *
 * Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 *
 */

package fr.redxil.core.common.data.link;

import fr.redxil.core.common.sql.utils.SQLColumns;

public enum LinkDataSql {

    LINK_ID_SQL("link", "link_id"),
    FROM_ID_SQL("link", "from_id"),
    TO_ID_SQL("link", "to_id"),
    LINK_TYPE_SQL("link", "link_state");

    final SQLColumns sqlColumns;

    LinkDataSql(String table, String columns) {
        this.sqlColumns = new SQLColumns(table, columns);
    }

    public SQLColumns getSQLColumns() {
        return this.sqlColumns;
    }

}
