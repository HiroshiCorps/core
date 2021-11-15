/* Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 */

package fr.redxil.core.bungee.commands.mod;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.Command;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import fr.redxil.api.common.message.Color;
import fr.redxil.api.common.message.TextComponentBuilder;
import fr.redxil.api.common.player.APIOfflinePlayer;
import fr.redxil.api.common.player.moderators.APIPlayerModerator;
import fr.redxil.api.velocity.BrigadierAPI;
import fr.redxil.core.bungee.CoreVelocity;
import fr.redxil.core.common.CoreAPI;

import java.util.ArrayList;
import java.util.List;

public class CibleCmd extends BrigadierAPI {


    public CibleCmd() {
        super("nickcheck");
    }

    @Override
    public int execute(CommandContext<CommandSource> commandContext) {
        CommandSource sender = commandContext.getSource();

        if (!(sender instanceof Player)) return 0;

        Player player = (Player) sender;
        APIPlayerModerator APIPlayerModAuthor = CoreAPI.get().getModeratorManager().getModerator(((Player) sender).getUniqueId());

        if (APIPlayerModAuthor == null) {
            TextComponentBuilder.createTextComponent("Vous n'avez pas la permission d'effectuer cette commande.").setColor(Color.RED)
                    .sendTo(((Player) sender).getUniqueId());
            return 1;
        }

        if (commandContext.getArgument("player", String.class) == null) {
            if (APIPlayerModAuthor.hasCible()) {
                APIPlayerModAuthor.setCible(null);
                TextComponentBuilder.createTextComponent("Vous n'avez plus de cible.").setColor(Color.RED)
                        .sendTo(((Player) sender).getUniqueId());

            } else
                TextComponentBuilder.createTextComponent("Syntax: /cible <pseudo>").setColor(Color.RED)
                        .sendTo(player.getUniqueId());
            return 1;
        }

        if (!APIPlayerModAuthor.isModeratorMod()) {

            TextComponentBuilder.createTextComponent("Commande accessible uniquement en mod moderation").setColor(Color.RED)
                    .sendTo(player.getUniqueId());
            return 0;

        }

        String target = commandContext.getArgument("player", String.class);
        APIOfflinePlayer playerTarget = CoreAPI.get().getPlayerManager().getOfflinePlayer(target);

        if (playerTarget == null) {
            TextComponentBuilder.createTextComponent(
                            Color.RED +
                                    "Cette target ne s'est jamais connecté").setColor(Color.RED)
                    .sendTo(player.getUniqueId());
            return 1;
        }

        if (playerTarget.getRank().isModeratorRank()) {
            TextComponentBuilder.createTextComponent(
                            "Impossible de cibler " + target).setColor(Color.RED)
                    .sendTo(player.getUniqueId());
            return 0;
        }

        APIPlayerModAuthor.setCible(playerTarget.getName());
        TextComponentBuilder.createTextComponent(
                        "Nouvelle cible: " + playerTarget.getName()).setColor(Color.GREEN)
                .sendTo(player.getUniqueId());
        return 1;
    }

    @Override
    public void registerArgs(LiteralCommandNode<CommandSource> command) {

        List<String> playerName = new ArrayList<>();
        List<Long> availablePlayer = CoreAPI.get().getPlayerManager().getLoadedPlayer();
        availablePlayer.removeAll(CoreAPI.get().getModeratorManager().getLoadedModerator());
        for (Long id : availablePlayer)
            playerName.add(CoreAPI.get().getPlayerManager().getPlayer(id).getName());

        this.addArgumentCommand(command, "player", StringArgumentType.word(), (String[]) playerName.toArray());

    }
}