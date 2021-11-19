/* Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 */

package fr.redxil.core.velocity.commands.mod.action.punish;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import fr.redxil.api.common.API;
import fr.redxil.api.common.message.Color;
import fr.redxil.api.common.message.TextComponentBuilder;
import fr.redxil.api.common.message.TextComponentBuilderVelocity;
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.api.common.player.data.SanctionInfo;
import fr.redxil.api.common.player.moderators.APIPlayerModerator;
import fr.redxil.api.velocity.Velocity;
import fr.redxil.core.velocity.CoreVelocity;
import fr.redxil.core.velocity.commands.BrigadierAPI;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class KickCmd extends BrigadierAPI {

    public KickCmd() {
        super("kick");
    }


    @Override
    public int execute(CommandContext<CommandSource> commandContext) {
        if (!(commandContext.getSource() instanceof Player)) return 1;

        Player player = (Player) commandContext.getSource();
        APIPlayerModerator APIPlayerModAuthor = API.getInstance().getModeratorManager().getModerator(player.getUniqueId());

        if (APIPlayerModAuthor == null) {
            TextComponentBuilder.createTextComponent("Vous n'avez pas la permission d'effectuer cette commande.").setColor(Color.RED)
                    .sendTo(player.getUniqueId());
            return 1;
        }

        if (commandContext.getArguments().size() < 2) {
            TextComponentBuilder.createTextComponent("Syntax: /kick <pseudo> <raison>").setColor(Color.RED)
                    .sendTo(player.getUniqueId());
            return 1;
        }

        String targetArgs = commandContext.getArgument("target", String.class);
        APIPlayer apiPlayerTarget = API.getInstance().getPlayerManager().getPlayer(targetArgs);
        if (apiPlayerTarget == null) {
            TextComponentBuilder.createTextComponent("Erreur: Le joueur: " + targetArgs + " n'a pas était trouvé").setColor(Color.RED)
                    .sendTo(player.getUniqueId());
            return 1;
        }

        if (apiPlayerTarget.getRank().isModeratorRank()) {
            TextComponentBuilder.createTextComponent("Erreur vous n'avez pas la permission.").setColor(Color.RED)
                    .sendTo(player.getUniqueId());
            return 1;
        }

        String reason = commandContext.getArgument("reason", String.class);

        if (reason.contains("{") || reason.contains("}")) {
            TextComponentBuilder.createTextComponent("Les caractéres { et } sont interdit d'utilisation dans les raisons").setColor(Color.RED)
                    .sendTo(player.getUniqueId());
            return 1;
        }

        SanctionInfo sm = apiPlayerTarget.kickPlayer(reason, APIPlayerModAuthor);
        if (sm != null) {
            TextComponentBuilder.createTextComponent("Le joueur: " + apiPlayerTarget.getName() + " à été kick.")
                    .sendTo(player.getUniqueId());
            Optional<Player> proxiedPlayer = Velocity.getInstance().getProxyServer().getPlayer(apiPlayerTarget.getName());
            proxiedPlayer.ifPresent((player2) -> player2.disconnect(((TextComponentBuilderVelocity) sm.getSancMessage()).getFinalTextComponent()));
        } else
            TextComponentBuilder.createTextComponent("Désolé, une erreur est survenue").setColor(Color.RED)
                    .sendTo(player.getUniqueId());

        return 1;
    }

    @Override
    public void registerArgs(LiteralCommandNode<CommandSource> literalCommandNode) {

        List<String> playerName = new ArrayList<>();

        for (Player player : CoreVelocity.getInstance().getProxyServer().getAllPlayers()) {
            playerName.add(player.getUsername());
        }

        this.addArgumentCommand(literalCommandNode, "target", StringArgumentType.word(), playerName.toArray(new String[0]));
        this.addArgumentCommand(literalCommandNode, "reason", StringArgumentType.string());
    }
}
