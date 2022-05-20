/*
 *
 * Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 *
 */

package fr.redxil.core.paper.event;

import fr.redxil.api.common.API;
import fr.redxil.api.common.player.APIOfflinePlayer;
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.api.common.player.moderators.APIPlayerModerator;
import fr.redxil.core.paper.CorePlugin;
import fr.redxil.core.paper.utils.Nick;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public record ConnectionListener(CorePlugin corePlugin) implements Listener {

    @EventHandler
    public void playerJoinEvent(PlayerLoginEvent event) {

        Player p = event.getPlayer();

        APIPlayer apiPlayer = API.getInstance().getPlayerManager().getPlayer(p.getUniqueId());
        if (apiPlayer == null) {
            event.setResult(PlayerLoginEvent.Result.KICK_OTHER);
        }

    }

    @EventHandler
    public void playerJoin(PlayerJoinEvent event) {

        APIPlayer apiPlayer = API.getInstance().getPlayerManager().getPlayer(event.getPlayer().getUniqueId());
        Player player = event.getPlayer();

        if (event.getJoinMessage() != null) {
            sendJoinMessage(apiPlayer);
            event.setJoinMessage(null);
        }

        Nick.applyNick(player, apiPlayer);
        corePlugin.getVanish().applyVanish(player);

        API.getInstance().getServer().setPlayerConnected(player.getUniqueId(), true);
        APIPlayerModerator playerModerator = API.getInstance().getModeratorManager().getModerator(apiPlayer.getMemberID());

        if (playerModerator != null) {
            if (apiPlayer.isLogin()) {
                corePlugin.getModeratorMain().setModerator(playerModerator, playerModerator.isModeratorMod(), true);
                corePlugin.getVanish().setVanish(playerModerator, playerModerator.isVanish());
            }
        }

    }

    @EventHandler
    public void playerQuitEvent(PlayerQuitEvent event) {

        Player player = event.getPlayer();
        APIOfflinePlayer osp = API.getInstance().getPlayerManager().getOfflinePlayer(player.getUniqueId());

        API.getInstance().getServer().setPlayerConnected(player.getUniqueId(), false);

        corePlugin.getVanish().playerDisconnect(player);
        corePlugin.getFreezeGestion().stopFreezeMessage(player.getUniqueId());

        event.setQuitMessage(null);
        sendQuitMessage(osp);

    }

    public void sendJoinMessage(APIPlayer apiPlayer) {

        String message = "§fLe joueur " + apiPlayer.getRank().getChatRankString() + apiPlayer.getName() + " §fà rejoint le serveur";

        Bukkit.getOnlinePlayers().forEach((player) -> player.sendMessage(message));

    }

    public void sendQuitMessage(APIOfflinePlayer apiPlayer) {

        String message = "§fLe joueur " + apiPlayer.getRank().getChatRankString() + " " + apiPlayer.getName() + " §fà quitté le serveur";
        Bukkit.getOnlinePlayers().forEach((player) -> player.sendMessage(message));

    }

}
