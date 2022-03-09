package fr.redxil.core.paper.event;

import fr.redxil.api.common.API;
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.api.common.player.moderators.APIPlayerModerator;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class PlayerInteractEvent implements Listener {

    @EventHandler
    public void playerDrop(PlayerDropItemEvent event) {

        APIPlayerModerator spm = API.getInstance().getModeratorManager().getModerator(event.getPlayer().getUniqueId());
        if (spm != null)
            if (spm.isModeratorMod()) event.setCancelled(true);

    }

    @EventHandler
    public void playerPickUp(EntityPickupItemEvent event) {

        if (!(event.getEntity() instanceof Player))
            return;

        APIPlayerModerator spm = API.getInstance().getModeratorManager().getModerator(event.getEntity().getUniqueId());
        if (spm != null)
            if (spm.isModeratorMod()) event.setCancelled(true);

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

        if (!(event.getEntity() instanceof Player player)) return;

        APIPlayer apiPlayer = API.getInstance().getPlayerManager().getPlayer(player.getUniqueId());
        if (apiPlayer.isFreeze())
            event.setCancelled(true);

        APIPlayerModerator spm = API.getInstance().getModeratorManager().getModerator(apiPlayer.getMemberID());
        if (spm != null)
            if (spm.isModeratorMod()) event.setCancelled(true);

    }

}
