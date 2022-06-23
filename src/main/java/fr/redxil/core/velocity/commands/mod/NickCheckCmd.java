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
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.api.common.utils.Color;
import fr.redxil.api.common.utils.cmd.LiteralArgumentCreator;
import fr.redxil.core.common.CoreAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

import java.util.Optional;

public class NickCheckCmd extends LiteralArgumentCreator<CommandSource> {

    public NickCheckCmd() {
        super("nickcheck");
        super.setExecutor(this::onMissingArgument);
        super.createThen("target", StringArgumentType.word(), this::execute);
    }

    public void onMissingArgument(CommandContext<CommandSource> commandContext, String s) {
        commandContext.getSource().sendMessage(Component.text("Syntax: /nickcheck (joueur)").color(TextColor.color(Color.RED.getRed(), Color.RED.getGreen(), Color.RED.getBlue())));
    }

    public void execute(CommandContext<CommandSource> commandContext, String s) {
        if (!(commandContext.getSource() instanceof Player))
            return;

        Optional<APIPlayer> apiPlayer = CoreAPI.getInstance().getPlayerManager().getPlayer(((Player) commandContext.getSource()).getUniqueId());
        if (apiPlayer.isEmpty() || !apiPlayer.get().getRank().isModeratorRank())
            return;

        Optional<APIPlayer> targetPlayer = CoreAPI.getInstance().getPlayerManager().getPlayer(commandContext.getArgument("target", String.class));
        String tcb;
        if (targetPlayer.isPresent() && targetPlayer.get().isNick())
            tcb = Color.RED.getMOTD() + "Le vrai pseudo de cette personne: " + targetPlayer.get().getRealName();
        else
            tcb = Color.RED.getMOTD() + "Ceci n'est pas un nick";

        commandContext.getSource().sendMessage(Component.text(tcb));
    }
}
