/* Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 */

package fr.redxil.core.velocity.commands.mod.action.cancel;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import fr.redxil.api.common.API;
import fr.redxil.api.common.message.Color;
import fr.redxil.api.common.message.TextComponentBuilder;
import fr.redxil.api.common.player.APIOfflinePlayer;
import fr.redxil.api.common.player.moderators.APIPlayerModerator;
import fr.redxil.api.velocity.BrigadierAPI;
import fr.redxil.core.velocity.CoreVelocity;

import java.util.ArrayList;
import java.util.List;

public class UnBanCmd extends BrigadierAPI {


    public UnBanCmd() {
        super("unban");
    }

    @Override
    public int execute(CommandContext<CommandSource> commandContext) {
        if (!(commandContext.getSource() instanceof Player)) return 1;

        Player player = (Player) commandContext.getSource();
        APIPlayerModerator APIPlayerModerator = API.getInstance().getModeratorManager().getModerator(player.getUniqueId());

        if (APIPlayerModerator == null) {
            TextComponentBuilder.createTextComponent("Vous n'avez pas la permission d'effectuer cette commande.").setColor(Color.RED)
                    .sendTo(player.getUniqueId());
            return 1;
        }

        if (commandContext.getArguments().size() < 1) {
            TextComponentBuilder.createTextComponent("Syntax: /unban <pseudo>").setColor(Color.RED)
                    .sendTo(player.getUniqueId());
            return 1;
        }

        String targetArgs = commandContext.getArgument("target", String.class);
        APIOfflinePlayer apiPlayerTarget = API.getInstance().getPlayerManager().getOfflinePlayer(targetArgs);

        if (apiPlayerTarget == null) {
            TextComponentBuilder.createTextComponent("La target ne s'est jamais connecté.").setColor(Color.RED)
                    .sendTo(player.getUniqueId());
            return 1;
        }

        if (apiPlayerTarget.unBan(APIPlayerModerator)) {
            TextComponentBuilder.createTextComponent("La personne se nommant: " + apiPlayerTarget.getName() + " est maintenant unBan.").setColor(Color.GREEN)
                    .sendTo(player.getUniqueId());
        } else {
            TextComponentBuilder.createTextComponent("Impossible de unban: " + apiPlayerTarget.getName()).setColor(Color.RED)
                    .sendTo(player.getUniqueId());
        }

        return 1;
    }

    @Override
    public void registerArgs(LiteralCommandNode<CommandSource> literalCommandNode) {

        List<String> playerName = new ArrayList<>();

        for (Player player : CoreVelocity.getInstance().getProxyServer().getAllPlayers()) {

            playerName.add(player.getUsername());

        }

        this.addArgumentCommand(literalCommandNode, "target", StringArgumentType.word(), playerName.toArray(new String[0]));

    }
}
