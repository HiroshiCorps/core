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
import fr.redxil.api.common.player.APIOfflinePlayer;
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.api.common.player.moderators.APIPlayerModerator;
import fr.redxil.api.common.player.rank.Rank;
import fr.redxil.api.common.utils.Color;
import fr.redxil.api.common.utils.cmd.LiteralArgumentCreator;
import fr.redxil.core.common.CoreAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

import java.util.Optional;

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
        commandContext.getSource().sendMessage(Component.text("Merci de faire /setrank (joueur) (rank)").color(TextColor.color(Color.RED.getRed(), Color.RED.getGreen(), Color.RED.getBlue())));
    }

    public void execute(CommandContext<CommandSource> commandContext, String s) {

        Optional<APIPlayer> apiPlayer;
        if (commandContext.getSource() instanceof Player)
            apiPlayer = CoreAPI.getInstance().getPlayerManager().getPlayer(((Player) commandContext.getSource()).getUniqueId());
        else apiPlayer = Optional.of(CoreAPI.getInstance().getPlayerManager().getServerPlayer());

        if (apiPlayer.isEmpty()) return;

        if (!apiPlayer.get().hasPermission(Rank.ADMINISTRATEUR.getRankPower())) {
            commandContext.getSource().sendMessage(Component.text("Seulement un administrateur peut executer cette commande").color(TextColor.color(Color.RED.getRed(), Color.RED.getGreen(), Color.RED.getBlue())));
            return;
        }

        Optional<APIOfflinePlayer> offlineTarget = CoreAPI.getInstance().getPlayerManager().getOfflinePlayer(commandContext.getArgument("target", String.class));
        if (offlineTarget.isEmpty()) {
            commandContext.getSource().sendMessage(Component.text("Le joueur: " + commandContext.getArgument("target", String.class) + " ne s'est jamais connecté").color(TextColor.color(Color.RED.getRed(), Color.RED.getGreen(), Color.RED.getBlue())));
            return;
        }

        if (offlineTarget.get().hasPermission(Rank.ADMINISTRATEUR.getRankPower())) {
            commandContext.getSource().sendMessage(Component.text("Impossible d'affecter un nouveau rank à ce joueur").color(TextColor.color(Color.RED.getRed(), Color.RED.getGreen(), Color.RED.getBlue())));
            return;
        }

        String rankArg = commandContext.getArgument("rank", String.class);
        Rank newRank;


        if (!isInt(rankArg))
            newRank = Rank.getRank(rankArg).orElse(null);
        else
            newRank = Rank.getRank(Long.parseLong(rankArg)).orElse(null);


        if (newRank == null) {
            commandContext.getSource().sendMessage(Component.text("Aucun rank avec le power ou le nom: " + commandContext.getArgument("rank", String.class)).color(TextColor.color(Color.RED.getRed(), Color.RED.getGreen(), Color.RED.getBlue())));
            return;
        }

        Optional<APIPlayerModerator> playerModerator = CoreAPI.getInstance().getModeratorManager().getModerator(offlineTarget.get().getMemberID());
        playerModerator.ifPresent(APIPlayerModerator::disconnectModerator);

        if (offlineTarget.get() instanceof APIPlayer) {
            ((APIPlayer) offlineTarget.get()).setRealRank(newRank);
            CoreAPI.getInstance().getModeratorManager().loadModerator(offlineTarget.get().getMemberID(), offlineTarget.get().getUUID(), offlineTarget.get().getName());
        } else
            offlineTarget.get().setRank(newRank);

        commandContext.getSource().sendMessage(Component.text("La personne §d" +
                offlineTarget.get().getName() +
                " §7à été rank: §a" +
                newRank.getRankName()).color(TextColor.color(Color.GREEN.getRed(), Color.GREEN.getGreen(), Color.GREEN.getBlue()))
        );
    }
}
