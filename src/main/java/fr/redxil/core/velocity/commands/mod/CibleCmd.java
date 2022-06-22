/*
 *
 * Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 *
 */

package fr.redxil.core.velocity.commands.mod;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import fr.redxil.api.common.message.Color;
import fr.redxil.api.common.message.TextComponentBuilder;
import fr.redxil.api.common.player.APIOfflinePlayer;
import fr.redxil.api.common.player.moderators.APIPlayerModerator;
import fr.redxil.api.common.utils.cmd.LiteralArgumentCreator;
import fr.redxil.core.common.CoreAPI;

import java.util.Optional;

public class CibleCmd extends LiteralArgumentCreator<CommandSource> {

    public CibleCmd() {
        super("nickcheck");
        super.setExecutor(this::onCommandWithoutArgs);
        super.createArgument("player", StringArgumentType.word(), this::execute);
    }

    public void onCommandWithoutArgs(CommandContext<CommandSource> commandContext, String s) {
        CommandSource sender = commandContext.getSource();

        if (!(sender instanceof Player player)) return;

        Optional<APIPlayerModerator> apiPlayerModAuthor = CoreAPI.getInstance().getModeratorManager().getModerator(((Player) sender).getUniqueId());

        if (apiPlayerModAuthor.isEmpty()) {
            TextComponentBuilder.createTextComponent("Vous n'avez pas la permission d'effectuer cette commande.").setColor(Color.RED)
                    .sendTo(((Player) sender).getUniqueId());
            return;
        }

        if (commandContext.getArgument("player", String.class) == null) {
            if (apiPlayerModAuthor.get().hasCible()) {
                apiPlayerModAuthor.get().setCible(null);
                TextComponentBuilder.createTextComponent("Vous n'avez plus de cible.").setColor(Color.RED)
                        .sendTo(((Player) sender).getUniqueId());

            } else
                TextComponentBuilder.createTextComponent("Syntax: /cible <pseudo>").setColor(Color.RED)
                        .sendTo(player.getUniqueId());
        }

    }

    public void execute(CommandContext<CommandSource> commandContext, String s) {
        CommandSource sender = commandContext.getSource();

        if (!(sender instanceof Player player)) return;

        Optional<APIPlayerModerator> apiPlayerModerator = CoreAPI.getInstance().getModeratorManager().getModerator(((Player) sender).getUniqueId());

        if (apiPlayerModerator.isEmpty() || !apiPlayerModerator.get().isModeratorMod()) {

            TextComponentBuilder.createTextComponent("Commande accessible uniquement en mod moderation").setColor(Color.RED)
                    .sendTo(player.getUniqueId());
            return;

        }

        String target = commandContext.getArgument("player", String.class);
        Optional<APIOfflinePlayer> playerTarget = CoreAPI.getInstance().getPlayerManager().getOfflinePlayer(target);

        if (playerTarget.isEmpty()) {
            TextComponentBuilder.createTextComponent(
                            Color.RED +
                                    "Cette target ne s'est jamais connecté").setColor(Color.RED)
                    .sendTo(player.getUniqueId());
            return;
        }

        if (playerTarget.get().getRank().isModeratorRank()) {
            TextComponentBuilder.createTextComponent(
                            "Impossible de cibler " + target).setColor(Color.RED)
                    .sendTo(player.getUniqueId());
            return;
        }

        apiPlayerModerator.get().setCible(playerTarget.get().getName());
        TextComponentBuilder.createTextComponent(
                        "Nouvelle cible: " + playerTarget.get().getName()).setColor(Color.GREEN)
                .sendTo(player.getUniqueId());
    }
}