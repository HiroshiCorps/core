/* Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 */

package fr.redxil.core.velocity.commands.party;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import fr.redxil.api.common.API;
import fr.redxil.api.common.group.party.Party;
import fr.redxil.api.common.group.party.PartyManager;
import fr.redxil.api.common.group.party.PartyRank;
import fr.redxil.api.common.message.Color;
import fr.redxil.api.common.message.TextComponentBuilder;
import fr.redxil.api.common.message.TextComponentBuilderVelocity;
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.core.velocity.CoreVelocity;
import fr.redxil.core.velocity.commands.BrigadierAPI;
import net.kyori.adventure.text.TextComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class PartyCmd extends BrigadierAPI<CommandSource> {

    public PartyCmd() {
        super("party");
    }

    @Override
    public int execute(CommandContext<CommandSource> commandContext) {
        if (!(commandContext.getSource() instanceof Player)) {
            return 0;
        }

        Player player = (Player) commandContext.getSource();

        String usedCmd = commandContext.getArgument("cmd", String.class);
        if (usedCmd == null) {
            return this.sendCommandList(commandContext);
        }

        int i = 0;
        boolean cmdAvailable = false;

        while (!cmdAvailable && i < ListCmd.values().length) {

            if (ListCmd.values()[i].getName().equalsIgnoreCase(usedCmd)) {
                cmdAvailable = true;
            }

            i++;

        }

        String nameArg = commandContext.getArgument("name", String.class);


        switch (Objects.requireNonNull(ListCmd.getCommand(usedCmd))) {
            case LEAVE: {
                return leaveCmd(commandContext, player, null);
            }
            case LIST: {
                return listCmd(commandContext, player, null);
            }
            case CREATE: {
                return createCmd(commandContext, player, null);
            }
            case JOIN: {
                if (nameArg == null) {
                    return this.sendCommandList(commandContext);
                }
                return joinCmd(commandContext, player, nameArg);
            }
            case INVITE: {
                if (nameArg == null) {
                    return this.sendCommandList(commandContext);
                }
                return inviteCmd(commandContext, player, nameArg);
            }

        }
        return 1;
    }

    @Override
    public void registerArgs(LiteralCommandNode<CommandSource> literalCommandNode) {

        List<String> playerName = new ArrayList<>();

        for (Player player : CoreVelocity.getInstance().getProxyServer().getAllPlayers()) {
            playerName.add(player.getUsername());
        }

        CommandNode<CommandSource> cmd = this.addArgumentCommand(literalCommandNode, "cmd", StringArgumentType.word(), playerName.toArray(new String[0]));
        this.addArgumentCommand(cmd, "name", StringArgumentType.greedyString());

    }


    public int sendCommandList(CommandContext<CommandSource> commandContext) {
        CommandSource commandSource = commandContext.getSource();
        TextComponentBuilder textComponentBuilder = TextComponentBuilder.createTextComponent("Veuillez utiliser l'une des composantes suivantes:");
        for (ListCmd cmd : ListCmd.values()) {
            textComponentBuilder.appendNewComponentBuilder("\n" + Color.GRAY + "> /party " +
                    cmd.getName() + ": " + Color.BLUE + " " + cmd.getUtility());
        }
        commandSource.sendMessage((TextComponent) textComponentBuilder.getFinalTextComponentBuilder().getTextComponent());
        return 1;
    }

    public int createCmd(CommandContext<CommandSource> commandContext, Player player, String subArgument) {
        PartyManager partyManager = API.getInstance().getPartyManager();
        APIPlayer apiPlayer = API.getInstance().getPlayerManager().getPlayer(player.getUniqueId());
        if (partyManager.createParty(apiPlayer) != null) {
            player.sendMessage(
                    ((TextComponentBuilderVelocity) TextComponentBuilder.createTextComponent("Vous venez de creer une partie.").setColor(Color.GREEN)).getFinalTextComponent()
            );
        } else {
            player.sendMessage(
                    ((TextComponentBuilderVelocity) TextComponentBuilder.createTextComponent(Color.GREEN + "Vous êtes déjà dans une partie " +
                            Color.BLUE + "/party leave " +
                            Color.GREEN + "pour quitter votre partie.")).getFinalTextComponent()
            );
        }

        return 1;
    }


    public int leaveCmd(CommandContext<CommandSource> commandContext, Player player, String subArgument) {
        PartyManager partyManager = API.getInstance().getPartyManager();
        APIPlayer apiPlayer = API.getInstance().getPlayerManager().getPlayer(player.getUniqueId());

        if (!partyManager.hasParty(apiPlayer)) {
            player.sendMessage(
                    ((TextComponentBuilderVelocity) TextComponentBuilder.createTextComponent(Color.GREEN + "Vous devez être dans une partie.")).getFinalTextComponent()
            );
            return 1;
        }

        partyManager.getPlayerParty(apiPlayer).quitParty(apiPlayer);

        player.sendMessage(
                ((TextComponentBuilderVelocity) TextComponentBuilder.createTextComponent(Color.GREEN + "Vous avez quitté votre partie.")).getFinalTextComponent()
        );
        return 1;
    }


    public int listCmd(CommandContext<CommandSource> commandContext, Player player, String subArgument) {
        PartyManager partyManager = API.getInstance().getPartyManager();
        APIPlayer apiPlayer = API.getInstance().getPlayerManager().getPlayer(player.getUniqueId());
        if (!partyManager.hasParty(apiPlayer)) {
            player.sendMessage(
                    ((TextComponentBuilderVelocity) TextComponentBuilder.createTextComponent(Color.RED + "Vous devez être dans une partie pour faire ceci.")).getFinalTextComponent()
            );
            return 1;
        }

        player.sendMessage(
                ((TextComponentBuilderVelocity) TextComponentBuilder.createTextComponent(Color.GREEN + "Liste des membres de la partie:")).getFinalTextComponent()
        );

        for (Map.Entry<String, PartyRank> members : partyManager.getPlayerParty(apiPlayer).getRank().entrySet()) {
            String name = members.getKey();
            if (members.getValue() == PartyRank.OWNER) {
                player.sendMessage(
                        ((TextComponentBuilderVelocity) TextComponentBuilder.createTextComponent(Color.WHITE + name + Color.YELLOW + " > " + members.getValue().getRankName())).getFinalTextComponent()
                );
            }

            if (members.getValue() == PartyRank.PLAYER) {
                player.sendMessage(
                        ((TextComponentBuilderVelocity) TextComponentBuilder.createTextComponent(Color.WHITE + name + Color.BLUE + " > Membre")).getFinalTextComponent()
                );
            }
        }
        return 1;
    }


    public int joinCmd(CommandContext<CommandSource> commandContext, Player player, String subArgument) {
        APIPlayer apiPlayer = API.getInstance().getPlayerManager().getPlayer(player.getUniqueId());
        APIPlayer owner = API.getInstance().getPlayerManager().getPlayer(subArgument);
        if (owner == null) {
            player.sendMessage(((TextComponentBuilderVelocity) TextComponentBuilder.createTextComponent(Color.RED + "Ce joueur n'existe pas")).getFinalTextComponent());
            return 1;
        }

        Party party = API.getInstance().getPartyManager().getPlayerParty(owner);
        if (party == null) {
            player.sendMessage(((TextComponentBuilderVelocity) TextComponentBuilder.createTextComponent(Color.RED + "Ce joueur n'a pas de partie")).getFinalTextComponent());
            return 1;
        }

        if (party.joinParty(apiPlayer)) {

            player.sendMessage(
                    ((TextComponentBuilderVelocity) TextComponentBuilder.createTextComponent(Color.GREEN + "Vous venez de rejoindre la partie.")).getFinalTextComponent()
            );

        } else {

            player.sendMessage(
                    ((TextComponentBuilderVelocity) TextComponentBuilder.createTextComponent(Color.RED + "Vous n'avez pas pûes rejoindre la partie.")).getFinalTextComponent()
            );

        }
        return 1;
    }


    public int inviteCmd(CommandContext<CommandSource> commandContext, Player player, String subArgument) {
        PartyManager partyManager = API.getInstance().getPartyManager();
        APIPlayer apiPlayer = API.getInstance().getPlayerManager().getPlayer(player.getUniqueId());

        if (!partyManager.hasParty(apiPlayer)) {
            player.sendMessage(
                    ((TextComponentBuilderVelocity) TextComponentBuilder.createTextComponent(Color.RED + "Vous n'êtes pas dans une partie.")).getFinalTextComponent()
            );
            return 1;
        }

        if (!partyManager.getPlayerParty(apiPlayer).isPartyOwner(apiPlayer)) {
            player.sendMessage(
                    ((TextComponentBuilderVelocity) TextComponentBuilder.createTextComponent(Color.RED + "Vous devez être le chef de la partie.")).getFinalTextComponent()
            );
            return 1;
        }

        APIPlayer targetPlayer = API.getInstance().getPlayerManager().getPlayer(subArgument);

        if (partyManager.hasParty(targetPlayer)) {
            player.sendMessage(
                    ((TextComponentBuilderVelocity) TextComponentBuilder.createTextComponent(Color.RED + "Ce joueur est déja dans une partie.")).getFinalTextComponent()
            );
            return 1;
        }

        if (partyManager.getPlayerParty(apiPlayer).invitePlayer(targetPlayer)) {
            TextComponentBuilder.createTextComponent(Color.WHITE + "Vous avez été invité par " + Color.GREEN + apiPlayer.getName(true));
            player.sendMessage(((TextComponentBuilderVelocity) TextComponentBuilder.createTextComponent(Color.WHITE + "Vous venez d'inviter " + Color.GREEN + subArgument)).getFinalTextComponent());
        } else {
            player.sendMessage(
                    ((TextComponentBuilderVelocity) TextComponentBuilder.createTextComponent(Color.WHITE + "Vous n'avez pas pûes inviter " + Color.RED + subArgument)).getFinalTextComponent()
            );
        }
        return 1;
    }


    public enum ListCmd {
        CREATE("create", "Permet de creer une partie.", 1),
        INVITE("invite", "Permet d'invité un autre joueur.", 2),
        JOIN("join", "Permet de rejoindre une partie.", 2),
        LEAVE("leave", "Permet de leave une partie.", 1), /// *
        LIST("list", "Permet de voir la liste des joueurs", 1); /// *

        String name;
        String utility;
        int argument;

        ListCmd(String name, String utility, int argument) {
            this.name = name;
            this.utility = utility;
            this.argument = argument;
        }

        public static ListCmd getCommand(String name) {

            for (ListCmd cmd : ListCmd.values()) {
                if (cmd.getName().equalsIgnoreCase(name)) {
                    return cmd;
                }
            }
            return null;
        }

        public static List<ListCmd> getCommand(int argument) {

            List<ListCmd> cmdList = new ArrayList<>();

            for (ListCmd cmd : ListCmd.values()) {
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
