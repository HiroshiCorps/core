/* Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 */

package fr.redxil.core.bungee.commands.friend;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
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

import java.util.ArrayList;
import java.util.List;

public class FriendCmd implements Command {

    public BrigadierCommand getCommand() {

        LiteralCommandNode<CommandSource> lcn = LiteralArgumentBuilder.<CommandSource>literal("friend")
                .executes(this::sendCommandList)
                .build();

        LiteralCommandNode<CommandSource> cmd = LiteralArgumentBuilder.<CommandSource>literal("cmd")
                .executes(commandContext -> {

                    if (!(commandContext.getSource() instanceof Player)) {
                        return 0;
                    }

                    Player player = (Player) commandContext.getSource();

                    FriendCmd.ListCmd usedCmd = FriendCmd.ListCmd.getCommand(commandContext.getArgument("cmd", String.class));
                    if (usedCmd == null) {
                        return this.sendCommandList(commandContext);
                    }

                    if (!FriendCmd.ListCmd.getCommand(1).contains(usedCmd)) {
                        player.sendMessage((TextComponent) TextComponentBuilder.createTextComponent("Merci de faire /party " + usedCmd.getName() + " (nom/joueur)").setColor(Color.RED).getTextComponent());
                        return 1;
                    }

                    switch (usedCmd) {

                        case LIST: {
                            return listCmd(commandContext, player, null);
                        }

                    }
                    return 1;
                })
                .build();

        LiteralCommandNode<CommandSource> name = LiteralArgumentBuilder.<CommandSource>literal("name")
                .executes(commandContext -> {

                    if (!(commandContext.getSource() instanceof Player)) {
                        return 0;
                    }

                    Player player = (Player) commandContext.getSource();

                    FriendCmd.ListCmd usedCmd = FriendCmd.ListCmd.getCommand(commandContext.getArgument("cmd", String.class));
                    if (usedCmd == null) {
                        return this.sendCommandList(commandContext);
                    }

                    if (!FriendCmd.ListCmd.getCommand(2).contains(usedCmd)) {
                        player.sendMessage((TextComponent) TextComponentBuilder.createTextComponent("Merci de faire /party " + usedCmd.getName()).setColor(Color.RED).getTextComponent());
                        return 1;
                    }

                    String nameArg = commandContext.getArgument("name", String.class);

                    switch (usedCmd) {
                        case REMOVE: {
                            return removeCmd(commandContext, player, nameArg);
                        }
                        case REVOKE: {
                            return revokeCmd(commandContext, player, nameArg);
                        }
                        case REFUSE: {
                            return refuseCmd(commandContext, player, nameArg);
                        }
                        case ACCEPT: {
                            return acceptCmd(commandContext, player, nameArg);
                        }
                    }
                    return 1;
                })
                .build();

        lcn.addChild(cmd);

        cmd.addChild(name);

        return new BrigadierCommand(lcn);

    }

    public int sendCommandList(CommandContext<CommandSource> commandContext) {
        CommandSource commandSource = commandContext.getSource();
        TextComponentBuilder textComponentBuilder = TextComponentBuilder.createTextComponent("Veuillez utiliser l'une des composantes suivantes:");
        for (FriendCmd.ListCmd cmd : FriendCmd.ListCmd.values()) {
            textComponentBuilder.appendNewComponentBuilder("\n" + Color.GRAY + "> /friend " +
                    cmd.getName() + ": " + Color.BLUE + " " + cmd.getUtility());
        }
        commandSource.sendMessage((TextComponent) textComponentBuilder.getFinalTextComponent());
        return 1;
    }

    public void inviteCmd(CommandContext<CommandSource> commandContext, Player player, String argument) {

        APIPlayer apiPlayer = CoreAPI.get().getPlayerManager().getPlayer(player.getUniqueId());
        APIOfflinePlayer target = CoreAPI.get().getPlayerManager().getOfflinePlayer(argument);
        if (target == null) {
            TextComponentBuilder.createTextComponent("Erreur, la personne n'est pas connue").setColor(Color.RED).sendTo(apiPlayer);
            return;
        }

        apiPlayer.sendFriendInvite(target);
        TextComponentBuilder.createTextComponent("Demande d'amis envoyée").setColor(Color.GREEN).sendTo(apiPlayer);

    }

    public int acceptCmd(CommandContext<CommandSource> commandContext, Player player, String argument) {

        APIPlayer apiPlayer = CoreAPI.get().getPlayerManager().getPlayer(player.getUniqueId());
        APIOfflinePlayer target = CoreAPI.get().getPlayerManager().getOfflinePlayer(argument);
        if (target == null) {
            TextComponentBuilder.createTextComponent("Erreur, la personne n'est pas connue").setColor(Color.RED).sendTo(apiPlayer);
            return 1;
        }

        apiPlayer.acceptFriendInvite(target);
        TextComponentBuilder.createTextComponent("Sa demande d'amis à été accepté").setColor(Color.GREEN).sendTo(apiPlayer);
        return 1;

    }

    public int refuseCmd(CommandContext<CommandSource> commandContext, Player player, String argument) {

        APIPlayer apiPlayer = CoreAPI.get().getPlayerManager().getPlayer(player.getUniqueId());
        APIOfflinePlayer target = CoreAPI.get().getPlayerManager().getOfflinePlayer(argument);
        if (target == null) {
            TextComponentBuilder.createTextComponent("Erreur, la personne n'est pas connue").setColor(Color.RED).sendTo(apiPlayer);
            return 1;
        }

        apiPlayer.refusedFriendInvite(target);
        TextComponentBuilder.createTextComponent("Sa demande d'amis à été refusée").setColor(Color.GREEN).sendTo(apiPlayer);
        return 1;

    }

    public int revokeCmd(CommandContext<CommandSource> commandContext, Player player, String argument) {

        APIPlayer apiPlayer = CoreAPI.get().getPlayerManager().getPlayer(player.getUniqueId());
        APIOfflinePlayer target = CoreAPI.get().getPlayerManager().getOfflinePlayer(argument);
        if (target == null) {
            TextComponentBuilder.createTextComponent("Erreur, la personne n'est pas connue").setColor(Color.RED).sendTo(apiPlayer);
            return 1;
        }

        apiPlayer.revokeFriendInvite(target);
        TextComponentBuilder.createTextComponent("Votre demande d'amis à été retiré").setColor(Color.GREEN).sendTo(apiPlayer);

        return 1;

    }

    public int removeCmd(CommandContext<CommandSource> commandContext, Player player, String argument) {
        APIPlayer apiPlayer = CoreAPI.get().getPlayerManager().getPlayer(player.getUniqueId());
        APIOfflinePlayer target = CoreAPI.get().getPlayerManager().getOfflinePlayer(argument);
        if (target == null) {
            TextComponentBuilder.createTextComponent("Erreur, la personne n'est pas connue").setColor(Color.RED).sendTo(apiPlayer);
            return 1;
        }

        apiPlayer.removeFriend(target);
        TextComponentBuilder.createTextComponent("Joueur retiré de vos Amis").setColor(Color.GREEN).sendTo(apiPlayer);

        return 1;
    }

    public int listCmd(CommandContext<CommandSource> commandContext, Player player, String argument) {

        APIPlayer apiPlayer = CoreAPI.get().getPlayerManager().getPlayer(player.getUniqueId());
        List<String> amisList = apiPlayer.getFriendList();

        if (amisList.size() == 0) {
            TextComponentBuilder.createTextComponent("Je suis désolée de te l'apprendre, mais tu n'a pas d'amis, en espérant que tu en ais dans la vrai vie").setColor(Color.GREEN).sendTo(apiPlayer);
            return 1;
        }

        TextComponentBuilder tcb = TextComponentBuilder.createTextComponent("Heuresement pour toi, tu as ").setColor(Color.GREEN).appendNewComponentBuilder(Integer.valueOf(amisList.size()).toString()).setColor(Color.BLUE).appendNewComponentBuilder(" amis").setColor(Color.GREEN);

        for (String ami : amisList) {
            String connect = "Déconnecté";
            Color bc = Color.RED;
            if (CoreAPI.get().getPlayerManager().isLoadedPlayer(ami)) {
                connect = "Connecté";
                bc = Color.GREEN;
            }
            tcb.appendNewComponentBuilder("\n" + ami + " ").setColor(Color.WHITE).appendNewComponentBuilder(connect).setColor(bc);
        }

        tcb.sendTo(apiPlayer);

        return 1;

    }

    public enum ListCmd {
        INVITE("invite", "Envoyer une demande d'amis", 2),
        ACCEPT("accept", "Accepter une demande d'amis", 2),
        REFUSE("refuse", "Refuser une demande d'amis", 2),
        REVOKE("revoke", "Retire votre demande d'amis", 2),
        REMOVE("remove", "Remove un joueur de la liste", 2),
        LIST("list", "Permet de voir la liste des joueurs", 1); /// *

        String name;
        String utility;
        int argument;

        ListCmd(String name, String utility, int argument) {
            this.name = name;
            this.utility = utility;
            this.argument = argument;
        }

        public static FriendCmd.ListCmd getCommand(String name) {

            for (FriendCmd.ListCmd cmd : FriendCmd.ListCmd.values()) {
                if (cmd.getName().equalsIgnoreCase(name)) {
                    return cmd;
                }
            }
            return null;
        }

        public static List<FriendCmd.ListCmd> getCommand(int argument) {

            List<FriendCmd.ListCmd> cmdList = new ArrayList<>();

            for (FriendCmd.ListCmd cmd : FriendCmd.ListCmd.values()) {
                if (cmd.getArgument() == argument) {
                    cmdList.add(cmd);
                }
            }
            return cmdList;
        }

        public String getName() {
            return name;
        }

        public String getUtility() {
            return utility;
        }

        public int getArgument() {
            return argument;
        }
    }

}
