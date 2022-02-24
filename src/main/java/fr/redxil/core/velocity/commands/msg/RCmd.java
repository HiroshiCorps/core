/*
 *
 * Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 *
 */

package fr.redxil.core.velocity.commands.msg;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import fr.redxil.api.common.API;
import fr.redxil.api.common.message.Color;
import fr.redxil.api.common.message.TextComponentBuilder;
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.api.velocity.Velocity;
import fr.redxil.core.common.data.player.PlayerDataRedis;
import fr.redxil.core.velocity.commands.BrigadierAPI;

import java.util.UUID;

public class RCmd extends BrigadierAPI<CommandSource> {

    public RCmd() {
        super("r");
    }

    public void onMissingArgument(CommandContext<CommandSource> commandContext) {
        UUID playerUUID = ((Player) commandContext.getSource()).getUniqueId();
        TextComponentBuilder.createTextComponent("Merci de faire /r (message)").setColor(Color.RED).sendTo(playerUUID);
    }

    @Override
    public void onCommandWithoutArgs(CommandContext<CommandSource> commandExecutor) {
        this.onMissingArgument(commandExecutor);
    }

    public void execute(CommandContext<CommandSource> commandContext) {
        if (!(commandContext.getSource() instanceof Player)) return;

        UUID playerUUID = ((Player) commandContext.getSource()).getUniqueId();
        APIPlayer sp = API.getInstance().getPlayerManager().getPlayer(playerUUID);

        String targetName = API.getInstance().getRedisManager().getRedisString(PlayerDataRedis.PLAYER_LASTMSG_REDIS.getString(sp));

        if (targetName == null) {
            TextComponentBuilder.createTextComponent("Erreur, vous avez jusque la pas envoyé de message").setColor(Color.RED).sendTo(playerUUID);
            return;
        }

        Velocity.getInstance().getProxyServer().getCommandManager().executeImmediatelyAsync(commandContext.getSource(), "/msg " + targetName + " " + commandContext.getArgument("message", String.class));

    }

    @Override
    public void registerArgs(LiteralCommandNode<CommandSource> literalCommandNode) {
        this.addArgumentCommand(literalCommandNode, "message", StringArgumentType.greedyString(), this::execute);
    }
}
