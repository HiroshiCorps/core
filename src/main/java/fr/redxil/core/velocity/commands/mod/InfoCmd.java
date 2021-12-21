/* Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
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

import java.util.ArrayList;
import java.util.List;

public class InfoCmd extends BrigadierAPI<CommandSource> {


    public InfoCmd() {
        super("info");
    }

    @Override
    public int execute(CommandContext<CommandSource> commandContext) {
        if (!(commandContext.getSource() instanceof Player))
            return 1;

        APIPlayerModerator playerModerator = API.getInstance().getModeratorManager().getModerator(((Player) commandContext.getSource()).getUniqueId());
        if (playerModerator == null)
            return 1;

        SanctionType sanctionType = SanctionType.getSanctionType(commandContext.getArgument("sanc", String.class));
        if (sanctionType == null) {
            TextComponentBuilder.createTextComponent("Le type de sanction: " + commandContext.getArgument("sanc", String.class) + "n'a pas était reconnue").setColor(Color.RED);
            return 1;
        }

        APIOfflinePlayer target = API.getInstance().getPlayerManager().getOfflinePlayer(commandContext.getArgument("target", String.class));
        playerModerator.printSanction(target, sanctionType);

        return 1;
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

        CommandNode<CommandSource> sanc = this.addArgumentCommand(literalCommandNode, "sanc", StringArgumentType.word(), sanctionName.toArray(new String[0]));

        this.addArgumentCommand(sanc, "target", StringArgumentType.word(), playerName.toArray(new String[0]));

    }
}