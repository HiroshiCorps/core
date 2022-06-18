/*
 *
 * Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 *
 */

package fr.redxil.core.velocity.commands.mod.highstaff;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import fr.redxil.api.common.message.Color;
import fr.redxil.api.common.message.TextComponentBuilder;
import fr.redxil.api.common.player.moderators.APIPlayerModerator;
import fr.redxil.core.common.CoreAPI;
import fr.redxil.core.velocity.commands.BrigadierAPI;

import java.util.Optional;
import java.util.UUID;

public class StaffCmd extends BrigadierAPI<CommandSource> {


    public StaffCmd() {
        super("staff");
    }

    public void onMissingArgument(CommandContext<CommandSource> commandContext) {
        UUID playerUUID = ((Player) commandContext.getSource()).getUniqueId();
        TextComponentBuilder.createTextComponent("Syntax: /staff" + Color.GREEN + " (message)").setColor(Color.RED).sendTo(playerUUID);
    }

    @Override
    public void onCommandWithoutArgs(CommandContext<CommandSource> commandExecutor) {
        this.onMissingArgument(commandExecutor);
    }

    public void execute(CommandContext<CommandSource> commandContext) {
        if (!(commandContext.getSource() instanceof Player player)) return;

        Optional<APIPlayerModerator> apiPlayerModerator = CoreAPI.getInstance().getModeratorManager().getModerator(((Player) commandContext.getSource()).getUniqueId());

        if (apiPlayerModerator.isEmpty()) {
            TextComponentBuilder.createTextComponent(
                    Color.RED + "Vous n'avez pas la permission d'effectuer cette commande."
            ).sendTo(player.getUniqueId());
            return;
        }

        CoreAPI.getInstance().getModeratorManager().sendToModerators(TextComponentBuilder.createTextComponent("§4{StaffChat} §r" + player.getUsername() + ": " + commandContext.getArgument("message", String.class)));

    }

    @Override
    public void registerArgs(LiteralCommandNode<CommandSource> literalCommandNode) {
        this.addArgumentCommand(literalCommandNode, "message", StringArgumentType.greedyString(), this::execute);
    }
}
