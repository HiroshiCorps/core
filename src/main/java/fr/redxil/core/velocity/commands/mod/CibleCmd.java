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
import fr.redxil.api.common.player.APIOfflinePlayer;
import fr.redxil.api.common.player.moderators.APIPlayerModerator;
import fr.redxil.api.common.utils.Color;
import fr.redxil.api.common.utils.cmd.LiteralArgumentCreator;
import fr.redxil.core.common.CoreAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

import java.util.Optional;

public class CibleCmd extends LiteralArgumentCreator<CommandSource> {

    public CibleCmd() {
        super("nickcheck");
        super.setExecutor(this::onCommandWithoutArgs);
        super.createArgument("player", StringArgumentType.word(), this::execute);
    }

    public void onCommandWithoutArgs(CommandContext<CommandSource> commandContext, String s) {
        CommandSource sender = commandContext.getSource();

        if (!(sender instanceof Player)) return;

        Optional<APIPlayerModerator> apiPlayerModAuthor = CoreAPI.getInstance().getModeratorManager().getModerator(((Player) sender).getUniqueId());

        if (apiPlayerModAuthor.isEmpty()) {
            commandContext.getSource().sendMessage(Component.text("Vous n'avez pas la permission d'effectuer cette commande.").color(TextColor.color(Color.RED.getRed(), Color.RED.getGreen(), Color.RED.getBlue())));
            return;
        }

        if (commandContext.getArgument("player", String.class) == null) {
            if (apiPlayerModAuthor.get().hasCible()) {
                apiPlayerModAuthor.get().setCible(null);
                commandContext.getSource().sendMessage(Component.text("Vous n'avez plus de cible.").color(TextColor.color(Color.GREEN.getRed(), Color.GREEN.getGreen(), Color.GREEN.getBlue())));
            } else
                commandContext.getSource().sendMessage(Component.text("Syntax: /cible <pseudo>").color(TextColor.color(Color.RED.getRed(), Color.RED.getGreen(), Color.RED.getBlue())));
        }

    }

    public void execute(CommandContext<CommandSource> commandContext, String s) {
        CommandSource sender = commandContext.getSource();

        if (!(sender instanceof Player player)) return;

        Optional<APIPlayerModerator> apiPlayerModerator = CoreAPI.getInstance().getModeratorManager().getModerator(((Player) sender).getUniqueId());

        if (apiPlayerModerator.isEmpty() || !apiPlayerModerator.get().isModeratorMod()) {
            commandContext.getSource().sendMessage(Component.text("Commande accessible uniquement en mod moderation").color(TextColor.color(Color.RED.getRed(), Color.RED.getGreen(), Color.RED.getBlue())));
            return;
        }

        String target = commandContext.getArgument("player", String.class);
        Optional<APIOfflinePlayer> playerTarget = CoreAPI.getInstance().getPlayerManager().getOfflinePlayer(target);

        if (playerTarget.isEmpty()) {
            commandContext.getSource().sendMessage(Component.text("Cette target ne s'est jamais connecté").color(TextColor.color(Color.RED.getRed(), Color.RED.getGreen(), Color.RED.getBlue())));
            return;
        }

        if (playerTarget.get().getRank().isModeratorRank()) {
            commandContext.getSource().sendMessage(Component.text("Impossible de cibler " + target).color(TextColor.color(Color.RED.getRed(), Color.RED.getGreen(), Color.RED.getBlue())));
            return;
        }

        apiPlayerModerator.get().setCible(playerTarget.get().getName());
        commandContext.getSource().sendMessage(Component.text("Nouvelle cible: " + playerTarget.get().getName()).color(TextColor.color(Color.GREEN.getRed(), Color.GREEN.getGreen(), Color.GREEN.getBlue())));

    }
}