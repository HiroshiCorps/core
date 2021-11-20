package fr.redxil.core.velocity.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import fr.redxil.api.common.API;
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.api.common.server.Server;
import fr.redxil.api.common.server.type.ServerType;
import fr.redxil.api.velocity.Velocity;

import java.util.Optional;

public class ServerListener {

    @Subscribe
    public void serverConnect(ServerPreConnectEvent event) {

        APIPlayer apiPlayer = API.getInstance().getPlayerManager().getPlayer(event.getPlayer().getUniqueId());
        Server server = API.getInstance().getServerManager().getServer(event.getOriginalServer().getServerInfo().getName());

        if (apiPlayer.getServer() == null) {

            Server serverFinalTarget = API.getInstance().getServerManager().getConnectableServer(apiPlayer, ServerType.HUB);
            if (serverFinalTarget == null) {
                event.setResult(ServerPreConnectEvent.ServerResult.denied());
                return;
            }

            Optional<RegisteredServer> registeredServerOptional = Velocity.getInstance().getProxyServer().getServer(serverFinalTarget.getServerName());
            if (registeredServerOptional.isPresent())
                event.setResult(ServerPreConnectEvent.ServerResult.allowed(registeredServerOptional.get()));
            else
                event.setResult(ServerPreConnectEvent.ServerResult.denied());

            return;

        }

        if (!server.getServerAccess().canAccess(server, apiPlayer)) {
            event.setResult(ServerPreConnectEvent.ServerResult.denied());
            return;
        }

        if (apiPlayer.isFreeze() || !apiPlayer.isLogin())
            event.setResult(ServerPreConnectEvent.ServerResult.denied());

    }

}
