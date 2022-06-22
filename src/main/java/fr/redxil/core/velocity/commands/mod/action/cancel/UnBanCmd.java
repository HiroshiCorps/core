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
import fr.redxil.api.common.message.Color;
import fr.redxil.api.common.message.TextComponentBuilder;
import fr.redxil.api.common.player.APIOfflinePlayer;
import fr.redxil.api.common.player.moderators.APIPlayerModerator;
import fr.redxil.api.common.utils.cmd.LiteralArgumentCreator;
import fr.redxil.core.common.CoreAPI;

import java.util.Optional;
import java.util.UUID;

public class UnBanCmd extends LiteralArgumentCreator<CommandSource> {

    public UnBanCmd() {
        super("unban");
        super.setExecutor(this::onMissingArgument);
        super.createThen("target", StringArgumentType.word(), this::execute);
    }

    public void onMissingArgument(CommandContext<CommandSource> commandContext, String s) {
        UUID playerUUID = ((Player) commandContext.getSource()).getUniqueId();
        TextComponentBuilder.createTextComponent("Syntax: /unban <pseudo>").setColor(Color.RED).sendTo(playerUUID);
    }

    public void execute(CommandContext<CommandSource> commandContext, String s) {
        if (!(commandContext.getSource() instanceof Player player)) return;

        Optional<APIPlayerModerator> apiPlayerModerator = CoreAPI.getInstance().getModeratorManager().getModerator(player.getUniqueId());

        if (apiPlayerModerator.isEmpty()) {
            TextComponentBuilder.createTextComponent("Vous n'avez pas la permission d'effectuer cette commande.").setColor(Color.RED)
                    .sendTo(player.getUniqueId());
            return;
        }

        String targetArgs = commandContext.getArgument("target", String.class);
        Optional<APIOfflinePlayer> apiPlayerTarget = CoreAPI.getInstance().getPlayerManager().getOfflinePlayer(targetArgs);

        if (apiPlayerTarget.isEmpty()) {
            TextComponentBuilder.createTextComponent("La target ne s'est jamais connecté.").setColor(Color.RED)
                    .sendTo(player.getUniqueId());
            return;
        }

        if (apiPlayerTarget.get().unBan(apiPlayerModerator.get())) {
            TextComponentBuilder.createTextComponent("La personne se nommant: " + apiPlayerTarget.get().getName() + " est maintenant unBan.").setColor(Color.GREEN)
                    .sendTo(player.getUniqueId());
        } else {
            TextComponentBuilder.createTextComponent("Impossible de unban: " + apiPlayerTarget.get().getName()).setColor(Color.RED)
                    .sendTo(player.getUniqueId());
        }
    }

}
