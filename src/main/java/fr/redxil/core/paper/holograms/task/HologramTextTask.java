package fr.redxil.core.paper.holograms.task;

import fr.redxil.core.paper.holograms.EntityHologram;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class HologramTextTask extends BukkitRunnable {

    private final EntityHologram entity;

    public HologramTextTask(JavaPlugin plugin, EntityHologram entity, long interval) {
        this.entity = entity;
        this.runTaskTimerAsynchronously(plugin, interval, interval);
    }

    @Override
    public void run() {
        if (this.entity.isRemoved()) {
            this.cancel();
            return;
        }

        this.entity.update();
    }
}
