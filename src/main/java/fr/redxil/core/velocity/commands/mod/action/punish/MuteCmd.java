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
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import fr.redxil.api.common.player.APIOfflinePlayer;
import fr.redxil.api.common.player.data.SanctionInfo;
import fr.redxil.api.common.player.moderators.APIPlayerModerator;
import fr.redxil.api.common.time.DateUtility;
import fr.redxil.api.common.utils.Color;
import fr.redxil.api.common.utils.cmd.LiteralArgumentCreator;
import fr.redxil.core.common.CoreAPI;
import fr.redxil.core.velocity.CoreVelocity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

import java.sql.Timestamp;
import java.util.Optional;

public class MuteCmd extends LiteralArgumentCreator<CommandSource> {

    public MuteCmd() {
        super("mute");
        super.setExecutor(this::onMissingArgument);
        super.createThen("target", StringArgumentType.word(), this::onMissingArgument)
                .createThen("time", StringArgumentType.word(), this::onMissingArgument);
        createThen("reason", StringArgumentType.greedyString(), this::execute);
    }

    public static void mutePlayer(APIOfflinePlayer apiPlayerTarget, String timeArgs, APIPlayerModerator apiPlayerModerator, String reason) {

        Optional<Player> playerOptional = CoreVelocity.getInstance().getProxyServer().getPlayer(apiPlayerModerator.getUUID());
        if (playerOptional.isEmpty()) return;
        Player proxiedPlayer = playerOptional.get();

        Optional<Timestamp> durationTime = DateUtility.toTimeStamp(timeArgs);

        if (durationTime.isEmpty() && !timeArgs.equals("perm")) {
            proxiedPlayer.sendMessage(Component.text("Erreur: " + timeArgs + " n'est pas une durée valide").color(TextColor.color(Color.RED.getRed(), Color.RED.getGreen(), Color.RED.getBlue())));
            return;
        }

        Timestamp end = durationTime.map(DateUtility::addToCurrentTimeStamp).orElse(null);

        String format = DateUtility.getMessage(end);

        Optional<SanctionInfo> sm = apiPlayerTarget.mutePlayer(reason, end, apiPlayerModerator);
        if (sm.isPresent()) {

            String banMessage = "Le modérateur §d" +
                    apiPlayerModerator.getName() +
                    " §7à mute l'utilisateur §a" +
                    apiPlayerTarget.getName() + " §7jusqu'au " +
                    format + " pour raison: "
                    + reason + ".";

            CoreAPI.getInstance().getModeratorManager().sendToModerators(banMessage);

            Optional<Player> onlinePlayerOptional = CoreVelocity.getInstance().getProxyServer().getPlayer(apiPlayerTarget.getName());

            onlinePlayerOptional.ifPresent(player -> player.disconnect(Component.text(sm.get().getSancMessage())));

        } else
            proxiedPlayer.sendMessage(Component.text("Désolé, une erreur est survenue").color(TextColor.color(Color.RED.getRed(), Color.RED.getGreen(), Color.RED.getBlue())));

    }

    public void onMissingArgument(CommandContext<CommandSource> commandContext, String s) {
        commandContext.getSource().sendMessage(Component.text("Syntax: /mute <pseudo> <temps> <raison>").color(TextColor.color(Color.RED.getRed(), Color.RED.getGreen(), Color.RED.getBlue())));
    }

    public void execute(CommandContext<CommandSource> commandContext, String s) {
        if (!(commandContext.getSource() instanceof Player player)) return;

        Optional<APIPlayerModerator> apiPlayerModerator = CoreAPI.getInstance().getModeratorManager().getModerator(player.getUniqueId());

        if (apiPlayerModerator.isEmpty()) {
            commandContext.getSource().sendMessage(Component.text("Vous n'avez pas la permission d'effectuer cette commande.").color(TextColor.color(Color.RED.getRed(), Color.RED.getGreen(), Color.RED.getBlue())));
            return;
        }

        String targetArgs = commandContext.getArgument("target", String.class);
        Optional<APIOfflinePlayer> apiPlayerTarget = CoreAPI.getInstance().getPlayerManager().getOfflinePlayer(targetArgs);
        if (apiPlayerTarget.isEmpty()) {
            commandContext.getSource().sendMessage(Component.text("La target ne s'est jamais connecté.").color(TextColor.color(Color.RED.getRed(), Color.RED.getGreen(), Color.RED.getBlue())));
            return;
        }

        if (apiPlayerTarget.get().getRank().isModeratorRank()) {
            commandContext.getSource().sendMessage(Component.text("Vous n'avez pas la permission d'effectuer cette commande.").color(TextColor.color(Color.RED.getRed(), Color.RED.getGreen(), Color.RED.getBlue())));
            return;
        }

        if (apiPlayerTarget.get().isMute()) {
            commandContext.getSource().sendMessage(Component.text("Erreur, le joueur est déjà mute.").color(TextColor.color(Color.RED.getRed(), Color.RED.getGreen(), Color.RED.getBlue())));
            return;
        }

        String timeArgs = commandContext.getArgument("time", String.class);


        String reason = commandContext.getArgument("reason", String.class);

        if (reason.contains("{") || reason.contains("}")) {
            commandContext.getSource().sendMessage(Component.text("Les caractéres { et } sont interdit d'utilisation dans les raisons").color(TextColor.color(Color.RED.getRed(), Color.RED.getGreen(), Color.RED.getBlue())));
            return;
        }

        mutePlayer(apiPlayerTarget.get(), timeArgs, apiPlayerModerator.get(), reason);

    }
}
