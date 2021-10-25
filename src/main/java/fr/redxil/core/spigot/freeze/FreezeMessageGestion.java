/* Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 */

package fr.redxil.core.spigot.freeze;

import fr.redxil.core.common.data.PlayerDataValue;
import fr.redxil.api.common.moderators.APIPlayerModerator;
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.api.spigot.utils.Title;
import fr.redxil.core.common.CoreAPI;
import fr.redxil.core.spigot.CorePlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

public class FreezeMessageGestion {

    final CorePlugin corePlugin;
    final HashMap<UUID, Timer> map = new HashMap<>();

    public FreezeMessageGestion(CorePlugin corePlugin) {
        this.corePlugin = corePlugin;
    }

    public void setFreeze(boolean b, APIPlayer apiPlayer, APIPlayerModerator moderator) {
        if (b) {
            Player player = Bukkit.getPlayer(apiPlayer.getUUID());
            if (player == null) return;
            CoreAPI.get().getRedisManager().setRedisLong(PlayerDataValue.PLAYER_FREEZE_REDIS.getString(apiPlayer), moderator.getMemberId());
            sendMessage(player, moderator);
        } else {
            CoreAPI.get().getRedisManager().getRedissonClient().getBucket(PlayerDataValue.PLAYER_FREEZE_REDIS.getString(apiPlayer)).delete();
            stopFreezeMessage(apiPlayer.getUUID());
        }
    }

    public void stopFreezeMessage(UUID uuid) {
        if (map.containsKey(uuid)) {
            Timer timer = map.remove(uuid);
            timer.cancel();
            timer.purge();
            map.remove(uuid);
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) return;
            Title.clearTitle(player);
        }
    }

    public void sendMessage(Player player, APIPlayerModerator APIPlayerModerator) {
        APIPlayer apiPlayer = CoreAPI.get().getPlayerManager().getPlayer(player.getUniqueId());
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                if (APIPlayerModerator.isConnected() && APIPlayerModerator.getAPIPlayer().getServer().getServerName().equals(apiPlayer.getServer().getServerName()))
                    Title.sendTitle(player, "§bAttention", "§8Vous êtes actuellement en inspection", 20, 40, 20);
                else
                    setFreeze(false, apiPlayer, null);
            }
        };

        timer.schedule(timerTask, 0L, 5000L);
        map.put(apiPlayer.getUUID(), timer);
    }

}
