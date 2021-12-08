package fr.redxil.core.paper.holograms.task;

import fr.redxil.api.paper.Paper;
import fr.redxil.core.paper.holograms.CoreHologram;
import org.bukkit.scheduler.BukkitRunnable;

public class HologramTask extends BukkitRunnable {

    private final CoreHologram hologram;
    private boolean removed;

    public HologramTask(CoreHologram hologram, long interval) {
        this.hologram = hologram;
        this.runTaskTimerAsynchronously(Paper.getInstance(), interval, interval);
    }

    @Override
    public void run() {
        if (this.removed) {
            this.cancel();
            return;
        }

        this.hologram.refresh();
    }

    public void remove() {
        this.removed = true;
    }
}
