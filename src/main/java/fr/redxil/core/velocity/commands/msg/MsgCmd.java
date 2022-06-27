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
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.api.common.player.data.LinkCheck;
import fr.redxil.api.common.utils.Color;
import fr.redxil.api.common.utils.cmd.LiteralArgumentCreator;
import fr.redxil.core.common.CoreAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

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
        commandContext.getSource().sendMessage(Component.text("Merci de faire /msg (pseudo) (message)").color(TextColor.color(Color.RED.getRed(), Color.RED.getGreen(), Color.RED.getBlue())));
    }

    public void execute(CommandContext<CommandSource> commandContext, String s) {
        if (!(commandContext.getSource() instanceof Player)) return;

        UUID playerUUID = ((Player) commandContext.getSource()).getUniqueId();
        Optional<APIPlayer> sp = CoreAPI.getInstance().getPlayerManager().getPlayer(playerUUID);
        if (sp.isEmpty())
            return;

        Optional<APIPlayer> target = CoreAPI.getInstance().getPlayerManager().getPlayer(commandContext.getArgument("target.get()", String.class));
        if (target.isEmpty()) {
            commandContext.getSource().sendMessage(Component.text("Le joueur: " + commandContext.getArgument("target.get()", String.class) + " n'est pas connecté").color(TextColor.color(Color.RED.getRed(), Color.RED.getGreen(), Color.RED.getBlue())));
            return;
        }

        if (sp.get().hasLinkWith(LinkCheck.SENDER, target.get(), "blacklist")) {
            commandContext.getSource().sendMessage(Component.text("Vous ne pouvez pas mp un joueur que vous avez blacklisté").color(TextColor.color(Color.RED.getRed(), Color.RED.getGreen(), Color.RED.getBlue())));
            return;
        }

        String message = commandContext.getArgument("message", String.class);

        if (!target.get().hasLinkWith(LinkCheck.SENDER, sp.get(), "blacklist")) {
            target.get().sendMessage(
                    Color.RED.getMOTD() + "N'oubliez pas le /blacklist add en cas d'harcélement" +
                            "\n" + sp.get() + Color.WHITE.getBlue() + ": " + message
            );
            target.get().setLastMSGPlayer(sp.get().getName());
        }

        commandContext.getSource().sendMessage(Component.text(Color.WHITE.getMOTD() + target.get().getName() + Color.WHITE.getMOTD() + ": " + message));
        sp.get().setLastMSGPlayer(target.get().getName());
    }
}
