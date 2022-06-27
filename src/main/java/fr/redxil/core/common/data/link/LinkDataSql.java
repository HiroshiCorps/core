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
    RECEIVED_ID_SQL("link", "receiver_id"),
    SENDER_ID_SQL("link", "sender_id"),
    LINK_NAME_SQL("link", "link_name"),

    LINK_TYPE_SQL("link", "link_type");

    final SQLColumns sqlColumns;

    LinkDataSql(String table, String columns) {
        this.sqlColumns = new SQLColumns(table, columns);
    }

    public SQLColumns getSQLColumns() {
        return this.sqlColumns;
    }

}
