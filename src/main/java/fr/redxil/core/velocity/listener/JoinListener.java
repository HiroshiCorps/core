/* Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 */

package fr.redxil.core.velocity.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import fr.redline.pms.utils.IpInfo;
import fr.redxil.api.common.API;
import fr.redxil.api.common.message.TextComponentBuilder;
import fr.redxil.api.common.player.APIOfflinePlayer;
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.api.common.player.data.SanctionInfo;
import fr.redxil.api.common.server.Server;
import fr.redxil.api.common.server.type.ServerStatus;
import fr.redxil.api.common.server.type.ServerType;
import fr.redxil.api.common.utils.SanctionType;
import fr.redxil.api.velocity.Velocity;
import fr.redxil.core.common.CoreAPI;
import fr.redxil.core.common.data.PlayerDataValue;
import fr.redxil.core.velocity.commands.mod.action.punish.BanCmd;
import net.kyori.adventure.text.Component;

import java.util.Collection;
import java.util.Optional;

public class JoinListener {

    public APIPlayer loadPlayer(Player player) {
        String[] splittedIP = player.getRemoteAddress().toString().split(":");

        APIPlayer apiPlayer = API.getInstance().getPlayerManager().loadPlayer(
                player.getUsername(),
                player.getUniqueId(),
                new IpInfo(splittedIP[0].replace("/", ""), Integer.valueOf(splittedIP[1]))
        );

        APIOfflinePlayer nicked = API.getInstance().getNickGestion().getAPIOfflinePlayer(player.getUsername());
        if (nicked != null)
            API.getInstance().getNickGestion().removeNick(nicked);

        API.getInstance().getModeratorManager().loadModerator(apiPlayer);

        return apiPlayer;

    }

    public RegisteredServer getServer(APIPlayer apiPlayer) {
        Collection<Server> serverList = API.getInstance().getServerManager().getListServer(ServerType.HUB);
        if (serverList.isEmpty()) return null;

        Server server = null;
        int totalPlayer = -1;

        for (Server serverCheck : serverList) {

            if (serverCheck.getServerAccess().canAccess(server, apiPlayer))
                continue;

            int playerConnected = serverCheck.getPlayerList().size();
            if (serverCheck.getMaxPlayers() - playerConnected > 0) {
                if (totalPlayer == -1 || totalPlayer > playerConnected) {
                    server = serverCheck;
                    totalPlayer = playerConnected;
                }
            }

        }

        if (server != null) {
            Optional<RegisteredServer> proxyServer = Velocity.getInstance().getProxyServer().getServer(server.getServerName());
            if (proxyServer.isPresent()) return proxyServer.get();
        }

        return null;
    }

    @Subscribe
    public void onPlayerJoin(PostLoginEvent e) {

        Player player = e.getPlayer();

        if (API.getInstance().getPlayerManager().isLoadedPlayer(player.getUniqueId())) {
            player.disconnect((Component) TextComponentBuilder.createTextComponent(
                    "§4§lSERVER NETWORK§r\n"
                            + "§cConnexion non autorisé§r\n\n"
                            + "§7Raison: Une personne est déjà connecté avec votre UUID§e§r\n"
            ));
            return;
        }

        if (CoreAPI.getInstance().getServerAccessEnum() == CoreAPI.ServerAccessEnum.CRACK)
            if (API.getInstance().getPlayerManager().isLoadedPlayer(player.getUsername())) {
                player.disconnect((Component) TextComponentBuilder.createTextComponent(
                        "§4§lSERVER NETWORK§r\n"
                                + "§cConnexion non autorisé§r\n\n"
                                + "§7Raison: Une personne est déjà connecté avec votre username§e§r\n"
                ));
                return;
            }

        APIOfflinePlayer apiOfflinePlayer;
        if (CoreAPI.getInstance().getServerAccessEnum() == CoreAPI.ServerAccessEnum.CRACK) {
            apiOfflinePlayer = API.getInstance().getPlayerManager().getOfflinePlayer(player.getUsername());
            APIOfflinePlayer previousWithUUID = API.getInstance().getPlayerManager().getOfflinePlayer(player.getUniqueId());
            if (previousWithUUID != null)
                previousWithUUID.setUUID(null);
        } else apiOfflinePlayer = API.getInstance().getPlayerManager().getOfflinePlayer(player.getUniqueId());

        if (apiOfflinePlayer != null) {

            SanctionInfo model = apiOfflinePlayer.getLastSanction(SanctionType.BAN);
            if (model != null && model.isEffective()) {
                player.disconnect((Component) model.getSancMessage());
                return;
            }

        }

        APIPlayer apiPlayer = loadPlayer(player);

        player.createConnectionRequest(getServer(apiPlayer));

    }

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

        if (moderatorId != null)
            BanCmd.banPlayer(API.getInstance().getPlayerManager().getOfflinePlayer(player.getUsername()), "perm", API.getInstance().getModeratorManager().getModerator(moderatorId), "{Core} Déconnexion en inspection");

    }

    @Subscribe
    public void connection(PreLoginEvent event) {
        if (API.getInstance().getNickGestion().isIllegalName(event.getUsername())) {
            event.setResult(PreLoginEvent.PreLoginComponentResult.denied((Component) TextComponentBuilder.createTextComponent("Illegal Name detected")));
        }
        if (API.getInstance().getServer().getServerStatus() != ServerStatus.ONLINE) {
            event.setResult(PreLoginEvent.PreLoginComponentResult.denied((Component) TextComponentBuilder.createTextComponent("Connection refusée")));
        }
    }

    @Subscribe
    public void serverConnect(ServerPreConnectEvent event) {

        APIPlayer apiPlayer = API.getInstance().getPlayerManager().getPlayer(event.getPlayer().getUniqueId());
        Server server = API.getInstance().getServerManager().getServer(event.getOriginalServer().getServerInfo().getName());

        if (apiPlayer.getServer() != null) {
            if (apiPlayer.isFreeze() || !apiPlayer.isLogin()) {
                event.setResult(ServerPreConnectEvent.ServerResult.denied());
                return;
            }
        }

        if (!server.getServerAccess().canAccess(server, apiPlayer))
            event.setResult(ServerPreConnectEvent.ServerResult.denied());

    }

}
