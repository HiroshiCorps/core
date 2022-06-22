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
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.api.common.utils.cmd.LiteralArgumentCreator;
import fr.redxil.core.common.CoreAPI;

import java.util.Optional;
import java.util.UUID;

public class NickCheckCmd extends LiteralArgumentCreator<CommandSource> {

    public NickCheckCmd() {
        super("nickcheck");
        super.setExecutor(this::onMissingArgument);
        super.createThen("target", StringArgumentType.word(), this::execute);
    }

    public void onMissingArgument(CommandContext<CommandSource> commandContext, String s) {
        UUID playerUUID = ((Player) commandContext.getSource()).getUniqueId();
        TextComponentBuilder.createTextComponent("Syntax: /nickcheck (joueur)").setColor(Color.RED).sendTo(playerUUID);
    }

    public void execute(CommandContext<CommandSource> commandContext, String s) {
        if (!(commandContext.getSource() instanceof Player))
            return;

        Optional<APIPlayer> apiPlayer = CoreAPI.getInstance().getPlayerManager().getPlayer(((Player) commandContext.getSource()).getUniqueId());
        if (apiPlayer.isEmpty() || !apiPlayer.get().getRank().isModeratorRank())
            return;

        Optional<APIPlayer> targetPlayer = CoreAPI.getInstance().getPlayerManager().getPlayer(commandContext.getArgument("target", String.class));
        TextComponentBuilder tcb;
        if (targetPlayer.isPresent() && targetPlayer.get().isNick())
            tcb = TextComponentBuilder.createTextComponent("Le vrai pseudo de cette personne: " + targetPlayer.get().getRealName());
        else
            tcb = TextComponentBuilder.createTextComponent("Ceci n'est pas un nick").setColor(Color.RED);

        tcb.sendTo(((Player) commandContext.getSource()).getUniqueId());
    }
}
