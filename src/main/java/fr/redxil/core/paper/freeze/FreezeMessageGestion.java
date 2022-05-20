/*
 *
 * Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 *
 */

package fr.redxil.core.paper.freeze;

import fr.redxil.api.common.API;
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.api.common.player.moderators.APIPlayerModerator;
import fr.redxil.core.common.data.player.PlayerDataRedis;
import fr.redxil.core.paper.CorePlugin;
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
            API.getInstance().getRedisManager().setRedisLong(PlayerDataRedis.PLAYER_FREEZE_REDIS.getString(apiPlayer), moderator.getMemberID());
            sendMessage(player, moderator);
        } else {
            API.getInstance().getRedisManager().getRedissonClient().getBucket(PlayerDataRedis.PLAYER_FREEZE_REDIS.getString(apiPlayer)).delete();
            stopFreezeMessage(apiPlayer.getUUID());
        }
    }

    public void stopFreezeMessage(UUID uuid) {
        if (map.containsKey(uuid)) {
            Timer timer = map.remove(uuid);
            timer.cancel();
            timer.purge();
            map.remove(uuid);
        }
    }

    public void sendMessage(Player player, APIPlayerModerator APIPlayerModerator) {
        APIPlayer apiPlayer = API.getInstance().getPlayerManager().getPlayer(player.getUniqueId());
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                APIPlayer apiPlayerMod = API.getInstance().getPlayerManager().getPlayer(APIPlayerModerator.getMemberID());
                if (apiPlayerMod.isConnected() && apiPlayerMod.getServer().getServerName().equals(apiPlayer.getServer().getServerName()))
                    player.sendTitle("§bAttention", "§8Vous êtes actuellement en inspection", 20, 40, 20);
                else
                    setFreeze(false, apiPlayer, null);
            }
        };

        timer.schedule(timerTask, 0L, 5000L);
        map.put(apiPlayer.getUUID(), timer);
    }

}
