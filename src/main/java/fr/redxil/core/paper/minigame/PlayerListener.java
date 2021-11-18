/* Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 */

package fr.redxil.core.paper.minigame;

import fr.redxil.api.common.game.GameState;
import fr.redxil.api.spigot.minigame.GameBuilder;
import fr.redxil.api.spigot.minigame.teams.TeamsGUI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class PlayerListener implements Listener {

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getItem() == null || !event.getItem().hasItemMeta() && !event.getItem().getItemMeta().hasDisplayName())
            return;

        if (GameBuilder.getGameBuilder() != null) {

            if (GameBuilder.getGameBuilder().getGame().isGameState(GameState.WAITING, GameState.STARTING, GameState.FINISH)) {
                event.setCancelled(true);
            }

            switch (event.getItem().getItemMeta().getDisplayName()) {
                case "§e§lÉquipes §7(Clique droit)":
                    new TeamsGUI(event.getPlayer(), 0).openGUI(event.getPlayer());
                    break;
                case "§c§lQuitter §7(Clique droit)":
                    event.getPlayer().kickPlayer("");
                    break;

            }

        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        if (GameBuilder.getGameBuilder() != null) {
            if (GameBuilder.getGameBuilder().getGame().isGameState(GameState.WAITING, GameState.STARTING, GameState.FINISH)) {
                e.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent e) {
        if (GameBuilder.getGameBuilder() != null) {
            if (GameBuilder.getGameBuilder().getGame().isGameState(GameState.WAITING, GameState.STARTING, GameState.FINISH)) {
                e.setCancelled(true);
            }
        }
    }


    @EventHandler
    public void onDamage(EntityDamageEvent e) {
        if (GameBuilder.getGameBuilder() != null) {
            if (GameBuilder.getGameBuilder().getGame().isGameState(GameState.WAITING, GameState.STARTING, GameState.FINISH)) {
                e.setCancelled(true);
            }
        }
    }


    @EventHandler
    public void onDrop(PlayerDropItemEvent e) {
        if (GameBuilder.getGameBuilder() != null) {
            if (GameBuilder.getGameBuilder().getGame().isGameState(GameState.WAITING, GameState.STARTING, GameState.FINISH)) {
                e.setCancelled(true);
            }
        }
    }


    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        if (GameBuilder.getGameBuilder() != null) {
            Player player = event.getPlayer();

            GameBuilder gameBuilder = GameBuilder.getGameBuilder();

            if (gameBuilder.getTeamManager().hasTeam(player.getUniqueId())) {
                event.setFormat("§8[" + gameBuilder.getTeamManager().getPlayerTeam(player.getUniqueId()).getColoredName() + "§8] §7" + player.getName() + " §8§l» §7" + event.getMessage());
            } else {
                event.setFormat("§7" + player.getName() + " §8§l» §7" + event.getMessage());
            }

        }
    }

}
