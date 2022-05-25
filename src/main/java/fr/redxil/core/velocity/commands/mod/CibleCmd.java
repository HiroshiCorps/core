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
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import fr.redxil.api.common.API;
import fr.redxil.api.common.message.Color;
import fr.redxil.api.common.message.TextComponentBuilder;
import fr.redxil.api.common.player.APIOfflinePlayer;
import fr.redxil.api.common.player.moderators.APIPlayerModerator;
import fr.redxil.core.velocity.commands.BrigadierAPI;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CibleCmd extends BrigadierAPI<CommandSource> {


    public CibleCmd() {
        super("nickcheck");
    }

    public void onMissingArgument(CommandContext<CommandSource> commandContext) {
        UUID playerUUID = ((Player) commandContext.getSource()).getUniqueId();
        TextComponentBuilder.createTextComponent("Syntax: /cible <pseudo>").setColor(Color.RED).sendTo(playerUUID);
    }

    @Override
    public void onCommandWithoutArgs(CommandContext<CommandSource> commandContext) {
        CommandSource sender = commandContext.getSource();

        if (!(sender instanceof Player player)) return;

        Optional<APIPlayerModerator> apiPlayerModAuthor = API.getInstance().getModeratorManager().getModerator(((Player) sender).getUniqueId());

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

    public void execute(CommandContext<CommandSource> commandContext) {
        CommandSource sender = commandContext.getSource();

        if (!(sender instanceof Player player)) return;

        Optional<APIPlayerModerator> apiPlayerModerator = API.getInstance().getModeratorManager().getModerator(((Player) sender).getUniqueId());

        if (apiPlayerModerator.isEmpty() || !apiPlayerModerator.get().isModeratorMod()) {

            TextComponentBuilder.createTextComponent("Commande accessible uniquement en mod moderation").setColor(Color.RED)
                    .sendTo(player.getUniqueId());
            return;

        }

        String target = commandContext.getArgument("player", String.class);
        Optional<APIOfflinePlayer> playerTarget = API.getInstance().getPlayerManager().getOfflinePlayer(target);

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

    @Override
    public void registerArgs(LiteralCommandNode<CommandSource> command) {

        List<String> playerName = new ArrayList<>();
        List<Long> availablePlayer = API.getInstance().getPlayerManager().getLoadedPlayer();
        availablePlayer.removeAll(API.getInstance().getModeratorManager().getLoadedModerator());
        for (Long id : availablePlayer)
            API.getInstance().getPlayerManager().getPlayer(id).ifPresent(player -> playerName.add(player.getName()));

        this.addArgumentCommand(command, "player", StringArgumentType.word(), this::execute, playerName.toArray(new String[0]));

    }
}