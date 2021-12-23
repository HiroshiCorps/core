/* Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 */

package fr.redxil.core.velocity.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import fr.redxil.api.common.API;
import fr.redxil.api.common.message.Color;
import fr.redxil.api.common.message.TextComponentBuilder;
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.api.common.player.rank.Rank;

public class NickCmd extends BrigadierAPI<CommandSource> {


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

            if (!apiPlayer.isNick()) {

                TextComponentBuilder.createTextComponent("Syntax: /nick <nick>").setColor(Color.RED)
                        .sendTo(((Player) commandContext.getSource()).getUniqueId());

            } else {

                apiPlayer.setName(apiPlayer.getRealName());
                TextComponentBuilder.createTextComponent("Vous avez retrouvé votre Pseudo: " + apiPlayer.getName())
                        .sendTo(((Player) commandContext.getSource()).getUniqueId());

            }

            return 1;

        }

        String nick = commandContext.getArgument("nick", String.class);
        Rank nickRank = Rank.JOUEUR;

        if (commandContext.getArguments().size() >= 2) {

            String argRank = commandContext.getArgument("rank", String.class);

            if (!isInt(argRank)) {
                for (Rank Rank : Rank.values()) {
                    if (Rank.getRankName().equalsIgnoreCase(argRank)) {
                        nickRank = Rank;
                        break;
                    }
                }
            } else {
                nickRank = Rank.getRank(Integer.parseInt(argRank));
            }

            if (nickRank == null) {
                TextComponentBuilder.createTextComponent("Erreur, " + argRank + " doit être un power de grade" + apiPlayer.getName()).setColor(Color.RED)
                        .sendTo(((Player) commandContext.getSource()).getUniqueId());
                return 1;
            }

            if (nickRank.getRankPower() > apiPlayer.getRealRankPower()) {
                TextComponentBuilder.createTextComponent("Erreur, " + argRank + " vous ne pouvez pas vous nick en " + nickRank.getRankName()).setColor(Color.RED)
                        .sendTo(((Player) commandContext.getSource()).getUniqueId());
                return 1;
            }

        }

        if (apiPlayer.setName(nick)) {
            apiPlayer.setRank(nickRank);
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
        CommandNode<CommandSource> nick = this.addArgumentCommand(literalCommandNode, "nick", StringArgumentType.word());
        this.addArgumentCommand(nick, "rank", StringArgumentType.word());
    }
}
