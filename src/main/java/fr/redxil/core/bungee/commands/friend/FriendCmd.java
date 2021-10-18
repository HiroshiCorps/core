/* Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 */

package fr.redxil.core.bungee.commands.friend;

import com.velocitypowered.api.command.Command;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import fr.redxil.api.common.message.Color;
import fr.redxil.api.common.message.TextComponentBuilder;
import fr.redxil.api.common.player.APIOfflinePlayer;
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.api.common.utils.TextUtils;
import fr.redxil.core.common.CoreAPI;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;

public class FriendCmd implements Command {

    public void execute(CommandSource sender, String @NonNull [] args) {

        if (!(sender instanceof Player)) return;
        APIPlayer apiPlayer = CoreAPI.get().getPlayerManager().getPlayer(((Player) sender).getUniqueId());

        if (args.length == 0) {
            TextComponentBuilder.createTextComponent(TextUtils.getPrefix("amis"))
                    .appendNewComponentBuilder("Erreur: Merci de faire /friend help").setColor(Color.RED)
                    .sendTo(apiPlayer);
            return;
        }

        if (args.length == 2)
            if (args[1].equals(apiPlayer.getName())) {
                TextComponentBuilder.createTextComponent(TextUtils.getPrefix("amis"))
                        .appendNewComponentBuilder("Hmm très intéressant, quel est ta vie pour intérargir avec toi-même ?").setColor(Color.RED)
                        .sendTo(apiPlayer);
                return;
            }

        if (args[0].equalsIgnoreCase("help"))
            helpCmd(apiPlayer);
        else if (args[0].equalsIgnoreCase("invite"))
            inviteCmd(apiPlayer, args);
        else if (args[0].equalsIgnoreCase("accept"))
            acceptCmd(apiPlayer, args);
        else if (args[0].equalsIgnoreCase("refuse"))
            refuseCmd(apiPlayer, args);
        else if (args[0].equalsIgnoreCase("revoke"))
            revokeCmd(apiPlayer, args);
        else if (args[0].equalsIgnoreCase("list"))
            listCmd(apiPlayer);
        else if (args[0].equalsIgnoreCase("remove"))
            removeCmd(apiPlayer, args);
        else helpCmd(apiPlayer);

    }

    public void helpCmd(APIPlayer apiPlayer) {
        TextComponentBuilder.createTextComponent(TextUtils.getPrefix("amis"))
                .appendNewComponentBuilder("help\n").setColor(Color.WHITE)
                .appendNewComponentBuilder(TextUtils.getPrefix("amis"))
                .appendNewComponentBuilder("invite\n").setColor(Color.WHITE)
                .appendNewComponentBuilder(TextUtils.getPrefix("amis"))
                .appendNewComponentBuilder("accept\n").setColor(Color.WHITE)
                .appendNewComponentBuilder(TextUtils.getPrefix("amis"))
                .appendNewComponentBuilder("refuse\n").setColor(Color.WHITE)
                .appendNewComponentBuilder(TextUtils.getPrefix("amis"))
                .appendNewComponentBuilder("revoke\n").setColor(Color.WHITE)
                .appendNewComponentBuilder(TextUtils.getPrefix("amis"))
                .appendNewComponentBuilder("list\n").setColor(Color.WHITE)
                .appendNewComponentBuilder(TextUtils.getPrefix("amis"))
                .appendNewComponentBuilder("remove\n").setColor(Color.WHITE).sendTo(apiPlayer);

    }

    public void inviteCmd(APIPlayer apiPlayer, String[] subArgs) {

        if (subArgs.length != 2) {
            TextComponentBuilder.createTextComponent(TextUtils.getPrefix("amis"))
                    .appendNewComponentBuilder("Erreur, merci de faire /friend invite (pseudo)").setColor(Color.RED).sendTo(apiPlayer);
            return;
        }

        APIOfflinePlayer target = CoreAPI.get().getPlayerManager().getOfflinePlayer(subArgs[1]);
        if (target == null) {
            TextComponentBuilder.createTextComponent(TextUtils.getPrefix("amis"))
                    .appendNewComponentBuilder("Erreur, la personne n'est pas connue").setColor(Color.RED).sendTo(apiPlayer);
            return;
        }

        apiPlayer.sendFriendInvite(target);

    }

    public void acceptCmd(APIPlayer apiPlayer, String[] subArgs) {

        if (subArgs.length != 2) {
            TextComponentBuilder.createTextComponent(TextUtils.getPrefix("amis"))
                    .appendNewComponentBuilder("Erreur, merci de faire /friend accept (pseudo)").setColor(Color.RED).sendTo(apiPlayer);
            return;
        }

        APIOfflinePlayer target = CoreAPI.get().getPlayerManager().getOfflinePlayer(subArgs[1]);
        if (target == null) {
            TextComponentBuilder.createTextComponent(TextUtils.getPrefix("amis"))
                    .appendNewComponentBuilder("Erreur, la personne n'est pas connue").setColor(Color.RED).sendTo(apiPlayer);
            return;
        }

        apiPlayer.acceptFriendInvite(target);

    }

    public void refuseCmd(APIPlayer apiPlayer, String[] subArgs) {

        if (subArgs.length != 2) {
            TextComponentBuilder.createTextComponent(TextUtils.getPrefix("amis"))
                    .appendNewComponentBuilder("Erreur, merci de faire /friend refuse (pseudo)").setColor(Color.RED).sendTo(apiPlayer);
            return;
        }

        APIOfflinePlayer target = CoreAPI.get().getPlayerManager().getOfflinePlayer(subArgs[1]);
        if (target == null) {
            TextComponentBuilder.createTextComponent(TextUtils.getPrefix("amis"))
                    .appendNewComponentBuilder("Erreur, la personne n'est pas connue").setColor(Color.RED).sendTo(apiPlayer);
            return;
        }

        apiPlayer.refusedFriendInvite(target);

    }

    public void revokeCmd(APIPlayer apiPlayer, String[] subArgs) {

        if (subArgs.length != 2) {
            TextComponentBuilder.createTextComponent(TextUtils.getPrefix("amis"))
                    .appendNewComponentBuilder("Erreur, merci de faire /friend revoke (pseudo)").setColor(Color.RED).sendTo(apiPlayer);
            return;
        }

        APIOfflinePlayer target = CoreAPI.get().getPlayerManager().getOfflinePlayer(subArgs[1]);
        if (target == null) {
            TextComponentBuilder.createTextComponent(TextUtils.getPrefix("amis"))
                    .appendNewComponentBuilder("Erreur, la personne n'est pas connue").setColor(Color.RED).sendTo(apiPlayer);
            return;
        }

        apiPlayer.revokeFriendInvite(target);

    }

    public void removeCmd(APIPlayer apiPlayer, String[] subArgs) {

        if (subArgs.length != 2) {
            TextComponentBuilder.createTextComponent(TextUtils.getPrefix("amis"))
                    .appendNewComponentBuilder("Erreur, merci de faire /friend remove (pseudo)").setColor(Color.RED).sendTo(apiPlayer);
            return;
        }

        APIOfflinePlayer target = CoreAPI.get().getPlayerManager().getOfflinePlayer(subArgs[1]);
        if (target == null) {
            TextComponentBuilder.createTextComponent(TextUtils.getPrefix("amis"))
                    .appendNewComponentBuilder("Erreur, la personne n'est pas connue").setColor(Color.RED).sendTo(apiPlayer);
            return;
        }

        apiPlayer.removeFriend(target);

    }

    public void listCmd(APIPlayer apiPlayer) {

        List<String> amisList = apiPlayer.getFriendList();

        if (amisList.size() == 0) {
            TextComponentBuilder.createTextComponent(TextUtils.getPrefix("amis"))
                    .appendNewComponentBuilder("Je suis désolée de te l'apprendre, mais tu n'a pas d'amis, en espérant que tu en ais dans la vrai vie").setColor(Color.GREEN).sendTo(apiPlayer);
            return;
        }

        TextComponentBuilder tcb = TextComponentBuilder.createTextComponent(TextUtils.getPrefix("amis"))
                .appendNewComponentBuilder("Heuresement pour toi, tu as ").setColor(Color.GREEN).appendNewComponentBuilder(Integer.valueOf(amisList.size()).toString()).setColor(Color.BLUE).appendNewComponentBuilder(" amis").setColor(Color.GREEN);

        for (String ami : amisList) {
            String connect = "Déconnecté";
            Color bc = Color.RED;
            if (CoreAPI.get().getPlayerManager().isLoadedPlayer(ami)) {
                connect = "Connecté";
                bc = Color.GREEN;
            }
            tcb.appendNewComponentBuilder("\n" + TextUtils.getPrefix("amis")).appendNewComponentBuilder(ami + " ").setColor(Color.WHITE).appendNewComponentBuilder(connect).setColor(bc);
        }

        tcb.sendTo(apiPlayer);

    }

}
