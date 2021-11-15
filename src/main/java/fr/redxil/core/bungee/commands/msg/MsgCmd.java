/* Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 */

package fr.redxil.core.bungee.commands.msg;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.Command;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import fr.redxil.api.common.message.Color;
import fr.redxil.api.common.message.TextComponentBuilder;
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.api.velocity.BrigadierAPI;
import fr.redxil.core.bungee.CoreVelocity;
import fr.redxil.core.common.CoreAPI;
import fr.redxil.core.common.data.PlayerDataValue;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MsgCmd extends BrigadierAPI {


    public MsgCmd() {
        super("msg");
    }

    @Override
    public int execute(CommandContext<CommandSource> commandContext) {
        if (!(commandContext.getSource() instanceof Player)) return 1;

        UUID playerUUID = ((Player) commandContext.getSource()).getUniqueId();
        APIPlayer sp = CoreAPI.get().getPlayerManager().getPlayer(playerUUID);

        if (commandContext.getArguments().size() < 2) {
            TextComponentBuilder.createTextComponent("Merci de faire /msg (pseudo) (message)").setColor(Color.RED).sendTo(playerUUID);
            return 1;
        }

        APIPlayer target = CoreAPI.get().getPlayerManager().getPlayer(commandContext.getArgument("target", String.class));
        if (target == null) {
            TextComponentBuilder.createTextComponent("Le joueur: " + commandContext.getArgument("target", String.class) + " n'est pas connecté").setColor(Color.RED).sendTo(playerUUID);
            return 1;
        }

        if (sp.isBlackList(target)) {
            TextComponentBuilder.createTextComponent("Vous ne pouvez pas mp un joueur que vous avez blacklisté").setColor(Color.RED).sendTo(playerUUID);
            return 1;
        }

        if (target.isBlackList(sp)) {
            TextComponentBuilder.createTextComponent("Le joueur: " + target.getName() + " n'est pas connecté").setColor(Color.RED).sendTo(playerUUID);
            return 1;
        }



        String message = commandContext.getArgument("message", String.class);

        TextComponentBuilder.createTextComponent(sp.getName(true)).setColor(Color.GREEN).setHover("N'oubliez pas le /blacklist add en cas d'harcélement")
                .appendNewComponentBuilder(": ").setColor(Color.WHITE)
                .appendNewComponentBuilder(message).sendTo(target.getUUID());

        TextComponentBuilder.createTextComponent(target.getName()).setColor(Color.RED)
                .appendNewComponentBuilder(": ").setColor(Color.WHITE)
                .appendNewComponentBuilder(message).sendTo(sp.getUUID());

        CoreAPI.get().getRedisManager().setRedisString(PlayerDataValue.PLAYER_LASTMSG_REDIS.getString(sp), target.getName(true));
        CoreAPI.get().getRedisManager().setRedisString(PlayerDataValue.PLAYER_LASTMSG_REDIS.getString(target), sp.getName(true));
        return 1;
    }

    @Override
    public void registerArgs(LiteralCommandNode<CommandSource> literalCommandNode) {
        List<String> playerName = new ArrayList<>();

        for(Player player : CoreVelocity.getInstance().getProxyServer().getAllPlayers()){
            playerName.add(player.getUsername());
        }

        this.addArgumentCommand(literalCommandNode, "target", StringArgumentType.word(), playerName.toArray(new String[0]));
        this.addArgumentCommand(literalCommandNode, "message", StringArgumentType.string());
    }
}
