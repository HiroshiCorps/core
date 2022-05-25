/*
 *
 * Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 *
 */

package fr.redxil.core.velocity.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.PlayerChatEvent;
import fr.redxil.api.common.API;
import fr.redxil.api.common.message.TextComponentBuilder;
import fr.redxil.api.common.message.TextComponentBuilderVelocity;
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.api.common.player.data.SanctionInfo;
import fr.redxil.api.common.player.moderators.APIPlayerModerator;
import fr.redxil.api.common.time.DateUtility;
import fr.redxil.api.common.utils.SanctionType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class PlayerListener {

    List<String> forbiddenCmd = new ArrayList<>() {{
        add("/me");
        add("/say");
    }};

    @Subscribe
    public void playerMessage(PlayerChatEvent chatEvent) {

        Optional<APIPlayer> apiPlayer = API.getInstance().getPlayerManager().getPlayer(chatEvent.getPlayer().getUniqueId());
        if (apiPlayer.isEmpty()) return;

        String[] message = chatEvent.getMessage().split(" ");

        if (chatEvent.getMessage().charAt(0) == "/".charAt(0)) {
            if (forbiddenCmd.contains(message[0]) || message[0].contains("/minecraft:")) {
                chatEvent.setResult(PlayerChatEvent.ChatResult.denied());
            }
            return;
        }

        Optional<APIPlayerModerator> apiPlayerModerator = API.getInstance().getModeratorManager().getModerator(apiPlayer.get().getMemberID());

        if (apiPlayerModerator.isPresent() && message[0].startsWith("!s")) {
            String newMessage = chatEvent.getMessage().replace("!s", "");
            chatEvent.getPlayer().spoofChatInput("/staff " + newMessage);
            chatEvent.setResult(PlayerChatEvent.ChatResult.denied());
            return;
        }

        Optional<SanctionInfo> model = apiPlayer.get().getLastSanction(SanctionType.MUTE);

        if (model.isPresent() && model.get().isEffective()) {

            chatEvent.getPlayer().sendMessage(((TextComponentBuilderVelocity) TextComponentBuilder.createTextComponent("Vous êtes mute jusqu'au " + DateUtility.getMessage(model.get().getSanctionEndTS()))).getFinalTextComponent());
            chatEvent.setResult(PlayerChatEvent.ChatResult.denied());

        }

    }

    /*@Subscribe
    public void onKick(KickedFromServerEvent event) {
        Player p = event.getPlayer();
        Optional<ServerConnection> sco = p.getCurrentServer();
        if (sco.isPresent()) {
            CoreVelocity.getInstance().getProxyServer().getScheduler().buildTask(CoreVelocity.getInstance(), () -> p.createConnectionRequest(getServer()).connect());
        }
    }*/

}