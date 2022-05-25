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

import java.util.Optional;

public class PlayerInteractEvent implements Listener {

    @EventHandler
    public void playerDrop(PlayerDropItemEvent event) {

        Optional<APIPlayerModerator> spm = API.getInstance().getModeratorManager().getModerator(event.getPlayer().getUniqueId());
        if (spm.isPresent())
            if (spm.get().isModeratorMod()) event.setCancelled(true);

    }

    @EventHandler
    public void playerPickUp(EntityPickupItemEvent event) {

        if (!(event.getEntity() instanceof Player))
            return;

        Optional<APIPlayerModerator> spm = API.getInstance().getModeratorManager().getModerator(event.getEntity().getUniqueId());
        if (spm.isPresent())
            if (spm.get().isModeratorMod()) event.setCancelled(true);

    }

    @EventHandler
    public void playerChat(AsyncPlayerChatEvent asyncPlayerChatEvent) {

        API.getInstance().getPlayerManager().getPlayer(asyncPlayerChatEvent.getPlayer().getUniqueId()).ifPresent(apiPlayer -> asyncPlayerChatEvent.setFormat(apiPlayer.getChatString() + asyncPlayerChatEvent.getMessage()));

    }

    @EventHandler
    public void playerMove(PlayerMoveEvent event) {
        Optional<APIPlayer> player = API.getInstance().getPlayerManager().getPlayer(event.getPlayer().getUniqueId());
        if (player.isPresent() && player.get().isFreeze())
            event.setCancelled(true);
    }

    @EventHandler
    public void playerDamage(EntityDamageEvent event) {

        if (!(event.getEntity() instanceof Player player)) return;

        Optional<APIPlayer> apiPlayer = API.getInstance().getPlayerManager().getPlayer(player.getUniqueId());
        if (apiPlayer.isPresent() && apiPlayer.get().isFreeze())
            event.setCancelled(true);

        if (apiPlayer.isEmpty())
            return;

        Optional<APIPlayerModerator> spm = API.getInstance().getModeratorManager().getModerator(apiPlayer.get().getMemberID());
        if (spm.isPresent() && spm.get().isModeratorMod()) event.setCancelled(true);

    }

}
