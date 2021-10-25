/*
 *  Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 *  * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 *  * Proprietary and confidential
 *  * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 *
 */

package fr.redxil.core.common.sql.server;

import fr.redxil.core.common.data.ServerDataValue;
import fr.redxil.api.common.server.type.ServerType;
import fr.redxil.core.common.sql.SQLModel;

public class ServerModel extends SQLModel {

    public ServerModel() {
        super("list_servers", "server_id");
    }

    public ServerModel(String name, int players_max, int access, boolean maintenance, ServerType type) {
        this();
        this.set(ServerDataValue.SERVER_NAME_SQL.getString(null), name);
        this.set(ServerDataValue.SERVER_MAXP_SQL.getString(null), players_max);
        this.set(ServerDataValue.SERVER_ACCES_SQL.getString(null), access);
        int bool = 0;
        if (maintenance) bool += 1;
        this.set(ServerDataValue.SERVER_MAINTENANCE_SQL.getString(null), bool);
        this.set(ServerDataValue.SERVER_TYPE_SQL.getString(null), type.toString());
    }

    public String getServerName() {
        return this.getString(ServerDataValue.SERVER_NAME_SQL.getString(null));
    }

    public int getMaxPlayers() {
        return this.getInt(ServerDataValue.SERVER_MAXP_SQL.getString(null));
    }

    public int getServerAccess() {
        return this.getInt(ServerDataValue.SERVER_ACCES_SQL.getString(null));
    }

    public boolean getMaintenance() {
        int bool = this.getInt(ServerDataValue.SERVER_MAINTENANCE_SQL.getString(null));
        if (bool == 1) return true;
        else return false;
    }

    public ServerType getServerType() {
        return ServerType.valueOf(this.getString(ServerDataValue.SERVER_TYPE_SQL.getString(null)));
    }

    public int getServerID() {
        return this.getInt(ServerDataValue.SERVER_ID_SQL.getString(null));
    }

}
