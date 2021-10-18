/* Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 */

package fr.redxil.core.bungee.commands.friend;

import com.velocitypowered.api.command.Command;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import fr.redxil.api.common.message.Color;
import fr.redxil.api.common.message.TextComponentBuilder;
import fr.redxil.api.common.player.APIOfflinePlayer;
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.api.common.utils.TextUtils;
import fr.redxil.core.common.CoreAPI;

import java.util.UUID;

public class BlackListCmd implements Command {

    public void execute(CommandSource sender, String[] args) {

        if (!(sender instanceof Player)) return;

        UUID playerUUID = ((Player) sender).getUniqueId();

        if (args.length != 2) {
            TextComponentBuilder.createTextComponent(TextUtils.getPrefix("blacklist"))
                    .appendNewComponentBuilder("Merci de faire /bl (add|remove) (pseudo)").setColor(Color.RED).sendTo(playerUUID);
            return;
        }

        if (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("remove")) {

            APIOfflinePlayer osp = CoreAPI.get().getPlayerManager().getOfflinePlayer(args[1]);
            APIPlayer sp = CoreAPI.get().getPlayerManager().getPlayer(playerUUID);

            if (osp == null) {
                TextComponentBuilder.createTextComponent(TextUtils.getPrefix("blacklist"))
                        .appendNewComponentBuilder("Erreur, le joueur: " + args[1] + " est inconnue").setColor(Color.RED).sendTo(playerUUID);
                return;
            }

            boolean remove = args[0].equalsIgnoreCase("remove");
            if (remove) {

                if (!sp.isBlackList(osp)) {
                    TextComponentBuilder.createTextComponent(TextUtils.getPrefix("blacklist"))
                            .appendNewComponentBuilder("Erreur, le joueur: " + args[1] + " n'est pas BlackList").setColor(Color.RED).sendTo(playerUUID);
                    return;
                }

                sp.removeBlackList(osp);

            } else {

                if (sp.isBlackList(osp)) {
                    TextComponentBuilder.createTextComponent(TextUtils.getPrefix("blacklist"))
                            .appendNewComponentBuilder("Erreur, le joueur: " + args[1] + " est déjà BlackList").setColor(Color.RED).sendTo(playerUUID);
                    return;
                }

                sp.addBlackList(osp);

            }

            return;
        }

        TextComponentBuilder.createTextComponent(TextUtils.getPrefix("blacklist"))
                .appendNewComponentBuilder("Merci de faire /bl (add|remove) (pseudo)").setColor(Color.RED).sendTo(playerUUID);

    }

}
