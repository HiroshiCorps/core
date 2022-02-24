/*
 *  Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 *  * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 *  * Proprietary and confidential
 *  * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 *
 */

package fr.redxil.core.common.server;

import fr.redxil.api.common.server.type.ServerType;
import fr.redxil.core.common.data.server.ServerDataSql;
import fr.redxil.core.common.sql.SQLModel;

public class ServerModel extends SQLModel {

    public ServerModel() {
        super("server", ServerDataSql.SERVER_ID_SQL.getSQLColumns());
    }

    public String getServerName() {
        return this.getString(ServerDataSql.SERVER_NAME_SQL.getSQLColumns());
    }

    public int getMaxPlayers() {
        return this.getInt(ServerDataSql.SERVER_MAXP_SQL.getSQLColumns());
    }

    public ServerType getServerType() {
        return ServerType.valueOf(this.getString(ServerDataSql.SERVER_TYPE_SQL.getSQLColumns()));
    }

    public int getServerID() {
        return this.getInt(ServerDataSql.SERVER_ID_SQL.getSQLColumns());
    }

}
