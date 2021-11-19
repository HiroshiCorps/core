/* Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 */

package fr.redxil.core.paper.event;

import fr.redxil.api.common.API;
import fr.redxil.api.common.game.GameState;
import fr.redxil.api.common.game.Games;
import fr.redxil.api.common.game.Hosts;
import fr.redxil.api.common.player.APIOfflinePlayer;
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.api.common.player.moderators.APIPlayerModerator;
import fr.redxil.api.common.player.nick.NickData;
import fr.redxil.api.common.utils.JavaUtils;
import fr.redxil.api.spigot.event.PlayerLoggedEvent;
import fr.redxil.api.spigot.itemstack.APIItemStack;
import fr.redxil.api.spigot.minigame.GameBuilder;
import fr.redxil.api.spigot.minigame.teams.TeamsGUI;
import fr.redxil.api.spigot.utils.NBTEditor;
import fr.redxil.core.paper.CorePlugin;
import fr.redxil.core.paper.hosts.HostScoreboard;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_12_R1.util.CraftChatMessage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class EventListener implements Listener {

    final CorePlugin corePlugin;
    final HostScoreboard hostScoreboard;
    public boolean acceptConnection = true;

    public EventListener(CorePlugin corePlugin) {
        this.corePlugin = corePlugin;

        if (API.getInstance().isHostServer())
            this.hostScoreboard = new HostScoreboard(API.getInstance().getHost());
        else
            this.hostScoreboard = null;
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
    public void asyncJoin(AsyncPlayerPreLoginEvent event) {

        if (acceptConnection) return;
        event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_OTHER, "Connection refused");

    }

    @EventHandler
    public void playerJoinEvent(PlayerJoinEvent event) {

        Player p = event.getPlayer();

        if (!acceptConnection) {
            p.kickPlayer("Connection refused");
            return;
        }

        APIPlayer apiPlayer = API.getInstance().getPlayerManager().getPlayer(p.getUniqueId());
        if (apiPlayer == null) {
            p.kickPlayer("An error appears with you're data");
            return;
        }

        API.getInstance().getServer().setPlayerInServer(apiPlayer);
        APIPlayerModerator playerModerator = API.getInstance().getModeratorManager().getModerator(apiPlayer);

        if (playerModerator != null) {
            if (apiPlayer.isLogin()) {
                corePlugin.getModeratorMain().setModerator(playerModerator, playerModerator.isModeratorMod(), true);
                corePlugin.getVanish().setVanish(playerModerator, playerModerator.isVanish());
                if (playerModerator.isModeratorMod())
                    event.setJoinMessage(null);
            }
        }

        corePlugin.getVanish().applyVanish(p);
        applyNick(p, apiPlayer, true);

        if (event.getJoinMessage() != null && GameBuilder.getGameBuilder() == null) {
            event.setJoinMessage(null);
            if (apiPlayer.isLogin())
                sendJoinMessage(apiPlayer);
        }

        if (GameBuilder.getGameBuilder() == null)
            return;

        GameBuilder gameBuilder = GameBuilder.getGameBuilder();
        Games games = API.getInstance().getGame();

        boolean spectate = games.isSpectator(apiPlayer.getName());

        if (games.getPlayers().size() >= games.getMaxPlayer() && !spectate) {
            p.kickPlayer("§cErreur vous ne pouvez pas rejoindre ce serveur");
            return;
        }

        if (!spectate && !games.isGameState(GameState.WAITING, GameState.STARTING)) {
            p.kickPlayer("§cErreur vous ne pouvez pas rejoindre ce serveur");
            return;
        }

        if (!spectate) {
            games.getInConnectPlayer().remove(apiPlayer.getName());
            gameBuilder.onPlayerJoin(p);
            gameBuilder.broadcastActionBar("§a" + p.getName() + "§7 à rejoins la partie §8(§a" + games.getPlayers() + "§8/§e" + games.getMaxPlayer() + "§8)");

            if (gameBuilder.hasTeams())
                p.getInventory().setItem(0, new APIItemStack(Material.BANNER, 1, (byte) 15).setName("§e§lÉquipes §7(Clique droit)").setOFFInvAction((player, event2) -> {
                    event2.setCancelled(true);
                    new TeamsGUI(player, 0).openGUI(player);
                }));

        } else
            gameBuilder.onSpectatorJoin(p);

        p.getInventory().setItem(8, new APIItemStack(Material.BARRIER).setName("§c§lQuitter §7(Clique droit)"));

        if (!games.isHostLinked()) return;

        Hosts hosts = games.getHost();

        p.sendMessage("Serveur host");
        p.sendMessage("§aBienvenue dans l'host de : " + hosts.getAuthor() + " dans le mode de jeu " + hosts.getGame());

        p.getInventory().setItem(8, new APIItemStack(Material.BED).setName("§c§lQuitter §7(Clique droit)"));
        if (games.isGameState(GameState.WAITING, GameState.STARTING) && apiPlayer.getName().equals(hosts.getAuthor()))
            p.getInventory().setItem(0, new APIItemStack(Material.REDSTONE_COMPARATOR).setName("§e§lConfigurer §7(Clique droit)"));

        if (hostScoreboard != null)
            hostScoreboard.addScoreboard(event.getPlayer());

    }

    @EventHandler
    public void playerQuitEvent(PlayerQuitEvent event) {

        Player player = event.getPlayer();
        APIOfflinePlayer osp = API.getInstance().getPlayerManager().getOfflinePlayer(event.getPlayer().getName());

        API.getInstance().getServer().removePlayerInServer(event.getPlayer().getUniqueId());

        corePlugin.getVanish().playerDisconnect(event.getPlayer());
        corePlugin.getFreezeGestion().stopFreezeMessage(event.getPlayer().getUniqueId());

        event.setQuitMessage(null);
        sendQuitMessage(osp);

        if (GameBuilder.getGameBuilder() == null)
            return;

        GameBuilder gameBuilder = GameBuilder.getGameBuilder();
        Games games = API.getInstance().getGame();

        boolean spectator = games.isSpectator(osp.getName());

        if (!spectator) {
            games.getPlayers().remove(osp.getName());
            gameBuilder.broadcastActionBar("§a" + osp.getName(true) + "§7 à quitté la partie §8(§a" + games.getPlayers() + "§8/§e" + games.getMaxPlayer() + "§8)");
            gameBuilder.onPlayerLeave(player);
        } else {
            if (games.getInGameSpectators().contains(osp.getName()))
                games.getInGameSpectators().remove(osp.getName());
            else
                games.getOutGameSpectators().remove(osp.getName());
            gameBuilder.onSpectatorLeave(player);
        }

        if (!API.getInstance().isHostServer()) return;

        if (hostScoreboard != null)
            hostScoreboard.removeScoreboard(event.getPlayer());

    }

    @EventHandler
    public void playerLogin(PlayerLoggedEvent event) {
        APIPlayerModerator playerModerator = API.getInstance().getModeratorManager().getModerator(event.getAPIPlayer());

        if (playerModerator != null) {
            corePlugin.getModeratorMain().setModerator(playerModerator, playerModerator.isModeratorMod(), true);
            corePlugin.getVanish().setVanish(playerModerator, playerModerator.isVanish());
        }
    }

    public void sendJoinMessage(APIPlayer apiPlayer) {


        NickData nickData = API.getInstance().getNickGestion().getNickData(apiPlayer);

        String message = "§fLe joueur " + nickData.getRank().getChatRankString() + " " + nickData.getName() + " §fà rejoint le serveur";

        Bukkit.getOnlinePlayers().forEach((player) -> player.sendMessage(message));

    }

    public void sendQuitMessage(APIOfflinePlayer apiPlayer) {

        NickData nickData = API.getInstance().getNickGestion().getNickData(apiPlayer);

        String message = "§fLe joueur " + nickData.getRank().getChatRankString() + " " + nickData.getName() + " §fà quitté le serveur";
        Bukkit.getOnlinePlayers().forEach((player) -> player.sendMessage(message));

    }

    @EventHandler
    public void playerChat(AsyncPlayerChatEvent asyncPlayerChatEvent) {

        asyncPlayerChatEvent.setFormat(API.getInstance().getPlayerManager().getPlayer(asyncPlayerChatEvent.getPlayer().getUniqueId()).getChatString() + asyncPlayerChatEvent.getMessage());

    }

    @EventHandler
    public void playerMove(PlayerMoveEvent event) {
        APIPlayer player = API.getInstance().getPlayerManager().getPlayer(event.getPlayer().getUniqueId());
        if (player == null) return;
        if (player.isFreeze())
            event.setCancelled(true);
    }

    @EventHandler
    public void playerDamage(EntityDamageEvent event) {

        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();

        APIPlayer apiPlayer = API.getInstance().getPlayerManager().getPlayer(player.getUniqueId());
        if (apiPlayer.isFreeze())
            event.setCancelled(true);

        APIPlayerModerator spm = API.getInstance().getModeratorManager().getModerator(apiPlayer.getMemberId());
        if (spm != null)
            if (spm.isModeratorMod()) event.setCancelled(true);

    }

    @EventHandler
    public void playerDrop(PlayerDropItemEvent event) {

        APIPlayerModerator spm = API.getInstance().getModeratorManager().getModerator(event.getPlayer().getUniqueId());
        if (spm != null)
            if (spm.isModeratorMod()) event.setCancelled(true);

        if (!NBTEditor.contains(event.getItemDrop(), "uuid"))
            NBTEditor.set(event.getItemDrop(), UUID.randomUUID(), "uuid");

    }

    @EventHandler
    public void playerPickUp(PlayerPickupItemEvent event) {

        APIPlayerModerator spm = API.getInstance().getModeratorManager().getModerator(event.getPlayer().getUniqueId());
        if (spm != null)
            if (spm.isModeratorMod()) event.setCancelled(true);


        if (!NBTEditor.contains(event.getItem(), "uuid"))
            NBTEditor.set(event.getItem(), UUID.randomUUID(), "uuid");

    }

}
