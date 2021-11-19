/* Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 */

package fr.redxil.core.velocity.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import fr.redxil.api.common.API;
import fr.redxil.api.common.message.Color;
import fr.redxil.api.common.message.TextComponentBuilder;
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.api.common.player.nick.NickData;
import fr.redxil.api.common.rank.RankList;
import fr.redxil.api.velocity.BrigadierAPI;

public class NickCmd extends BrigadierAPI {


    public NickCmd() {
        super("nick");
    }

    public boolean isInt(String in) {
        try {
            Integer.parseInt(in);
            return true;
        } catch (NumberFormatException ignored) {
            return false;
        }
    }

    @Override
    public int execute(CommandContext<CommandSource> commandContext) {
        if (!(commandContext.getSource() instanceof Player)) return 1;

        APIPlayer apiPlayer = API.getInstance().getPlayerManager().getPlayer(((Player) commandContext.getSource()).getUniqueId());

        if (commandContext.getArguments().size() == 0) {

            if (!API.getInstance().getNickGestion().hasNick(apiPlayer)) {

                TextComponentBuilder.createTextComponent("Syntax: /nick <nick>").setColor(Color.RED)
                        .sendTo(((Player) commandContext.getSource()).getUniqueId());

            } else {

                API.getInstance().getNickGestion().removeNick(apiPlayer);
                TextComponentBuilder.createTextComponent("Vous avez retrouvé votre Pseudo: " + apiPlayer.getName())
                        .sendTo(((Player) commandContext.getSource()).getUniqueId());

            }

            return 1;

        }

        String nick = commandContext.getArgument("nick", String.class);
        RankList nickRank = RankList.JOUEUR;

        if (commandContext.getArguments().size() >= 2) {

            String argRank = commandContext.getArgument("rank", String.class);

            if (!isInt(argRank)) {
                for (RankList rankList : RankList.values()) {
                    if (rankList.getRankName().equalsIgnoreCase(argRank)) {
                        nickRank = rankList;
                        break;
                    }
                }
            } else {
                nickRank = RankList.getRank(Integer.parseInt(argRank));
            }

            if (nickRank == null) {
                TextComponentBuilder.createTextComponent("Erreur, " + argRank + " doit être un power de grade" + apiPlayer.getName()).setColor(Color.RED)
                        .sendTo(((Player) commandContext.getSource()).getUniqueId());
                return 1;
            }

            if (nickRank.getRankPower() > apiPlayer.getRankPower()) {
                TextComponentBuilder.createTextComponent("Erreur, " + argRank + " vous ne pouvez pas vous nick en " + nickRank.getRankName()).setColor(Color.RED)
                        .sendTo(((Player) commandContext.getSource()).getUniqueId());
                return 1;
            }

        }

        if (API.getInstance().getNickGestion().setNick(apiPlayer, new NickData(nick, nickRank))) {
            TextComponentBuilder.createTextComponent("Nick changé")
                    .sendTo(((Player) commandContext.getSource()).getUniqueId());
        } else {
            TextComponentBuilder.createTextComponent("Impossible de changer le nick, veuillez vérifier que le pseudo n'est pas déjà utilisé").setColor(Color.RED)
                    .sendTo(((Player) commandContext.getSource()).getUniqueId());
        }
        return 0;
    }

    @Override
    public void registerArgs(LiteralCommandNode<CommandSource> literalCommandNode) {
        this.addArgumentCommand(literalCommandNode, "nick", StringArgumentType.word());
        this.addArgumentCommand(literalCommandNode, "rank", StringArgumentType.word());
    }
}
