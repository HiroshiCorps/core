/*
 *
 * Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 *
 */

package fr.redxil.core.velocity.commands.mod.highstaff;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import fr.redxil.api.common.message.Color;
import fr.redxil.api.common.message.TextComponentBuilder;
import fr.redxil.api.common.message.TextComponentBuilderVelocity;
import fr.redxil.api.common.player.APIOfflinePlayer;
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.api.common.player.moderators.APIPlayerModerator;
import fr.redxil.api.common.player.rank.Rank;
import fr.redxil.api.common.utils.cmd.LiteralArgumentCreator;
import fr.redxil.core.common.CoreAPI;

import java.util.Optional;
import java.util.UUID;

public class SetRankCmd extends LiteralArgumentCreator<CommandSource> {


    public SetRankCmd() {
        super("setrank");
        super.setExecutor(this::onMissingArgument);
        super.createThen("target", StringArgumentType.word(), this::onMissingArgument)
                .createThen("rank", StringArgumentType.word(), this::execute);
    }

    public boolean isInt(String info) {
        try {
            Integer.parseInt(info);
            return true;
        } catch (NumberFormatException ignored) {
            return false;
        }
    }

    public void onMissingArgument(CommandContext<CommandSource> commandContext, String s) {
        UUID playerUUID = ((Player) commandContext.getSource()).getUniqueId();
        TextComponentBuilder.createTextComponent("Merci de faire /setrank (joueur) (rank)").setColor(Color.RED).sendTo(playerUUID);
    }

    public void execute(CommandContext<CommandSource> commandContext, String s) {

        Optional<APIPlayer> apiPlayer;
        if (commandContext.getSource() instanceof Player)
            apiPlayer = CoreAPI.getInstance().getPlayerManager().getPlayer(((Player) commandContext.getSource()).getUniqueId());
        else apiPlayer = Optional.of(CoreAPI.getInstance().getPlayerManager().getServerPlayer());

        if (apiPlayer.isEmpty()) return;

        if (!apiPlayer.get().hasPermission(Rank.ADMINISTRATEUR.getRankPower())) {
            commandContext.getSource().sendMessage(((TextComponentBuilderVelocity) TextComponentBuilder.createTextComponent(
                    "Seulement un adminnistrateur peut executer cette commande"
            ).setColor(Color.RED)).getFinalTextComponent());
            return;
        }

        Optional<APIOfflinePlayer> offlineTarget = CoreAPI.getInstance().getPlayerManager().getOfflinePlayer(commandContext.getArgument("target", String.class));
        if (offlineTarget.isEmpty()) {
            commandContext.getSource().sendMessage(((TextComponentBuilderVelocity) TextComponentBuilder.createTextComponent(
                    "Le joueur: " + commandContext.getArgument("target", String.class) + " ne s'est jamais connecté").setColor(Color.RED)
            ).getFinalTextComponent());
            return;
        }

        if (offlineTarget.get().hasPermission(Rank.ADMINISTRATEUR.getRankPower())) {
            commandContext.getSource().sendMessage(((TextComponentBuilderVelocity) TextComponentBuilder.createTextComponent(
                    "Impossible d'affecter un nouveau rank à ce joueur"
            ).setColor(Color.RED)).getFinalTextComponent());
            return;
        }

        String rankArg = commandContext.getArgument("rank", String.class);
        Rank newRank = null;


        if (!isInt(rankArg)) {
            for (Rank Rank : Rank.values()) {
                if (Rank.getRankName().equalsIgnoreCase(rankArg)) {
                    newRank = Rank;
                    break;
                }
            }
        } else {

            newRank = Rank.getRank(Long.parseLong(rankArg)).orElse(null);

        }


        if (newRank == null) {
            commandContext.getSource().sendMessage(((TextComponentBuilderVelocity) TextComponentBuilder.createTextComponent(
                    "Aucun rank avec le power ou le nom: " + commandContext.getArgument("rank", String.class)
            ).setColor(Color.RED)).getFinalTextComponent());
            return;
        }

        Optional<APIPlayerModerator> playerModerator = CoreAPI.getInstance().getModeratorManager().getModerator(offlineTarget.get().getMemberID());
        playerModerator.ifPresent(APIPlayerModerator::disconnectModerator);

        if (offlineTarget.get() instanceof APIPlayer) {
            ((APIPlayer) offlineTarget.get()).setRealRank(newRank);
            CoreAPI.getInstance().getModeratorManager().loadModerator(offlineTarget.get().getMemberID(), offlineTarget.get().getUUID(), offlineTarget.get().getName());
        } else
            offlineTarget.get().setRank(newRank);

        commandContext.getSource().sendMessage(((TextComponentBuilderVelocity) TextComponentBuilder.createTextComponent(
                        "La personne §d" +
                                offlineTarget.get().getName() +
                                " §7à été rank: §a" +
                                newRank.getRankName()
                )).getFinalTextComponent()
        );
    }
}
