/* Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 */

package fr.redxil.core.paper.hosts;

import fr.redxil.api.common.API;
import fr.redxil.api.common.game.Hosts;
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.api.spigot.scoreboard.Scoreboard;
import fr.redxil.api.spigot.scoreboard.ScoreboardBuilder;
import org.bukkit.Bukkit;

import java.util.HashMap;

public class HostScoreboard extends Scoreboard {

    private final Hosts hosts;

    public HostScoreboard(Hosts hosts) {
        super("   §e§l" + hosts.getGame() + " HOST   ", 3, 5);

        this.hosts = hosts;
    }

    @Override
    protected void buildScoreboard(APIPlayer apiPlayer, HashMap<Integer, String> lines) {
        lines.put(0, "§6");
        lines.put(1, "§b§lInfos");
        lines.put(2, "§7╸ §fJoueurs: §b" + Bukkit.getOnlinePlayers().size() + "/" + hosts.getSettings("host_slots"));
        ///lines.put(3, "§7╸ §fÉquipes: §b" + hosts.getHostData().getHostTeamsType().getDisplayName());
        lines.put(3, "§7");
        lines.put(4, "§7╸ §fServeur: §7" + API.getInstance().getPluginEnabler().getServerName().toUpperCase());
        lines.put(5, "");
        lines.put(6, "§6play.swampmc.net");
    }

    @Override
    protected void updateScoreboard(APIPlayer apiPlayer, ScoreboardBuilder scoreboardBuilder) {
        if (hosts.hasSettings("host_name")
                && !hosts.getSettings("host_name").equals("")) {
            scoreboardBuilder.setObjectiveName("§e§l" + hosts.getSettings("host_name") + "");
        } else {
            scoreboardBuilder.setObjectiveName("   §e§l" + hosts.getGame() + " HOST   ");
        }

        scoreboardBuilder.setLine(2, "§7╸ §fJoueurs: §b" + Bukkit.getOnlinePlayers().size() + "/" + hosts.getSettings("host_slots"));
        ///scoreboardBuilder.setLine(3, "§7╸ §fÉquipes: §b" + hosts.getHostData().getHostTeamsType().getDisplayName());
    }
}
