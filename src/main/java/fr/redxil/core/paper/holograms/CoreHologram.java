package fr.redxil.core.paper.holograms;

import fr.redxil.api.paper.holograms.Hologram;
import fr.redxil.api.paper.holograms.HologramText;
import fr.redxil.core.paper.holograms.task.HologramTask;
import net.minecraft.server.v1_12_R1.EntityArmorStand;
import net.minecraft.server.v1_12_R1.EntityPlayer;
import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class CoreHologram implements Hologram {

    private final CoreHologramsManager manager;
    private final List<EntityHologram> entities = new ArrayList<>();
    private HologramTask currentTask;
    private final List<HologramText> lines;
    private Location currentLocation;
    private long currentInterval = 0L;
    private double indent = 0.25;

    public CoreHologram(CoreHologramsManager manager, Location location, List<HologramText> hologramText) {
        this.manager = manager;
        this.currentLocation = location;
        this.lines = hologramText;
        this.refresh();
    }

    private EntityArmorStand createEntity(Location location, HologramText line, int index) {
        EntityArmorStand entity = new EntityArmorStand(((CraftWorld) location.getWorld()).getHandle(), location.getX(), location.getY() - this.indent * index, location.getZ());
        entity.setSmall(true);
        entity.setInvisible(true);
        entity.setCustomName(line.getText());
        entity.setCustomNameVisible(true);
        return entity;
    }

    public void updateText(int line) {
        Validate.isTrue(line < lines.size(), "Cannot update an unexisting line");
        this.entities.get(line).update();
    }

    public void updateTextFor(int line, Player player) {
        Validate.isTrue(line < lines.size(), "Cannot update an unexisting line");
        this.entities.get(line).updateFor(player);
    }

    public void refresh() {
        this.delete();
        this.entities.clear();
        for (int i = 0; i < lines.size(); i++) {
            HologramText line = lines.get(i);
            EntityHologram entityHologram = new EntityHologram(line, this.createEntity(getCurrentLocation(), line, i));
            entities.add(entityHologram);

            for (Player player : getCurrentLocation().getWorld().getPlayers()) {
                EntityPlayer ep = (EntityPlayer) player;
                ep.playerConnection.sendPacket(entityHologram.getSpawnPacket());
            }
        }

        if (currentInterval > 0) {
            this.currentTask = new HologramTask(this, currentInterval);
        }
    }

    public void send(Player player) {
        if (player.getWorld().equals(this.currentLocation.getWorld())) {
            for (EntityHologram entity : this.entities) {
                ((CraftPlayer) player).getHandle().playerConnection.sendPacket(entity.getSpawnPacket());
            }
        }
    }

    public void destroy(Player human) {
        EntityPlayer ep = (EntityPlayer) human;
        this.entities.forEach(entity -> {
            entity.setRemoved(true);
            ep.playerConnection.sendPacket(entity.getDestroyPacket());
        });
    }

    public void delete() {
        currentLocation.getWorld().getPlayers().forEach(this::destroy);
        this.currentTask.remove();
        this.currentTask = null;
    }

    public Location getCurrentLocation() {
        return this.currentLocation;
    }

    public void setCurrentLocation(Location location) {

        Validate.notNull(location, "Location cannot be null, please destroy");
        this.manager.updateWorld(this, getCurrentLocation().getWorld(), location.getWorld());
        this.currentLocation = location;
        refresh();

    }

    @Override
    public long getRefreshInterval() {
        return currentInterval;
    }

    @Override
    public void setRefreshInterval(long l) {
        this.currentInterval = l;
    }

    @Override
    public double getLineIndent() {
        return indent;
    }

    @Override
    public void setLineIndent(double v) {
        this.indent = v;
    }

}