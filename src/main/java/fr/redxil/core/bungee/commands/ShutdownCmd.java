/* Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 */

package fr.redxil.core.bungee.commands;

import com.velocitypowered.api.command.Command;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import fr.redxil.api.common.message.Color;
import fr.redxil.api.common.message.TextComponentBuilder;
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.api.common.rank.RankList;
import fr.redxil.api.common.server.Server;
import fr.redxil.core.common.CoreAPI;
import org.checkerframework.checker.nullness.qual.NonNull;

public class ShutdownCmd implements Command {

    public void execute(CommandSource commandSender, String @NonNull [] strings) {

        if (!(commandSender instanceof Player)) return;
        APIPlayer apiPlayer = CoreAPI.get().getPlayerManager().getPlayer(((Player) commandSender).getUniqueId());
        if (!apiPlayer.hasPermission(RankList.DEVELOPPEUR.getRankPower())) {
            return;
        }

        if (strings.length != 1) {
            TextComponentBuilder.createTextComponent("Erreur, merci de faire /shutdown (server)").setColor(Color.RED).sendTo(apiPlayer);
            return;
        }

        Server server = CoreAPI.get().getServerManager().getServer(strings[0]);
        if (server == null) {
            TextComponentBuilder.createTextComponent("Erreur, le server exists pas").setColor(Color.RED).sendTo(apiPlayer);
            return;
        }

        server.sendShutdownOrder();
        TextComponentBuilder.createTextComponent("L'ordre de shutdown est envoyé").setColor(Color.GREEN).sendTo(apiPlayer);


    }
}
