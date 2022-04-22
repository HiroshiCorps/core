/*
 *
 * Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 *
 */

package fr.redxil.core.common.data.server;

import fr.redxil.core.common.sql.utils.SQLColumns;

public enum ServerDataSql {

    SERVER_ID_SQL("server", "server_id"),
    SERVER_NAME_SQL("server", "server_name"),
    SERVER_MAXP_SQL("server", "server_max_players"),
    SERVER_TYPE_SQL("server", "server_type"),
    SERVER_STATUS_SQL("server", "server_status"),
    SERVER_ACCESS_SQL("server", "server_access"),
    SERVER_NEEDRANK_SQL("server", "server_needrank"),
    SERVER_IP_SQL("server", "server_ip"),
    SERVER_PORT_SQL("server", "server_port");

    final SQLColumns sqlColumns;

    ServerDataSql(String table, String columns) {
        this.sqlColumns = new SQLColumns(table, columns);
    }

    public SQLColumns getSQLColumns() {
        return this.sqlColumns;
    }

}
