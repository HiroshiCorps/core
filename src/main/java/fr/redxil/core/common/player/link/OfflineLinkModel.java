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
import fr.redxil.api.common.player.data.LinkData;
import fr.redxil.api.common.player.data.LinkUsage;
import fr.redxil.core.common.CoreAPI;
import fr.redxil.core.common.data.link.LinkDataSql;
import fr.redxil.core.common.redis.RedisManager;
import fr.redxil.core.common.sql.SQLModel;

import java.util.Optional;

public class OfflineLinkModel extends SQLModel implements LinkData {

    LinkUsage linkUsage = null;

    @Override
    public int getLinkID() {
        return getInt(LinkDataSql.LINK_ID_SQL.getSQLColumns());
    }

    public OfflineLinkModel(APIOfflinePlayer creator, APIOfflinePlayer target, String type, LinkUsage linkUsage) {
        super("link", LinkDataSql.LINK_ID_SQL.getSQLColumns());
        this.set(LinkDataSql.SENDER_ID_SQL.getSQLColumns(), Long.valueOf(creator.getMemberID()).intValue());
        this.set(LinkDataSql.RECEIVED_ID_SQL.getSQLColumns(), Long.valueOf(target.getMemberID()).intValue());
        this.set(LinkDataSql.LINK_TYPE_SQL.getSQLColumns(), type);
        if (linkUsage == LinkUsage.BOTH)
            this.set(LinkDataSql.LINK_USAGE_SQL.getSQLColumns(), linkUsage.name());
        else this.set(LinkDataSql.LINK_USAGE_SQL.getSQLColumns(), "SENDER_RECEIVER");
    }

    @Override
    public long getPlayerSender() {
        return getInt(LinkDataSql.SENDER_ID_SQL.getSQLColumns()).longValue();
    }

    @Override
    public String getLinkType() {
        return getString(LinkDataSql.LINK_TYPE_SQL.getSQLColumns());
    }

    @Override
    public long getPlayerReceiver() {
        return getInt(LinkDataSql.RECEIVED_ID_SQL.getSQLColumns()).longValue();
    }

    @Override
    public LinkUsage getLinkUsage() {
        return linkUsage;
    }

    public void setLinkUsage(long connectedPlayer) {
        if (!this.getString(LinkDataSql.LINK_USAGE_SQL.getSQLColumns()).equals(LinkUsage.BOTH.name()))
            this.linkUsage = connectedPlayer == getPlayerSender() ? LinkUsage.SENDER : LinkUsage.RECEIVER;
        else this.linkUsage = LinkUsage.BOTH;
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
        if (getLinkUsage() == LinkUsage.BOTH) {
            if (connectedPlayer == getPlayerSender())
                redisManager.getRedisMap("link/" + getPlayerSender() + LinkUsage.BOTH + getLinkType()).put(getPlayerReceiver(), getLinkID());
            else
                redisManager.getRedisMap("link/" + getPlayerReceiver() + LinkUsage.BOTH + getLinkType()).put(getPlayerSender(), getLinkID());
        } else if (getLinkUsage() == LinkUsage.RECEIVER) {
            redisManager.getRedisMap("link/" + getPlayerReceiver() + LinkUsage.RECEIVER + getLinkType()).put(getPlayerSender(), getLinkID());
        } else
            redisManager.getRedisMap("link/" + getPlayerSender() + LinkUsage.SENDER + getLinkType()).put(getPlayerReceiver(), getLinkID());
    }

}
