/* Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 */

package fr.redxil.core.velocity.commands.friend;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import fr.redxil.api.common.API;
import fr.redxil.api.common.message.Color;
import fr.redxil.api.common.message.TextComponentBuilder;
import fr.redxil.api.common.player.APIOfflinePlayer;
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.api.common.player.data.LinkData;
import fr.redxil.api.common.player.data.LinkUsage;
import fr.redxil.core.common.CoreAPI;
import fr.redxil.core.velocity.CoreVelocity;
import fr.redxil.core.velocity.commands.BrigadierAPI;
import net.kyori.adventure.text.TextComponent;

import java.util.ArrayList;
import java.util.List;

public class FriendCmd extends BrigadierAPI<CommandSource> {

    public FriendCmd() {
        super("friend");
    }

    @Override
    public int execute(CommandContext<CommandSource> commandContext) {
        if (!(commandContext.getSource() instanceof Player)) {
            return 0;
        }

        Player player = (Player) commandContext.getSource();

        FriendCmd.ListCmd usedCmd = FriendCmd.ListCmd.getCommand(commandContext.getArgument("cmd", String.class));
        if (usedCmd == null) {
            return this.sendCommandList(commandContext);
        }

        if (!FriendCmd.ListCmd.getCommand(2).contains(usedCmd)) {
            player.sendMessage((TextComponent) TextComponentBuilder.createTextComponent("Merci de faire /friend " + usedCmd.getName()).setColor(Color.RED).getTextComponent());
            return 1;
        }

        String nameArg = commandContext.getArgument("target", String.class);

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
            case LIST: {
                return listCmd(commandContext, player, null);
            }
        }
        return 1;
    }

    @Override
    public void registerArgs(LiteralCommandNode<CommandSource> literalCommandNode) {
        List<String> cmdName = new ArrayList<>();

        for (ListCmd listCmd : ListCmd.values()) {

            cmdName.add(listCmd.getName());

        }

        List<String> playerName = new ArrayList<>();

        for (Player player : CoreVelocity.getInstance().getProxyServer().getAllPlayers()) {
            playerName.add(player.getUsername());
        }

        CommandNode<CommandSource> cmd = this.addArgumentCommand(literalCommandNode, "cmd", StringArgumentType.word(), cmdName.toArray(new String[0]));
        this.addArgumentCommand(cmd, "target", StringArgumentType.greedyString(), playerName.toArray(new String[0]));
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

        APIPlayer apiPlayer = API.getInstance().getPlayerManager().getPlayer(player.getUniqueId());
        APIOfflinePlayer target = API.getInstance().getPlayerManager().getOfflinePlayer(argument);
        if (target == null) {
            TextComponentBuilder.createTextComponent("Erreur, la personne n'est pas connue").setColor(Color.RED).sendTo(apiPlayer);
            return;
        }

        if (apiPlayer.hasLinkWith(LinkUsage.BOTH, target, "friend", "friendInvite")) {
            TextComponentBuilder.createTextComponent("Une ou un semblant de relation existe déjà entre vous").sendTo(apiPlayer);
            return;
        }
        apiPlayer.createLink(target, "friendInvite");
        TextComponentBuilder.createTextComponent("Demande d'amis envoyée").setColor(Color.GREEN).sendTo(apiPlayer);

    }

    public int acceptCmd(CommandContext<CommandSource> commandContext, Player player, String argument) {

        APIPlayer apiPlayer = API.getInstance().getPlayerManager().getPlayer(player.getUniqueId());
        APIOfflinePlayer target = API.getInstance().getPlayerManager().getOfflinePlayer(argument);
        if (target == null) {
            TextComponentBuilder.createTextComponent("Erreur, la personne n'est pas connue").setColor(Color.RED).sendTo(apiPlayer);
            return 1;
        }

        LinkData linkData = apiPlayer.getLink(LinkUsage.FROM, target, "friendInvite");
        if (linkData == null) {
            TextComponentBuilder.createTextComponent("Il n'y a aucune demande en cours de sa part").setColor(Color.RED).sendTo(apiPlayer);
            return 1;
        }
        linkData.setLinkType("friend");
        TextComponentBuilder.createTextComponent("Sa demande d'amis à été accepté").setColor(Color.GREEN).sendTo(apiPlayer);
        return 1;

    }

    public int refuseCmd(CommandContext<CommandSource> commandContext, Player player, String argument) {

        APIPlayer apiPlayer = API.getInstance().getPlayerManager().getPlayer(player.getUniqueId());
        APIOfflinePlayer target = API.getInstance().getPlayerManager().getOfflinePlayer(argument);
        if (target == null) {
            TextComponentBuilder.createTextComponent("Erreur, la personne n'est pas connue").setColor(Color.RED).sendTo(apiPlayer);
            return 1;
        }

        LinkData linkData = apiPlayer.getLink(LinkUsage.FROM, target, "friendInvite");
        if (linkData == null) {
            TextComponentBuilder.createTextComponent("Il n'y a aucune demande en cours de sa part").setColor(Color.RED).sendTo(apiPlayer);
            return 1;
        }
        linkData.setLinkType("friendRefused");
        TextComponentBuilder.createTextComponent("Sa demande d'amis à été refusée").setColor(Color.GREEN).sendTo(apiPlayer);
        return 1;

    }

    public int revokeCmd(CommandContext<CommandSource> commandContext, Player player, String argument) {

        APIPlayer apiPlayer = API.getInstance().getPlayerManager().getPlayer(player.getUniqueId());
        APIOfflinePlayer target = API.getInstance().getPlayerManager().getOfflinePlayer(argument);
        if (target == null) {
            TextComponentBuilder.createTextComponent("Erreur, la personne n'est pas connue").setColor(Color.RED).sendTo(apiPlayer);
            return 1;
        }

        LinkData linkData = apiPlayer.getLink(LinkUsage.TO, target, "friendInvite");
        if (linkData == null) {
            TextComponentBuilder.createTextComponent("Il n'y a aucune demande en cours de sa part").setColor(Color.RED).sendTo(apiPlayer);
            return 1;
        }
        linkData.setLinkType("friendRevoke");
        TextComponentBuilder.createTextComponent("Votre demande d'amis à été retiré").setColor(Color.GREEN).sendTo(apiPlayer);

        return 1;

    }

    public int removeCmd(CommandContext<CommandSource> commandContext, Player player, String argument) {
        APIPlayer apiPlayer = API.getInstance().getPlayerManager().getPlayer(player.getUniqueId());
        APIOfflinePlayer target = API.getInstance().getPlayerManager().getOfflinePlayer(argument);
        if (target == null) {
            TextComponentBuilder.createTextComponent("Erreur, la personne n'est pas connue").setColor(Color.RED).sendTo(apiPlayer);
            return 1;
        }

        LinkData linkData = apiPlayer.getLink(LinkUsage.FROM, target, "friend");
        if (linkData == null) {
            TextComponentBuilder.createTextComponent("Cette personne n'est pas dans vos amis").setColor(Color.RED).sendTo(apiPlayer);
            return 1;
        }

        linkData.setLinkType("friendRemove");
        TextComponentBuilder.createTextComponent("Joueur retiré de vos Amis").setColor(Color.GREEN).sendTo(apiPlayer);
        return 1;
    }

    public int listCmd(CommandContext<CommandSource> commandContext, Player player, String argument) {

        APIPlayer apiPlayer = API.getInstance().getPlayerManager().getPlayer(player.getUniqueId());
        List<LinkData> amisList = apiPlayer.getLinks(LinkUsage.BOTH, null, "friend");

        if (amisList.size() == 0) {
            TextComponentBuilder.createTextComponent("Je suis désolée de te l'apprendre, mais tu n'a pas d'amis, en espérant que tu en ais dans la vrai vie").setColor(Color.GREEN).sendTo(apiPlayer);
            return 1;
        }

        TextComponentBuilder tcb = TextComponentBuilder.createTextComponent("Heuresement pour toi, tu as ").setColor(Color.GREEN).appendNewComponentBuilder(Integer.valueOf(amisList.size()).toString()).setColor(Color.BLUE).appendNewComponentBuilder(" amis").setColor(Color.GREEN);

        for (LinkData ami : amisList) {
            String connect = "Déconnecté";
            long amis = ami.getFromPlayer() == apiPlayer.getMemberID() ? ami.getToPlayer() : ami.getFromPlayer();
            Color bc = Color.RED;
            if (API.getInstance().getPlayerManager().isLoadedPlayer(amis)) {
                connect = "Connecté";
                bc = Color.GREEN;
            }
            tcb.appendNewComponentBuilder("\n" + CoreAPI.getInstance().getPlayerManager().getOfflinePlayer(amis).getName() + " ").setColor(Color.WHITE).appendNewComponentBuilder(connect).setColor(bc);
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
