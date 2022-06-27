package fr.redxil.core.paper.event;

import fr.redxil.api.common.API;
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.api.common.player.moderators.APIPlayerModerator;
import fr.redxil.core.common.CoreAPI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Optional;

public class PlayerInteractEvent implements Listener {

    @EventHandler
    public void playerDrop(PlayerDropItemEvent event) {

        Optional<APIPlayerModerator> spm = CoreAPI.getInstance().getModeratorManager().getModerator(event.getPlayer().getUniqueId());
        if (spm.isPresent())
            if (spm.get().isModeratorMod()) event.setCancelled(true);

    }

    @EventHandler
    public void playerPickUp(EntityPickupItemEvent event) {

        if (!(event.getEntity() instanceof Player))
            return;

        Optional<APIPlayerModerator> spm = CoreAPI.getInstance().getModeratorManager().getModerator(event.getEntity().getUniqueId());
        if (spm.isPresent())
            if (spm.get().isModeratorMod()) event.setCancelled(true);

    }

    @EventHandler
    public void playerChat(AsyncPlayerChatEvent asyncPlayerChatEvent) {

        CoreAPI.getInstance().getPlayerManager().getPlayer(asyncPlayerChatEvent.getPlayer().getUniqueId()).ifPresent(apiPlayer -> asyncPlayerChatEvent.setFormat(apiPlayer.getChatString() + asyncPlayerChatEvent.getMessage()));

    }

    @EventHandler
    public void playerMove(PlayerMoveEvent event) {
        Optional<APIPlayer> player = CoreAPI.getInstance().getPlayerManager().getPlayer(event.getPlayer().getUniqueId());
        if (player.isPresent() && player.get().isFreeze())
            event.setCancelled(true);
    }

    @EventHandler
    public void playerDamage(EntityDamageEvent event) {

        if (!(event.getEntity() instanceof Player player)) return;

        Optional<APIPlayer> apiPlayer = CoreAPI.getInstance().getPlayerManager().getPlayer(player.getUniqueId());

        if (apiPlayer.isEmpty()) {
            event.setCancelled(true);
            return;
        }

        if (apiPlayer.get().isFreeze())
            event.setCancelled(true);

        Optional<APIPlayerModerator> spm = CoreAPI.getInstance().getModeratorManager().getModerator(apiPlayer.get().getMemberID());
        if (spm.isPresent() && spm.get().isModeratorMod()) event.setCancelled(true);

    }

    @EventHandler
    public void playerDeath(PlayerDeathEvent playerDeathEvent) {
        EntityDamageEvent entityDamageEvent = playerDeathEvent.getEntity().getLastDamageCause();
        if (entityDamageEvent == null)
            return;
        if (entityDamageEvent.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
            if (entityDamageEvent.getEntity() instanceof Player player) {
                API.getInstance().getModeratorManager().getModerator(player.getUniqueId()).ifPresent(moderator -> {
                    if (moderator.isVanish())
                        playerDeathEvent.setDeathMessage(playerDeathEvent.getEntity().getName() + "est mort");
                });
            }
        }
    }

}
