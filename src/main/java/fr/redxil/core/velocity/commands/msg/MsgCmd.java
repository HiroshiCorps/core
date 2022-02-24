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
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import fr.redxil.api.common.API;
import fr.redxil.api.common.message.Color;
import fr.redxil.api.common.message.TextComponentBuilder;
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.api.common.player.data.LinkUsage;
import fr.redxil.core.common.data.player.PlayerDataRedis;
import fr.redxil.core.velocity.CoreVelocity;
import fr.redxil.core.velocity.commands.BrigadierAPI;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MsgCmd extends BrigadierAPI<CommandSource> {


    public MsgCmd() {
        super("msg");
    }

    public void onMissingArgument(CommandContext<CommandSource> commandContext) {
        UUID playerUUID = ((Player) commandContext.getSource()).getUniqueId();
        TextComponentBuilder.createTextComponent("Merci de faire /msg (pseudo) (message)").setColor(Color.RED).sendTo(playerUUID);
    }

    @Override
    public void onCommandWithoutArgs(CommandContext<CommandSource> commandExecutor) {
        this.onMissingArgument(commandExecutor);
    }

    public void execute(CommandContext<CommandSource> commandContext) {
        if (!(commandContext.getSource() instanceof Player)) return;

        UUID playerUUID = ((Player) commandContext.getSource()).getUniqueId();
        APIPlayer sp = API.getInstance().getPlayerManager().getPlayer(playerUUID);

        APIPlayer target = API.getInstance().getPlayerManager().getPlayer(commandContext.getArgument("target", String.class));
        if (target == null) {
            TextComponentBuilder.createTextComponent("Le joueur: " + commandContext.getArgument("target", String.class) + " n'est pas connecté").setColor(Color.RED).sendTo(playerUUID);
            return;
        }

        if (sp.hasLinkWith(LinkUsage.TO, target, "blacklist")) {
            TextComponentBuilder.createTextComponent("Vous ne pouvez pas mp un joueur que vous avez blacklisté").setColor(Color.RED).sendTo(playerUUID);
            return;
        }

        String message = commandContext.getArgument("message", String.class);

        if (!target.hasLinkWith(LinkUsage.TO, sp, "blacklist"))
            TextComponentBuilder.createTextComponent(sp.getName()).setColor(Color.GREEN).setHover("N'oubliez pas le /blacklist add en cas d'harcélement")
                    .appendNewComponentBuilder(": ").setColor(Color.WHITE)
                    .appendNewComponentBuilder(message).sendTo(target.getUUID());

        TextComponentBuilder.createTextComponent(target.getName()).setColor(Color.RED)
                .appendNewComponentBuilder(": ").setColor(Color.WHITE)
                .appendNewComponentBuilder(message).sendTo(sp.getUUID());

        API.getInstance().getRedisManager().setRedisString(PlayerDataRedis.PLAYER_LASTMSG_REDIS.getString(sp), target.getName());
        API.getInstance().getRedisManager().setRedisString(PlayerDataRedis.PLAYER_LASTMSG_REDIS.getString(target), sp.getName());
    }

    @Override
    public void registerArgs(LiteralCommandNode<CommandSource> literalCommandNode) {
        List<String> playerName = new ArrayList<>();

        for (Player player : CoreVelocity.getInstance().getProxyServer().getAllPlayers()) {
            playerName.add(player.getUsername());
        }

        CommandNode<CommandSource> target = this.addArgumentCommand(literalCommandNode, "target", StringArgumentType.word(), this::onMissingArgument, playerName.toArray(new String[0]));
        this.addArgumentCommand(target, "message", StringArgumentType.string(), this::execute);
    }
}
