/*
 *
 * Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 *
 */

package fr.redxil.core.common.player.link;

import fr.redxil.api.common.player.APIOfflinePlayer;
import fr.redxil.api.common.player.data.LinkCheck;
import fr.redxil.api.common.player.data.LinkData;
import fr.redxil.api.common.player.data.LinkType;
import fr.redxil.core.common.CoreAPI;
import fr.redxil.core.common.data.link.LinkDataSql;
import fr.redxil.core.common.redis.RedisManager;
import fr.redxil.core.common.sql.SQLModel;

import java.util.Optional;

public class OfflineLinkModel extends SQLModel implements LinkData {
    @Override
    public int getLinkID() {
        return getInt(LinkDataSql.LINK_ID_SQL.getSQLColumns());
    }


    public OfflineLinkModel(APIOfflinePlayer sender, APIOfflinePlayer receiver, String type, LinkType linkType) {
        super("link", LinkDataSql.LINK_ID_SQL.getSQLColumns());
        this.set(LinkDataSql.SENDER_ID_SQL.getSQLColumns(), Long.valueOf(sender.getMemberID()).intValue());
        this.set(LinkDataSql.RECEIVED_ID_SQL.getSQLColumns(), Long.valueOf(receiver.getMemberID()).intValue());
        this.set(LinkDataSql.LINK_NAME_SQL.getSQLColumns(), type);
        this.set(LinkDataSql.LINK_TYPE_SQL.getSQLColumns(), linkType);
    }

    @Override
    public long getPlayerSender() {
        return getInt(LinkDataSql.SENDER_ID_SQL.getSQLColumns()).longValue();
    }

    @Override
    public LinkType getLinkType() {
        return LinkType.valueOf(this.getString(LinkDataSql.LINK_TYPE_SQL.getSQLColumns()));
    }

    @Override
    public long getPlayerReceiver() {
        return getInt(LinkDataSql.RECEIVED_ID_SQL.getSQLColumns()).longValue();
    }

    @Override
    public String getLinkName() {
        return this.getString(LinkDataSql.LINK_NAME_SQL.getSQLColumns());
    }

    @Override
    public void deleteLink() {
        CoreAPI.getInstance().getSQLConnection().ifPresent(sqlConnection -> sqlConnection.asyncExecute("DELETE * FROM link WHERE " + LinkDataSql.LINK_ID_SQL.getSQLColumns() + " = ?", getLinkID()));
    }

    public void redisRegister(long connectedPlayer) {
        Optional<RedisManager> redisManagerOptional = CoreAPI.getInstance().getRedisManager();
        if (redisManagerOptional.isEmpty())
            return;
        RedisManager redisManager = redisManagerOptional.get();
        boolean sender = connectedPlayer == getPlayerSender();
        if (getLinkType() == LinkType.BOTH) {
            redisManager.getRedisMap("link/" + connectedPlayer + "/" + LinkCheck.BOTH + "/" + getLinkType()).put(getLinkID(), sender ? getPlayerReceiver() : getPlayerSender());
        } else {
            LinkCheck linkCheck = sender ? LinkCheck.SENDER : LinkCheck.RECEIVER;
            redisManager.getRedisMap("link/" + getPlayerSender() + "/" + linkCheck + "/" + getLinkType()).put(getLinkID(), getPlayerReceiver());
        }
    }

}
