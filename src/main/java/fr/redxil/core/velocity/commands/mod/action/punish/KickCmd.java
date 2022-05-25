/*
 *
 * Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 *
 */

package fr.redxil.core.velocity.commands.mod.action.punish;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.CommandNode;
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
import fr.redxil.core.velocity.CoreVelocity;
import fr.redxil.core.velocity.commands.BrigadierAPI;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class KickCmd extends BrigadierAPI<CommandSource> {

    public KickCmd() {
        super("kick");
    }

    public void onMissingArgument(CommandContext<CommandSource> commandContext) {
        UUID playerUUID = ((Player) commandContext.getSource()).getUniqueId();
        TextComponentBuilder.createTextComponent("Syntax: /kick <pseudo> <raison>").setColor(Color.RED).sendTo(playerUUID);
    }

    @Override
    public void onCommandWithoutArgs(CommandContext<CommandSource> commandExecutor) {
        this.onMissingArgument(commandExecutor);
    }

    public void execute(CommandContext<CommandSource> commandContext) {
        if (!(commandContext.getSource() instanceof Player player)) return;

        Optional<APIPlayerModerator> apiPlayerModerator = API.getInstance().getModeratorManager().getModerator(player.getUniqueId());

        if (apiPlayerModerator.isEmpty()) {
            TextComponentBuilder.createTextComponent("Vous n'avez pas la permission d'effectuer cette commande.").setColor(Color.RED)
                    .sendTo(player.getUniqueId());
            return;
        }

        String targetArgs = commandContext.getArgument("target", String.class);
        Optional<APIPlayer> apiPlayerTarget = API.getInstance().getPlayerManager().getPlayer(targetArgs);
        if (apiPlayerTarget.isEmpty()) {
            TextComponentBuilder.createTextComponent("Erreur: Le joueur: " + targetArgs + " n'a pas était trouvé").setColor(Color.RED)
                    .sendTo(player.getUniqueId());
            return;
        }

        if (apiPlayerTarget.get().getRank().isModeratorRank()) {
            TextComponentBuilder.createTextComponent("Erreur vous n'avez pas la permission.").setColor(Color.RED)
                    .sendTo(player.getUniqueId());
            return;
        }

        String reason = commandContext.getArgument("reason", String.class);

        if (reason.contains("{") || reason.contains("}")) {
            TextComponentBuilder.createTextComponent("Les caractéres { et } sont interdit d'utilisation dans les raisons").setColor(Color.RED)
                    .sendTo(player.getUniqueId());
            return;
        }

        Optional<SanctionInfo> sm = apiPlayerTarget.get().kickPlayer(reason, apiPlayerModerator.get());
        if (sm.isPresent()) {
            TextComponentBuilder.createTextComponent("Le joueur: " + apiPlayerTarget.get().getName() + " à été kick.")
                    .sendTo(player.getUniqueId());
            Optional<Player> proxiedPlayer = CoreVelocity.getInstance().getProxyServer().getPlayer(apiPlayerTarget.get().getName());
            proxiedPlayer.ifPresent((player2) -> player2.disconnect(((TextComponentBuilderVelocity) sm.get().getSancMessage()).getFinalTextComponent()));
        } else
            TextComponentBuilder.createTextComponent("Désolé, une erreur est survenue").setColor(Color.RED)
                    .sendTo(player.getUniqueId());

    }

    @Override
    public void registerArgs(LiteralCommandNode<CommandSource> literalCommandNode) {

        List<String> playerName = new ArrayList<>();

        for (Player player : CoreVelocity.getInstance().getProxyServer().getAllPlayers()) {
            playerName.add(player.getUsername());
        }

        CommandNode<CommandSource> target = this.addArgumentCommand(literalCommandNode, "target", StringArgumentType.word(), this::onMissingArgument, playerName.toArray(new String[0]));
        this.addArgumentCommand(target, "reason", StringArgumentType.greedyString(), this::execute);
    }
}
