/* Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 */

package fr.redxil.core.bungee.commands.mod;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.Command;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import fr.redxil.api.common.message.Color;
import fr.redxil.api.common.message.TextComponentBuilder;
import fr.redxil.api.common.player.APIOfflinePlayer;
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.api.velocity.BrigadierAPI;
import fr.redxil.core.bungee.CoreVelocity;
import fr.redxil.core.common.CoreAPI;

import java.util.ArrayList;
import java.util.List;

public class NickCheckCmd extends BrigadierAPI {


    public NickCheckCmd() {
        super("nickcheck");
    }

    @Override
    public int execute(CommandContext<CommandSource> commandContext) {
        if (!(commandContext.getSource() instanceof Player))
            return 1;

        APIPlayer apiPlayer = CoreAPI.get().getPlayerManager().getPlayer(((Player) commandContext.getSource()).getUniqueId());
        if (!apiPlayer.getRank().isModeratorRank())
            return 1;

        APIOfflinePlayer targetPlayer = CoreAPI.get().getNickGestion().getAPIOfflinePlayer(commandContext.getArgument("target", String.class));
        TextComponentBuilder tcb;
        if (targetPlayer != null)
            tcb = TextComponentBuilder.createTextComponent("Le vrai pseudo de cette personne: " + targetPlayer.getName());
        else
            tcb = TextComponentBuilder.createTextComponent("Ceci n'est pas un nick").setColor(Color.RED);

        tcb.sendTo(((Player) commandContext.getSource()).getUniqueId());

        return 1;
    }

    @Override
    public void registerArgs(LiteralCommandNode<CommandSource> literalCommandNode) {
        List<String> playerName = new ArrayList<>();

        for(Player player : CoreVelocity.getInstance().getProxyServer().getAllPlayers()){
            playerName.add(player.getUsername());
        }

        this.addArgumentCommand(literalCommandNode, "target", StringArgumentType.word(), playerName.toArray(new String[0]));

    }
}
