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
import fr.redxil.api.common.utils.SanctionType;
import fr.redxil.core.common.CoreAPI;

public class InfoCmd implements Command {

    public BrigadierCommand getCommand() {

        LiteralCommandNode<CommandSource> lcnSancName = LiteralArgumentBuilder.<CommandSource>literal("sanc")
                .executes(commandContext -> {

                    if (!(commandContext.getSource() instanceof Player))
                        return 0;

                    APIPlayerModerator playerModerator = CoreAPI.get().getModeratorManager().getModerator(((Player) commandContext.getSource()).getUniqueId());
                    if (playerModerator == null)
                        return 0;

                    SanctionType sanctionType = SanctionType.getSanctionType(commandContext.getArgument("sanc", String.class));
                    if (sanctionType == null) {
                        TextComponentBuilder.createTextComponent("Le type de sanction: " + commandContext.getArgument("sanc", String.class) + "n'a pas était reconnue").setColor(Color.RED);
                        return 0;
                    }

                    APIOfflinePlayer target = CoreAPI.get().getPlayerManager().getOfflinePlayer(commandContext.getArgument("player", String.class));
                    playerModerator.printSanction(target, sanctionType);

                    return 1;

                })
                .build();

        LiteralCommandNode<CommandSource> lcnPlayer = LiteralArgumentBuilder.<CommandSource>literal("player")
                .executes(commandContext -> {

                    if (!(commandContext.getSource() instanceof Player))
                        return 0;

                    APIPlayerModerator playerModerator = CoreAPI.get().getModeratorManager().getModerator(((Player) commandContext.getSource()).getUniqueId());
                    if (playerModerator == null)
                        return 0;

                    APIOfflinePlayer target = CoreAPI.get().getPlayerManager().getOfflinePlayer(commandContext.getArgument("player", String.class));
                    playerModerator.printInfo(target);

                    return 1;

                })
                .build();

        lcnPlayer.addChild(lcnSancName);

        LiteralCommandNode<CommandSource> lcn = LiteralArgumentBuilder.<CommandSource>literal("info").then(lcnPlayer).build();

        return new BrigadierCommand(lcn);

    }

}