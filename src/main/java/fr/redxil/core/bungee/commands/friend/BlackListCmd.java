/* Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 */

package fr.redxil.core.bungee.commands.friend;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.Command;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import fr.redxil.api.common.message.Color;
import fr.redxil.api.common.message.TextComponentBuilder;
import fr.redxil.api.common.player.APIOfflinePlayer;
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.core.common.CoreAPI;
import net.kyori.adventure.text.TextComponent;

import java.util.UUID;

public class BlackListCmd implements Command {

    public BrigadierCommand getCommand() {

        LiteralCommandNode<CommandSource> lcn = LiteralArgumentBuilder.<CommandSource>literal("name")
                .executes(commandContext -> {

                    CommandSource commandSource = commandContext.getSource();

                    if (!(commandSource instanceof Player)) {
                        return 0;
                    }

                    String playerName = commandContext.getArgument("name", String.class);

                    APIOfflinePlayer osp = CoreAPI.get().getPlayerManager().getOfflinePlayer(playerName);

                    if (osp == null) {
                        commandSource.sendMessage((TextComponent) TextComponentBuilder.createTextComponent("Erreur, personne introuvable").setColor(Color.RED).getTextComponent());
                        return 0;
                    }

                    if (commandContext.getArgument("addorrm", String.class).equals("add")) {
                        CoreAPI.get().getPlayerManager().getPlayer(((Player) commandSource).getUniqueId()).addBlackList(osp);
                        commandSource.sendMessage((TextComponent) TextComponentBuilder.createTextComponent("Joueur BlackList").setColor(Color.GREEN));
                    } else {
                        CoreAPI.get().getPlayerManager().getPlayer(((Player) commandSource).getUniqueId()).removeBlackList(osp);
                        commandSource.sendMessage((TextComponent) TextComponentBuilder.createTextComponent("Joueur plus BlackList").setColor(Color.GREEN));
                    }

                    return 1;

                }).build();

        LiteralCommandNode<CommandSource> addorrm =
                LiteralArgumentBuilder.<CommandSource>literal("addorrm")
                        .then(lcn).build();

        LiteralCommandNode<CommandSource> blackList =
                LiteralArgumentBuilder.<CommandSource>literal("blacklist")
                        .then(addorrm).build();

        blackList.addChild(addorrm);
        addorrm.addChild(lcn);

        return new BrigadierCommand(blackList);

    }

    public void execute(CommandSource sender, String[] args) {

        if (!(sender instanceof Player)) return;

        UUID playerUUID = ((Player) sender).getUniqueId();

        if (args.length != 2) {
            TextComponentBuilder.createTextComponent("Merci de faire /bl (add|remove) (pseudo)").setColor(Color.RED).sendTo(playerUUID);
            return;
        }

        if (args[0].equalsIgnoreCase("add") || args[0].equalsIgnoreCase("remove")) {

            APIOfflinePlayer osp = CoreAPI.get().getPlayerManager().getOfflinePlayer(args[1]);
            APIPlayer sp = CoreAPI.get().getPlayerManager().getPlayer(playerUUID);

            if (osp == null) {
                TextComponentBuilder.createTextComponent("Erreur, le joueur: " + args[1] + " est inconnue").setColor(Color.RED).sendTo(playerUUID);
                return;
            }

            boolean remove = args[0].equalsIgnoreCase("remove");
            if (remove) {

                if (!sp.isBlackList(osp)) {
                    TextComponentBuilder.createTextComponent("Erreur, le joueur: " + args[1] + " n'est pas BlackList").setColor(Color.RED).sendTo(playerUUID);
                    return;
                }

                sp.removeBlackList(osp);

            } else {

                if (sp.isBlackList(osp)) {
                    TextComponentBuilder.createTextComponent("Erreur, le joueur: " + args[1] + " est déjà BlackList").setColor(Color.RED).sendTo(playerUUID);
                    return;
                }

                sp.addBlackList(osp);

            }

            return;
        }

        TextComponentBuilder.createTextComponent("Merci de faire /bl (add|remove) (pseudo)").setColor(Color.RED).sendTo(playerUUID);

    }

}
