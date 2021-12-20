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
import fr.redxil.api.common.player.APIOfflinePlayer;
import fr.redxil.api.common.player.data.SanctionInfo;
import fr.redxil.api.common.player.moderators.APIPlayerModerator;
import fr.redxil.api.common.time.DateUtility;
import fr.redxil.api.velocity.Velocity;
import fr.redxil.core.velocity.CoreVelocity;
import fr.redxil.core.velocity.commands.BrigadierAPI;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BanCmd extends BrigadierAPI {

    public BanCmd() {
        super("ban");
    }

    public static void banPlayer(APIOfflinePlayer apiPlayerTarget, String timeArgs, APIPlayerModerator APIPlayerModAuthor, String reason) {
        long durationTime = DateUtility.toTimeStamp(timeArgs);

        Optional<Player> playerOptional = Velocity.getInstance().getProxyServer().getPlayer(APIPlayerModAuthor.getUUID());
        if (!playerOptional.isPresent()) return;
        Player proxiedPlayer = playerOptional.get();

        long end = DateUtility.addToCurrentTimeStamp(durationTime);

        String format = DateUtility.getMessage(end);

        if (durationTime != -2L) {

            SanctionInfo sm = apiPlayerTarget.banPlayer(reason, end, APIPlayerModAuthor);
            if (sm != null) {

                TextComponentBuilder banMessage = TextComponentBuilder.createTextComponent(
                        "Le modérateur §d" +
                                APIPlayerModAuthor.getName() +
                                " §7à ban l'utilisateur §a" +
                                apiPlayerTarget.getName() + " §7jusqu'au " +
                                format + " pour raison: "
                                + reason + ".");

                API.getInstance().getModeratorManager().sendToModerators(banMessage);

                Optional<Player> onlinePlayerOptional = Velocity.getInstance().getProxyServer().getPlayer(apiPlayerTarget.getName());

                onlinePlayerOptional.ifPresent(player -> player.disconnect(((TextComponentBuilderVelocity) sm.getSancMessage()).getFinalTextComponent()));

            } else
                TextComponentBuilder.createTextComponent("Désolé, une erreur est survenue").setColor(Color.RED)
                        .sendTo(proxiedPlayer.getUniqueId());

        } else {
            TextComponentBuilder.createTextComponent("Erreur: " + timeArgs + " n'est pas une durée valide").setColor(Color.RED)
                    .sendTo(proxiedPlayer.getUniqueId());
        }

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

        if (commandContext.getArguments().size() < 3) {
            TextComponentBuilder.createTextComponent("Syntax: /ban <pseudo> <temps> <raison>").setColor(Color.RED)
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

        if (apiPlayerTarget.getRank().isModeratorRank()) {
            TextComponentBuilder.createTextComponent("Vous n'avez pas la permission d'effectuer cette commande.").setColor(Color.RED)
                    .sendTo(player.getUniqueId());
            return 1;
        }

        if (apiPlayerTarget.isBan()) {
            TextComponentBuilder.createTextComponent("Erreur, le joueur est déjà ban.").setColor(Color.RED)
                    .sendTo(player.getUniqueId());
            return 1;
        }

        String timeArgs = commandContext.getArgument("time", String.class);

        String reason = commandContext.getArgument("reason", String.class);

        if (reason.contains("{") || reason.contains("}")) {
            TextComponentBuilder.createTextComponent("Les caractéres { et } sont interdit d'utilisation dans les raisons").setColor(Color.RED)
                    .sendTo(player.getUniqueId());
            return 1;
        }

        banPlayer(apiPlayerTarget, timeArgs, APIPlayerModAuthor, reason);
        return 1;
    }

    @Override
    public void registerArgs(LiteralCommandNode<CommandSource> literalCommandNode) {

        List<String> playerName = new ArrayList<>();

        for (Player player : CoreVelocity.getInstance().getProxyServer().getAllPlayers()) {
            playerName.add(player.getUsername());
        }

        this.addArgumentCommand(literalCommandNode, "target", StringArgumentType.word(), playerName.toArray(new String[0]));
        this.addArgumentCommand(literalCommandNode, "time", StringArgumentType.word(), "perm", "0s", "0h", "0j", "0m");
        this.addArgumentCommand(literalCommandNode, "reason", StringArgumentType.string());
    }
}
