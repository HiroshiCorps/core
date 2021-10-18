/* Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 */

package fr.redxil.core.spigot.moderatormode;

import fr.redxil.api.common.data.ModeratorDataValue;
import fr.redxil.api.common.moderators.APIPlayerModerator;
import fr.redxil.api.spigot.event.PlayerJoinModerationEvent;
import fr.redxil.api.spigot.event.PlayerQuitModerationEvent;
import fr.redxil.api.spigot.utils.ModeratorTools;
import fr.redxil.api.spigot.utils.Title;
import fr.redxil.core.common.CoreAPI;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ModeratorMain {

    public void setModerator(APIPlayerModerator APIPlayerModerator, boolean b, boolean onConnection) {

        CoreAPI.get().getRedisManager().setRedisString(ModeratorDataValue.MODERATOR_MOD_REDIS.getString(APIPlayerModerator), Boolean.valueOf(b).toString());

        Player player = Bukkit.getPlayer(APIPlayerModerator.getUUID());
        player.getInventory().clear();

        if (b) {
            player.setCollidable(false);
            ModeratorTools.setModeratorInventory(player);
            if (!onConnection) {
                Title.sendTitle(player, "§b§lModération", "§7Vous êtes en mode modération", 1, 40, 1);
                Bukkit.getPluginManager().callEvent(new PlayerJoinModerationEvent(APIPlayerModerator));
            }
        } else {
            player.setCollidable(true);
            APIPlayerModerator.setCible(null);
            if (!onConnection) {
                Title.sendTitle(player, "§b§lModération", "§7Vous êtes plus en mode modération", 1, 40, 1);
                Bukkit.getPluginManager().callEvent(new PlayerQuitModerationEvent(APIPlayerModerator));
            }
        }

    }

}
