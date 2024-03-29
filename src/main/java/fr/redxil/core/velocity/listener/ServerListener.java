package fr.redxil.core.velocity.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import fr.redline.pms.utils.IpInfo;
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.api.common.server.Server;
import fr.redxil.core.common.CoreAPI;
import fr.redxil.core.velocity.CoreVelocity;
import fr.xilitra.hiroshisav.enums.ServerType;

import java.net.InetSocketAddress;
import java.util.Optional;

public class ServerListener {

    @Subscribe
    public void serverConnect(ServerPreConnectEvent event) {

        Optional<APIPlayer> apiPlayer = CoreAPI.getInstance().getPlayerManager().getPlayer(event.getPlayer().getUniqueId());
        if (apiPlayer.isEmpty()) {
            event.setResult(ServerPreConnectEvent.ServerResult.denied());
            return;
        }

        if (apiPlayer.get().getServerID() == null) {
            Optional<Server> serverFinalTarget = CoreAPI.getInstance().getServerManager().getConnectableServer(apiPlayer.get(), ServerType.HUB);
            if (serverFinalTarget.isEmpty()) {
                event.setResult(ServerPreConnectEvent.ServerResult.denied());
                return;
            }

            Optional<String> serverNameOptional = serverFinalTarget.get().getServerName();

            if (serverNameOptional.isEmpty()) {
                event.setResult(ServerPreConnectEvent.ServerResult.denied());
                return;
            }

            String serverName = serverNameOptional.get();

            Optional<RegisteredServer> registeredServerOptional = CoreVelocity.getInstance().getProxyServer().getServer(serverNameOptional.get());

            if (registeredServerOptional.isEmpty()) {
                IpInfo ipInfo = serverFinalTarget.get().getServerIP();
                CoreVelocity.getInstance().getProxyServer().registerServer(new ServerInfo(serverName, new InetSocketAddress(ipInfo.getIp(), ipInfo.getPort())));
                registeredServerOptional = CoreVelocity.getInstance().getProxyServer().getServer(serverName);
            }

            if (registeredServerOptional.isPresent())
                event.setResult(ServerPreConnectEvent.ServerResult.allowed(registeredServerOptional.get()));
            else {
                event.setResult(ServerPreConnectEvent.ServerResult.denied());
            }

            return;

        }

        Optional<Server> server = CoreAPI.getInstance().getServerManager().getServer(event.getOriginalServer().getServerInfo().getName());
        if (server.isPresent()) {
            if (!server.get().getServerAccess().canAccess(server.get(), apiPlayer.get())) {
                event.setResult(ServerPreConnectEvent.ServerResult.denied());
                return;
            }
        }

        if (apiPlayer.get().isFreeze())
            event.setResult(ServerPreConnectEvent.ServerResult.denied());

    }

}
