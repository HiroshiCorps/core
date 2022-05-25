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
import fr.redxil.api.common.player.moderators.APIPlayerModerator;
import fr.redxil.core.paper.CorePlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class VanishCmd implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, String[] args) {
        if (!(commandSender instanceof Player)) return false;

        Optional<APIPlayerModerator> mod = API.getInstance().getModeratorManager().getModerator(((Player) commandSender).getUniqueId());
        if (mod.isEmpty())
            return false;
        String message;

        boolean newState = !mod.get().isVanish();
        CorePlugin.getInstance().getVanish().setVanish(mod.get(), newState);

        if (!newState)
            message = ChatColor.RED + "Vous n'êtes plus vanish";
        else
            message = ChatColor.GREEN + "Vous êtes vanish";

        commandSender.sendMessage(message);
        return true;
    }
}
