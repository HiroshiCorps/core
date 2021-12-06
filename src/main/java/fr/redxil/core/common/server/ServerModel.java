/*
 *  Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 *  * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 *  * Proprietary and confidential
 *  * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 *
 */

package fr.redxil.core.common.server;

import fr.redxil.api.common.server.type.ServerType;
import fr.redxil.core.common.data.ServerDataValue;
import fr.redxil.core.common.sql.SQLModel;

public class ServerModel extends SQLModel {

    public ServerModel() {
        super("server", "server_id");
    }

    public String getServerName() {
        return this.getString(ServerDataValue.SERVER_NAME_SQL.getString(null));
    }

    public int getMaxPlayers() {
        return this.getInt(ServerDataValue.SERVER_MAXP_SQL.getString(null));
    }

    public ServerType getServerType() {
        return ServerType.valueOf(this.getString(ServerDataValue.SERVER_TYPE_SQL.getString(null)));
    }

    public int getServerID() {
        return this.getInt(ServerDataValue.SERVER_ID_SQL.getString(null));
    }

}
