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
import fr.redxil.api.common.message.Color;
import fr.redxil.api.common.message.TextComponentBuilder;
import fr.redxil.api.common.message.TextComponentBuilderVelocity;
import fr.redxil.api.common.player.APIOfflinePlayer;
import fr.redxil.api.common.player.data.SanctionInfo;
import fr.redxil.api.common.player.moderators.APIPlayerModerator;
import fr.redxil.api.common.time.DateUtility;
import fr.redxil.core.common.CoreAPI;
import fr.redxil.core.velocity.CoreVelocity;
import fr.redxil.core.velocity.commands.BrigadierAPI;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class MuteCmd extends BrigadierAPI<CommandSource> {

    public MuteCmd() {
        super("mute");
    }

    public static void mutePlayer(APIOfflinePlayer apiPlayerTarget, String timeArgs, APIPlayerModerator apiPlayerModerator, String reason) {

        Optional<Player> playerOptional = CoreVelocity.getInstance().getProxyServer().getPlayer(apiPlayerModerator.getUUID());
        if (playerOptional.isEmpty()) return;
        Player proxiedPlayer = playerOptional.get();

        Optional<Timestamp> durationTime = DateUtility.toTimeStamp(timeArgs);

        if (durationTime.isEmpty() && !timeArgs.equals("perm")) {
            TextComponentBuilder.createTextComponent("Erreur: " + timeArgs + " n'est pas une durée valide").setColor(Color.RED)
                    .sendTo(proxiedPlayer.getUniqueId());
            return;
        }

        Timestamp end = durationTime.map(DateUtility::addToCurrentTimeStamp).orElse(null);

        String format = DateUtility.getMessage(end);

        Optional<SanctionInfo> sm = apiPlayerTarget.mutePlayer(reason, end, apiPlayerModerator);
        if (sm.isPresent()) {

            TextComponentBuilder banMessage = TextComponentBuilder.createTextComponent(
                    "Le modérateur §d" +
                            apiPlayerModerator.getName() +
                            " §7à mute l'utilisateur §a" +
                            apiPlayerTarget.getName() + " §7jusqu'au " +
                            format + " pour raison: "
                            + reason + ".");

            CoreAPI.getInstance().getModeratorManager().sendToModerators(banMessage);

            Optional<Player> onlinePlayerOptional = CoreVelocity.getInstance().getProxyServer().getPlayer(apiPlayerTarget.getName());

            onlinePlayerOptional.ifPresent(player -> player.disconnect(((TextComponentBuilderVelocity) sm.get().getSancMessage()).getFinalTextComponent()));

        } else
            TextComponentBuilder.createTextComponent("Désolé, une erreur est survenue").setColor(Color.RED)
                    .sendTo(proxiedPlayer.getUniqueId());


    }

    public void onMissingArgument(CommandContext<CommandSource> commandContext) {
        UUID playerUUID = ((Player) commandContext.getSource()).getUniqueId();
        TextComponentBuilder.createTextComponent("Syntax: /mute <pseudo> <temps> <raison>").setColor(Color.RED).sendTo(playerUUID);
    }

    @Override
    public void onCommandWithoutArgs(CommandContext<CommandSource> commandExecutor) {
        this.onMissingArgument(commandExecutor);
    }

    public void execute(CommandContext<CommandSource> commandContext) {
        if (!(commandContext.getSource() instanceof Player player)) return;

        Optional<APIPlayerModerator> apiPlayerModerator = CoreAPI.getInstance().getModeratorManager().getModerator(player.getUniqueId());

        if (apiPlayerModerator.isEmpty()) {
            TextComponentBuilder.createTextComponent("Vous n'avez pas la permission d'effectuer cette commande.").setColor(Color.RED)
                    .sendTo(player.getUniqueId());
            return;
        }

        String targetArgs = commandContext.getArgument("target", String.class);
        Optional<APIOfflinePlayer> apiPlayerTarget = CoreAPI.getInstance().getPlayerManager().getOfflinePlayer(targetArgs);
        if (apiPlayerTarget.isEmpty()) {
            TextComponentBuilder.createTextComponent("La target ne s'est jamais connecté.").setColor(Color.RED)
                    .sendTo(player.getUniqueId());
            return;
        }

        if (apiPlayerTarget.get().getRank().isModeratorRank()) {
            TextComponentBuilder.createTextComponent("Vous n'avez pas la permission d'effectuer cette commande.").setColor(Color.RED)
                    .sendTo(player.getUniqueId());
            return;
        }

        if (apiPlayerTarget.get().isMute()) {
            TextComponentBuilder.createTextComponent("Erreur, le joueur est déjà mute.").setColor(Color.RED)
                    .sendTo(player.getUniqueId());
            return;
        }

        String timeArgs = commandContext.getArgument("time", String.class);


        String reason = commandContext.getArgument("reason", String.class);

        if (reason.contains("{") || reason.contains("}")) {
            TextComponentBuilder.createTextComponent("Les caractéres { et } sont interdit d'utilisation dans les raisons").setColor(Color.RED)
                    .sendTo(player.getUniqueId());
            return;
        }

        mutePlayer(apiPlayerTarget.get(), timeArgs, apiPlayerModerator.get(), reason);

    }

    @Override
    public void registerArgs(LiteralCommandNode<CommandSource> literalCommandNode) {

        List<String> playerName = new ArrayList<>();

        for (Player player : CoreVelocity.getInstance().getProxyServer().getAllPlayers()) {
            playerName.add(player.getUsername());
        }

        CommandNode<CommandSource> targetNode = this.addArgumentCommand(literalCommandNode, "target", StringArgumentType.word(), this::onMissingArgument, playerName.toArray(new String[0]));
        CommandNode<CommandSource> timeNode = this.addArgumentCommand(targetNode, "time", StringArgumentType.word(), this::onMissingArgument, "perm", "0s", "0h", "0j", "0m");
        this.addArgumentCommand(timeNode, "reason", StringArgumentType.greedyString(), this::execute);
    }
}
