/*
 *
 * Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 *
 */

package fr.redxil.core.paper.cmd;

import fr.redxil.api.common.API;
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.api.common.player.moderators.APIPlayerModerator;
import fr.redxil.core.paper.CorePlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Optional;

public class FreezeCmd implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(commandSender instanceof Player)) return false;

        Optional<APIPlayerModerator> mod = API.getInstance().getModeratorManager().getModerator(((Player) commandSender).getUniqueId());
        if (mod.isEmpty())
            return false;

        if (args.length != 1) {
            commandSender.sendMessage(ChatColor.RED + "Merci d'utiliser: /freeze (pseudo)");
            return true;
        }

        Optional<APIPlayer> target = API.getInstance().getPlayerManager().getPlayer(args[0]);
        if (target.isEmpty()) {
            commandSender.sendMessage(ChatColor.RED + "Joueur: " + args[0] + " introuvable");
            return true;
        }

        String server = target.get().getServerID().toString();

        if (!Objects.equals(server, API.getInstance().getServerName())) {

            commandSender.sendMessage(ChatColor.RED + "Impossible d'interargir avec la cible, server different: " + server);
            return true;

        }

        if (target.get().getRank().isModeratorRank()) {
            commandSender.sendMessage(ChatColor.RED + "Vous n'avez pas la permission d'utiliser cette commande sur votre cible: " + target.get().getName());
            return true;
        }

        String message;

        boolean newState = !target.get().isFreeze();
        CorePlugin.getInstance().getFreezeGestion().setFreeze(newState, target.get(), mod.get());

        if (!newState)
            message = ChatColor.RED + "Le joueur est unfreeze";
        else
            message = ChatColor.GREEN + "Le joueur est freeze";

        commandSender.sendMessage(message);
        return true;
    }
}
