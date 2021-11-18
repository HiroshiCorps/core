/* Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 */

package fr.redxil.core.paper.cmd;

import fr.redxil.api.common.player.moderators.APIPlayerModerator;
import fr.redxil.core.common.CoreAPI;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class FlyCmd implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String label, String[] args) {
        if (!(commandSender instanceof Player)) return false;

        APIPlayerModerator mod = CoreAPI.get().getModeratorManager().getModerator(((Player) commandSender).getUniqueId());
        if (mod == null)
            return false;

        boolean newState = !((Player) commandSender).getAllowFlight();
        ((Player) commandSender).setAllowFlight(newState);

        commandSender.sendMessage(newState ? ChatColor.GREEN + "Vous avez rejoins le fly" : ChatColor.RED + "Vous avez quitté le fly");
        return true;
    }
}
