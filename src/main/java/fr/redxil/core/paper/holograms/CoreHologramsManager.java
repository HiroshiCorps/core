package fr.redxil.core.paper.holograms;

import fr.redxil.api.paper.holograms.Hologram;
import fr.redxil.api.paper.holograms.HologramsManager;
import net.minecraft.server.v1_12_R1.EntityPlayer;
import net.minecraft.server.v1_12_R1.World;
import org.apache.commons.lang3.Validate;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class CoreHologramsManager implements HologramsManager {

    private final Map<Hologram, CoreHologram> hologramMap = new HashMap<>();
    private final Map<World, List<CoreHologram>> worldHolograms = new HashMap<>();

    @Override
    public void spawn(Hologram hologram) {
        CoreHologram coreHologram = new CoreHologram(this, hologram);
        this.hologramMap.put(hologram, coreHologram);
        List<CoreHologram> list = this.worldHolograms.computeIfAbsent(coreHologram.getCurrentWorld(), world -> new ArrayList<>());
        list.add(coreHologram);
    }

    @Override
    public void delete(Hologram hologram) {
        CoreHologram coreHologram = this.getCoreHologram(hologram);
        coreHologram.delete();
        List<CoreHologram> list = this.worldHolograms.get(coreHologram.getCurrentWorld());
        if (list != null) list.remove(coreHologram);
    }

    @Override
    public void updateLine(Hologram hologram, int line) {
        this.getCoreHologram(hologram).update(line);
    }

    @Override
    public void updateLineFor(Hologram hologram, int line, Player player) {
        this.getCoreHologram(hologram).updateFor(line, player);
    }

    @Override
    public void refresh(Hologram hologram) {
        this.getCoreHologram(hologram).refresh();
    }

    @Override
    public void destroy(Hologram hologram, Player player) {
        this.getCoreHologram(hologram).destroy(((CraftPlayer) player).getHandle());
    }

    @Override
    public void send(Hologram hologram, Player player) {
        this.getCoreHologram(hologram).send(((CraftPlayer) player).getHandle());
    }

    public void updateWorld(CoreHologram hologram, World oldWorld, World world) {
        List<CoreHologram> list = this.worldHolograms.get(oldWorld);
        if (list != null) {
            list.remove(hologram);
        }
        list = this.worldHolograms.computeIfAbsent(world, w -> new ArrayList<>());
        list.add(hologram);
    }

    private void sendWorldPackets(Player player, org.bukkit.World bukkitWorld, BiConsumer<CoreHologram, EntityPlayer> consumer) {
        World world = ((CraftWorld) bukkitWorld).getHandle();
        EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
        List<CoreHologram> list = this.worldHolograms.get(world);
        if (list != null) {
            for (CoreHologram coreHologram : list) {
                consumer.accept(coreHologram, entityPlayer);
            }
        }
    }

    @Override
    public void spawnHolograms(Player player, org.bukkit.World bukkitWorld) {
        this.sendWorldPackets(player, bukkitWorld, CoreHologram::send);
    }

    @Override
    public void destroyHolograms(Player player, org.bukkit.World bukkitWorld) {
        this.sendWorldPackets(player, bukkitWorld, CoreHologram::destroy);
    }

    private CoreHologram getCoreHologram(Hologram hologram) {
        CoreHologram coreHologram = this.hologramMap.get(hologram);
        Validate.notNull(coreHologram, "Cannot found the target hologram");
        return coreHologram;
    }
}
