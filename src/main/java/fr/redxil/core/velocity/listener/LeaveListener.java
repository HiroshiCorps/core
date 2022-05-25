package fr.redxil.core.velocity.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.proxy.Player;
import fr.redxil.api.common.API;
import fr.redxil.api.common.player.APIOfflinePlayer;
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.api.common.player.moderators.APIPlayerModerator;
import fr.redxil.core.common.data.player.PlayerDataRedis;
import fr.redxil.core.velocity.commands.mod.action.punish.BanCmd;

import java.util.Optional;

public class LeaveListener {

    @Subscribe
    public void onPlayerDisconnect(DisconnectEvent e) {

        Player player = e.getPlayer();
        Optional<APIPlayer> apiPlayer = API.getInstance().getPlayerManager().getPlayer(
                player.getUniqueId()
        );

        if (apiPlayer.isEmpty()) return;

        Long moderatorID = (Long) API.getInstance().getRedisManager().getRedisObject(PlayerDataRedis.PLAYER_FREEZE_REDIS.getString(apiPlayer.get()));
        API.getInstance().getServer().setPlayerConnected(player.getUniqueId(), false);
        apiPlayer.get().unloadPlayer();

        if (moderatorID != null && moderatorID != 0L) {
            Optional<APIPlayerModerator> playerModerator = API.getInstance().getModeratorManager().getModerator(moderatorID);
            if (playerModerator.isPresent()) {
                Optional<APIOfflinePlayer> target = API.getInstance().getPlayerManager().getOfflinePlayer(player.getUsername());
                target.ifPresent(apiOfflinePlayer -> BanCmd.banPlayer(apiOfflinePlayer, "perm", playerModerator.get(), "{Core} Déconnexion en inspection"));
            }
        }

    }

}
