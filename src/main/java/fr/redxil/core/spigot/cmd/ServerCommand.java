/* Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 */

package fr.redxil.core.spigot.cmd;

import fr.redxil.api.common.server.Server;
import fr.redxil.api.common.server.type.ServerStatus;
import fr.redxil.api.spigot.command.CommandBuilder;
import fr.redxil.api.spigot.command.CommandInfo;
import fr.redxil.api.spigot.utils.Title;
import fr.redxil.core.common.CoreAPI;
import fr.redxil.core.spigot.CorePlugin;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

@CommandInfo(
        name = "serveur",
        aliases = "admin",
        permission = 777
)
public class ServerCommand extends CommandBuilder {

    public ServerCommand(JavaPlugin plugin) {
        super(plugin);
    }

    @Override
    public void execute(CommandSender commandSender, Command command, String s, String[] args) {
        Player player = (Player) commandSender;
        Server server = CoreAPI.get().getServer();

        if (args.length == 0) {

            player.sendMessage(" ");
            player.sendMessage("§7§m                                                             ");
            player.sendMessage("§eNom du serveur§7: §6" + server.getServerName());
            player.sendMessage("§eJoueur(s) connecté(s)§7: §6" + server.getPlayerList().size() + "§7/" + server.getMaxPlayers());
            player.sendMessage(" ");
            StringBuilder builder = new StringBuilder();

            builder.append("§8» ");

            for (Player p : Bukkit.getOnlinePlayers()) {
                builder.append("§e").append(p.getName()).append(", ");
            }

            player.sendMessage(builder.toString());

            String status = ServerStatus.ONLINE.toString();

            if (server.isInMaintenance()) {
                status = ServerStatus.MAINTENANCE.toString();
            }

            player.sendMessage(" ");
            player.sendMessage("§eStatus du serveur§7: §6" + status);
            player.sendMessage("§7§m                                                             ");
            player.sendMessage(" ");

        } else {
            switch (args[0]) {
                case "restart":
                    if (args.length == 2) {
                        int seconds = Integer.parseInt(args[1]);

                        for (Player p : Bukkit.getOnlinePlayers()) {
                            p.sendMessage("§8§l» §cRedémarrage du serveur " + CoreAPI.get().getPluginEnabler().getServerName() + " dans " + seconds + " secondes.");
                        }

                        new BukkitRunnable() {
                            int seconds = Integer.parseInt(args[1]);

                            @Override
                            public void run() {
                                if (seconds == 0) {
                                    CoreAPI.get().getPluginEnabler().shutdownServer("§cRedémarrage du serveur");

                                    cancel();
                                }

                                if (seconds <= 5) {
                                    for (Player p : Bukkit.getOnlinePlayers()) {
                                        Title.sendTitle(p, "§c§lRedémarrage", "§7Du serveur dans " + seconds + " secondes !", 10, 30, 10);
                                        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BELL, 40, 40);
                                    }
                                }

                                switch (seconds) {
                                    case 60:
                                    case 45:
                                    case 30:
                                    case 20:
                                    case 15:
                                    case 10:
                                        for (Player p : Bukkit.getOnlinePlayers())
                                            Title.sendTitle(p, "§c§lRedémarrage", "§7Du serveur dans " + seconds + " secondes !", 10, 30, 10);
                                        break;
                                }

                                seconds--;
                            }
                        }.runTaskTimer(CorePlugin.getInstance(), 0, 20);
                    } else {
                        player.sendMessage("§cErreur veuillez utiliser : /server restart <Second>");
                    }
                    break;

                case "maintenance":

                    if (server.isInMaintenance()) {
                        server.changeMaintenance(false);

                        player.sendMessage("§cla maintenance a bien été désactivée");
                    } else {

                        server.changeMaintenance(true);
                        player.sendMessage("§ala maintenance a bien été activée !");
                    }

                    break;
            }
        }

    }
}
