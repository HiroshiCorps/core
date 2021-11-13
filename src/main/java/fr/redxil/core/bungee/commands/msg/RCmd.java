/* Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 */

package fr.redxil.core.bungee.commands.msg;

import com.velocitypowered.api.command.Command;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import fr.redxil.api.common.message.Color;
import fr.redxil.api.common.message.TextComponentBuilder;
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.core.common.CoreAPI;
import fr.redxil.core.common.data.PlayerDataValue;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.UUID;

public class RCmd implements Command {

    public void execute(CommandSource sender, String @NonNull [] args) {

        if (!(sender instanceof Player)) return;

        UUID playerUUID = ((Player) sender).getUniqueId();
        APIPlayer sp = CoreAPI.get().getPlayerManager().getPlayer(playerUUID);

        if (args.length < 1) {
            TextComponentBuilder.createTextComponent("Merci de faire /r (message)").setColor(Color.RED).sendTo(playerUUID);
            return;
        }

        String targetName = CoreAPI.get().getRedisManager().getRedisString(PlayerDataValue.PLAYER_LASTMSG_REDIS.getString(sp));

        if (targetName == null) {
            TextComponentBuilder.createTextComponent("Erreur, vous avez jusque la pas envoyé de message").setColor(Color.RED).sendTo(playerUUID);
            return;
        }

        APIPlayer target = CoreAPI.get().getPlayerManager().getPlayer(targetName);
        if (target == null) {
            TextComponentBuilder.createTextComponent("Le joueur: " + targetName + " n'est pas connecté").setColor(Color.RED).sendTo(playerUUID);
            return;
        }

        if (sp.isBlackList(target)) {
            TextComponentBuilder.createTextComponent("Vous ne pouvez pas mp un joueur que vous avez blacklisté").setColor(Color.RED).sendTo(playerUUID);
            return;
        }

        if (target.isBlackList(sp)) {
            TextComponentBuilder.createTextComponent("Le joueur: " + targetName + " n'est pas connecté").setColor(Color.RED).sendTo(playerUUID);
            return;
        }

        StringBuilder messageB = new StringBuilder(args[0]);
        for (int i = 1; i < args.length; i++) {
            messageB.append(" ").append(args[i]);
        }

        String message = messageB.toString();

        TextComponentBuilder.createTextComponent(sp.getName(true)).setColor(Color.GREEN).setHover("N'oubliez pas le /blacklist add en cas d'harcélement")
                .appendNewComponentBuilder(": ").setColor(Color.WHITE)
                .appendNewComponentBuilder(message).sendTo(target.getUUID());

        TextComponentBuilder.createTextComponent(targetName).setColor(Color.RED)
                .appendNewComponentBuilder(": ").setColor(Color.WHITE)
                .appendNewComponentBuilder(message).sendTo(sp.getUUID());

    }

}
