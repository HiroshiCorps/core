package fr.redxil.core.paper.holograms;

import fr.redxil.api.paper.holograms.HologramText;
import fr.redxil.api.paper.holograms.HologramsManager;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class CoreHologramsManager implements HologramsManager {

    private final Map<World, List<CoreHologram>> worldHolograms = new HashMap<>();

    @Override
    public void create(Location location, List<HologramText> hologramTextList) {
        CoreHologram coreHologram = new CoreHologram(this, location, hologramTextList);
        List<CoreHologram> list = this.worldHolograms.computeIfAbsent(coreHologram.getCurrentLocation().getWorld(), world -> new ArrayList<>());
        list.add(coreHologram);
    }

    public void updateWorld(CoreHologram hologram, World oldWorld, World world) {
        List<CoreHologram> list = this.worldHolograms.get(oldWorld);
        if (list != null) {
            list.remove(hologram);
        }
        list = this.worldHolograms.computeIfAbsent(world, w -> new ArrayList<>());
        list.add(hologram);
    }

    private void sendWorldPackets(Player player, World world, BiConsumer<CoreHologram, Player> consumer) {
        List<CoreHologram> list = this.worldHolograms.get(world);
        if (list != null) {
            for (CoreHologram coreHologram : list) {
                consumer.accept(coreHologram, player);
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

}
