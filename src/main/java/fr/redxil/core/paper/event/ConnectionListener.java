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
import fr.redxil.api.common.game.Game;
import fr.redxil.api.common.game.utils.GameState;
import fr.redxil.api.common.game.utils.PlayerState;
import fr.redxil.api.common.player.APIOfflinePlayer;
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.api.common.player.APIPlayerManager;
import fr.redxil.api.common.player.moderators.APIPlayerModerator;
import fr.redxil.api.paper.game.GameBuilder;
import fr.redxil.core.common.CoreAPI;
import fr.redxil.core.paper.CorePlugin;
import fr.redxil.core.paper.utils.Nick;
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

        Player p = event.getPlayer();

        Optional<APIPlayer> apiPlayer = CoreAPI.getInstance().getPlayerManager().getPlayer(p.getUniqueId());
        if (apiPlayer.isPresent() && CoreAPI.getInstance().isOnlineMod()) {
            event.setResult(PlayerLoginEvent.Result.KICK_OTHER);
        }

    }

    @EventHandler
    public void playerJoin(PlayerJoinEvent event) {

        APIPlayerManager apiPlayerManager = CoreAPI.getInstance().getPlayerManager();
        Player player = event.getPlayer();

        Optional<APIPlayer> apiPlayer;
        if (CoreAPI.getInstance().isOnlineMod())
            apiPlayer = apiPlayerManager.getPlayer(player.getUniqueId());
        else {
            if (player.getAddress() == null) {
                player.kickPlayer("No ip data");
                return;
            }
            apiPlayer = apiPlayerManager.loadPlayer(player.getDisplayName(), player.getUniqueId(), new IpInfo(player.getAddress().getHostName(), player.getAddress().getPort()));
            apiPlayer.ifPresent(apiPlayer1 -> CoreAPI.getInstance().getModeratorManager().loadModerator(apiPlayer1.getMemberID(), apiPlayer1.getUUID(), apiPlayer1.getRealName()));
        }

        if (apiPlayer.isEmpty()) {
            player.kickPlayer("No data");
            return;
        }

        if (event.getJoinMessage() != null) {
            event.setJoinMessage(getJoinMessage(apiPlayer.get()));
        }

        Nick.applyNick(player, apiPlayer.get());
        corePlugin.getVanish().applyVanish(player);

        CoreAPI.getInstance().getServer().setPlayerConnected(player.getUniqueId(), true);
        apiPlayer.get().setServerID(CoreAPI.getInstance().getServerID());
        Optional<APIPlayerModerator> playerModerator = CoreAPI.getInstance().getModeratorManager().getModerator(apiPlayer.get().getMemberID());

        if (playerModerator.isPresent()) {
            corePlugin.getModeratorMain().setModerator(playerModerator.get(), playerModerator.get().isModeratorMod(), true);
            corePlugin.getVanish().setVanish(playerModerator.get(), playerModerator.get().isVanish());
        }

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

        CoreAPI.getInstance().getPlayerManager().getOfflinePlayer(player.getUniqueId()).ifPresent(apiOfflinePlayer -> event.setQuitMessage(getQuitMessage(apiOfflinePlayer)));

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
