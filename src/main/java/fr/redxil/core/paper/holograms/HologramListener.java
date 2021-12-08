package fr.redxil.core.paper.holograms;

import fr.redxil.api.paper.Paper;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public class HologramListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Paper.getInstance().getHologramManager().spawnHolograms(event.getPlayer(), event.getPlayer().getWorld());
    }

    @EventHandler
    public void onChangeWorld(PlayerChangedWorldEvent event) {
        World from = event.getFrom();
        World to = event.getPlayer().getWorld();
        Player player = event.getPlayer();
        Paper.getInstance().getHologramManager().destroyHolograms(player, from);
        Paper.getInstance().getHologramManager().spawnHolograms(player, to);
    }
}
