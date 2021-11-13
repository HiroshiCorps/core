/* Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 */

package fr.redxil.core.bungee.commands;

import com.velocitypowered.api.command.Command;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.ServerInfo;
import fr.redxil.api.common.message.TextComponentBuilder;
import fr.redxil.api.velocity.Velocity;
import fr.redxil.core.common.CoreAPI;
import net.kyori.adventure.text.Component;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.net.InetSocketAddress;
import java.util.HashMap;

public class ProxyCmd implements Command {

    HashMap<Integer, String> node = new HashMap<>();

    public ProxyCmd() {
        node.put(1, "Node-1");
    }

    public void execute(CommandSource sender, String @NonNull [] args) {
        if (sender instanceof Player) {
            if (CoreAPI.get().getPlayerManager().getPlayer(((Player) sender).getUsername()).getRankPower() < 700) {
                return;
            }
        }

        if (args[0].equals("load")) {

            if (args.length != 4) {
                sender.sendMessage((Component) TextComponentBuilder.createTextComponent("§cErreur : Veuillez utiliser : /proxy <load/remove> <Node> <Server> <Port>"));
                return;
            }

            if (!node.containsKey(Integer.valueOf(args[1]))) {
                sender.sendMessage((Component) TextComponentBuilder.createTextComponent("§cErreur : Cette node n'existe pas"));
                return;
            }

            if (Velocity.getInstance().getProxyServer().getServer(args[2]).isPresent()) {
                sender.sendMessage((Component) TextComponentBuilder.createTextComponent("§cErreur : Ce serveur est déjà chargé"));
                return;
            }

            ServerInfo serverInfo = new ServerInfo(args[2], new InetSocketAddress("51.210.83.170", Integer.parseInt(args[3])));

            Velocity.getInstance().getProxyServer().registerServer(serverInfo);

            sender.sendMessage((Component) TextComponentBuilder.createTextComponent("le serveur §a" + args[2] + "§7 a été ajouté au proxy avec succès."));

        } else if (args[0].equals("remove")) {

            if (args.length != 3) {
                sender.sendMessage((Component) TextComponentBuilder.createTextComponent("§cErreur : Veuillez utiliser : /proxy <load/remove> <Node> <Server>"));
                return;
            }

            if (!node.containsKey(Integer.valueOf(args[1]))) {
                sender.sendMessage((Component) TextComponentBuilder.createTextComponent("§cErreur : Cette node n'existe pas"));
                return;
            }

            if (!Velocity.getInstance().getProxyServer().getServer(args[2]).isPresent()) {
                sender.sendMessage((Component) TextComponentBuilder.createTextComponent("§cErreur : Ce serveur n'est pas chargé"));
                return;
            }

            Velocity.getInstance().getProxyServer().unregisterServer(Velocity.getInstance().getProxyServer().getServer(args[2]).get().getServerInfo());
            sender.sendMessage((Component) TextComponentBuilder.createTextComponent("le serveur §c" + args[2] + "§7 a été supprimé du proxy avec succès."));

        }

    }
}
