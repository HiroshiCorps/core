/* Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 */

package fr.redxil.core.paper.cmd;

import fr.redxil.api.common.API;
import fr.redxil.api.common.player.moderators.APIPlayerModerator;
import fr.redxil.core.paper.CorePlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ModCmd implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if (!(commandSender instanceof Player)) return false;

        APIPlayerModerator mod = API.getInstance().getModeratorManager().getModerator(((Player) commandSender).getUniqueId());
        if (mod == null)
            return false;
        String message;

        boolean newState = !mod.isModeratorMod();
        CorePlugin.getInstance().getModeratorMain().setModerator(mod, newState, false);

        if (!newState)
            message = ChatColor.RED + "Vous avez quitté le mod modérateur";
        else
            message = ChatColor.GREEN + "Vous avez rejoins le mod modérateur";

        commandSender.sendMessage(message);
        return true;
    }
}
