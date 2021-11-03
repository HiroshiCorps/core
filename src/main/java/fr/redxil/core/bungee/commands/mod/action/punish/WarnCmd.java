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
import fr.redxil.api.common.utils.TextUtils;
import fr.redxil.api.velocity.Velocity;
import fr.redxil.core.common.CoreAPI;
import org.checkerframework.checker.nullness.qual.NonNull;

public class WarnCmd implements Command {

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

        if (args.length < 2) {
            TextComponentBuilder.createTextComponent(TextUtils.getPrefix("MODERATION"))
                    .appendNewComponentBuilder("Syntax: /warn <pseudo> <raison>").setColor(Color.RED)
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

        StringBuilder reasonBuilder = new StringBuilder();

        for (int i = 1; i < args.length; i++) {
            reasonBuilder.append(args[i]).append(" ");
        }

        String reason = reasonBuilder.toString();

        if (reason.contains("{") || reason.contains("}")) {
            TextComponentBuilder.createTextComponent(TextUtils.getPrefix("MODERATION"))
                    .appendNewComponentBuilder("Les caractéres { et } sont interdit d'utilisation dans les raisons").setColor(Color.RED)
                    .sendTo(player.getUniqueId());
            return;
        }

        SanctionInfo sm = apiPlayerTarget.warnPlayer(reasonBuilder.toString(), APIPlayerModAuthor);
        if (sm != null) {
            sender.sendMessage(((TextComponentBuilderVelocity) TextComponentBuilder.createTextComponent(TextUtils.getPrefix("MODERATION") + "Le joueur: " + apiPlayerTarget.getName() + " à été warn.")).getFinalTextComponent());
            Velocity.getInstance().getProxyServer().getPlayer(apiPlayerTarget.getName()).ifPresent((proxiedPlayer) -> sm.getSancMessage().sendTo(proxiedPlayer.getUniqueId()));
        } else
            TextComponentBuilder.createTextComponent(TextUtils.getPrefix("MODERATION"))
                    .appendNewComponentBuilder("Désolé, une erreur est survenue").setColor(Color.RED)
                    .sendTo(player.getUniqueId());

    }
}
