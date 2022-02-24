/*
 *
 * Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 *
 */

package fr.redxil.core.common.player.sqlmodel.player;

import fr.redxil.api.common.player.APIOfflinePlayer;
import fr.redxil.api.common.player.data.LinkData;
import fr.redxil.core.common.CoreAPI;
import fr.redxil.core.common.data.link.LinkDataSql;
import fr.redxil.core.common.sql.SQLModel;

public class PlayerLinkModel extends SQLModel implements LinkData {

    public PlayerLinkModel(APIOfflinePlayer creator, APIOfflinePlayer target, String type) {
        super("link", LinkDataSql.LINK_ID_SQL.getSQLColumns());
        this.set(LinkDataSql.FROM_ID_SQL.getSQLColumns(), Long.valueOf(creator.getMemberID()).intValue());
        this.set(LinkDataSql.TO_ID_SQL.getSQLColumns(), Long.valueOf(target.getMemberID()).intValue());
        this.set(LinkDataSql.LINK_TYPE_SQL.getSQLColumns(), type);
    }

    @Override
    public int getLinkID() {
        return getInt(LinkDataSql.LINK_ID_SQL.getSQLColumns());
    }

    @Override
    public long getFromPlayer() {
        return Integer.valueOf(getInt(LinkDataSql.FROM_ID_SQL.getSQLColumns())).longValue();
    }

    @Override
    public long getToPlayer() {
        return Integer.valueOf(getInt(LinkDataSql.TO_ID_SQL.getSQLColumns())).longValue();
    }

    @Override
    public String getLinkType() {
        return getString(LinkDataSql.LINK_TYPE_SQL.getSQLColumns());
    }

    @Override
    public void setLinkType(String linkType) {
        set(LinkDataSql.LINK_TYPE_SQL.getSQLColumns(), linkType);
    }

    @Override
    public void deleteLink() {
        CoreAPI.getInstance().getSQLConnection().asyncExecute("DELETE * FROM link WHERE " + LinkDataSql.LINK_ID_SQL.getSQLColumns() + " = ?", getLinkID());
    }

}
