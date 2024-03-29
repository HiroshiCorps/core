/*
 *
 * Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 *
 */

package fr.redxil.core.velocity.commands.mod;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import fr.redxil.api.common.player.APIOfflinePlayer;
import fr.redxil.api.common.player.moderators.APIPlayerModerator;
import fr.redxil.api.common.utils.Color;
import fr.redxil.api.common.utils.SanctionType;
import fr.redxil.api.common.utils.cmd.LiteralArgumentCreator;
import fr.redxil.core.common.CoreAPI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

import java.util.Optional;

public class InfoCmd extends LiteralArgumentCreator<CommandSource> {

    public InfoCmd() {
        super("info");
        super.setExecutor(this::onCommandWithoutArgs);
        super.createThen("target", StringArgumentType.word(), this::onMissingSanc)
                .createThen("sanc", StringArgumentType.word(), this::execute);
    }

    public void onMissingSanc(CommandContext<CommandSource> commandContext, String s) {
        if (!(commandContext.getSource() instanceof Player))
            return;

        Optional<APIPlayerModerator> playerModerator = CoreAPI.getInstance().getModeratorManager().getModerator(((Player) commandContext.getSource()).getUniqueId());
        if (playerModerator.isEmpty()) {
            commandContext.getSource().sendMessage(Component.text("Vous n'êtes pas modérateur"));
            return;
        }

        Optional<APIOfflinePlayer> target;
        String targetName = commandContext.getArgument("target", String.class);
        Long targetID = null;
        try {
            targetID = Long.valueOf(targetName);
        } catch (NumberFormatException ignore) {

        }

        if (targetID == null)
            target = CoreAPI.getInstance().getPlayerManager().getOfflinePlayer(targetName);
        else target = CoreAPI.getInstance().getPlayerManager().getOfflinePlayer(targetID);

        if (target.isEmpty()) {
            commandContext.getSource().sendMessage(Component.text("Cible non trouvé"));
            return;
        }

        playerModerator.get().printInfo(target.get());
    }

    public void onCommandWithoutArgs(CommandContext<CommandSource> commandContext, String s) {
        commandContext.getSource().sendMessage(Component.text("Syntax: /info (joueur) (info|ban|other)").color(TextColor.color(Color.RED.getRed(), Color.RED.getGreen(), Color.RED.getBlue())));
    }

    public void execute(CommandContext<CommandSource> commandContext, String s) {
        if (!(commandContext.getSource() instanceof Player))
            return;

        Optional<APIPlayerModerator> playerModerator = CoreAPI.getInstance().getModeratorManager().getModerator(((Player) commandContext.getSource()).getUniqueId());
        if (playerModerator.isEmpty()) {
            commandContext.getSource().sendMessage(Component.text("Vous n'êtes pas modérateur"));
            return;
        }

        Optional<APIOfflinePlayer> target;
        String targetName = commandContext.getArgument("target", String.class);
        Long targetID = null;
        try {
            targetID = Long.valueOf(targetName);
        } catch (NumberFormatException ignore) {

        }

        if (targetID == null)
            target = CoreAPI.getInstance().getPlayerManager().getOfflinePlayer(targetName);
        else target = CoreAPI.getInstance().getPlayerManager().getOfflinePlayer(targetID);

        if (target.isEmpty()) {
            commandContext.getSource().sendMessage(Component.text("Cible non trouvé"));
            return;
        }

        String sanc = commandContext.getArgument("sanc", String.class);

        Optional<SanctionType> sanctionType = SanctionType.getSanctionType(sanc);
        if (sanctionType.isEmpty() || sanc.equalsIgnoreCase("info")) {
            playerModerator.get().printInfo(target.get());
            return;
        }

        playerModerator.get().printSanction(target.get(), sanctionType.get());

    }
}