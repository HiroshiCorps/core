/* Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 */

package fr.redxil.core.bungee.commands.mod.action.punish;

import com.velocitypowered.api.command.Command;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import fr.redxil.api.common.message.Color;
import fr.redxil.api.common.message.TextComponentBuilder;
import fr.redxil.api.common.message.TextComponentBuilderVelocity;
import fr.redxil.api.common.player.APIOfflinePlayer;
import fr.redxil.api.common.player.data.SanctionInfo;
import fr.redxil.api.common.player.moderators.APIPlayerModerator;
import fr.redxil.api.common.time.DateUtility;
import fr.redxil.api.velocity.Velocity;
import fr.redxil.core.common.CoreAPI;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Optional;

public class MuteCmd implements Command {

    public static void mutePlayer(APIOfflinePlayer apiPlayerTarget, String timeArgs, APIPlayerModerator APIPlayerModAuthor, String reason) {

        long durationTime = DateUtility.toTimeStamp(timeArgs);

        Optional<Player> proxiedPlayerOptional = Velocity.getInstance().getProxyServer().getPlayer(APIPlayerModAuthor.getName());

        if (!proxiedPlayerOptional.isPresent()) return;

        Player proxiedPlayer = proxiedPlayerOptional.get();

        long end = DateUtility.addToCurrentTimeStamp(DateUtility.toTimeStamp(timeArgs));

        String endDate = DateUtility.getMessage(end);

        if (durationTime != -2L) {

            SanctionInfo sm = apiPlayerTarget.mutePlayer(reason, end, APIPlayerModAuthor);

            if (sm != null) {
                Velocity.getInstance().getProxyServer().getPlayer(apiPlayerTarget.getName()).ifPresent((onlinePlayer) -> onlinePlayer.sendMessage(((TextComponentBuilderVelocity) sm.getSancMessage()).getFinalTextComponent()));

                TextComponentBuilder muteMessage = TextComponentBuilder.createTextComponent(
                        "Le modérateur §d" +
                                APIPlayerModAuthor.getName() +
                                " §7à mute l'utilisateur §a" +
                                apiPlayerTarget.getName() + " §7jusqu'au " +
                                endDate + " pour raison: "
                                + reason + ".");

                CoreAPI.get().getModeratorManager().sendToModerators(muteMessage);

            } else
                TextComponentBuilder.createTextComponent("Désolé, une erreur est survenue").setColor(Color.RED)
                        .sendTo(proxiedPlayer.getUniqueId());
        } else {
            TextComponentBuilder tcb = TextComponentBuilder.createTextComponent(
                    "Erreur: " + timeArgs + " n'est pas une durée valide").setColor(Color.RED);

            tcb.sendTo(proxiedPlayer.getUniqueId());
        }

    }

    public void execute(CommandSource sender, String @NonNull [] args) {
        if (!(sender instanceof Player)) return;

        Player player = (Player) sender;
        APIPlayerModerator APIPlayerModAuthor = CoreAPI.get().getModeratorManager().getModerator(player.getUniqueId());

        if (APIPlayerModAuthor == null) {
            TextComponentBuilder.createTextComponent("Vous n'avez pas la permission d'effectuer cette commande.").setColor(Color.RED)
                    .sendTo(player.getUniqueId());
            return;
        }

        if (args.length < 3) {
            TextComponentBuilder.createTextComponent("Syntax: /mute <pseudo> <temps> <raison>").setColor(Color.RED)
                    .sendTo(player.getUniqueId());
            return;
        }

        String targetArgs = args[0];
        APIOfflinePlayer apiPlayerTarget = CoreAPI.get().getPlayerManager().getOfflinePlayer(targetArgs);
        if (apiPlayerTarget == null) {
            TextComponentBuilder.createTextComponent("La target ne s'est jamais connecté.").setColor(Color.RED)
                    .sendTo(player.getUniqueId());
            return;
        }

        if (apiPlayerTarget.getRank().isModeratorRank()) {
            TextComponentBuilder.createTextComponent("Vous n'avez pas la permission d'effectuer cette commande.").setColor(Color.RED)
                    .sendTo(player.getUniqueId());
            return;
        }

        if (apiPlayerTarget.isMute()) {
            TextComponentBuilder.createTextComponent("Erreur, le joueur est déjà mute.").setColor(Color.RED)
                    .sendTo(player.getUniqueId());
            return;
        }

        String timeArgs = args[1];
        StringBuilder reasonBuilder = new StringBuilder(args[2]);

        for (int i = 3; i < args.length; i++)
            reasonBuilder.append(" ").append(args[i]);

        String reason = reasonBuilder.toString();

        if (reason.contains("{") || reason.contains("}")) {
            TextComponentBuilder.createTextComponent("Les caractéres { et } sont interdit d'utilisation dans les raisons").setColor(Color.RED)
                    .sendTo(player.getUniqueId());
            return;
        }

        mutePlayer(apiPlayerTarget, timeArgs, APIPlayerModAuthor, reason);

    }

}
