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
import fr.redxil.api.common.message.Color;
import fr.redxil.api.common.message.TextComponentBuilder;
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.api.velocity.BrigadierAPI;
import fr.redxil.core.common.CoreAPI;
import fr.redxil.core.common.data.PlayerDataValue;

import java.util.UUID;

public class RCmd extends BrigadierAPI {


    public RCmd() {
        super("r");
    }

    @Override
    public int execute(CommandContext<CommandSource> commandContext) {
        if (!(commandContext.getSource() instanceof Player)) return 1;

        UUID playerUUID = ((Player) commandContext.getSource()).getUniqueId();
        APIPlayer sp = CoreAPI.get().getPlayerManager().getPlayer(playerUUID);

        if (commandContext.getArguments().size() < 1) {
            TextComponentBuilder.createTextComponent("Merci de faire /r (message)").setColor(Color.RED).sendTo(playerUUID);
            return 1;
        }

        String targetName = CoreAPI.get().getRedisManager().getRedisString(PlayerDataValue.PLAYER_LASTMSG_REDIS.getString(sp));

        if (targetName == null) {
            TextComponentBuilder.createTextComponent("Erreur, vous avez jusque la pas envoyé de message").setColor(Color.RED).sendTo(playerUUID);
            return 1;
        }

        APIPlayer target = CoreAPI.get().getPlayerManager().getPlayer(targetName);
        if (target == null) {
            TextComponentBuilder.createTextComponent("Le joueur: " + targetName + " n'est pas connecté").setColor(Color.RED).sendTo(playerUUID);
            return 1;
        }

        if (sp.isBlackList(target)) {
            TextComponentBuilder.createTextComponent("Vous ne pouvez pas mp un joueur que vous avez blacklisté").setColor(Color.RED).sendTo(playerUUID);
            return 1;
        }

        if (target.isBlackList(sp)) {
            TextComponentBuilder.createTextComponent("Le joueur: " + targetName + " n'est pas connecté").setColor(Color.RED).sendTo(playerUUID);
            return 1;
        }


        String message = commandContext.getArgument("message", String.class);

        TextComponentBuilder.createTextComponent(sp.getName(true)).setColor(Color.GREEN).setHover("N'oubliez pas le /blacklist add en cas d'harcélement")
                .appendNewComponentBuilder(": ").setColor(Color.WHITE)
                .appendNewComponentBuilder(message).sendTo(target.getUUID());

        TextComponentBuilder.createTextComponent(targetName).setColor(Color.RED)
                .appendNewComponentBuilder(": ").setColor(Color.WHITE)
                .appendNewComponentBuilder(message).sendTo(sp.getUUID());

        return 1;
    }

    @Override
    public void registerArgs(LiteralCommandNode<CommandSource> literalCommandNode) {
        this.addArgumentCommand(literalCommandNode, "message", StringArgumentType.string());
    }
}
