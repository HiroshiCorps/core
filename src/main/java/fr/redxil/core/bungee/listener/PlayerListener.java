/* Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 */

package fr.redxil.core.bungee.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.KickedFromServerEvent;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import fr.redxil.api.common.message.Color;
import fr.redxil.api.common.message.TextComponentBuilder;
import fr.redxil.api.common.message.TextComponentBuilderVelocity;
import fr.redxil.api.common.moderators.APIPlayerModerator;
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.api.common.player.data.SanctionInfo;
import fr.redxil.api.common.server.Server;
import fr.redxil.api.common.server.type.ServerType;
import fr.redxil.api.common.time.DateUtility;
import fr.redxil.api.common.utils.SanctionType;
import fr.redxil.api.velocity.Velocity;
import fr.redxil.core.bungee.CoreVelocity;
import fr.redxil.core.common.CoreAPI;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class PlayerListener {

    List<String> forbiddenCmd = new ArrayList<String>() {{
        add("/me");
        add("/say");
    }};

    @Subscribe
    public void playerMessage(PlayerChatEvent chatEvent) {

        APIPlayer apiPlayer = CoreAPI.get().getPlayerManager().getPlayer(chatEvent.getPlayer().getUniqueId());
        if (apiPlayer == null) return;

        String[] message = chatEvent.getMessage().split(" ");

        if (chatEvent.getMessage().charAt(0) == "/".charAt(0)) {
            if (forbiddenCmd.contains(message[0]) || message[0].contains("/minecraft:")) {
                chatEvent.setResult(PlayerChatEvent.ChatResult.denied());
            } else if (!apiPlayer.isLogin() && !(message[0].equals("/login") || message[0].equals("/register"))) {
                chatEvent.setResult(PlayerChatEvent.ChatResult.denied());
                chatEvent.getPlayer().sendMessage(((TextComponentBuilderVelocity) TextComponentBuilder.createTextComponent(Color.RED + "Vous avez uniquement le droit à: /login ou /register")).getFinalTextComponent());
            }
            return;
        } else if (!apiPlayer.isLogin()) {
            chatEvent.setResult(PlayerChatEvent.ChatResult.denied());
            chatEvent.getPlayer().sendMessage(((TextComponentBuilderVelocity) TextComponentBuilder.createTextComponent(Color.RED + "Vous avez uniquement le droit à: /login ou /register")).getFinalTextComponent());
            return;
        }

        APIPlayerModerator APIPlayerModerator = CoreAPI.get().getModeratorManager().getModerator(apiPlayer.getMemberId());

        if (APIPlayerModerator != null && message[0].startsWith("!s")) {
            String newMessage = chatEvent.getMessage().replace("!s", "");
            chatEvent.getPlayer().spoofChatInput("/staff " + newMessage);
            chatEvent.setResult(PlayerChatEvent.ChatResult.denied());
            return;
        }

        SanctionInfo model = apiPlayer.getLastSanction(SanctionType.MUTE);

        if (model != null && model.isEffective()) {

            chatEvent.getPlayer().sendMessage(((TextComponentBuilderVelocity) TextComponentBuilder.createTextComponent(Color.RED + "Vous êtes mute jusqu'au " + DateUtility.getMessage(model.getSanctionEndTS()))).getFinalTextComponent());
            chatEvent.setResult(PlayerChatEvent.ChatResult.denied());

        }

    }

    @Subscribe
    public void onKick(KickedFromServerEvent event) {
        Player p = event.getPlayer();
        Optional<ServerConnection> sco = p.getCurrentServer();
        if (sco.isPresent()) {
            CoreVelocity.getInstance().getProxyServer().getScheduler().buildTask(CoreVelocity.getInstance(), () -> {
                p.createConnectionRequest(getServer()).connect();
            });
        }
    }

    public RegisteredServer getServer() {
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
            if (proxyServer.isPresent()) return proxyServer.get();
        }
        return null;
    }

}