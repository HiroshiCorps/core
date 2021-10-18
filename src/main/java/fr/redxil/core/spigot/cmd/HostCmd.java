/* Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 */

package fr.redxil.core.spigot.cmd;

import fr.redxil.api.spigot.command.CommandBuilder;
import fr.redxil.api.spigot.command.CommandInfo;
import fr.redxil.core.common.CoreAPI;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

@CommandInfo(
        name = "host",
        cooldown = 0L
)
public class HostCmd extends CommandBuilder {

    public HostCmd(JavaPlugin plugin) {
        super(plugin);
    }

    @Override
    public void execute(CommandSender commandSender, Command command, String s, String[] args) {
        Player player = (Player) commandSender;

        if (!CoreAPI.get().isHostServer() || !CoreAPI.get().getGamesManager().isHostExist(CoreAPI.get().getPluginEnabler().getServerName())) {
            player.sendMessage("§cErreur : Vous ne pouvez que éxécuter cette commande sur un serveur host");
            return;
        }

        if (args.length == 0) {
            return;
        }
        switch (args[0]) {
            case "stop":
                CoreAPI.get().getPluginEnabler().shutdownServer("§cServeur d'host fermé");
                break;
        }
    }
}
