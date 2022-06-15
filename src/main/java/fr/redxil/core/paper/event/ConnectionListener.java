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
import fr.redxil.api.common.player.APIOfflinePlayer;
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.api.common.player.APIPlayerManager;
import fr.redxil.api.common.player.moderators.APIPlayerModerator;
import fr.redxil.api.paper.game.GameBuilder;
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

        Optional<APIPlayer> apiPlayer = API.getInstance().getPlayerManager().getPlayer(p.getUniqueId());
        if (apiPlayer.isPresent() && API.getInstance().isOnlineMod()) {
            event.setResult(PlayerLoginEvent.Result.KICK_OTHER);
        }

    }

    @EventHandler
    public void playerJoin(PlayerJoinEvent event) {

        APIPlayerManager apiPlayerManager = API.getInstance().getPlayerManager();
        Player player = event.getPlayer();

        Optional<APIPlayer> apiPlayer;
        if (API.getInstance().isOnlineMod())
            apiPlayer = apiPlayerManager.getPlayer(player.getUniqueId());
        else {
            if (player.getAddress() == null) {
                player.kickPlayer("No ip data");
                return;
            }
            apiPlayer = apiPlayerManager.loadPlayer(player.getDisplayName(), player.getUniqueId(), new IpInfo(player.getAddress().getHostName(), player.getAddress().getPort()));
            apiPlayer.ifPresent(apiPlayer1 -> API.getInstance().getModeratorManager().loadModerator(apiPlayer1.getMemberID(), apiPlayer1.getUUID(), apiPlayer1.getRealName()));
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

        API.getInstance().getServer().setPlayerConnected(player.getUniqueId(), true);
        apiPlayer.get().setServerName(API.getInstance().getServerName());
        Optional<APIPlayerModerator> playerModerator = API.getInstance().getModeratorManager().getModerator(apiPlayer.get().getMemberID());

        if (playerModerator.isPresent()) {
            corePlugin.getModeratorMain().setModerator(playerModerator.get(), playerModerator.get().isModeratorMod(), true);
            corePlugin.getVanish().setVanish(playerModerator.get(), playerModerator.get().isVanish());
        }

        if (GameBuilder.getGameBuilder().isEmpty())
            return;

        API.getInstance().getGame().ifPresent(game -> {
            if (!game.isSpectator(player.getUniqueId()) && !game.isGameState(GameState.WAITING)) {
                game.setSpectator(player.getUniqueId(), true);
            }
        });

    }

    @EventHandler
    public void playerQuitEvent(PlayerQuitEvent event) {

        Player player = event.getPlayer();

        API.getInstance().getServer().setPlayerConnected(player.getUniqueId(), false);

        corePlugin.getVanish().playerDisconnect(player);
        corePlugin.getFreezeGestion().stopFreezeMessage(player.getUniqueId());

        if (!API.getInstance().isOnlineMod()) {
            API.getInstance().getModeratorManager().getModerator(player.getUniqueId()).ifPresent(APIPlayerModerator::disconnectModerator);
        }

        API.getInstance().getPlayerManager().getOfflinePlayer(player.getUniqueId()).ifPresent(apiOfflinePlayer -> event.setQuitMessage(getQuitMessage(apiOfflinePlayer)));

        Optional<GameBuilder> gameBuilderOptional = GameBuilder.getGameBuilder();

        if (gameBuilderOptional.isEmpty())
            return;

        GameBuilder gameBuilder = gameBuilderOptional.get();

        Optional<Game> gameOptional = API.getInstance().getGame();
        if (gameOptional.isEmpty())
            return;

        Game game = gameOptional.get();

        boolean spectator = game.isSpectator(player.getUniqueId());

        if (!spectator) {
            game.getConnectedPlayers().remove(player.getUniqueId());
            gameBuilder.broadcastActionBar("§a" + player.getName() + "§7 à quitté la partie §8(§a" + game.getConnectedPlayers() + "§8/§e" + game.getMaxPlayer() + "§8)");
            gameBuilder.onPlayerLeave(player);
        } else {
            game.getPlayerSpectators().remove(player.getUniqueId());
            gameBuilder.onSpectatorLeave(player);
        }

    }

    public String getJoinMessage(APIPlayer apiPlayer) {

        return "§fLe joueur " + apiPlayer.getRank().getChatRankString() + apiPlayer.getName() + " §fà rejoint le serveur";

    }

    public String getQuitMessage(APIOfflinePlayer apiPlayer) {

        return "§fLe joueur " + apiPlayer.getRank().getChatRankString() + " " + apiPlayer.getName() + " §fà quitté le serveur";

    }

}
