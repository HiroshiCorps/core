/* Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 */

package fr.redxil.core.spigot.cmd;

import fr.redxil.api.common.moderators.APIPlayerModerator;
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.core.common.CoreAPI;
import fr.redxil.core.spigot.CorePlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FreezeCmd implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if (!(commandSender instanceof Player)) return false;

        APIPlayerModerator mod = CoreAPI.get().getModeratorManager().getModerator(((Player) commandSender).getUniqueId());
        if (mod == null)
            return false;

        if (args.length != 1) {
            commandSender.sendMessage(ChatColor.RED + "Merci d'utiliser: /freeze (pseudo)");
            return true;
        }

        APIPlayer target = CoreAPI.get().getPlayerManager().getPlayer(args[0]);
        if (target == null) {
            commandSender.sendMessage(ChatColor.RED + "Joueur: " + args[0] + " introuvable");
            return true;
        }

        if (!target.getServer().getServerName().equals(CoreAPI.get().getPluginEnabler().getServerName())) {

            commandSender.sendMessage(ChatColor.RED + "Impossible d'interargir avec la cible, server different: " + target.getServer().getServerName());
            return true;

        }

        if (target.getRank().isModeratorRank()) {
            commandSender.sendMessage(ChatColor.RED + "Vous n'avez pas la permission d'utiliser cette commande sur votre cible: " + target.getName());
            return true;
        }

        String message;

        boolean newState = !target.isFreeze();
        CorePlugin.getInstance().getFreezeGestion().setFreeze(newState, target, mod);

        if (!newState)
            message = ChatColor.RED + "Le joueur est unfreeze";
        else
            message = ChatColor.GREEN + "Le joueur est freeze";

        commandSender.sendMessage(message);
        return true;
    }
}
