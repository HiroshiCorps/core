/*
 *
 * Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 *
 */

package fr.redxil.core.paper.event;

import fr.redline.pms.utils.IpInfo;
import fr.redxil.api.common.API;
import fr.redxil.api.common.game.Game;
import fr.redxil.api.common.game.utils.GameState;
import fr.redxil.api.common.game.utils.PlayerState;
import fr.redxil.api.common.player.APIOfflinePlayer;
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.api.common.player.APIPlayerManager;
import fr.redxil.api.common.player.moderators.APIPlayerModerator;
import fr.redxil.api.paper.event.connection.PlayerConnectedEvent;
import fr.redxil.api.paper.event.connection.PlayerDisconnectedEvent;
import fr.redxil.api.paper.event.connection.PlayerLoggedEvent;
import fr.redxil.api.paper.game.GameBuilder;
import fr.redxil.core.common.CoreAPI;
import fr.redxil.core.paper.CorePlugin;
import fr.redxil.core.paper.utils.Nick;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Optional;

public record ConnectionListener(CorePlugin corePlugin) implements Listener {

    @EventHandler
    public void playerJoinEvent(PlayerLoginEvent event) {

        Player player = event.getPlayer();

        Optional<APIPlayer> apiPlayerOptional = CoreAPI.getInstance().getPlayerManager().getPlayer(player.getUniqueId());
        Optional<APIPlayerModerator> apiPlayerModeratorOptional = Optional.empty();

        if (apiPlayerOptional.isEmpty() && CoreAPI.getInstance().isOnlineMod()) {
            event.setResult(PlayerLoginEvent.Result.KICK_OTHER);
            return;
        }

        if (apiPlayerOptional.isEmpty()) {

            if (player.getAddress() == null) {
                event.setResult(PlayerLoginEvent.Result.KICK_OTHER);
                return;
            }

            apiPlayerOptional = API.getInstance().getPlayerManager().loadPlayer(player.getDisplayName(), player.getUniqueId(), new IpInfo(player.getAddress().getHostName(), player.getAddress().getPort()));

            if (apiPlayerOptional.isEmpty()) {
                event.setResult(PlayerLoginEvent.Result.KICK_OTHER);
                return;
            }

            APIPlayer apiPlayer = apiPlayerOptional.get();
            apiPlayerModeratorOptional = CoreAPI.getInstance().getModeratorManager().loadModerator(apiPlayer.getMemberID(), apiPlayer.getUUID(), apiPlayer.getRealName());

        }

        APIPlayer apiPlayer = apiPlayerOptional.get();

        Bukkit.getPluginManager().callEvent(new PlayerLoggedEvent(apiPlayer));
        Nick.applyNick(player, apiPlayer);
        corePlugin.getVanish().applyVanish(player);

        apiPlayerModeratorOptional = apiPlayerModeratorOptional.isEmpty() ? CoreAPI.getInstance().getModeratorManager().getModerator(apiPlayer.getMemberID()) : apiPlayerModeratorOptional;

        if (apiPlayerModeratorOptional.isPresent()) {
            APIPlayerModerator playerModerator = apiPlayerModeratorOptional.get();
            corePlugin.getModeratorMain().setModerator(playerModerator, playerModerator.isModeratorMod(), true);
            corePlugin.getVanish().setVanish(playerModerator, playerModerator.isVanish());
        }

    }

    @EventHandler
    public void playerJoin(PlayerJoinEvent event) {

        APIPlayerManager apiPlayerManager = CoreAPI.getInstance().getPlayerManager();
        Player player = event.getPlayer();

        Optional<APIPlayer> apiPlayerOptional = apiPlayerManager.getPlayer(player.getUniqueId());

        if (apiPlayerOptional.isEmpty()) {
            player.kickPlayer("No data");
            return;
        }

        APIPlayer apiPlayer = apiPlayerOptional.get();

        if (event.getJoinMessage() != null) {
            event.setJoinMessage(getJoinMessage(apiPlayer));
        }

        CoreAPI.getInstance().getServer().setPlayerConnected(player.getUniqueId(), true);
        apiPlayer.setServerID(CoreAPI.getInstance().getServerID());

        Bukkit.getPluginManager().callEvent(new PlayerConnectedEvent(apiPlayer));

        Optional<GameBuilder> gameBuilderOptional = GameBuilder.getGameBuilder();
        if (gameBuilderOptional.isEmpty())
            return;

        CoreAPI.getInstance().getGameManager().getGameByServerID(CoreAPI.getInstance().getServerID()).ifPresent(game -> {

            PlayerState playerState = game.getPlayerState(player.getUniqueId());
            GameState gameState = game.getGameState();

            if (playerState == PlayerState.INCONNECT) {
                if (gameState == GameState.START || gameState == GameState.WAITING)
                    gameBuilderOptional.get().onPlayerJoin(player);
                else {
                    game.setPlayerState(player.getUniqueId(), PlayerState.SPECTATE);
                    gameBuilderOptional.get().onSpectatorJoin(player);
                }
            } else {
                if (playerState == null)
                    game.setPlayerState(player.getUniqueId(), PlayerState.MODSPECTATE);
                gameBuilderOptional.get().onSpectatorJoin(player);
            }
        });

    }

    @EventHandler
    public void playerQuitEvent(PlayerQuitEvent event) {

        Player player = event.getPlayer();

        CoreAPI.getInstance().getServer().setPlayerConnected(player.getUniqueId(), false);

        corePlugin.getVanish().playerDisconnect(player);
        corePlugin.getFreezeGestion().stopFreezeMessage(player.getUniqueId());

        if (!CoreAPI.getInstance().isOnlineMod()) {
            CoreAPI.getInstance().getModeratorManager().getModerator(player.getUniqueId()).ifPresent(APIPlayerModerator::disconnectModerator);
        }

        CoreAPI.getInstance().getPlayerManager().getOfflinePlayer(player.getUniqueId()).ifPresent(apiOfflinePlayer -> {
            event.setQuitMessage(getQuitMessage(apiOfflinePlayer));
            Bukkit.getPluginManager().callEvent(new PlayerDisconnectedEvent(apiOfflinePlayer));
        });

        Optional<Game> gameOptional = CoreAPI.getInstance().getGameManager().getGameByServerID(CoreAPI.getInstance().getServerID());
        if (gameOptional.isEmpty())
            return;

        Game game = gameOptional.get();

        boolean spectator = game.isSpectator(player.getUniqueId());

        game.setPlayerState(player.getUniqueId(), PlayerState.DISCONNECTED);

        Optional<GameBuilder> gameBuilderOptional = GameBuilder.getGameBuilder();

        if (gameBuilderOptional.isEmpty())
            return;

        GameBuilder gameBuilder = gameBuilderOptional.get();

        if (!spectator) {
            gameBuilder.broadcastActionBar("§a" + player.getName() + "§7 à quitté la partie §8(§a" + game.getPlayerList(PlayerState.CONNECTED) + "§8/§e" + game.getMaxPlayer() + "§8)");
            gameBuilder.onPlayerLeave(player);
        } else
            gameBuilder.onSpectatorLeave(player);

    }

    public String getJoinMessage(APIPlayer apiPlayer) {

        return "§fLe joueur " + apiPlayer.getRank().getChatRankString() + apiPlayer.getName() + " §fà rejoint le serveur";

    }

    public String getQuitMessage(APIOfflinePlayer apiPlayer) {

        return "§fLe joueur " + apiPlayer.getRank().getChatRankString() + " " + apiPlayer.getName() + " §fà quitté le serveur";

    }

}
