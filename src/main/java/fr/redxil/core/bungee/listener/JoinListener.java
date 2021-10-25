/* Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 */

package fr.redxil.core.bungee.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import de.dytanic.cloudnet.driver.CloudNetDriver;
import de.dytanic.cloudnet.driver.permission.IPermissionManagement;
import de.dytanic.cloudnet.driver.permission.IPermissionUser;
import fr.redline.pms.utils.IpInfo;
import fr.redxil.api.common.API;
import fr.redxil.core.common.data.PlayerDataValue;
import fr.redxil.api.common.utils.SanctionType;
import fr.redxil.api.common.message.TextComponentBuilder;
import fr.redxil.api.common.player.APIOfflinePlayer;
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.api.common.player.data.SanctionInfo;
import fr.redxil.api.common.redis.RedisManager;
import fr.redxil.api.common.server.Server;
import fr.redxil.api.common.server.type.ServerType;
import fr.redxil.api.common.utils.ServerAccessEnum;
import fr.redxil.api.velocity.Velocity;
import fr.redxil.core.bungee.commands.mod.action.punish.BanCmd;
import fr.redxil.core.common.CoreAPI;
import net.kyori.adventure.text.Component;

import java.util.Collection;
import java.util.Optional;

public class JoinListener {

    public boolean acceptConnection = true;

    @Subscribe
    public void onPlayerJoin(PostLoginEvent e) {

        Player player = e.getPlayer();

        if (!acceptConnection) {
            player.disconnect((Component) TextComponentBuilder.createTextComponent("Connection refused"));
            return;
        }

        if (CoreAPI.get().getNickGestion().isIllegalName(player.getUsername())) {
            player.disconnect((Component) TextComponentBuilder.createTextComponent(
                    "§4§lSERVER NETWORK§r\n"
                            + "§cConnexion non autorisé§r\n\n"
                            + "§7Raison: Présence d'un caractére interdit dans votre pseudo§e§r\n"
            ));
            return;
        }

        if (CoreAPI.get().getPlayerManager().isLoadedPlayer(player.getUniqueId())) {
            player.disconnect((Component) TextComponentBuilder.createTextComponent(
                    "§4§lSERVER NETWORK§r\n"
                            + "§cConnexion non autorisé§r\n\n"
                            + "§7Raison: Une personne est déjà connecté avec votre UUID§e§r\n"
            ));
            return;
        }

        if(API.get().getServerAccessEnum() == ServerAccessEnum.CRACK)
            if (CoreAPI.get().getPlayerManager().isLoadedPlayer(player.getUsername())) {
                player.disconnect((Component) TextComponentBuilder.createTextComponent(
                        "§4§lSERVER NETWORK§r\n"
                                + "§cConnexion non autorisé§r\n\n"
                                + "§7Raison: Une personne est déjà connecté avec votre username§e§r\n"
                ));
                return;
            }

        APIOfflinePlayer apiOfflinePlayer;
        if(API.get().getServerAccessEnum() == ServerAccessEnum.CRACK){
            apiOfflinePlayer = CoreAPI.get().getPlayerManager().getOfflinePlayer(player.getUsername());
            APIOfflinePlayer previousWithUUID = CoreAPI.get().getPlayerManager().getOfflinePlayer(player.getUniqueId());
            if(previousWithUUID != null)
                previousWithUUID.setUUID(null);
        }else apiOfflinePlayer = CoreAPI.get().getPlayerManager().getOfflinePlayer(player.getUniqueId());

        if (apiOfflinePlayer != null) {
            SanctionInfo model = apiOfflinePlayer.getLastSanction(SanctionType.BAN);

            if (model != null && model.isEffective()) {
                player.disconnect((Component) model.getSancMessage());
                return;
            }

        }

        String[] splittedIP = player.getRemoteAddress().toString().split(":");

        APIPlayer apiPlayer = CoreAPI.get().getPlayerManager().loadPlayer(
                player.getUsername(),
                player.getUniqueId(),
                new IpInfo(splittedIP[0].replace("/", ""), Integer.valueOf(splittedIP[1]))
        );

        IPermissionManagement ipm = CloudNetDriver.getInstance().getPermissionManagement();

        ipm.deleteUser(player.getUsername());

        IPermissionUser ipu = ipm.getOrCreateUser(player.getUniqueId(), player.getUsername());
        ipu.addGroup(apiPlayer.getRank().getRankName());

        APIPlayer nicked = CoreAPI.get().getNickGestion().getAPIPlayer(player.getUsername());
        if (nicked != null)
            CoreAPI.get().getNickGestion().removeNick(nicked);

        CoreAPI.get().getModeratorManager().loadModerator(apiPlayer);

        RedisManager redisManager = CoreAPI.get().getRedisManager();
        redisManager.setRedisLong(PlayerDataValue.PLAYER_NUMBER.getString(null), redisManager.getRedisLong(PlayerDataValue.PLAYER_NUMBER.getString(null)) + 1);

    }

    public ServerInfo getServer() {
        Collection<Server> ServerList = CoreAPI.get().getServerManager().getListServer(ServerType.HUB);
        if (ServerList.isEmpty()) return null;

        Server server = null;
        int totalPlayer = -1;

        for (Server serverCheck : ServerList) {
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
            if (proxyServer.isPresent()) return proxyServer.get().getServerInfo();
        }
        return null;
    }

    @Subscribe
    public void onPlayerDisconnect(DisconnectEvent e) {
        Player player = e.getPlayer();
        APIPlayer apiPlayer = CoreAPI.get().getPlayerManager().getPlayer(
                player.getUniqueId()
        );

        if (apiPlayer == null) return;

        Long moderatorId = (Long) CoreAPI.get().getRedisManager().getRedisObject(PlayerDataValue.PLAYER_FREEZE_REDIS.getString(apiPlayer));
        CoreAPI.get().getServer().removePlayerInServer(player.getUniqueId());
        apiPlayer.unloadPlayer();

        if (moderatorId != null)
            BanCmd.banPlayer(CoreAPI.get().getPlayerManager().getOfflinePlayer(player.getUsername()), "perm", CoreAPI.get().getModeratorManager().getModerator(moderatorId), "{Core} Déconnexion en inspection");

        RedisManager redisManager = CoreAPI.get().getRedisManager();
        redisManager.setRedisLong(PlayerDataValue.PLAYER_NUMBER.getString(null), redisManager.getRedisLong(PlayerDataValue.PLAYER_NUMBER.getString(null)) - 1);
    }

    @Subscribe
    public void connection(PreLoginEvent event) {
        if (!acceptConnection) {
            event.setResult(PreLoginEvent.PreLoginComponentResult.denied((Component) TextComponentBuilder.createTextComponent("Connection refusée")));
        }
    }

    @Subscribe
    public void serverConnect(ServerPreConnectEvent event) {

        APIPlayer apiPlayer = CoreAPI.get().getPlayerManager().getPlayer(event.getPlayer().getUniqueId());
        if (apiPlayer.getServer() != null)
            if (apiPlayer.isFreeze() || !apiPlayer.isLogin())
                event.setResult(ServerPreConnectEvent.ServerResult.denied());

    }

}
