/* Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 */

package fr.redxil.core.velocity.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.connection.PreLoginEvent;
import com.velocitypowered.api.proxy.Player;
import fr.redline.pms.utils.IpInfo;
import fr.redxil.api.common.API;
import fr.redxil.api.common.message.TextComponentBuilder;
import fr.redxil.api.common.player.APIOfflinePlayer;
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.api.common.player.data.SanctionInfo;
import fr.redxil.api.common.server.Server;
import fr.redxil.api.common.server.type.ServerStatus;
import fr.redxil.api.common.utils.SanctionType;
import fr.redxil.core.common.CoreAPI;
import net.kyori.adventure.text.Component;

import java.util.logging.Level;

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

    @Subscribe
    public void connection(PreLoginEvent event) {
        if (API.getInstance().getNickGestion().isIllegalName(event.getUsername())) {
            event.setResult(PreLoginEvent.PreLoginComponentResult.denied((Component) TextComponentBuilder.createTextComponent("Illegal Name detected").getFinalTextComponent()));
        }
        if (API.getInstance().getServer().getServerStatus() != ServerStatus.ONLINE) {
            event.setResult(PreLoginEvent.PreLoginComponentResult.denied((Component) TextComponentBuilder.createTextComponent("Connection refusée").getFinalTextComponent()));
        }
    }

    @Subscribe
    public void onPlayerJoin(PostLoginEvent e) {

        Player player = e.getPlayer();
        API.getInstance().getPluginEnabler().printLog(Level.FINE, "First Connection");
        if (API.getInstance().getPlayerManager().isLoadedPlayer(player.getUniqueId())) {
            player.disconnect((Component) TextComponentBuilder.createTextComponent(
                    "§4§lSERVER NETWORK§r\n"
                            + "§cConnexion non autorisé§r\n\n"
                            + "§7Raison: Une personne est déjà connecté avec votre UUID§e§r\n"
            ).getFinalTextComponent());
            return;
        }

        if (CoreAPI.getInstance().getServerAccessEnum() == CoreAPI.ServerAccessEnum.CRACK)
            if (API.getInstance().getPlayerManager().isLoadedPlayer(player.getUsername())) {
                player.disconnect((Component) TextComponentBuilder.createTextComponent(
                        "§4§lSERVER NETWORK§r\n"
                                + "§cConnexion non autorisé§r\n\n"
                                + "§7Raison: Une personne est déjà connecté avec votre username§e§r\n"
                ).getFinalTextComponent());
                return;
            }

        API.getInstance().getPluginEnabler().printLog(Level.FINE, "Checking Offline PLayer");
        APIOfflinePlayer apiOfflinePlayer;
        if (CoreAPI.getInstance().getServerAccessEnum() == CoreAPI.ServerAccessEnum.CRACK) {
            apiOfflinePlayer = API.getInstance().getPlayerManager().getOfflinePlayer(player.getUsername());
        } else apiOfflinePlayer = API.getInstance().getPlayerManager().getOfflinePlayer(player.getUniqueId());


        API.getInstance().getPluginEnabler().printLog(Level.FINE, "Checking Offline Player 2");
        if (apiOfflinePlayer != null) {

            SanctionInfo model = apiOfflinePlayer.getLastSanction(SanctionType.BAN);
            if (model != null && model.isEffective()) {
                player.disconnect((Component) model.getSancMessage().getFinalTextComponent());
                return;
            }

            Server velocityServer = API.getInstance().getServer();

            if (!velocityServer.getServerAccess().canAccess(velocityServer, player.getUsername(), apiOfflinePlayer.getRank())) {
                player.disconnect((Component) TextComponentBuilder.createTextComponent("Vous ne pouvez pas acceder au server"));
                return;
            }

        }

        API.getInstance().getPluginEnabler().printLog(Level.FINE, "Checking Offline Player 3");
        loadPlayer(player);
        API.getInstance().getPluginEnabler().printLog(Level.FINE, "Checking Offline Player 4");

    }

}
