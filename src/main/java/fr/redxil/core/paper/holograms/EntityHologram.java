package fr.redxil.core.paper.holograms;

import fr.redxil.api.paper.Paper;
import fr.redxil.api.paper.holograms.HologramLine;
import fr.redxil.core.paper.holograms.task.HologramLineTask;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class EntityHologram {

    private final HologramLine line;
    private final EntityArmorStand entity;
    private final PacketPlayOutSpawnEntityLiving spawnPacket;
    private final PacketPlayOutEntityDestroy destroyPacket;

    private boolean removed = false;

    public EntityHologram(HologramLine line, EntityArmorStand entity) {
        this.line = line;
        this.entity = entity;
        this.spawnPacket = new PacketPlayOutSpawnEntityLiving(entity);
        this.destroyPacket = new PacketPlayOutEntityDestroy(entity.getId());
        long interval = line.getInterval();
        if (interval > 0) {
            new HologramLineTask(Paper.getInstance(), this, interval);
        }
    }

    public PacketPlayOutSpawnEntityLiving getSpawnPacket() {
        return spawnPacket;
    }

    public PacketPlayOutEntityDestroy getDestroyPacket() {
        return destroyPacket;
    }

    public HologramLine getLine() {
        return line;
    }

    public EntityArmorStand getEntity() {
        return entity;
    }

    public boolean isRemoved() {
        return removed;
    }

    public void setRemoved(boolean removed) {
        this.removed = removed;
    }

    public void updateFor(Player player) {
        this.entity.setCustomName(this.line.getUpdatingLine(player));
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(new PacketPlayOutEntityMetadata(this.entity.getId(), this.entity.getDataWatcher(), true));
    }

    public void update() {
        String line = this.line.getLine();
        if (this.line.getBeforeUpdate() != null) {
            this.line.getBeforeUpdate().accept(line);
        }
        for (EntityHuman player : this.entity.world.players) {
            EntityPlayer ep = ((EntityPlayer) player);
            this.entity.setCustomName(this.line.getUpdatingLine(ep.getBukkitEntity()));
            ep.playerConnection.sendPacket(new PacketPlayOutEntityMetadata(this.entity.getId(), this.entity.getDataWatcher(), true));
        }
        if (this.line.getThenUpdate() != null) {
            this.line.getThenUpdate().accept(line);
        }
    }
}
