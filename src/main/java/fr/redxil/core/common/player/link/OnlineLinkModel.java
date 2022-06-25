package fr.redxil.core.common.player.link;

import fr.redxil.api.common.player.data.LinkData;
import fr.redxil.api.common.player.data.LinkUsage;
import fr.redxil.core.common.CoreAPI;
import fr.redxil.core.common.data.link.LinkDataSql;
import fr.redxil.core.common.redis.RedisManager;

import java.util.Optional;

public class OnlineLinkModel implements LinkData {

    final long sender;
    final long receiver;
    final int linkID;
    LinkUsage linkUsage;
    String linkType;

    OnlineLinkModel(int linkID, LinkUsage linkUsage, long sender, long receiver, String linkType) {
        this.sender = sender;
        this.receiver = receiver;
        this.linkID = linkID;
        this.linkUsage = linkUsage;
        this.linkType = linkType;
    }

    @Override
    public int getLinkID() {
        return linkID;
    }

    @Override
    public long getPlayerSender() {
        return sender;
    }

    @Override
    public long getPlayerReceiver() {
        return receiver;
    }

    @Override
    public String getLinkType() {
        return linkType;
    }

    @Override
    public LinkUsage getLinkUsage() {
        return linkUsage;
    }

    @Override
    public void deleteLink() {
        Optional<RedisManager> redisManagerOptional = CoreAPI.getInstance().getRedisManager();
        if (redisManagerOptional.isEmpty())
            return;
        RedisManager redisManager = redisManagerOptional.get();
        if (getLinkUsage() == LinkUsage.BOTH) {
            redisManager.getRedisMap("link/" + getPlayerSender() + LinkUsage.BOTH + getLinkType()).remove(getPlayerReceiver(), getLinkID());
            redisManager.getRedisMap("link/" + getPlayerReceiver() + LinkUsage.BOTH + getLinkType()).remove(getPlayerSender(), getLinkID());
        } else {
            redisManager.getRedisMap("link/" + getPlayerReceiver() + LinkUsage.RECEIVER + getLinkType()).remove(getPlayerSender(), getLinkID());
            redisManager.getRedisMap("link/" + getPlayerSender() + LinkUsage.SENDER + getLinkType()).remove(getPlayerReceiver(), getLinkID());
        }

        CoreAPI.getInstance().getSQLConnection().ifPresent(sqlConnection -> sqlConnection.asyncExecute("DELETE * FROM link WHERE " + LinkDataSql.LINK_ID_SQL.getSQLColumns() + " = ?", getLinkID()));
    }

}
