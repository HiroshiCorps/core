/* Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 */

package fr.redxil.core.velocity.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import fr.redxil.api.common.API;
import fr.redxil.api.common.message.Color;
import fr.redxil.api.common.message.TextComponentBuilder;
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.api.common.rank.RankList;
import fr.redxil.api.common.server.Server;

public class ShutdownCmd extends BrigadierAPI {


    public ShutdownCmd() {
        super("shutdown");
    }

    @Override
    public int execute(CommandContext<CommandSource> commandContext) {
        if (!(commandContext.getSource() instanceof Player)) return 1;
        APIPlayer apiPlayer = API.getInstance().getPlayerManager().getPlayer(((Player) commandContext.getSource()).getUniqueId());
        if (!apiPlayer.hasPermission(RankList.DEVELOPPEUR.getRankPower())) {
            return 1;
        }

        if (commandContext.getArguments().size() != 1) {
            TextComponentBuilder.createTextComponent("Erreur, merci de faire /shutdown (server)").setColor(Color.RED).sendTo(apiPlayer);
            return 1;
        }

        Server server = API.getInstance().getServerManager().getServer(commandContext.getArgument("server", String.class));
        if (server == null) {
            TextComponentBuilder.createTextComponent("Erreur, le server exists pas").setColor(Color.RED).sendTo(apiPlayer);
            return 1;
        }

        server.sendShutdownOrder();
        TextComponentBuilder.createTextComponent("L'ordre de shutdown est envoyé").setColor(Color.GREEN).sendTo(apiPlayer);
        return 1;
    }

    @Override
    public void registerArgs(LiteralCommandNode<CommandSource> literalCommandNode) {
        this.addArgumentCommand(literalCommandNode, "server", StringArgumentType.word());
    }
}
