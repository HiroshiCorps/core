/*
 *
 * Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 *
 */

package fr.redxil.core.paper.freeze;

import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.api.common.player.moderators.APIPlayerModerator;
import fr.redxil.core.common.CoreAPI;
import fr.redxil.core.paper.CorePlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

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
            apiPlayer.setFreeze(moderator.getMemberID());
            sendMessage(player, moderator);
        } else {
            apiPlayer.setFreeze(null);
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
        Optional<APIPlayer> apiPlayer = CoreAPI.getInstance().getPlayerManager().getPlayer(player.getUniqueId());
        if (apiPlayer.isEmpty())
            return;
        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                Optional<APIPlayer> apiPlayerMod = CoreAPI.getInstance().getPlayerManager().getPlayer(APIPlayerModerator.getMemberID());
                if (apiPlayerMod.isPresent() && Objects.equals(apiPlayerMod.get().getServerID(), apiPlayer.get().getServerID()))
                    player.sendTitle("§bAttention", "§8Vous êtes actuellement en inspection", 20, 40, 20);
                else
                    setFreeze(false, apiPlayer.get(), null);
            }
        };

        timer.schedule(timerTask, 0L, 5000L);
        map.put(apiPlayer.get().getUUID(), timer);
    }

}
