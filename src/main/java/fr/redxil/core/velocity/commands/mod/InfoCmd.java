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
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import fr.redxil.api.common.API;
import fr.redxil.api.common.message.Color;
import fr.redxil.api.common.message.TextComponentBuilder;
import fr.redxil.api.common.player.APIOfflinePlayer;
import fr.redxil.api.common.player.moderators.APIPlayerModerator;
import fr.redxil.api.common.utils.SanctionType;
import fr.redxil.core.velocity.CoreVelocity;
import fr.redxil.core.velocity.commands.BrigadierAPI;
import net.kyori.adventure.text.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class InfoCmd extends BrigadierAPI<CommandSource> {

    public InfoCmd() {
        super("info");
    }

    public void onMissingArgument(CommandContext<CommandSource> commandContext) {
        if (!(commandContext.getSource() instanceof Player))
            return;

        APIPlayerModerator playerModerator = API.getInstance().getModeratorManager().getModerator(((Player) commandContext.getSource()).getUniqueId());
        if (playerModerator == null) {
            commandContext.getSource().sendMessage((Component) TextComponentBuilder.createTextComponent("Vous n'êtes pas modérateur").getFinalTextComponent());
            return;
        }

        APIOfflinePlayer target;
        String targetName = commandContext.getArgument("target", String.class);
        Long targetID = null;
        try {
            targetID = Long.valueOf(targetName);
        } catch (NumberFormatException ignore) {

        }

        if (targetID == null)
            target = API.getInstance().getPlayerManager().getOfflinePlayer(targetName);
        else target = API.getInstance().getPlayerManager().getOfflinePlayer(targetID);

        if (target == null) {
            commandContext.getSource().sendMessage((Component) TextComponentBuilder.createTextComponent("Cible non trouvé").getFinalTextComponent());
            return;
        }

        playerModerator.printInfo(target);
    }

    @Override
    public void onCommandWithoutArgs(CommandContext<CommandSource> commandContext) {
        UUID playerUUID = ((Player) commandContext.getSource()).getUniqueId();
        TextComponentBuilder.createTextComponent("Syntax: /info (joueur) (info|ban|other)").setColor(Color.RED).sendTo(playerUUID);
    }

    public void execute(CommandContext<CommandSource> commandContext) {
        if (!(commandContext.getSource() instanceof Player))
            return;

        APIPlayerModerator playerModerator = API.getInstance().getModeratorManager().getModerator(((Player) commandContext.getSource()).getUniqueId());
        if (playerModerator == null) {
            commandContext.getSource().sendMessage((Component) TextComponentBuilder.createTextComponent("Vous n'êtes pas modérateur").getFinalTextComponent());
            return;
        }

        APIOfflinePlayer target;
        String targetName = commandContext.getArgument("target", String.class);
        Long targetID = null;
        try {
            targetID = Long.valueOf(targetName);
        } catch (NumberFormatException ignore) {

        }

        if(targetID == null)
            target = API.getInstance().getPlayerManager().getOfflinePlayer(targetName);
        else target = API.getInstance().getPlayerManager().getOfflinePlayer(targetID);

        if(target == null){
            commandContext.getSource().sendMessage((Component) TextComponentBuilder.createTextComponent("Cible non trouvé").getFinalTextComponent());
            return;
        }

        String sanc = commandContext.getArgument("sanc", String.class);

        SanctionType sanctionType = SanctionType.getSanctionType(sanc);
        if (sanctionType == null || sanc.equalsIgnoreCase("info")) {
            playerModerator.printInfo(target);
            return;
        }

        playerModerator.printSanction(target, sanctionType);

    }

    @Override
    public void registerArgs(LiteralCommandNode<CommandSource> literalCommandNode) {

        List<String> playerName = new ArrayList<>();

        for (Player player : CoreVelocity.getInstance().getProxyServer().getAllPlayers()) {
            playerName.add(player.getUsername());
        }

        List<String> sanctionName = new ArrayList<>();

        for (SanctionType sanctionType : SanctionType.values()) {
            sanctionName.add(sanctionType.getName());
        }

        CommandNode<CommandSource> sanc = this.addArgumentCommand(literalCommandNode, "target", StringArgumentType.word(), this::onMissingArgument, playerName.toArray(new String[0]));

        this.addArgumentCommand(sanc, "sanc", StringArgumentType.word(), this::execute, sanctionName.toArray(new String[0]));

    }
}