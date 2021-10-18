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

public class NickCheckCmd implements Command {

    public void execute(CommandSource sender, String @NonNull [] args) {
        if (!(sender instanceof Player)) return;

        Player player = (Player) sender;
        APIPlayerModerator APIPlayerModerator = CoreAPI.get().getModeratorManager().getModerator(player.getUniqueId());

        if (APIPlayerModerator == null) {
            TextComponentBuilder.createTextComponent(TextUtils.getPrefix("MODERATION"))
                    .appendNewComponentBuilder("Vous n'avez pas la permission d'effectuer cette commande.").setColor(Color.RED)
                    .sendTo(player.getUniqueId());
            return;
        }

        if (args.length < 1) {
            TextComponentBuilder.createTextComponent(TextUtils.getPrefix("MODERATION"))
                    .appendNewComponentBuilder("Syntax: /nickcheck <nick>").setColor(Color.RED)
                    .sendTo(player.getUniqueId());
            return;
        }

        String targetArgs = args[0];
        APIOfflinePlayer targetPlayer = CoreAPI.get().getNickGestion().getAPIOfflinePlayer(targetArgs);
        TextComponentBuilder tcb;
        if (targetPlayer != null)

            tcb = TextComponentBuilder.createTextComponent(TextUtils.getPrefix("MODERATION"))
                    .appendNewComponentBuilder("Le vrai pseudo de cette personne: " + targetPlayer.getName());

        else
            tcb = TextComponentBuilder.createTextComponent(TextUtils.getPrefix("MODERATION"))
                    .appendNewComponentBuilder("Ceci n'est pas un nick").setColor(Color.RED);

        tcb.sendTo(player.getUniqueId());
        return;

    }

}
