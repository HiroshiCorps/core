/* Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
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
import fr.redxil.core.common.data.PlayerDataValue;
import fr.redxil.core.velocity.commands.BrigadierAPI;

import java.util.UUID;

public class RCmd extends BrigadierAPI<CommandSource> {


    public RCmd() {
        super("r");
    }

    @Override
    public int execute(CommandContext<CommandSource> commandContext) {
        if (!(commandContext.getSource() instanceof Player)) return 1;

        UUID playerUUID = ((Player) commandContext.getSource()).getUniqueId();
        APIPlayer sp = API.getInstance().getPlayerManager().getPlayer(playerUUID);

        if (commandContext.getArguments().size() < 1) {
            TextComponentBuilder.createTextComponent("Merci de faire /r (message)").setColor(Color.RED).sendTo(playerUUID);
            return 1;
        }

        String targetName = API.getInstance().getRedisManager().getRedisString(PlayerDataValue.PLAYER_LASTMSG_REDIS.getString(sp));

        if (targetName == null) {
            TextComponentBuilder.createTextComponent("Erreur, vous avez jusque la pas envoyé de message").setColor(Color.RED).sendTo(playerUUID);
            return 1;
        }

        Velocity.getInstance().getProxyServer().getCommandManager().executeImmediatelyAsync(commandContext.getSource(), "/msg " + targetName + " " + commandContext.getArgument("message", String.class));

        return 1;
    }

    @Override
    public void registerArgs(LiteralCommandNode<CommandSource> literalCommandNode) {
        this.addArgumentCommand(literalCommandNode, "message", StringArgumentType.string());
    }
}
