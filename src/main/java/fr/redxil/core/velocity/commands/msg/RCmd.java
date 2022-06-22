/*
 *
 * Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 *
 */

package fr.redxil.core.velocity.commands.msg;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import fr.redxil.api.common.message.Color;
import fr.redxil.api.common.message.TextComponentBuilder;
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.api.common.utils.cmd.LiteralArgumentCreator;
import fr.redxil.core.common.CoreAPI;
import fr.redxil.core.velocity.CoreVelocity;

import java.util.Optional;
import java.util.UUID;

public class RCmd extends LiteralArgumentCreator<CommandSource> {

    public RCmd() {
        super("r");
        super.setExecutor(this::onMissingArgument);
        super.createThen("message", StringArgumentType.greedyString(), this::execute);
    }

    public void onMissingArgument(CommandContext<CommandSource> commandContext, String s) {
        UUID playerUUID = ((Player) commandContext.getSource()).getUniqueId();
        TextComponentBuilder.createTextComponent("Merci de faire /r (message)").setColor(Color.RED).sendTo(playerUUID);
    }

    public void execute(CommandContext<CommandSource> commandContext, String s) {
        if (!(commandContext.getSource() instanceof Player)) return;

        UUID playerUUID = ((Player) commandContext.getSource()).getUniqueId();
        Optional<APIPlayer> sp = CoreAPI.getInstance().getPlayerManager().getPlayer(playerUUID);

        if (sp.isEmpty())
            return;

        Optional<String> targetName = sp.get().getLastMSGPlayer();

        if (targetName.isEmpty()) {
            TextComponentBuilder.createTextComponent("Erreur, vous avez jusque la pas envoyé de message").setColor(Color.RED).sendTo(playerUUID);
            return;
        }

        CoreVelocity.getInstance().getProxyServer().getCommandManager().executeImmediatelyAsync(commandContext.getSource(), "/msg " + targetName.get() + " " + commandContext.getArgument("message", String.class));

    }
}
