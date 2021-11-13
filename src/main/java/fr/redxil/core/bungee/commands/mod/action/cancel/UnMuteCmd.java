/* Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 */

package fr.redxil.core.bungee.commands.mod.action.cancel;

import com.velocitypowered.api.command.Command;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import fr.redxil.api.common.message.Color;
import fr.redxil.api.common.message.TextComponentBuilder;
import fr.redxil.api.common.player.APIOfflinePlayer;
import fr.redxil.api.common.player.moderators.APIPlayerModerator;
import fr.redxil.core.common.CoreAPI;
import org.checkerframework.checker.nullness.qual.NonNull;

public class UnMuteCmd implements Command {

    public void execute(CommandSource sender, String @NonNull [] args) {
        if (!(sender instanceof Player)) return;

        Player player = (Player) sender;
        APIPlayerModerator APIPlayerModerator = CoreAPI.get().getModeratorManager().getModerator(player.getUniqueId());

        if (APIPlayerModerator == null) {
            TextComponentBuilder.createTextComponent("Vous n'avez pas la permission d'effectuer cette commande.").setColor(Color.RED)
                    .sendTo(player.getUniqueId());
            return;
        }

        if (args.length < 1) {
            TextComponentBuilder.createTextComponent("Syntax: /unmute <pseudo>").setColor(Color.RED)
                    .sendTo(player.getUniqueId());
            return;
        }

        String targetArgs = args[0];
        APIOfflinePlayer apiPlayerTarget = CoreAPI.get().getPlayerManager().getOfflinePlayer(targetArgs);

        if (apiPlayerTarget == null) {
            TextComponentBuilder.createTextComponent("La target ne s'est jamais connecté").setColor(Color.RED)
                    .sendTo(player.getUniqueId());
            return;
        }

        if (apiPlayerTarget.unMute(APIPlayerModerator)) {
            TextComponentBuilder.createTextComponent("La personne se nommant: " + apiPlayerTarget.getName() + " est maintenant unMute.")
                    .sendTo(player.getUniqueId());
        } else {
            TextComponentBuilder.createTextComponent("Impossible de unmute: " + apiPlayerTarget.getName()).setColor(Color.RED)
                    .sendTo(player.getUniqueId());
        }

    }

}
