/* Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 */

package fr.redxil.core.bungee.commands.mod.highstaff;

import com.velocitypowered.api.command.Command;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import fr.redxil.api.common.message.Color;
import fr.redxil.api.common.message.TextComponentBuilder;
import fr.redxil.api.common.message.TextComponentBuilderVelocity;
import fr.redxil.api.common.player.APIOfflinePlayer;
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.api.common.player.moderators.APIPlayerModerator;
import fr.redxil.api.common.rank.RankList;
import fr.redxil.core.common.CoreAPI;
import org.checkerframework.checker.nullness.qual.NonNull;

public class SetRankCmd implements Command {

    public void execute(CommandSource commandSender, String @NonNull [] strings) {
        if (!(commandSender instanceof Player)) return;

        APIPlayer apiPlayer = CoreAPI.get().getPlayerManager().getPlayer(((Player) commandSender).getUniqueId());
        if (apiPlayer == null) return;

        if (!apiPlayer.hasPermission(RankList.ADMINISTRATEUR.getRankPower())) {
            commandSender.sendMessage(((TextComponentBuilderVelocity) TextComponentBuilder.createTextComponent(
                    "Seulement un adminnistrateur peut executer cette commande"
            ).setColor(Color.RED)).getFinalTextComponent());
            return;
        }

        if (strings.length != 2) {
            commandSender.sendMessage(((TextComponentBuilderVelocity) TextComponentBuilder.createTextComponent(
                    "Merci de faire /setrank (joueur) (power rank)"
            ).setColor(Color.RED)).getFinalTextComponent());
            return;
        }

        APIOfflinePlayer offlineTarget = CoreAPI.get().getPlayerManager().getOfflinePlayer(strings[0]);
        if (offlineTarget == null) {
            commandSender.sendMessage(((TextComponentBuilderVelocity) TextComponentBuilder.createTextComponent(
                    "Le joueur: " + strings[0] + " ne s'est jamais connecté").setColor(Color.RED)
            ).getFinalTextComponent());
            return;
        }

        if (offlineTarget.hasPermission(RankList.ADMINISTRATEUR.getRankPower())) {
            commandSender.sendMessage(((TextComponentBuilderVelocity) TextComponentBuilder.createTextComponent(
                    "Impossible d'affecter un nouveau rank à ce joueur"
            ).setColor(Color.RED)).getFinalTextComponent());
            return;
        }

        if (!isInt(strings[1])) {
            commandSender.sendMessage(((TextComponentBuilderVelocity) TextComponentBuilder.createTextComponent(
                    "Merci de mettre un power à la place de: " + strings[1]
            ).setColor(Color.RED)).getFinalTextComponent());
            return;
        }

        RankList newRank = RankList.getRank(Integer.parseInt(strings[1]));

        if (newRank == null) {
            commandSender.sendMessage(((TextComponentBuilderVelocity) TextComponentBuilder.createTextComponent(
                    "Aucun rank avec le power: " + strings[1]
            ).setColor(Color.RED)).getFinalTextComponent());
            return;
        }

        APIPlayerModerator playerModerator = CoreAPI.get().getModeratorManager().getModerator(offlineTarget.getMemberId());
        if (playerModerator != null)
            playerModerator.disconnectModerator();

        offlineTarget.setRank(newRank);
        if (offlineTarget instanceof APIPlayer)
            CoreAPI.get().getModeratorManager().loadModerator((APIPlayer) offlineTarget);

        commandSender.sendMessage(((TextComponentBuilderVelocity) TextComponentBuilder.createTextComponent(
                        "La personne §d" +
                                offlineTarget.getName() +
                                " §7à été rank: §a" +
                                newRank.getRankName()
                )).getFinalTextComponent()
        );

    }

    public boolean isInt(String info) {
        try {
            Integer.parseInt(info);
            return true;
        } catch (NumberFormatException ignored) {
            return false;
        }
    }

}
