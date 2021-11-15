/* Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 */

package fr.redxil.core.bungee.commands.mod.highstaff;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import fr.redxil.api.common.message.Color;
import fr.redxil.api.common.message.TextComponentBuilder;
import fr.redxil.api.common.player.moderators.APIPlayerModerator;
import fr.redxil.api.velocity.BrigadierAPI;
import fr.redxil.core.common.CoreAPI;

public class StaffCmd extends BrigadierAPI {


    public StaffCmd() {
        super("staff");
    }

    @Override
    public int execute(CommandContext<CommandSource> commandContext) {
        if (!(commandContext.getSource() instanceof Player)) return 1;

        Player player = (Player) commandContext.getSource();
        APIPlayerModerator APIPlayerModAuthor = CoreAPI.get().getModeratorManager().getModerator(((Player) commandContext.getSource()).getUniqueId());

        if (APIPlayerModAuthor == null) {
            TextComponentBuilder.createTextComponent(
                    Color.RED + "Vous n'avez pas la permission d'effectuer cette commande."
            ).sendTo(player.getUniqueId());
            return 1;
        }

        if (commandContext.getArguments().size() == 0) {
            TextComponentBuilder.createTextComponent(
                    Color.RED + "Syntax: /staff" + Color.GREEN + " (message)"
            ).sendTo(player.getUniqueId());
            return 1;
        }


        CoreAPI.get().getModeratorManager().sendToModerators(TextComponentBuilder.createTextComponent("§4{StaffChat} §r" + player.getUsername() + ": " + commandContext.getArgument("message", String.class)));

        return 1;
    }

    @Override
    public void registerArgs(LiteralCommandNode<CommandSource> literalCommandNode) {
        this.addArgumentCommand(literalCommandNode, "message", StringArgumentType.string());
    }
}
