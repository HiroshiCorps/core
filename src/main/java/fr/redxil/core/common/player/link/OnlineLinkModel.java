package fr.redxil.core.common.player.link;

import fr.redxil.api.common.player.data.LinkCheck;
import fr.redxil.api.common.player.data.LinkData;
import fr.redxil.api.common.player.data.LinkType;
import fr.redxil.core.common.CoreAPI;
import fr.redxil.core.common.data.link.LinkDataSql;
import fr.redxil.core.common.redis.RedisManager;

import java.util.Optional;

public class OnlineLinkModel implements LinkData {

    final long sender;
    final long receiver;
    final int linkID;
    LinkType linkUsage;
    String linkName;

    OnlineLinkModel(int linkID, LinkType linkUsage, long sender, long receiver, String linkName) {
        this.sender = sender;
        this.receiver = receiver;
        this.linkID = linkID;
        this.linkUsage = linkUsage;
        this.linkName = linkName;
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
    public String getLinkName() {
        return linkName;
    }

    @Override
    public LinkType getLinkType() {
        return linkUsage;
    }

    @Override
    public void deleteLink() {
        Optional<RedisManager> redisManagerOptional = CoreAPI.getInstance().getRedisManager();
        if (redisManagerOptional.isEmpty())
            return;
        RedisManager redisManager = redisManagerOptional.get();
        if (getLinkType() == LinkType.BOTH) {
            redisManager.getRedisMap("link/" + getPlayerSender() + "/" + LinkCheck.BOTH + "/" + getLinkType()).remove(getPlayerReceiver(), getLinkID());
            redisManager.getRedisMap("link/" + getPlayerReceiver() + LinkCheck.BOTH + "/" + getLinkType()).remove(getPlayerSender(), getLinkID());
        } else {
            redisManager.getRedisMap("link/" + getPlayerReceiver() + "/" + LinkCheck.RECEIVER + "/" + getLinkType()).remove(getPlayerSender(), getLinkID());
            redisManager.getRedisMap("link/" + getPlayerSender() + "/" + LinkCheck.SENDER + "/" + getLinkType()).remove(getPlayerReceiver(), getLinkID());
        }

        CoreAPI.getInstance().getSQLConnection().ifPresent(sqlConnection -> sqlConnection.asyncExecute("DELETE * FROM link WHERE " + LinkDataSql.LINK_ID_SQL.getSQLColumns() + " = ?", getLinkID()));
    }

}
