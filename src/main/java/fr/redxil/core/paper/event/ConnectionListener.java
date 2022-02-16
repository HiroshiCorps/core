/* Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 */

package fr.redxil.core.paper.event;

import fr.redxil.api.common.API;
import fr.redxil.api.common.game.Game;
import fr.redxil.api.common.game.utils.GameState;
import fr.redxil.api.common.player.APIOfflinePlayer;
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.api.common.player.moderators.APIPlayerModerator;
import fr.redxil.api.paper.game.GameBuilder;
import fr.redxil.core.paper.CorePlugin;
import fr.redxil.core.paper.utils.Nick;
import fr.redxil.core.paper.utils.PlayerInjector;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ConnectionListener implements Listener {

    final CorePlugin corePlugin;

    public ConnectionListener(CorePlugin corePlugin) {
        this.corePlugin = corePlugin;
    }

    @EventHandler
    public void playerJoinEvent(PlayerLoginEvent event) {

        Player p = event.getPlayer();

        APIPlayer apiPlayer = API.getInstance().getPlayerManager().getPlayer(p.getUniqueId());
        if (apiPlayer == null) {
            event.setResult(PlayerLoginEvent.Result.KICK_OTHER);
            return;
        }

        GameBuilder gameBuilder = GameBuilder.getGameBuilder();
        Game games = API.getInstance().getGame();
        if (gameBuilder != null) {
            if (!games.isSpectator(apiPlayer.getUUID()) && !games.isGameState(GameState.WAITING)) {
                games.setSpectator(apiPlayer.getUUID(), true);
            }
        }

        PlayerInjector.injectPlayer(p);

    }

    public void playerJoinGameServer(Player p, APIPlayer apiPlayer) {

        GameBuilder gameBuilder = GameBuilder.getGameBuilder();
        Game games = API.getInstance().getGame();

        if (gameBuilder == null)
            return;

        boolean spectate = games.isSpectator(p.getUniqueId());

        if (!spectate) {
            games.getInConnectPlayer().remove(apiPlayer.getUUID());
            gameBuilder.onPlayerJoin(p);
            gameBuilder.broadcastActionBar("§a" + p.getName() + "§7 à rejoins la partie §8(§a" + games.getPlayers() + "§8/§e" + games.getMaxPlayer() + "§8)");
        } else
            gameBuilder.onSpectatorJoin(p);

    }

    @EventHandler
    public void playerJoin(PlayerJoinEvent event) {

        APIPlayer apiPlayer = API.getInstance().getPlayerManager().getPlayer(event.getPlayer().getUniqueId());

        if (event.getJoinMessage() != null && GameBuilder.getGameBuilder() == null) {
            event.setJoinMessage(null);
            if (apiPlayer.isLogin())
                sendJoinMessage(apiPlayer);
        }

        Nick.applyNick(event.getPlayer(), apiPlayer);
        corePlugin.getVanish().applyVanish(event.getPlayer());

        API.getInstance().getServer().setPlayerInServer(apiPlayer);
        APIPlayerModerator playerModerator = API.getInstance().getModeratorManager().getModerator(apiPlayer);

        if (playerModerator != null) {
            if (apiPlayer.isLogin()) {
                corePlugin.getModeratorMain().setModerator(playerModerator, playerModerator.isModeratorMod(), true);
                corePlugin.getVanish().setVanish(playerModerator, playerModerator.isVanish());
            }
        }

        if (GameBuilder.getGameBuilder() != null)
            playerJoinGameServer(event.getPlayer(), apiPlayer);

    }

    @EventHandler
    public void playerQuitEvent(PlayerQuitEvent event) {

        Player player = event.getPlayer();
        PlayerInjector.removeInject(player);

        APIOfflinePlayer osp = API.getInstance().getPlayerManager().getOfflinePlayer(event.getPlayer().getUniqueId());

        API.getInstance().getServer().removePlayerInServer(event.getPlayer().getUniqueId());

        corePlugin.getVanish().playerDisconnect(event.getPlayer());
        corePlugin.getFreezeGestion().stopFreezeMessage(event.getPlayer().getUniqueId());

        event.setQuitMessage(null);
        sendQuitMessage(osp);

        if (GameBuilder.getGameBuilder() == null)
            return;

        GameBuilder gameBuilder = GameBuilder.getGameBuilder();
        Game games = API.getInstance().getGame();

        boolean spectator = games.isSpectator(osp.getUUID());

        if (!spectator) {
            games.getPlayers().remove(osp.getUUID());
            gameBuilder.broadcastActionBar("§a" + osp.getName() + "§7 à quitté la partie §8(§a" + games.getPlayers() + "§8/§e" + games.getMaxPlayer() + "§8)");
            gameBuilder.onPlayerLeave(player);
        } else {
            if (games.getPlayerSpectators().contains(osp.getUUID()))
                games.getPlayerSpectators().remove(osp.getUUID());
            else
                games.getModeratorSpectators().remove(osp.getUUID());
            gameBuilder.onSpectatorLeave(player);
        }

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
