/*
 *  Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 *  * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 *  * Proprietary and confidential
 *  * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 *
 */

package fr.redxil.core.common.player.sqlmodel.player;

import fr.redxil.core.common.CoreAPI;
import fr.redxil.core.common.data.LinkDataValue;
import fr.redxil.core.common.player.link.LinkState;
import fr.redxil.core.common.sql.SQLModel;

public class PlayerLinkModel extends SQLModel {

    public PlayerLinkModel() {
        super("link", LinkDataValue.LINK_ID_SQL.getString());
    }

    public int getLinkID() {
        return getInt(LinkDataValue.LINK_ID_SQL.getString());
    }


    public Long getFromPlayer() {
        return Integer.valueOf(getInt(LinkDataValue.FROM_ID_SQL.getString())).longValue();
    }

    public Long getToPlayer() {
        return Integer.valueOf(getInt(LinkDataValue.TO_ID_SQL.getString())).longValue();
    }


    public LinkState getLinkState() {
        return LinkState.valueOf(getString(LinkDataValue.LINK_TYPE_SQL.getString()));
    }

    public void setLinkState(LinkState linkState) {
        set(LinkDataValue.LINK_TYPE_SQL.getString(), linkState.name());
    }


    public void deleteLink() {
        CoreAPI.getInstance().getSQLConnection().asyncExecute("DELETE * FROM link WHERE " + LinkDataValue.LINK_ID_SQL.getString() + " = ?", getLinkID());
    }

}
