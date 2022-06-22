/*
 *
 * Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 *
 */

package fr.redxil.core.velocity.commands.friend;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import fr.redxil.api.common.message.Color;
import fr.redxil.api.common.message.TextComponentBuilder;
import fr.redxil.api.common.player.APIOfflinePlayer;
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.api.common.player.data.LinkData;
import fr.redxil.api.common.player.data.LinkUsage;
import fr.redxil.api.common.utils.cmd.LiteralArgumentCreator;
import fr.redxil.core.common.CoreAPI;
import net.kyori.adventure.text.TextComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class FriendCmd extends LiteralArgumentCreator<CommandSource> {

    public FriendCmd() {
        super("friend");
        super.setExecutor(this::onMissingArgument);
        super.createThen("cmd", StringArgumentType.word(), this::onMissingArgument)
                .createThen("target", StringArgumentType.greedyString(), this::execute);
    }

    public void onMissingArgument(CommandContext<CommandSource> commandContext, String s) {
        UUID playerUUID = ((Player) commandContext.getSource()).getUniqueId();
        TextComponentBuilder.createTextComponent("Merci de faire /friend list").setColor(Color.RED).sendTo(playerUUID);
    }

    public void execute(CommandContext<CommandSource> commandContext, String s) {
        if (!(commandContext.getSource() instanceof Player player)) {
            return;
        }

        Optional<FriendCmd.ListCmd> usedCmdOpt = FriendCmd.ListCmd.getCommand(commandContext.getArgument("cmd", String.class));
        if (usedCmdOpt.isEmpty()) {
            this.sendCommandList(commandContext);
            return;
        }

        FriendCmd.ListCmd usedCmd = usedCmdOpt.get();

        if (!FriendCmd.ListCmd.getCommand(2).contains(usedCmd)) {
            player.sendMessage((TextComponent) TextComponentBuilder.createTextComponent("Merci de faire /friend " + usedCmd.getName()).setColor(Color.RED).getTextComponent());
            return;
        }

        String nameArg = commandContext.getArgument("target", String.class);

        switch (usedCmd) {
            case INVITE -> this.inviteCmd(commandContext, player, nameArg);
            case REMOVE -> this.removeCmd(commandContext, player, nameArg);
            case REVOKE -> this.revokeCmd(commandContext, player, nameArg);
            case REFUSE -> this.refuseCmd(commandContext, player, nameArg);
            case ACCEPT -> this.acceptCmd(commandContext, player, nameArg);
            case LIST -> this.listCmd(commandContext, player, null);
        }
    }


    public void sendCommandList(CommandContext<CommandSource> commandContext) {
        CommandSource commandSource = commandContext.getSource();
        TextComponentBuilder textComponentBuilder = TextComponentBuilder.createTextComponent("Veuillez utiliser l'une des composantes suivantes:");
        for (FriendCmd.ListCmd cmd : FriendCmd.ListCmd.values()) {
            textComponentBuilder.appendNewComponentBuilder("\n" + Color.GRAY + "> /friend " +
                    cmd.getName() + ": " + Color.BLUE + " " + cmd.getUtility());
        }
        commandSource.sendMessage((TextComponent) textComponentBuilder.getFinalTextComponent());
    }

    public void inviteCmd(CommandContext<CommandSource> commandContext, Player player, String argument) {

        Optional<APIPlayer> apiPlayer = CoreAPI.getInstance().getPlayerManager().getPlayer(player.getUniqueId());
        if (apiPlayer.isEmpty())
            return;
        Optional<APIOfflinePlayer> target = CoreAPI.getInstance().getPlayerManager().getOfflinePlayer(argument);
        if (target.isEmpty()) {
            TextComponentBuilder.createTextComponent("Erreur, la personne n'est pas connue").setColor(Color.RED).sendTo(apiPlayer.get());
            return;
        }

        if (apiPlayer.get().hasLinkWith(LinkUsage.BOTH, target.get(), "friend", "friendInvite")) {
            TextComponentBuilder.createTextComponent("Une ou un semblant de relation existe déjà entre vous").sendTo(apiPlayer.get());
            return;
        }
        apiPlayer.get().createLink(target.get(), "friendInvite");
        TextComponentBuilder.createTextComponent("Demande d'amis envoyée").setColor(Color.GREEN).sendTo(apiPlayer.get());

    }

    public void acceptCmd(CommandContext<CommandSource> commandContext, Player player, String argument) {

        Optional<APIPlayer> apiPlayer = CoreAPI.getInstance().getPlayerManager().getPlayer(player.getUniqueId());
        if (apiPlayer.isEmpty())
            return;
        Optional<APIOfflinePlayer> target = CoreAPI.getInstance().getPlayerManager().getOfflinePlayer(argument);
        if (target.isEmpty()) {
            TextComponentBuilder.createTextComponent("Erreur, la personne n'est pas connue").setColor(Color.RED).sendTo(apiPlayer.get());
            return;
        }

        Optional<LinkData> linkData = apiPlayer.get().getLink(LinkUsage.FROM, target.get(), "friendInvite");
        if (linkData.isEmpty()) {
            TextComponentBuilder.createTextComponent("Il n'y a aucune demande en cours de sa part").setColor(Color.RED).sendTo(apiPlayer.get());
            return;
        }
        linkData.get().setLinkType("friend");
        TextComponentBuilder.createTextComponent("Sa demande d'amis à été accepté").setColor(Color.GREEN).sendTo(apiPlayer.get());

    }

    public void refuseCmd(CommandContext<CommandSource> commandContext, Player player, String argument) {

        Optional<APIPlayer> apiPlayer = CoreAPI.getInstance().getPlayerManager().getPlayer(player.getUniqueId());
        if (apiPlayer.isEmpty())
            return;
        Optional<APIOfflinePlayer> target = CoreAPI.getInstance().getPlayerManager().getOfflinePlayer(argument);
        if (target.isEmpty()) {
            TextComponentBuilder.createTextComponent("Erreur, la personne n'est pas connue").setColor(Color.RED).sendTo(apiPlayer.get());
            return;
        }

        Optional<LinkData> linkData = apiPlayer.get().getLink(LinkUsage.FROM, target.get(), "friendInvite");
        if (linkData.isEmpty()) {
            TextComponentBuilder.createTextComponent("Il n'y a aucune demande en cours de sa part").setColor(Color.RED).sendTo(apiPlayer.get());
            return;
        }
        linkData.get().setLinkType("friendRefused");
        TextComponentBuilder.createTextComponent("Sa demande d'amis à été refusée").setColor(Color.GREEN).sendTo(apiPlayer.get());

    }

    public void revokeCmd(CommandContext<CommandSource> commandContext, Player player, String argument) {

        Optional<APIPlayer> apiPlayer = CoreAPI.getInstance().getPlayerManager().getPlayer(player.getUniqueId());
        if (apiPlayer.isEmpty())
            return;
        Optional<APIOfflinePlayer> target = CoreAPI.getInstance().getPlayerManager().getOfflinePlayer(argument);
        if (target.isEmpty()) {
            TextComponentBuilder.createTextComponent("Erreur, la personne n'est pas connue").setColor(Color.RED).sendTo(apiPlayer.get());
            return;
        }

        Optional<LinkData> linkData = apiPlayer.get().getLink(LinkUsage.TO, target.get(), "friendInvite");
        if (linkData.isEmpty()) {
            TextComponentBuilder.createTextComponent("Il n'y a aucune demande en cours de sa part").setColor(Color.RED).sendTo(apiPlayer.get());
            return;
        }

        linkData.get().setLinkType("friendRevoke");
        TextComponentBuilder.createTextComponent("Votre demande d'amis à été retiré").setColor(Color.GREEN).sendTo(apiPlayer.get());

    }

    public void removeCmd(CommandContext<CommandSource> commandContext, Player player, String argument) {
        Optional<APIPlayer> apiPlayer = CoreAPI.getInstance().getPlayerManager().getPlayer(player.getUniqueId());
        if (apiPlayer.isEmpty())
            return;
        Optional<APIOfflinePlayer> target = CoreAPI.getInstance().getPlayerManager().getOfflinePlayer(argument);
        if (target.isEmpty()) {
            TextComponentBuilder.createTextComponent("Erreur, la personne n'est pas connue").setColor(Color.RED).sendTo(apiPlayer.get());
            return;
        }

        Optional<LinkData> linkData = apiPlayer.get().getLink(LinkUsage.FROM, target.get(), "friend");
        if (linkData.isEmpty()) {
            TextComponentBuilder.createTextComponent("Cette personne n'est pas dans vos amis").setColor(Color.RED).sendTo(apiPlayer.get());
            return;
        }

        linkData.get().setLinkType("friendRemove");
        TextComponentBuilder.createTextComponent("Joueur retiré de vos Amis").setColor(Color.GREEN).sendTo(apiPlayer.get());

    }

    public void listCmd(CommandContext<CommandSource> commandContext, Player player, String argument) {

        Optional<APIPlayer> apiPlayer = CoreAPI.getInstance().getPlayerManager().getPlayer(player.getUniqueId());
        if (apiPlayer.isEmpty())
            return;
        List<LinkData> amisList = apiPlayer.get().getLinks(LinkUsage.BOTH, null, "friend");

        if (amisList.size() == 0) {
            TextComponentBuilder.createTextComponent("Je suis désolée de te l'apprendre, mais tu n'a pas d'amis, en espérant que tu en ais dans la vrai vie").setColor(Color.GREEN).sendTo(apiPlayer.get());
            return;
        }

        TextComponentBuilder tcb = TextComponentBuilder.createTextComponent("Heuresement pour toi, tu as ").setColor(Color.GREEN).appendNewComponentBuilder(Integer.valueOf(amisList.size()).toString()).setColor(Color.BLUE).appendNewComponentBuilder(" amis").setColor(Color.GREEN);

        for (LinkData ami : amisList) {
            String connect = "Déconnecté";
            Color bc = Color.RED;
            Optional<APIOfflinePlayer> amis = CoreAPI.getInstance().getPlayerManager().getOfflinePlayer(ami.getFromPlayer() == apiPlayer.get().getMemberID() ? ami.getToPlayer() : ami.getFromPlayer());
            if (amis.isPresent()) {
                if (amis.get() instanceof Player) {
                    connect = "Connecté";
                    bc = Color.GREEN;
                }
                tcb.appendNewComponentBuilder("\n" + amis.get().getName() + " ").setColor(Color.WHITE).appendNewComponentBuilder(connect).setColor(bc);
            }
        }

        tcb.sendTo(apiPlayer.get());

    }


    public enum ListCmd {
        INVITE("invite", "Envoyer une demande d'amis", 2),
        ACCEPT("accept", "Accepter une demande d'amis", 2),
        REFUSE("refuse", "Refuser une demande d'amis", 2),
        REVOKE("revoke", "Retire votre demande d'amis", 2),
        REMOVE("remove", "Remove un joueur de la liste", 2),
        LIST("list", "Permet de voir la liste des joueurs", 1); /// *

        final String name;
        final String utility;
        final int argument;

        ListCmd(String name, String utility, int argument) {
            this.name = name;
            this.utility = utility;
            this.argument = argument;
        }

        public static Optional<FriendCmd.ListCmd> getCommand(String name) {

            for (FriendCmd.ListCmd cmd : FriendCmd.ListCmd.values()) {
                if (cmd.getName().equalsIgnoreCase(name)) {
                    return Optional.of(cmd);
                }
            }
            return Optional.empty();
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
