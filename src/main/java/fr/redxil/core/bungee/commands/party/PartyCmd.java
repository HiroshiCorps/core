/* Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 */

package fr.redxil.core.bungee.commands.party;

import com.velocitypowered.api.command.Command;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import fr.redxil.api.common.message.Color;
import fr.redxil.api.common.message.TextComponentBuilder;
import fr.redxil.api.common.message.TextComponentBuilderVelocity;
import fr.redxil.api.common.party.Party;
import fr.redxil.api.common.party.PartyManager;
import fr.redxil.api.common.party.PartyRank;
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.api.common.rank.RankList;
import fr.redxil.core.common.CoreAPI;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Map;


public class PartyCmd implements Command {

    private int timeMax = 15;
    private int time = 0;

    public void execute(CommandSource sender, String @NonNull [] args) {

        if (!(sender instanceof Player)) {
            return;
        }

        Player player = (Player) sender;
        APIPlayer apiPlayer = CoreAPI.get().getPlayerManager().getPlayer(player.getUniqueId());

        PartyManager partyManager = CoreAPI.get().getPartyManager();

        if (apiPlayer.hasPermission(RankList.JOUEUR.getRankPower())) {

            if (args.length == 0) {

                player.sendMessage(((TextComponentBuilderVelocity) TextComponentBuilder.createTextComponent("Liste des commandes").setColor(Color.GREEN)).getFinalTextComponent());

                for (listCmd cmd : listCmd.values()) {

                    player.sendMessage(
                            ((TextComponentBuilderVelocity) TextComponentBuilder.createTextComponent(Color.GRAY + "> /p " +
                                    cmd.getName() + ": " + Color.BLUE + " " + cmd.getUtility())).getFinalTextComponent()
                    );

                }

                return;
            }


            if (args[0].equalsIgnoreCase(listCmd.CREATE.getName())) {

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

                return;
            }

            if (args[0].equalsIgnoreCase(listCmd.INVITE.getName())) {

                if (args.length != 2) {
                    player.sendMessage(
                            ((TextComponentBuilderVelocity) TextComponentBuilder.createTextComponent(Color.RED + "Indiquez le joueur que vous voulez inviter")).getFinalTextComponent()
                    );
                    return;
                }

                if (!partyManager.hasParty(apiPlayer)) {
                    player.sendMessage(
                            ((TextComponentBuilderVelocity) TextComponentBuilder.createTextComponent(Color.RED + "Vous n'êtes pas dans une partie.")).getFinalTextComponent()
                    );
                    return;
                }

                if (!partyManager.isOwner(apiPlayer)) {
                    player.sendMessage(
                            ((TextComponentBuilderVelocity) TextComponentBuilder.createTextComponent(Color.RED + "Vous devez être le chef de la partie.")).getFinalTextComponent()
                    );
                    return;
                }

                APIPlayer targetPlayer = CoreAPI.get().getPlayerManager().getPlayer(args[1]);

                if (partyManager.hasParty(targetPlayer)) {
                    player.sendMessage(
                            ((TextComponentBuilderVelocity) TextComponentBuilder.createTextComponent(Color.RED + "Ce joueur est déja dans une partie.")).getFinalTextComponent()
                    );
                    return;
                }

                if (partyManager.getParty(apiPlayer).invitePlayer(targetPlayer)) {
                    TextComponentBuilder.createTextComponent(Color.WHITE + "Vous avez été invité par " + Color.GREEN + apiPlayer.getName(true));
                    player.sendMessage(((TextComponentBuilderVelocity) TextComponentBuilder.createTextComponent(Color.WHITE + "Vous venez d'inviter " + Color.GREEN + args[1])).getFinalTextComponent());
                } else {
                    player.sendMessage(
                            ((TextComponentBuilderVelocity) TextComponentBuilder.createTextComponent(Color.WHITE + "Vous n'avez pas pûes inviter " + Color.RED + args[1])).getFinalTextComponent()
                    );
                }

            }

            if (args[0].equalsIgnoreCase(listCmd.JOIN.getName())) {

                if (args.length != 2) {
                    player.sendMessage(((TextComponentBuilderVelocity) TextComponentBuilder.createTextComponent(Color.RED + "Merci de faire /party join (player)")).getFinalTextComponent());
                    return;
                }

                APIPlayer owner = CoreAPI.get().getPlayerManager().getPlayer(args[1]);
                if (owner == null) {
                    player.sendMessage(((TextComponentBuilderVelocity) TextComponentBuilder.createTextComponent(Color.RED + "Ce joueur n'existe pas")).getFinalTextComponent());
                    return;
                }

                Party party = CoreAPI.get().getPartyManager().getParty(owner);
                if (party == null) {
                    player.sendMessage(((TextComponentBuilderVelocity) TextComponentBuilder.createTextComponent(Color.RED + "Ce joueur n'a pas de partie")).getFinalTextComponent());
                    return;
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
                return;
            }

            if (args[0].equalsIgnoreCase(listCmd.LEAVE.getName())) {
                if (!partyManager.hasParty(apiPlayer)) {
                    player.sendMessage(
                            ((TextComponentBuilderVelocity) TextComponentBuilder.createTextComponent(Color.GREEN + "Vous devez être dans une partie.")).getFinalTextComponent()
                    );
                    return;
                }

                partyManager.getParty(apiPlayer).quitParty(apiPlayer);

                player.sendMessage(
                        ((TextComponentBuilderVelocity) TextComponentBuilder.createTextComponent(Color.GREEN + "Vous avez quitté votre partie.")).getFinalTextComponent()
                );
                return;
            }

            if (args[0].equalsIgnoreCase(listCmd.LIST.getName())) {

                if (!partyManager.hasParty(apiPlayer)) {
                    player.sendMessage(
                            ((TextComponentBuilderVelocity) TextComponentBuilder.createTextComponent(Color.RED + "Vous devez être dans une partie pour faire ceci.")).getFinalTextComponent()
                    );
                    return;
                }

                player.sendMessage(
                        ((TextComponentBuilderVelocity) TextComponentBuilder.createTextComponent(Color.GREEN + "Liste des membres de la partie:")).getFinalTextComponent()
                );

                for (Map.Entry<String, PartyRank> members : partyManager.getParty(apiPlayer).getList().entrySet()) {
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

            }

        }

    }


    public enum listCmd {
        CREATE("create", "Permet de creer une partie."),
        INVITE("invite", "Permet d'invité un autre joueur."),
        JOIN("join", "Permet de rejoindre une partie."),
        LEAVE("leave", "Permet de leave une partie."),
        LIST("list", "Permet de voir la liste des joueurs");

        String name;
        String utility;

        listCmd(String name, String utility) {
            this.name = name;
            this.utility = utility;
        }

        public static boolean commandExist(String name) {

            for (listCmd cmd : listCmd.values()) {
                if (cmd.getName().equalsIgnoreCase(name)) {
                    return true;
                }
            }
            return false;
        }

        public String getName() {
            return name;
        }

        public String getUtility() {
            return utility;
        }
    }
}
