/* Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 */

package fr.redxil.core.bungee.commands.mod;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.Command;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import fr.redxil.api.common.message.Color;
import fr.redxil.api.common.message.TextComponentBuilder;
import fr.redxil.api.common.player.APIOfflinePlayer;
import fr.redxil.api.common.player.moderators.APIPlayerModerator;
import fr.redxil.core.common.CoreAPI;

public class CibleCmd implements Command {

    public BrigadierCommand getCommand() {

        LiteralCommandNode<CommandSource> lcn2 = LiteralArgumentBuilder.<CommandSource>literal("player")
                .executes(commandContext -> {
                    if (!(commandContext.getSource() instanceof Player))
                        return 0;
                    APIPlayerModerator apiPlayerModerator = CoreAPI.get().getModeratorManager().getModerator(((Player) commandContext.getSource()).getUniqueId());
                    if (apiPlayerModerator == null)
                        return 0;

                    if (!apiPlayerModerator.isModeratorMod()) {

                        TextComponentBuilder.createTextComponent("Commande accessible uniquement en mod moderation").setColor(Color.RED)
                                .sendTo(apiPlayerModerator.getUUID());
                        return 1;

                    }

                    String target = commandContext.getArgument("player", String.class);
                    APIOfflinePlayer playerTarget = CoreAPI.get().getPlayerManager().getOfflinePlayer(target);

                    if (playerTarget == null) {
                        TextComponentBuilder.createTextComponent(
                                        Color.RED +
                                                "Cette target ne s'est jamais connecté").setColor(Color.RED)
                                .sendTo(apiPlayerModerator.getUUID());
                        return 1;
                    }

                    if (playerTarget.getRank().isModeratorRank()) {
                        TextComponentBuilder.createTextComponent(
                                        "Impossible de cibler " + target).setColor(Color.RED)
                                .sendTo(apiPlayerModerator.getUUID());
                        return 1;
                    }

                    apiPlayerModerator.setCible(playerTarget.getName());
                    TextComponentBuilder.createTextComponent(
                                    "Nouvelle cible: " + playerTarget.getName()).setColor(Color.GREEN)
                            .sendTo(apiPlayerModerator.getUUID());

                    return 1;
                })
                .build();

        LiteralCommandNode<CommandSource> lcn = LiteralArgumentBuilder.<CommandSource>literal("nickcheck")
                .executes(commandContext -> {
                    if (!(commandContext.getSource() instanceof Player))
                        return 0;
                    APIPlayerModerator apiPlayerModerator = CoreAPI.get().getModeratorManager().getModerator(((Player) commandContext.getSource()).getUniqueId());
                    if (apiPlayerModerator == null)
                        return 0;
                    if (apiPlayerModerator.hasCible()) {
                        apiPlayerModerator.setCible(null);
                        TextComponentBuilder.createTextComponent("Vous n'avez plus de cible.").setColor(Color.RED)
                                .sendTo(((Player) commandContext.getSource()).getUniqueId());

                    } else
                        TextComponentBuilder.createTextComponent("Syntax: /cible <pseudo>").setColor(Color.RED)
                                .sendTo(((Player) commandContext.getSource()).getUniqueId());
                    return 1;
                })
                .build();

        lcn.addChild(lcn2);

        return new BrigadierCommand(lcn);

    }
}
