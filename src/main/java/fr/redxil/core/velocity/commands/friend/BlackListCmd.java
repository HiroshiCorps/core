/* Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 */

package fr.redxil.core.velocity.commands.friend;

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
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.api.common.player.data.LinkData;
import fr.redxil.api.common.player.data.LinkUsage;
import fr.redxil.core.velocity.CoreVelocity;
import fr.redxil.core.velocity.commands.BrigadierAPI;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BlackListCmd extends BrigadierAPI<CommandSource> {


    public BlackListCmd() {
        super("bl");
    }

    @Override
    public int execute(CommandContext<CommandSource> commandContext) {
        if (!(commandContext.getSource() instanceof Player)) return 1;

        UUID playerUUID = ((Player) commandContext.getSource()).getUniqueId();

        if (commandContext.getArguments().size() != 2) {
            TextComponentBuilder.createTextComponent("Merci de faire /bl (add|remove) (pseudo)").setColor(Color.RED).sendTo(playerUUID);
            return 1;
        }

        String cmd = commandContext.getArgument("cmd", String.class);
        String target = commandContext.getArgument("target", String.class);

        if (cmd.equalsIgnoreCase("add") || cmd.equalsIgnoreCase("remove")) {

            APIOfflinePlayer osp = API.getInstance().getPlayerManager().getOfflinePlayer(target);
            APIPlayer sp = API.getInstance().getPlayerManager().getPlayer(playerUUID);

            if (osp == null) {
                TextComponentBuilder.createTextComponent("Erreur, le joueur: " + target + " est inconnue").setColor(Color.RED).sendTo(playerUUID);
                return 1;
            }

            boolean remove = cmd.equalsIgnoreCase("remove");
            if (remove) {

                LinkData linkData = sp.getLink(LinkUsage.TO, osp, "blacklist");
                if (linkData == null) {
                    TextComponentBuilder.createTextComponent("Erreur, le joueur: " + target + " n'est pas BlackList").setColor(Color.RED).sendTo(playerUUID);
                    return 1;
                }

                linkData.setLinkType("blacklistRevoked");

            } else {

                if (sp.hasLinkWith(LinkUsage.TO, osp, "blacklist")) {
                    TextComponentBuilder.createTextComponent("Erreur, le joueur: " + target + " est déjà BlackList").setColor(Color.RED).sendTo(playerUUID);
                    return 1;
                }

                sp.createLink(osp, "blacklist");

            }

            return 1;
        }

        TextComponentBuilder.createTextComponent("Merci de faire /bl (add|remove) (pseudo)").setColor(Color.RED).sendTo(playerUUID);

        return 1;
    }

    @Override
    public void registerArgs(LiteralCommandNode<CommandSource> literalCommandNode) {
        List<String> playerName = new ArrayList<>();

        for (Player player : CoreVelocity.getInstance().getProxyServer().getAllPlayers()) {
            playerName.add(player.getUsername());
        }

        CommandNode<CommandSource> cmd = this.addArgumentCommand(literalCommandNode, "cmd", StringArgumentType.word(), "add", "remove");
        this.addArgumentCommand(cmd, "target", StringArgumentType.word(), playerName.toArray(new String[0]));
    }
}
