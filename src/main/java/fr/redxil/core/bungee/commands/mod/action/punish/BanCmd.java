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
import fr.redxil.api.common.utils.TextUtils;
import fr.redxil.api.velocity.Velocity;
import fr.redxil.core.common.CoreAPI;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Optional;

public class BanCmd implements Command {

    public static void banPlayer(APIOfflinePlayer apiPlayerTarget, String timeArgs, APIPlayerModerator APIPlayerModAuthor, String reason) {
        long durationTime = DateUtility.toTimeStamp(timeArgs);

        Optional<Player> playerOptional = Velocity.getInstance().getProxyServer().getPlayer(APIPlayerModAuthor.getUUID());
        if (!playerOptional.isPresent()) return;
        Player proxiedPlayer = playerOptional.get();

        long end = DateUtility.addToCurrentTimeStamp(durationTime);

        String format = DateUtility.getMessage(end);

        if (durationTime != -2L) {

            SanctionInfo sm = apiPlayerTarget.banPlayer(reason, end, APIPlayerModAuthor);
            if (sm != null) {

                TextComponentBuilder banMessage = TextComponentBuilder.createTextComponent(
                        TextUtils.getPrefix("moderation")
                                + "Le modérateur §d" +
                                APIPlayerModAuthor.getName() +
                                " §7à ban l'utilisateur §a" +
                                apiPlayerTarget.getName() + " §7jusqu'au " +
                                format + " pour raison: "
                                + reason + ".");

                CoreAPI.get().getModeratorManager().sendToModerators(banMessage);

                Optional<Player> onlinePlayerOptional = Velocity.getInstance().getProxyServer().getPlayer(apiPlayerTarget.getName());

                onlinePlayerOptional.ifPresent(player -> player.disconnect(((TextComponentBuilderVelocity) sm.getSancMessage()).getFinalTextComponent()));

            } else
                TextComponentBuilder.createTextComponent(TextUtils.getPrefix("MODERATION"))
                        .appendNewComponentBuilder("Désolé, une erreur est survenue").setColor(Color.RED)
                        .sendTo(proxiedPlayer.getUniqueId());

        } else {
            TextComponentBuilder.createTextComponent(TextUtils.getPrefix("MODERATION"))
                    .appendNewComponentBuilder("Erreur: " + timeArgs + " n'est pas une durée valide").setColor(Color.RED)
                    .sendTo(proxiedPlayer.getUniqueId());
        }

    }

    public void execute(CommandSource sender, String @NonNull [] args) {
        if (!(sender instanceof Player)) return;

        Player player = (Player) sender;
        APIPlayerModerator APIPlayerModAuthor = CoreAPI.get().getModeratorManager().getModerator(player.getUniqueId());

        if (APIPlayerModAuthor == null) {
            TextComponentBuilder.createTextComponent(TextUtils.getPrefix("MODERATION"))
                    .appendNewComponentBuilder("Vous n'avez pas la permission d'effectuer cette commande.").setColor(Color.RED)
                    .sendTo(player.getUniqueId());
            return;
        }

        if (args.length < 3) {
            TextComponentBuilder.createTextComponent(TextUtils.getPrefix("MODERATION"))
                    .appendNewComponentBuilder("Syntax: /ban <pseudo> <temps> <raison>").setColor(Color.RED)
                    .sendTo(player.getUniqueId());
            return;
        }

        String targetArgs = args[0];
        APIOfflinePlayer apiPlayerTarget = CoreAPI.get().getPlayerManager().getOfflinePlayer(targetArgs);

        if (apiPlayerTarget == null) {
            TextComponentBuilder.createTextComponent(TextUtils.getPrefix("MODERATION"))
                    .appendNewComponentBuilder("La target ne s'est jamais connecté.").setColor(Color.RED)
                    .sendTo(player.getUniqueId());
            return;
        }

        if (apiPlayerTarget.getRank().isModeratorRank()) {
            TextComponentBuilder.createTextComponent(TextUtils.getPrefix("MODERATION"))
                    .appendNewComponentBuilder("Vous n'avez pas la permission d'effectuer cette commande.").setColor(Color.RED)
                    .sendTo(player.getUniqueId());
            return;
        }

        if (apiPlayerTarget.isBan()) {
            TextComponentBuilder.createTextComponent(TextUtils.getPrefix("MODERATION"))
                    .appendNewComponentBuilder("Erreur, le joueur est déjà ban.").setColor(Color.RED)
                    .sendTo(player.getUniqueId());
            return;
        }

        String timeArgs = args[1];

        StringBuilder reasonBuilder = new StringBuilder(args[2]);

        for (int i = 3; i < args.length; i++)
            reasonBuilder.append(" ").append(args[i]);

        String reason = reasonBuilder.toString();

        if (reason.contains("{") || reason.contains("}")) {
            TextComponentBuilder.createTextComponent(TextUtils.getPrefix("MODERATION"))
                    .appendNewComponentBuilder("Les caractéres { et } sont interdit d'utilisation dans les raisons").setColor(Color.RED)
                    .sendTo(player.getUniqueId());
            return;
        }

        banPlayer(apiPlayerTarget, timeArgs, APIPlayerModAuthor, reason);

    }

}
