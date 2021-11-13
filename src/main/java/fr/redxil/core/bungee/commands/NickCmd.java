/* Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 */

package fr.redxil.core.bungee.commands;

import com.velocitypowered.api.command.Command;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import fr.redxil.api.common.message.Color;
import fr.redxil.api.common.message.TextComponentBuilder;
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.api.common.player.nick.NickData;
import fr.redxil.api.common.rank.RankList;
import fr.redxil.core.common.CoreAPI;
import org.checkerframework.checker.nullness.qual.NonNull;

public class NickCmd implements Command {

    public void execute(CommandSource sender, String @NonNull [] args) {
        if (!(sender instanceof Player)) return;

        APIPlayer apiPlayer = CoreAPI.get().getPlayerManager().getPlayer(((Player) sender).getUniqueId());

        if (args.length == 0) {

            if (!CoreAPI.get().getNickGestion().hasNick(apiPlayer)) {

                TextComponentBuilder.createTextComponent("Syntax: /nick <nick>").setColor(Color.RED)
                        .sendTo(((Player) sender).getUniqueId());

            } else {

                CoreAPI.get().getNickGestion().removeNick(apiPlayer);
                TextComponentBuilder.createTextComponent("Vous avez retrouvé votre Pseudo: " + apiPlayer.getName())
                        .sendTo(((Player) sender).getUniqueId());

            }

            return;

        }

        String nick = args[0];
        RankList nickRank = RankList.JOUEUR;

        if (args.length >= 2) {

            if (!isInt(args[1])) {
                TextComponentBuilder.createTextComponent("Erreur, " + args[1] + " doit être un power de grade" + apiPlayer.getName()).setColor(Color.RED)
                        .sendTo(((Player) sender).getUniqueId());
                return;
            }

            nickRank = RankList.getRank(Integer.parseInt(args[1]));
            if (nickRank == null) {
                TextComponentBuilder.createTextComponent("Erreur, " + args[1] + " doit être un power de grade" + apiPlayer.getName()).setColor(Color.RED)
                        .sendTo(((Player) sender).getUniqueId());
                return;
            }

            if (nickRank.getRankPower() > apiPlayer.getRankPower()) {
                TextComponentBuilder.createTextComponent("Erreur, " + args[1] + " vous ne pouvez pas vous nick en " + nickRank.getRankName()).setColor(Color.RED)
                        .sendTo(((Player) sender).getUniqueId());
                return;
            }

        }

        if (CoreAPI.get().getNickGestion().setNick(apiPlayer, new NickData(nick, nickRank))) {
            TextComponentBuilder.createTextComponent("Nick changé")
                    .sendTo(((Player) sender).getUniqueId());
        } else {
            TextComponentBuilder.createTextComponent("Impossible de changer le nick, veuillez vérifier que le pseudo n'est pas déjà utilisé").setColor(Color.RED)
                    .sendTo(((Player) sender).getUniqueId());
        }

    }

    public boolean isInt(String in) {
        try {
            Integer.parseInt(in);
            return true;
        } catch (NumberFormatException ignored) {
            return false;
        }
    }

}
