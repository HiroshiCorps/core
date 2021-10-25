/*
 *
 * Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 *
 */

package fr.redxil.core.spigot.moderatormode;

import fr.redxil.api.spigot.utils.NBTEditor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class UUIDCheckCmd implements CommandExecutor {


    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (!(commandSender instanceof Player))
            return false;
        Player player = ((Player) commandSender).getPlayer();
        if (NBTEditor.contains(player.getInventory().getItemInMainHand(), "uuid")) {

            player.sendMessage(NBTEditor.getString(player.getInventory().getItemInMainHand(), "uuid"));

        } else
            player.sendMessage("No UUID");
        return true;
    }

}
