/*
 *
 * Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 *
 */

package fr.redxil.core.velocity.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import fr.redxil.api.common.message.Color;
import fr.redxil.api.common.message.TextComponentBuilder;
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.api.common.player.rank.Rank;
import fr.redxil.api.common.server.Server;
import fr.redxil.api.common.utils.cmd.LiteralArgumentCreator;
import fr.redxil.core.common.CoreAPI;

import java.util.Optional;
import java.util.UUID;

public class ShutdownCmd extends LiteralArgumentCreator<CommandSource> {


    public ShutdownCmd() {
        super("shutdown");
        super.setExecutor(this::onMissingArgument);
        super.createThen("server", StringArgumentType.word(), this::execute);
    }

    public void onMissingArgument(CommandContext<CommandSource> commandContext, String s) {
        UUID playerUUID = ((Player) commandContext.getSource()).getUniqueId();
        TextComponentBuilder.createTextComponent("Erreur, merci de faire /shutdown (server)").setColor(Color.RED).sendTo(playerUUID);
    }

    public int execute(CommandContext<CommandSource> commandContext, String s) {
        if (!(commandContext.getSource() instanceof Player)) return 1;
        Optional<APIPlayer> apiPlayer = CoreAPI.getInstance().getPlayerManager().getPlayer(((Player) commandContext.getSource()).getUniqueId());
        if (apiPlayer.isEmpty())
            return 1;
        if (!apiPlayer.get().hasPermission(Rank.DEVELOPPEUR.getRankPower())) {
            return 1;
        }

        Optional<Server> server = CoreAPI.getInstance().getServerManager().getServer(commandContext.getArgument("server", String.class));
        if (server.isEmpty()) {
            TextComponentBuilder.createTextComponent("Erreur, le server exists pas").setColor(Color.RED).sendTo(apiPlayer.get());
            return 1;
        }

        ///server.sendShutdownOrder();
        TextComponentBuilder.createTextComponent("L'ordre de shutdown est désactivé").setColor(Color.GREEN).sendTo(apiPlayer.get());
        return 1;
    }
}
