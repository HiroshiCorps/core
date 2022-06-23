/*
 *
 * Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 *
 */

package fr.redxil.core.velocity.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.api.common.player.rank.Rank;
import fr.redxil.api.common.utils.Color;
import fr.redxil.api.common.utils.cmd.LiteralArgumentCreator;
import fr.redxil.core.common.CoreAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

import java.util.Optional;

public class NickCmd extends LiteralArgumentCreator<CommandSource> {

    public NickCmd() {
        super("nick");
        super.setExecutor(this::onCommandWithoutArgs);
        super.createThen("newNick", StringArgumentType.word(), this::execute).createThen("newRank", StringArgumentType.word(), this::executeSR);
    }

    public void onCommandWithoutArgs(CommandContext<CommandSource> commandContext, String s) {
        Optional<APIPlayer> apiPlayer = CoreAPI.getInstance().getPlayerManager().getPlayer(((Player) commandContext.getSource()).getUniqueId());

        if (apiPlayer.isEmpty())
            return;

        if (!apiPlayer.get().isNick())
            commandContext.getSource().sendMessage(Component.text("Syntax: /nick <nick>").color(TextColor.color(Color.RED.getRed(), Color.RED.getGreen(), Color.RED.getBlue())));
        else {

            apiPlayer.get().setName(apiPlayer.get().getRealName());
            commandContext.getSource().sendMessage(Component.text("Vous avez retrouvé votre Pseudo: " + apiPlayer.get().getName()));

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

    public void execute(CommandContext<CommandSource> commandContext, String s) {
        if (!(commandContext.getSource() instanceof Player)) return;

        Optional<APIPlayer> apiPlayer = CoreAPI.getInstance().getPlayerManager().getPlayer(((Player) commandContext.getSource()).getUniqueId());
        if (apiPlayer.isEmpty())
            return;

        String nick = commandContext.getArgument("newNick", String.class);
        Rank nickRank = Rank.JOUEUR;

        if (apiPlayer.get().setName(nick)) {
            apiPlayer.get().setRank(nickRank);
            commandContext.getSource().sendMessage(Component.text("Nick changé"));
        } else
            commandContext.getSource().sendMessage(Component.text("Impossible de changer le nick, veuillez vérifier que le pseudo n'est pas déjà utilisé").color(TextColor.color(Color.RED.getRed(), Color.RED.getGreen(), Color.RED.getBlue())));
    }

    public void executeSR(CommandContext<CommandSource> commandContext, String s) {
        if (!(commandContext.getSource() instanceof Player)) return;

        Optional<APIPlayer> apiPlayer = CoreAPI.getInstance().getPlayerManager().getPlayer(((Player) commandContext.getSource()).getUniqueId());
        if (apiPlayer.isEmpty())
            return;

        String nick = commandContext.getArgument("newNick", String.class);
        Rank nickRank = Rank.JOUEUR;

        String argRank = commandContext.getArgument("newRank", String.class);

        if (!isInt(argRank)) {
            for (Rank Rank : Rank.values()) {
                if (Rank.getRankName().equalsIgnoreCase(argRank)) {
                    nickRank = Rank;
                    break;
                }
            }
        } else {
            nickRank = Rank.getRank(Integer.parseInt(argRank)).orElse(null);
        }

        if (nickRank == null) {
            commandContext.getSource().sendMessage(Component.text("Erreur, " + argRank + " doit être un power de grade" + apiPlayer.get().getName()).color(TextColor.color(Color.RED.getRed(), Color.RED.getGreen(), Color.RED.getBlue())));
            return;
        }

        if (nickRank.getRankPower() > apiPlayer.get().getRealRankPower()) {
            commandContext.getSource().sendMessage(Component.text("Erreur, " + argRank + " vous ne pouvez pas vous nick en " + nickRank.getRankName()).color(TextColor.color(Color.RED.getRed(), Color.RED.getGreen(), Color.RED.getBlue())));
            return;
        }

        if (apiPlayer.get().setName(nick)) {
            apiPlayer.get().setRank(nickRank);
            commandContext.getSource().sendMessage(Component.text("Nick changé"));
        } else
            commandContext.getSource().sendMessage(Component.text("Impossible de changer le nick, veuillez vérifier que le pseudo n'est pas déjà utilisé").color(TextColor.color(Color.RED.getRed(), Color.RED.getGreen(), Color.RED.getBlue())));

    }
}
