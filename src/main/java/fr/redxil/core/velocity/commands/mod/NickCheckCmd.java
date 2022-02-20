/* Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
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
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.core.velocity.CoreVelocity;
import fr.redxil.core.velocity.commands.BrigadierAPI;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NickCheckCmd extends BrigadierAPI<CommandSource> {


    public NickCheckCmd() {
        super("nickcheck");
    }

    public void onMissingArgument(CommandContext<CommandSource> commandContext) {
        UUID playerUUID = ((Player) commandContext.getSource()).getUniqueId();
        TextComponentBuilder.createTextComponent(Color.RED + "Syntax: /nickcheck (joueur)").setColor(Color.RED).sendTo(playerUUID);
    }

    @Override
    public void onCommandWithoutArgs(CommandContext<CommandSource> commandExecutor) {
        this.onMissingArgument(commandExecutor);
    }

    public void execute(CommandContext<CommandSource> commandContext) {
        if (!(commandContext.getSource() instanceof Player))
            return;

        APIPlayer apiPlayer = API.getInstance().getPlayerManager().getPlayer(((Player) commandContext.getSource()).getUniqueId());
        if (!apiPlayer.getRank().isModeratorRank())
            return;

        APIPlayer targetPlayer = API.getInstance().getPlayerManager().getPlayer(commandContext.getArgument("target", String.class));
        TextComponentBuilder tcb;
        if (targetPlayer != null && targetPlayer.isNick())
            tcb = TextComponentBuilder.createTextComponent("Le vrai pseudo de cette personne: " + targetPlayer.getRealName());
        else
            tcb = TextComponentBuilder.createTextComponent("Ceci n'est pas un nick").setColor(Color.RED);

        tcb.sendTo(((Player) commandContext.getSource()).getUniqueId());

    }

    @Override
    public void registerArgs(LiteralCommandNode<CommandSource> literalCommandNode) {
        List<String> playerName = new ArrayList<>();

        for (Player player : CoreVelocity.getInstance().getProxyServer().getAllPlayers()) {
            playerName.add(player.getUsername());
        }

        this.addArgumentCommand(literalCommandNode, "target", StringArgumentType.word(), this::execute, playerName.toArray(new String[0]));

    }
}
