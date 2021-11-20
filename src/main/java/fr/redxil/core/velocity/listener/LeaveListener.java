package fr.redxil.core.velocity.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.proxy.Player;
import fr.redxil.api.common.API;
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.api.common.player.moderators.APIPlayerModerator;
import fr.redxil.core.common.data.PlayerDataValue;
import fr.redxil.core.velocity.commands.mod.action.punish.BanCmd;

public class LeaveListener {

    @Subscribe
    public void onPlayerDisconnect(DisconnectEvent e) {

        Player player = e.getPlayer();
        APIPlayer apiPlayer = API.getInstance().getPlayerManager().getPlayer(
                player.getUniqueId()
        );

        if (apiPlayer == null) return;

        Long moderatorId = (Long) API.getInstance().getRedisManager().getRedisObject(PlayerDataValue.PLAYER_FREEZE_REDIS.getString(apiPlayer));
        API.getInstance().getServer().removePlayerInServer(player.getUniqueId());
        apiPlayer.unloadPlayer();

        if (moderatorId != null) {
            APIPlayerModerator playerModerator = API.getInstance().getModeratorManager().getModerator(moderatorId);
            if (playerModerator != null)
                BanCmd.banPlayer(API.getInstance().getPlayerManager().getOfflinePlayer(player.getUsername()), "perm", playerModerator, "{Core} Déconnexion en inspection");
        }

    }

}
