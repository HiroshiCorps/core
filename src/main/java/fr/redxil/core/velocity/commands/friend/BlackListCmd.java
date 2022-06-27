/*
 *
 * Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 *
 */

package fr.redxil.core.velocity.commands.friend;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import fr.redxil.api.common.player.APIOfflinePlayer;
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.api.common.player.data.LinkCheck;
import fr.redxil.api.common.player.data.LinkData;
import fr.redxil.api.common.utils.Color;
import fr.redxil.api.common.utils.cmd.LiteralArgumentCreator;
import fr.redxil.core.common.CoreAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

import java.util.Optional;
import java.util.UUID;

public class BlackListCmd extends LiteralArgumentCreator<CommandSource> {


    public BlackListCmd() {
        super("bl");
        super.setExecutor(this::onMissingArgument);
        super.createThen("cmd", StringArgumentType.word(), this::onMissingArgument)
                .createThen("target", StringArgumentType.word(), this::execute);
    }

    public void onMissingArgument(CommandContext<CommandSource> commandContext, String s) {
        commandContext.getSource().sendMessage(Component.text("Merci de faire /bl (add|remove) (pseudo)").color(TextColor.color(Color.RED.getRed(), Color.RED.getGreen(), Color.RED.getBlue())));
    }

    public void execute(CommandContext<CommandSource> commandContext, String s) {
        if (!(commandContext.getSource() instanceof Player)) return;

        UUID playerUUID = ((Player) commandContext.getSource()).getUniqueId();

        String cmd = commandContext.getArgument("cmd", String.class);
        String target = commandContext.getArgument("target", String.class);

        if (cmd.equalsIgnoreCase("add") || cmd.equalsIgnoreCase("remove")) {

            Optional<APIOfflinePlayer> osp = CoreAPI.getInstance().getPlayerManager().getOfflinePlayer(target);
            Optional<APIPlayer> sp = CoreAPI.getInstance().getPlayerManager().getPlayer(playerUUID);

            if (osp.isEmpty()) {
                commandContext.getSource().sendMessage(Component.text("Erreur, le joueur: " + target + " est inconnue").color(TextColor.color(Color.RED.getRed(), Color.RED.getGreen(), Color.RED.getBlue())));
                return;
            }

            if (sp.isEmpty())
                return;

            boolean remove = cmd.equalsIgnoreCase("remove");
            if (remove) {

                Optional<LinkData> linkData = sp.get().getLink(LinkCheck.SENDER, osp.get(), "blacklist");
                if (linkData.isEmpty()) {
                    commandContext.getSource().sendMessage(Component.text("Erreur, le joueur: " + target + " n'est pas BlackList").color(TextColor.color(Color.RED.getRed(), Color.RED.getGreen(), Color.RED.getBlue())));
                    return;
                }

                linkData.get().deleteLink();

            } else {

                if (sp.get().hasLinkWith(LinkCheck.SENDER, osp.get(), "blacklist")) {
                    commandContext.getSource().sendMessage(Component.text("Erreur, le joueur: " + target + " est déjà BlackList").color(TextColor.color(Color.RED.getRed(), Color.RED.getGreen(), Color.RED.getBlue())));
                    return;
                }

                sp.get().createLink(LinkCheck.SENDER, osp.get(), "blacklist");

            }

            return;
        }
        commandContext.getSource().sendMessage(Component.text("Merci de faire /bl (add|remove) (pseudo)").color(TextColor.color(Color.RED.getRed(), Color.RED.getGreen(), Color.RED.getBlue())));
    }
}
