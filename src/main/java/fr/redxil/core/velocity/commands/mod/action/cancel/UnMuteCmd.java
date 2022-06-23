/*
 *
 * Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 *
 */

package fr.redxil.core.velocity.commands.mod.action.cancel;

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

public class UnMuteCmd extends LiteralArgumentCreator<CommandSource> {

    public UnMuteCmd() {
        super("unmute");
        super.setExecutor(this::onMissingArgument);
        super.createThen("target", StringArgumentType.word(), this::execute);
    }

    public void onMissingArgument(CommandContext<CommandSource> commandContext, String s) {
        commandContext.getSource().sendMessage(Component.text("Syntax: /unmute <pseudo>").color(TextColor.color(Color.RED.getRed(), Color.RED.getGreen(), Color.RED.getBlue())));
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
            commandContext.getSource().sendMessage(Component.text("La target ne s'est jamais connecté").color(TextColor.color(Color.RED.getRed(), Color.RED.getGreen(), Color.RED.getBlue())));
            return;
        }

        if (apiPlayerTarget.get().unMute(apiPlayerModerator.get()))
            commandContext.getSource().sendMessage(Component.text("La personne se nommant: " + apiPlayerTarget.get().getName() + " est maintenant unMute.").color(TextColor.color(Color.GREEN.getRed(), Color.GREEN.getGreen(), Color.GREEN.getBlue())));
        else
            commandContext.getSource().sendMessage(Component.text("Impossible de unmute: " + apiPlayerTarget.get().getName()).color(TextColor.color(Color.RED.getRed(), Color.RED.getGreen(), Color.RED.getBlue())));

    }
}
