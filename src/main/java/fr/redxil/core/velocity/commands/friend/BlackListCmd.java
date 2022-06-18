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
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import fr.redxil.api.common.message.Color;
import fr.redxil.api.common.message.TextComponentBuilder;
import fr.redxil.api.common.player.APIOfflinePlayer;
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.api.common.player.data.LinkData;
import fr.redxil.api.common.player.data.LinkUsage;
import fr.redxil.core.common.CoreAPI;
import fr.redxil.core.velocity.CoreVelocity;
import fr.redxil.core.velocity.commands.BrigadierAPI;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class BlackListCmd extends BrigadierAPI<CommandSource> {


    public BlackListCmd() {
        super("bl");
    }

    public void onMissingArgument(CommandContext<CommandSource> commandContext) {
        UUID playerUUID = ((Player) commandContext.getSource()).getUniqueId();
        TextComponentBuilder.createTextComponent("Merci de faire /bl (add|remove) (pseudo)").setColor(Color.RED).sendTo(playerUUID);
    }

    @Override
    public void onCommandWithoutArgs(CommandContext<CommandSource> commandExecutor) {
        this.onMissingArgument(commandExecutor);
    }

    public void execute(CommandContext<CommandSource> commandContext) {
        if (!(commandContext.getSource() instanceof Player)) return;

        UUID playerUUID = ((Player) commandContext.getSource()).getUniqueId();

        String cmd = commandContext.getArgument("cmd", String.class);
        String target = commandContext.getArgument("target", String.class);

        if (cmd.equalsIgnoreCase("add") || cmd.equalsIgnoreCase("remove")) {

            Optional<APIOfflinePlayer> osp = CoreAPI.getInstance().getPlayerManager().getOfflinePlayer(target);
            Optional<APIPlayer> sp = CoreAPI.getInstance().getPlayerManager().getPlayer(playerUUID);

            if (osp.isEmpty()) {
                TextComponentBuilder.createTextComponent("Erreur, le joueur: " + target + " est inconnue").setColor(Color.RED).sendTo(playerUUID);
                return;
            }

            if (sp.isEmpty())
                return;

            boolean remove = cmd.equalsIgnoreCase("remove");
            if (remove) {

                Optional<LinkData> linkData = sp.get().getLink(LinkUsage.TO, osp.get(), "blacklist");
                if (linkData.isEmpty()) {
                    TextComponentBuilder.createTextComponent("Erreur, le joueur: " + target + " n'est pas BlackList").setColor(Color.RED).sendTo(playerUUID);
                    return;
                }

                linkData.get().setLinkType("blacklistRevoked");

            } else {

                if (sp.get().hasLinkWith(LinkUsage.TO, osp.get(), "blacklist")) {
                    TextComponentBuilder.createTextComponent("Erreur, le joueur: " + target + " est déjà BlackList").setColor(Color.RED).sendTo(playerUUID);
                    return;
                }

                sp.get().createLink(osp.get(), "blacklist");

            }

            return;
        }

        TextComponentBuilder.createTextComponent("Merci de faire /bl (add|remove) (pseudo)").setColor(Color.RED).sendTo(playerUUID);
    }

    @Override
    public void registerArgs(LiteralCommandNode<CommandSource> literalCommandNode) {
        List<String> playerName = new ArrayList<>();

        for (Player player : CoreVelocity.getInstance().getProxyServer().getAllPlayers()) {
            playerName.add(player.getUsername());
        }

        CommandNode<CommandSource> cmd = this.addArgumentCommand(literalCommandNode, "cmd", StringArgumentType.word(), this::onMissingArgument, "add", "remove");
        this.addArgumentCommand(cmd, "target", StringArgumentType.word(), this::execute, playerName.toArray(new String[0]));
    }
}
