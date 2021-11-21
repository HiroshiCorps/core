package fr.redxil.core.velocity.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import fr.redxil.api.common.API;
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.api.common.server.Server;
import fr.redxil.api.common.server.type.ServerType;
import fr.redxil.api.velocity.Velocity;
import net.kyori.adventure.text.Component;

import java.util.Optional;
import java.util.logging.Level;

public class ServerListener {

    @Subscribe
    public void serverConnect(ServerPreConnectEvent event) {

        APIPlayer apiPlayer = API.getInstance().getPlayerManager().getPlayer(event.getPlayer().getUniqueId());
        Server server = API.getInstance().getServerManager().getServer(event.getOriginalServer().getServerInfo().getName());
        API.getInstance().getPluginEnabler().printLog(Level.FINE, "ServerListener 1");
        if (apiPlayer.getServer() == null) {

            API.getInstance().getPluginEnabler().printLog(Level.FINE, "ServerListener 3");
            Server serverFinalTarget = API.getInstance().getServerManager().getConnectableServer(apiPlayer, ServerType.HUB);
            if (serverFinalTarget == null) {
                event.setResult(ServerPreConnectEvent.ServerResult.denied());
                event.getPlayer().disconnect(Component.text("No Server available"));
                return;
            }

            API.getInstance().getPluginEnabler().printLog(Level.FINE, "ServerListener 4");
            Optional<RegisteredServer> registeredServerOptional = Velocity.getInstance().getProxyServer().getServer(serverFinalTarget.getServerName());
            if (registeredServerOptional.isPresent())
                event.setResult(ServerPreConnectEvent.ServerResult.allowed(registeredServerOptional.get()));
            else {
                event.setResult(ServerPreConnectEvent.ServerResult.denied());
                event.getPlayer().disconnect(Component.text("No Server available"));
            }

            return;

        }

        API.getInstance().getPluginEnabler().printLog(Level.FINE, "ServerListener 2");

        if (!server.getServerAccess().canAccess(server, apiPlayer)) {
            event.setResult(ServerPreConnectEvent.ServerResult.denied());
            return;
        }

        if (apiPlayer.isFreeze() || !apiPlayer.isLogin())
            event.setResult(ServerPreConnectEvent.ServerResult.denied());

    }

}
