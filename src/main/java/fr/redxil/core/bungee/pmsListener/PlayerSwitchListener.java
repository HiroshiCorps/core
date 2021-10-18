/* Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 */

package fr.redxil.core.bungee.pmsListener;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import fr.redline.pms.connect.linker.pm.PMManager;
import fr.redline.pms.connect.linker.pm.PMReceiver;
import fr.redxil.api.common.message.Color;
import fr.redxil.api.common.message.TextComponentBuilder;
import fr.redxil.api.velocity.Velocity;
import fr.redxil.core.common.CoreAPI;
import net.kyori.adventure.text.Component;

import java.util.Optional;

public class PlayerSwitchListener implements PMReceiver {

    public PlayerSwitchListener() {
        PMManager.addRedissonPMListener(CoreAPI.get().getRedisManager().getRedissonClient(), "switchServer", String.class, this);
    }

    @Override
    public void pluginMessageReceived(String title, Object message) {

        if (!(message instanceof String)) return;

        String[] dataList = ((String) message).split("<switchSplit>");
        Optional<Player> playerO = Velocity.getInstance().getProxyServer().getPlayer(dataList[0]);

        if (!playerO.isPresent()) return;

        Player player = playerO.get();

        Optional<RegisteredServer> serverInfo = Velocity.getInstance().getProxyServer().getServer(dataList[1]);
        if (!serverInfo.isPresent()) {
            player.sendMessage((Component) TextComponentBuilder.createTextComponent(Color.RED + "Cannot connect you to server: " + dataList[1]).getFinalTextComponent());
            return;
        }

        player.sendMessage((Component) TextComponentBuilder.createTextComponent("Le système me dits que vous êtes en cours de transfert").getFinalTextComponent());
        player.createConnectionRequest(serverInfo.get()).connect();

    }

}
