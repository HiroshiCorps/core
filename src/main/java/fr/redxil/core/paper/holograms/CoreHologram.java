package fr.redxil.core.paper.holograms;

import fr.redxil.api.paper.holograms.Hologram;
import fr.redxil.api.paper.holograms.HologramLine;
import fr.redxil.core.paper.holograms.task.HologramTask;
import net.minecraft.server.v1_12_R1.EntityArmorStand;
import net.minecraft.server.v1_12_R1.EntityHuman;
import net.minecraft.server.v1_12_R1.EntityPlayer;
import net.minecraft.server.v1_12_R1.World;
import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CoreHologram {

    private final CoreHologramsManager manager;
    private final Hologram hologram;
    private final List<EntityHologram> entities = new ArrayList<>();
    private World currentWorld;
    private HologramTask currentTask;
    private long currentInterval;
    private Location currentLocation;

    public CoreHologram(CoreHologramsManager manager, Hologram hologram) {
        this.manager = manager;
        this.hologram = hologram;
        this.refresh();
    }

    private EntityArmorStand createEntity(World world, Location location, HologramLine line, int index) {
        EntityArmorStand entity = new EntityArmorStand(world, location.getX(), location.getY() - this.hologram.indent() * index, location.getZ());
        entity.setSmall(true);
        entity.setInvisible(true);
        entity.setNoGravity(true);
        entity.setInvulnerable(true);
        entity.setCustomName(line.getLine());
        entity.setCustomNameVisible(true);
        return entity;
    }

    public void update(int line) {
        Validate.isTrue(line < this.entities.size(), "Cannot update an unexisting line");
        this.entities.get(line).update();
    }

    public void updateFor(int line, Player player) {
        Validate.isTrue(line < this.entities.size(), "Cannot update an unexisting line");
        this.entities.get(line).updateFor(player);
    }

    public void refresh() {
        Location location = hologram.location();
        boolean sameLocation = this.currentLocation == null || location.equals(this.currentLocation);
        if (!sameLocation) {
            this.delete();
        }
        this.currentLocation = location;
        World oldWorld = this.currentWorld;
        World world = ((CraftWorld) location.getWorld()).getHandle();
        boolean sameWorld = this.currentWorld == null || oldWorld.equals(world);
        this.entities.clear();
        this.currentWorld = world;
        List<HologramLine> lines = hologram.lines();
        for (int i = 0; i < lines.size(); i++) {
            HologramLine line = lines.get(i);
            EntityHologram entityHologram = new EntityHologram(line, this.createEntity(world, location, line, i));
            entities.add(entityHologram);

            for (EntityHuman player : world.players) {
                EntityPlayer ep = (EntityPlayer) player;
                ep.playerConnection.sendPacket(entityHologram.getSpawnPacket());
                this.hologram.spawned(ep.getBukkitEntity());
            }
        }

        long interval = this.hologram.refreshInterval();
        if (interval != this.currentInterval) {
            if (this.currentTask != null) {
                this.currentTask.remove();
                this.currentTask = null;
            }

            if (interval > 0) {
                this.currentTask = new HologramTask(this, interval);
            }
        }
        this.currentInterval = interval;

        if (!sameWorld) {
            this.manager.updateWorld(this, oldWorld, world);
        }
    }

    public void send(EntityPlayer player) {
        if (player.world.equals(this.currentWorld)) {
            for (EntityHologram entity : this.entities) {
                player.playerConnection.sendPacket(entity.getSpawnPacket());
            }
        }

        this.hologram.spawned(player.getBukkitEntity());
    }

    public void destroy(EntityHuman human) {
        EntityPlayer ep = (EntityPlayer) human;
        this.entities.forEach(entity -> {
            entity.setRemoved(true);
            ep.playerConnection.sendPacket(entity.getDestroyPacket());
        });

        this.hologram.destroyed(ep.getBukkitEntity());
    }

    public void delete() {
        this.currentWorld.players.forEach(this::destroy);
        this.currentTask.remove();
        this.currentTask = null;
    }

    public World getCurrentWorld() {
        return currentWorld;
    }
}