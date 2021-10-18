/* Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 */

package fr.redxil.core.bungee.commands.mod.highstaff;

import com.velocitypowered.api.command.Command;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import fr.redxil.api.common.message.Color;
import fr.redxil.api.common.message.TextComponentBuilder;
import fr.redxil.api.common.moderators.APIPlayerModerator;
import fr.redxil.core.common.CoreAPI;
import org.checkerframework.checker.nullness.qual.NonNull;

public class StaffCmd implements Command {

    public void execute(CommandSource sender, String @NonNull [] args) {
        if (!(sender instanceof Player)) return;

        Player player = (Player) sender;
        APIPlayerModerator APIPlayerModAuthor = CoreAPI.get().getModeratorManager().getModerator(((Player) sender).getUniqueId());

        if (APIPlayerModAuthor == null) {
            TextComponentBuilder.createTextComponent(
                    Color.RED + "Vous n'avez pas la permission d'effectuer cette commande."
            ).sendTo(player.getUniqueId());
            return;
        }

        if (args.length == 0) {
            TextComponentBuilder.createTextComponent(
                    Color.RED + "Syntax: /staff" + Color.GREEN + " (message)"
            ).sendTo(player.getUniqueId());
            return;
        }

        StringBuilder stringBuilder = new StringBuilder(args[0]);
        if (args.length > 1)
            for (int i = 1; i < args.length; i++)
                stringBuilder.append(" ").append(args[i]);

        CoreAPI.get().getModeratorManager().sendToModerators(TextComponentBuilder.createTextComponent("§4{StaffChat} §r" + player.getUsername() + ": " + stringBuilder));

    }
}
