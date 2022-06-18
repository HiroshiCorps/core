package fr.redxil.core.velocity.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.proxy.Player;
import fr.redxil.api.common.player.APIOfflinePlayer;
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.api.common.player.moderators.APIPlayerModerator;
import fr.redxil.core.common.CoreAPI;
import fr.redxil.core.velocity.commands.mod.action.punish.BanCmd;

import java.util.Optional;

public class LeaveListener {

    @Subscribe
    public void onPlayerDisconnect(DisconnectEvent e) {

        Player player = e.getPlayer();
        Optional<APIPlayer> apiPlayer = CoreAPI.getInstance().getPlayerManager().getPlayer(
                player.getUniqueId()
        );

        if (apiPlayer.isEmpty()) return;

        Optional<Long> moderatorIDOpt = apiPlayer.get().getFreeze();
        CoreAPI.getInstance().getServer().setPlayerConnected(player.getUniqueId(), false);
        apiPlayer.get().unloadPlayer();

        if (moderatorIDOpt.isPresent()) {
            Optional<APIPlayerModerator> playerModerator = CoreAPI.getInstance().getModeratorManager().getModerator(moderatorIDOpt.get());
            if (playerModerator.isPresent()) {
                Optional<APIOfflinePlayer> target = CoreAPI.getInstance().getPlayerManager().getOfflinePlayer(player.getUsername());
                target.ifPresent(apiOfflinePlayer -> BanCmd.banPlayer(apiOfflinePlayer, "perm", playerModerator.get(), "{Core} Déconnexion en inspection"));
            }
        }

    }

}
