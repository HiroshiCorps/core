/*
 *
 * Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 *
 */

package fr.redxil.core.velocity.commands.mod.action.punish;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import fr.redxil.api.common.player.APIOfflinePlayer;
import fr.redxil.api.common.player.data.SanctionInfo;
import fr.redxil.api.common.player.moderators.APIPlayerModerator;
import fr.redxil.api.common.utils.Color;
import fr.redxil.api.common.utils.cmd.LiteralArgumentCreator;
import fr.redxil.core.common.CoreAPI;
import fr.redxil.core.velocity.CoreVelocity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

import java.util.Optional;

public class WarnCmd extends LiteralArgumentCreator<CommandSource> {

    public WarnCmd() {
        super("warn");
        super.setExecutor(this::onMissingArgument);
        super.createThen("target", StringArgumentType.word(), this::onMissingArgument)
                .createThen("reason", StringArgumentType.greedyString(), this::execute);
    }

    public void onMissingArgument(CommandContext<CommandSource> commandContext, String s) {
        commandContext.getSource().sendMessage(Component.text("Syntax: /warn <pseudo> <raison>").color(TextColor.color(Color.RED.getRed(), Color.RED.getGreen(), Color.RED.getBlue())));
    }

    public void execute(CommandContext<CommandSource> commandContext, String s) {
        if (!(commandContext.getSource() instanceof Player player)) return;

        Optional<APIPlayerModerator> apiPlayerModerator = CoreAPI.getInstance().getModeratorManager().getModerator(player.getUniqueId());

        if (apiPlayerModerator.isEmpty()) {
            commandContext.getSource().sendMessage(Component.text("Vous n'avez pas la permission d'effectuer cette commande.").color(TextColor.color(Color.RED.getRed(), Color.RED.getGreen(), Color.RED.getBlue())));
            return;
        }

        String targetArgs = commandContext.getArgument("target", String.class);
        Optional<APIOfflinePlayer> apiPlayerTarget = CoreAPI.getInstance().getPlayerManager().getOfflinePlayer(targetArgs);
        if (apiPlayerTarget.isEmpty()) {
            commandContext.getSource().sendMessage(Component.text("La target ne s'est jamais connecté.").color(TextColor.color(Color.RED.getRed(), Color.RED.getGreen(), Color.RED.getBlue())));
            return;
        }

        if (apiPlayerTarget.get().getRank().isModeratorRank()) {
            commandContext.getSource().sendMessage(Component.text("Vous n'avez pas la permission d'effectuer cette commande.").color(TextColor.color(Color.RED.getRed(), Color.RED.getGreen(), Color.RED.getBlue())));
            return;
        }

        String reason = commandContext.getArgument("reason", String.class);

        if (reason.contains("{") || reason.contains("}")) {
            commandContext.getSource().sendMessage(Component.text("Erreur, le joueur est déjà mute.").color(TextColor.color(Color.RED.getRed(), Color.RED.getGreen(), Color.RED.getBlue())));
            return;
        }

        Optional<SanctionInfo> sm = apiPlayerTarget.get().warnPlayer(reason, apiPlayerModerator.get());
        if (sm.isPresent()) {
            player.sendMessage(Component.text("Le joueur: " + apiPlayerTarget.get().getName() + " à été warn."));
            CoreVelocity.getInstance().getProxyServer().getPlayer(apiPlayerTarget.get().getName()).ifPresent((proxiedPlayer) -> proxiedPlayer.sendMessage(Component.text(sm.get().getSancMessage())));
        } else
            commandContext.getSource().sendMessage(Component.text("Désolé, une erreur est survenue").color(TextColor.color(Color.RED.getRed(), Color.RED.getGreen(), Color.RED.getBlue())));

    }
}
