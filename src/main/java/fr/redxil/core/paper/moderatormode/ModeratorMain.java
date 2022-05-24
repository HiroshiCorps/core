/*
 *
 * Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 *
 */

package fr.redxil.core.paper.moderatormode;

import fr.redxil.api.common.player.moderators.APIPlayerModerator;
import fr.redxil.api.paper.event.PlayerJoinModerationEvent;
import fr.redxil.api.paper.event.PlayerQuitModerationEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ModeratorMain {

    public void setModerator(APIPlayerModerator apiPlayerModerator, boolean b, boolean onConnection) {

        apiPlayerModerator.setModeratorMod(b);

        Player player = Bukkit.getPlayer(apiPlayerModerator.getUUID());
        assert player != null;
        player.getInventory().clear();

        if (b) {
            player.setCollidable(false);
            if (!onConnection) {
                player.sendTitle("§b§lModération", "§7Vous êtes en mode modération", 1, 40, 1);
                Bukkit.getPluginManager().callEvent(new PlayerJoinModerationEvent(apiPlayerModerator));
            }
        } else {
            player.setCollidable(true);
            apiPlayerModerator.setCible(null);
            if (!onConnection) {
                player.sendTitle("§b§lModération", "§7Vous êtes plus en mode modération", 1, 40, 1);
                Bukkit.getPluginManager().callEvent(new PlayerQuitModerationEvent(apiPlayerModerator));
            }
        }

    }

}
