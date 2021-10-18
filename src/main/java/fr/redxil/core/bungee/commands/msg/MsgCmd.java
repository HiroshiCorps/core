/* Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 */

package fr.redxil.core.bungee.commands.msg;

import com.velocitypowered.api.command.Command;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import fr.redxil.api.common.data.PlayerDataValue;
import fr.redxil.api.common.message.Color;
import fr.redxil.api.common.message.TextComponentBuilder;
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.api.common.utils.TextUtils;
import fr.redxil.core.common.CoreAPI;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.UUID;

public class MsgCmd implements Command {

    public void execute(CommandSource sender, String @NonNull [] args) {

        if (!(sender instanceof Player)) return;

        UUID playerUUID = ((Player) sender).getUniqueId();
        APIPlayer sp = CoreAPI.get().getPlayerManager().getPlayer(playerUUID);

        if (args.length < 2) {
            TextComponentBuilder.createTextComponent(TextUtils.getPrefix("mp"))
                    .appendNewComponentBuilder("Merci de faire /msg (pseudo) (message)").setColor(Color.RED).sendTo(playerUUID);
            return;
        }

        APIPlayer target = CoreAPI.get().getPlayerManager().getPlayer(args[0]);
        if (target == null) {
            TextComponentBuilder.createTextComponent(TextUtils.getPrefix("mp"))
                    .appendNewComponentBuilder("Le joueur: " + args[0] + " n'est pas connecté").setColor(Color.RED).sendTo(playerUUID);
            return;
        }

        if (sp.isBlackList(target)) {
            TextComponentBuilder.createTextComponent(TextUtils.getPrefix("mp"))
                    .appendNewComponentBuilder("Vous ne pouvez pas mp un joueur que vous avez blacklisté").setColor(Color.RED).sendTo(playerUUID);
            return;
        }

        if (target.isBlackList(sp)) {
            TextComponentBuilder.createTextComponent(TextUtils.getPrefix("mp"))
                    .appendNewComponentBuilder("Le joueur: " + args[0] + " n'est pas connecté").setColor(Color.RED).sendTo(playerUUID);
            return;
        }

        StringBuilder messageB = new StringBuilder(args[1]);
        for (int i = 2; i < args.length; i++) {
            messageB.append(" ").append(args[i]);
        }

        String message = messageB.toString();

        TextComponentBuilder.createTextComponent(TextUtils.getPrefix("mp"))
                .appendNewComponentBuilder(sp.getName(true)).setColor(Color.GREEN).setHover("N'oubliez pas le /blacklist add en cas d'harcélement")
                .appendNewComponentBuilder(": ").setColor(Color.WHITE)
                .appendNewComponentBuilder(message).sendTo(target.getUUID());

        TextComponentBuilder.createTextComponent(TextUtils.getPrefix("mp"))
                .appendNewComponentBuilder(args[0]).setColor(Color.RED)
                .appendNewComponentBuilder(": ").setColor(Color.WHITE)
                .appendNewComponentBuilder(message).sendTo(sp.getUUID());

        CoreAPI.get().getRedisManager().setRedisString(PlayerDataValue.PLAYER_LASTMSG_REDIS.getString(sp), target.getName(true));
        CoreAPI.get().getRedisManager().setRedisString(PlayerDataValue.PLAYER_LASTMSG_REDIS.getString(target), sp.getName(true));

    }

}
