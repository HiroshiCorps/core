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
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import fr.redxil.api.common.API;
import fr.redxil.api.common.message.Color;
import fr.redxil.api.common.message.TextComponentBuilder;
import fr.redxil.api.common.message.TextComponentBuilderVelocity;
import fr.redxil.api.common.player.APIOfflinePlayer;
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.api.common.player.moderators.APIPlayerModerator;
import fr.redxil.api.common.player.rank.Rank;
import fr.redxil.core.velocity.CoreVelocity;
import fr.redxil.core.velocity.commands.BrigadierAPI;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class SetRankCmd extends BrigadierAPI<CommandSource> {


    public SetRankCmd() {
        super("setrank");
    }

    public boolean isInt(String info) {
        try {
            Integer.parseInt(info);
            return true;
        } catch (NumberFormatException ignored) {
            return false;
        }
    }

    public void onMissingArgument(CommandContext<CommandSource> commandContext) {
        UUID playerUUID = ((Player) commandContext.getSource()).getUniqueId();
        TextComponentBuilder.createTextComponent("Merci de faire /setrank (joueur) (rank)").setColor(Color.RED).sendTo(playerUUID);
    }

    @Override
    public void onCommandWithoutArgs(CommandContext<CommandSource> commandExecutor) {
        this.onMissingArgument(commandExecutor);
    }

    public void execute(CommandContext<CommandSource> commandContext) {
        if (!(commandContext.getSource() instanceof Player)) return;

        Optional<APIPlayer> apiPlayer = API.getInstance().getPlayerManager().getPlayer(((Player) commandContext.getSource()).getUniqueId());
        if (apiPlayer.isEmpty()) return;

        if (!apiPlayer.get().hasPermission(Rank.ADMINISTRATEUR.getRankPower())) {
            commandContext.getSource().sendMessage(((TextComponentBuilderVelocity) TextComponentBuilder.createTextComponent(
                    "Seulement un adminnistrateur peut executer cette commande"
            ).setColor(Color.RED)).getFinalTextComponent());
            return;
        }

        Optional<APIOfflinePlayer> offlineTarget = API.getInstance().getPlayerManager().getOfflinePlayer(commandContext.getArgument("target", String.class));
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

            newRank = Rank.getRank(Long.parseLong(rankArg));

        }


        if (newRank == null) {
            commandContext.getSource().sendMessage(((TextComponentBuilderVelocity) TextComponentBuilder.createTextComponent(
                    "Aucun rank avec le power ou le nom: " + commandContext.getArgument("rank", String.class)
            ).setColor(Color.RED)).getFinalTextComponent());
            return;
        }

        Optional<APIPlayerModerator> playerModerator = API.getInstance().getModeratorManager().getModerator(offlineTarget.get().getMemberID());
        playerModerator.ifPresent(APIPlayerModerator::disconnectModerator);

        if (offlineTarget.get() instanceof APIPlayer) {
            ((APIPlayer) offlineTarget.get()).setRealRank(newRank);
            API.getInstance().getModeratorManager().loadModerator(offlineTarget.get().getMemberID(), offlineTarget.get().getUUID(), offlineTarget.get().getName());
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

    @Override
    public void registerArgs(LiteralCommandNode<CommandSource> literalCommandNode) {
        List<String> playerName = new ArrayList<>();

        for (Player player : CoreVelocity.getInstance().getProxyServer().getAllPlayers()) {
            playerName.add(player.getUsername());
        }

        List<String> argRank = new ArrayList<>();

        for (Rank Rank : Rank.values()) {
            argRank.add(Rank.getRankName());
            argRank.add(String.valueOf(Rank.getRankPower()));
        }

        CommandNode<CommandSource> target = this.addArgumentCommand(literalCommandNode, "target", StringArgumentType.word(), this::onMissingArgument, playerName.toArray(new String[0]));
        this.addArgumentCommand(target, "rank", StringArgumentType.word(), this::execute, argRank.toArray(new String[0]));
    }
}
