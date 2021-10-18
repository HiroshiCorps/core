/* Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 */

package fr.redxil.core.bungee.commands.mod;

import com.velocitypowered.api.command.Command;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import fr.redxil.api.common.message.Color;
import fr.redxil.api.common.message.TextComponentBuilder;
import fr.redxil.api.common.moderators.APIPlayerModerator;
import fr.redxil.api.common.player.APIOfflinePlayer;
import fr.redxil.api.common.utils.TextUtils;
import fr.redxil.core.common.CoreAPI;
import org.checkerframework.checker.nullness.qual.NonNull;

public class CibleCmd implements Command {

    public void execute(CommandSource sender, String @NonNull [] args) {
        if (!(sender instanceof Player)) return;

        Player player = (Player) sender;
        APIPlayerModerator APIPlayerModAuthor = CoreAPI.get().getModeratorManager().getModerator(((Player) sender).getUniqueId());

        if (APIPlayerModAuthor == null) {
            TextComponentBuilder.createTextComponent(TextUtils.getPrefix("MODERATION"))
                    .appendNewComponentBuilder("Vous n'avez pas la permission d'effectuer cette commande.").setColor(Color.RED)
                    .sendTo(((Player) sender).getUniqueId());
            return;
        }

        if (args.length == 0) {
            if (APIPlayerModAuthor.hasCible()) {
                APIPlayerModAuthor.setCible(null);
                TextComponentBuilder.createTextComponent(TextUtils.getPrefix("MODERATION"))
                        .appendNewComponentBuilder("Vous n'avez plus de cible.").setColor(Color.RED)
                        .sendTo(((Player) sender).getUniqueId());

            } else
                TextComponentBuilder.createTextComponent(TextUtils.getPrefix("MODERATION"))
                        .appendNewComponentBuilder("Syntax: /cible <pseudo>").setColor(Color.RED)
                        .sendTo(player.getUniqueId());
            return;
        }

        if (!APIPlayerModAuthor.isModeratorMod()) {

            TextComponentBuilder.createTextComponent("Commande accessible uniquement en mod moderation").setColor(Color.RED)
                    .sendTo(player.getUniqueId());
            return;

        }

        String target = args[0];
        APIOfflinePlayer playerTarget = CoreAPI.get().getPlayerManager().getOfflinePlayer(target);

        if (playerTarget == null) {
            TextComponentBuilder.createTextComponent(
                            Color.RED +
                                    "Cette target ne s'est jamais connecté").setColor(Color.RED)
                    .sendTo(player.getUniqueId());
            return;
        }

        if (playerTarget.getRank().isModeratorRank()) {
            TextComponentBuilder.createTextComponent(
                            "Impossible de cibler " + target).setColor(Color.RED)
                    .sendTo(player.getUniqueId());
            return;
        }

        APIPlayerModAuthor.setCible(playerTarget.getName());
        TextComponentBuilder.createTextComponent(
                        "Nouvelle cible: " + playerTarget.getName()).setColor(Color.GREEN)
                .sendTo(player.getUniqueId());

    }
}
