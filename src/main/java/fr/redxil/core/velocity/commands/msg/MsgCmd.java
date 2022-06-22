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
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import fr.redxil.api.common.message.Color;
import fr.redxil.api.common.message.TextComponentBuilder;
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.api.common.player.data.LinkUsage;
import fr.redxil.api.common.utils.cmd.LiteralArgumentCreator;
import fr.redxil.core.common.CoreAPI;

import java.util.Optional;
import java.util.UUID;

public class MsgCmd extends LiteralArgumentCreator<CommandSource> {


    public MsgCmd() {
        super("msg");
        super.setExecutor(this::onMissingArgument);

        super.createThen("target", StringArgumentType.word(), this::onMissingArgument).
                createThen("message", StringArgumentType.string(), this::execute);
    }

    public void onMissingArgument(CommandContext<CommandSource> commandContext, String s) {
        UUID playerUUID = ((Player) commandContext.getSource()).getUniqueId();
        TextComponentBuilder.createTextComponent("Merci de faire /msg (pseudo) (message)").setColor(Color.RED).sendTo(playerUUID);
    }

    public void execute(CommandContext<CommandSource> commandContext, String s) {
        if (!(commandContext.getSource() instanceof Player)) return;

        UUID playerUUID = ((Player) commandContext.getSource()).getUniqueId();
        Optional<APIPlayer> sp = CoreAPI.getInstance().getPlayerManager().getPlayer(playerUUID);
        if (sp.isEmpty())
            return;

        Optional<APIPlayer> target = CoreAPI.getInstance().getPlayerManager().getPlayer(commandContext.getArgument("target.get()", String.class));
        if (target.isEmpty()) {
            TextComponentBuilder.createTextComponent("Le joueur: " + commandContext.getArgument("target.get()", String.class) + " n'est pas connecté").setColor(Color.RED).sendTo(playerUUID);
            return;
        }

        if (sp.get().hasLinkWith(LinkUsage.TO, target.get(), "blacklist")) {
            TextComponentBuilder.createTextComponent("Vous ne pouvez pas mp un joueur que vous avez blacklisté").setColor(Color.RED).sendTo(playerUUID);
            return;
        }

        String message = commandContext.getArgument("message", String.class);

        if (!target.get().hasLinkWith(LinkUsage.TO, sp.get(), "blacklist"))
            TextComponentBuilder.createTextComponent(sp.get().getName()).setColor(Color.GREEN).setHover("N'oubliez pas le /blacklist add en cas d'harcélement")
                    .appendNewComponentBuilder(": ").setColor(Color.WHITE)
                    .appendNewComponentBuilder(message).sendTo(target.get().getUUID());

        TextComponentBuilder.createTextComponent(target.get().getName()).setColor(Color.RED)
                .appendNewComponentBuilder(": ").setColor(Color.WHITE)
                .appendNewComponentBuilder(message).sendTo(sp.get().getUUID());

        sp.get().setLastMSGPlayer(target.get().getName());
        target.get().setLastMSGPlayer(sp.get().getName());
    }
}
