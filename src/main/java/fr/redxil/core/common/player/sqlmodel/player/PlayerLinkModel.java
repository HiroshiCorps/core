/*
 *  Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 *  * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 *  * Proprietary and confidential
 *  * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 *
 */

package fr.redxil.core.common.player.sqlmodel.player;

import fr.redxil.api.common.player.APIOfflinePlayer;
import fr.redxil.api.common.player.data.LinkData;
import fr.redxil.core.common.CoreAPI;
import fr.redxil.core.common.data.LinkDataValue;
import fr.redxil.core.common.sql.SQLModel;

public class PlayerLinkModel extends SQLModel implements LinkData {

    public PlayerLinkModel(APIOfflinePlayer creator, APIOfflinePlayer target, String type) {
        super("link", LinkDataValue.LINK_ID_SQL.getString());
        this.set(LinkDataValue.FROM_ID_SQL.getString(), Long.valueOf(creator.getMemberId()).intValue());
        this.set(LinkDataValue.TO_ID_SQL.getString(), Long.valueOf(target.getMemberId()).intValue());
        this.set(LinkDataValue.LINK_TYPE_SQL.getString(), type);
    }

    @Override
    public int getLinkID() {
        return getInt(LinkDataValue.LINK_ID_SQL.getString());
    }

    @Override
    public long getFromPlayer() {
        return Integer.valueOf(getInt(LinkDataValue.FROM_ID_SQL.getString())).longValue();
    }

    @Override
    public long getToPlayer() {
        return Integer.valueOf(getInt(LinkDataValue.TO_ID_SQL.getString())).longValue();
    }

    @Override
    public String getLinkType() {
        return getString(LinkDataValue.LINK_TYPE_SQL.getString());
    }

    @Override
    public void setLinkType(String linkType) {
        set(LinkDataValue.LINK_TYPE_SQL.getString(), linkType);
    }

    @Override
    public void deleteLink() {
        CoreAPI.getInstance().getSQLConnection().asyncExecute("DELETE * FROM link WHERE " + LinkDataValue.LINK_ID_SQL.getString() + " = ?", getLinkID());
    }

}
