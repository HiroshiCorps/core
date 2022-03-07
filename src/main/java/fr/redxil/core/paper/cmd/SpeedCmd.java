/*
 *
 * Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 *
 */

package fr.redxil.core.paper.cmd;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SpeedCmd implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {

        if (!(commandSender instanceof Player)) {
            return false;
        }

        if (strings.length != 1) {
            commandSender.sendMessage("Missing speed value");
            return true;
        }

        long value;

        try {
            value = Long.parseLong(strings[0]);
        } catch (NumberFormatException ignored) {
            commandSender.sendMessage("Float not recognized");
            return true;
        }

        if (value > 1 || value < -1) {
            commandSender.sendMessage("Speed value need to be between -1 and 1");
            return true;
        }

        Player player = ((Player) commandSender).getPlayer();
        if (player != null) {
            if (command.getName().equals("speed"))
                player.setWalkSpeed(value);
            else player.setFlySpeed(value);
        }

        return true;
    }
}
