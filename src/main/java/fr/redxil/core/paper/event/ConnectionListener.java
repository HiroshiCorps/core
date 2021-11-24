/* Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 */

package fr.redxil.core.paper.event;

import fr.redxil.api.common.API;
import fr.redxil.api.common.game.Game;
import fr.redxil.api.common.game.GameState;
import fr.redxil.api.common.player.APIOfflinePlayer;
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.api.common.player.moderators.APIPlayerModerator;
import fr.redxil.api.common.player.nick.NickData;
import fr.redxil.api.common.utils.JavaUtils;
import fr.redxil.api.paper.minigame.GameBuilder;
import fr.redxil.core.paper.CorePlugin;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_12_R1.util.CraftChatMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.ArrayList;
import java.util.List;

public class ConnectionListener implements Listener {

    final CorePlugin corePlugin;

    public ConnectionListener(CorePlugin corePlugin) {
        this.corePlugin = corePlugin;
    }

    public static void applyNick(Player p, APIPlayer apiPlayer, boolean connection) {

        EntityPlayer craftPlayerP = ((CraftPlayer) p).getHandle();
        JavaUtils.setDeclaredField(craftPlayerP, "listName", CraftChatMessage.fromString(apiPlayer.getTabString())[0]);
        JavaUtils.setDeclaredField(craftPlayerP.getProfile(), "name", apiPlayer.getName(true));

        craftPlayerP.playerConnection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.UPDATE_DISPLAY_NAME, craftPlayerP));

        if (connection) return;

        PacketPlayOutPlayerInfo removePacket = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, craftPlayerP);
        PacketPlayOutEntityDestroy entityDestroy = new PacketPlayOutEntityDestroy(craftPlayerP.getId());
        PacketPlayOutPlayerInfo joinPacket = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, craftPlayerP);
        PacketPlayOutNamedEntitySpawn entitySpawn = new PacketPlayOutNamedEntitySpawn(craftPlayerP);

        List<Player> playerList = new ArrayList<>(Bukkit.getOnlinePlayers());
        playerList.remove(p);

        playerList.forEach((player) -> {
            EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
            if (player.canSee(p)) {

                PlayerConnection playerConnection = entityPlayer.playerConnection;

                playerConnection.sendPacket(entityDestroy);
                playerConnection.sendPacket(removePacket);
                playerConnection.sendPacket(joinPacket);
                playerConnection.sendPacket(entitySpawn);

            }
        });

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
            boolean spectate = games.isSpectator(apiPlayer.getUUID());

            if (games.getPlayers().size() >= games.getMaxPlayer() && !spectate) {
                event.setResult(PlayerLoginEvent.Result.KICK_FULL);
                return;
            }

            if (!spectate && !games.isGameState(GameState.WAITING, GameState.STARTING)) {
                event.setResult(PlayerLoginEvent.Result.KICK_OTHER);
                return;
            }
        }

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

        applyNick(event.getPlayer(), apiPlayer, true);
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
            gameBuilder.broadcastActionBar("§a" + osp.getName(true) + "§7 à quitté la partie §8(§a" + games.getPlayers() + "§8/§e" + games.getMaxPlayer() + "§8)");
            gameBuilder.onPlayerLeave(player);
        } else {
            if (games.getInGameSpectators().contains(osp.getUUID()))
                games.getInGameSpectators().remove(osp.getUUID());
            else
                games.getOutGameSpectators().remove(osp.getUUID());
            gameBuilder.onSpectatorLeave(player);
        }

    }

    public void sendJoinMessage(APIPlayer apiPlayer) {

        NickData nickData = API.getInstance().getNickGestion().getNickData(apiPlayer);

        String message = "§fLe joueur " + nickData.getRank().getChatRankString() + nickData.getName() + " §fà rejoint le serveur";

        Bukkit.getOnlinePlayers().forEach((player) -> player.sendMessage(message));

    }

    public void sendQuitMessage(APIOfflinePlayer apiPlayer) {

        NickData nickData = API.getInstance().getNickGestion().getNickData(apiPlayer);

        String message = "§fLe joueur " + nickData.getRank().getChatRankString() + " " + nickData.getName() + " §fà quitté le serveur";
        Bukkit.getOnlinePlayers().forEach((player) -> player.sendMessage(message));

    }

}
