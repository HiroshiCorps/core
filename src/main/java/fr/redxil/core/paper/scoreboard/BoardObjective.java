/* Copyright (C) Hiroshi - Ibrahim - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Ibrahim for Hiroshi, braimsou@gmail.com - contact@hiroshimc.net - 2021
 */

package fr.redxil.core.paper.scoreboard;

import fr.redxil.api.paper.scoreboard.BoardLine;
import fr.redxil.api.paper.scoreboard.BoardProvider;
import fr.redxil.core.paper.utils.PacketUtils;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

public class BoardObjective extends ScoreboardObjective {

    @SuppressWarnings("deprecation")
    public static final Scoreboard scoreboard = new ScoreboardServer(MinecraftServer.getServer());

    private final int lineSize;
    private final BoardProvider provider;
    private final List<BoardEntry> entries;
    private final List<Player> viewers = new ArrayList<>();
    private final ExecutorService service = new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());

    public BoardObjective(Plugin plugin, BoardProvider provider) {
        super(scoreboard, "dummy", IScoreboardCriteria.b);
        this.provider = provider;
        List<BoardLine> lines = provider.lines();
        this.lineSize = lines.size();
        this.entries = new ArrayList<>(lineSize);
        int index = 0;
        for (BoardLine line : lines) {
            entries.add(new BoardEntry(plugin, this, line, index));
            index++;
        }

        long interval = provider.titleRefreshInterval();
        if (interval > 0) {
            Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, this::updateTitle, interval, interval);
        }
    }

    private void updateViewers(int line) {
        BoardEntry entry = this.entries.get(line);
        for (Player viewer : this.viewers) {
            this.updateLine(viewer, entry, BoardEntry::update);
        }
    }

    private void updateLine(Player viewer, BoardEntry entry, BiConsumer<BoardEntry, Player> consumer) {
        BoardLine line = entry.getLine();
        entry.setLine(entry.getLine().getUpdateFunction().apply(line.getLine(), viewer));
        consumer.accept(entry, viewer);
    }

    private void updateTitle() {
        this.viewers.forEach(player -> PacketUtils.sendPacket(player, new PacketPlayOutScoreboardObjective(this, 2)));
    }

    public void apply(Player player) {
        this.viewers.add(player);

        PacketUtils.sendPacket(player, new PacketPlayOutScoreboardObjective(this, 0));
        PacketUtils.sendPacket(player, new PacketPlayOutScoreboardDisplayObjective(1, this));
        for (BoardEntry entry : this.entries) {
            this.updateLine(player, entry, BoardEntry::create);
        }
    }

    public void remove(Player player) {
        this.viewers.remove(player);
        PacketUtils.sendPacket(player, new PacketPlayOutScoreboardObjective(this, 1));
    }

    public BoardProvider getProvider() {
        return provider;
    }

    public void updateLine(int line) {
        this.service.execute(() -> {
            if (line == -1) {
                this.updateTitle();
            } else {
                this.updateViewers(line);
            }
        });
    }

    protected int lineSize() {
        return this.lineSize;
    }

    @Override
    public String getDisplayName() {
        return this.provider.getTitle();
    }
}
