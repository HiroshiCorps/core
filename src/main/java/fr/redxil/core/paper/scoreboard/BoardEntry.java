/* Copyright (C) Hiroshi - Ibrahim - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Ibrahim for Hiroshi, braimsou@gmail.com - contact@hiroshimc.net - 2021
 */

package fr.redxil.core.paper.scoreboard;

import fr.redxil.api.paper.scoreboard.BoardLine;
import fr.redxil.core.paper.utils.PacketUtils;
import net.minecraft.server.v1_12_R1.PacketPlayOutScoreboardScore;
import net.minecraft.server.v1_12_R1.PacketPlayOutScoreboardTeam;
import net.minecraft.server.v1_12_R1.ScoreboardTeam;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public class BoardEntry extends ScoreboardTeam {

    private final BoardLine line;
    private final BoardPosition position;
    private final Set<String> nameSet;

    private String prefix = "";
    private String suffix = "";

    public BoardEntry(Plugin plugin, BoardObjective objective, BoardLine line, int index) {
        super(BoardObjective.scoreboard, "entry_" + index);
        this.line = line;
        this.position = new BoardPosition(objective, index);
        this.nameSet = Collections.singleton(this.position.getPlayerName());
        long interval = line.getUpdateInterval();
        if (interval > 0) {
            Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> objective.addPendingUpdate(index), interval, interval);
        }
    }

    @Override
    public String getPrefix() {
        return this.prefix;
    }

    @Override
    public String getSuffix() {
        return this.suffix;
    }

    public void update(Player player) {
        PacketUtils.sendPacket(player, new PacketPlayOutScoreboardTeam(this, 2));
    }

    public void create(Player player) {
        PacketPlayOutScoreboardScore score = new PacketPlayOutScoreboardScore(this.position);
        PacketPlayOutScoreboardTeam team = new PacketPlayOutScoreboardTeam(this, 0);
        PacketUtils.sendPacket(player, score);
        PacketUtils.sendPacket(player, team);
    }

    public BoardLine getLine() {
        return line;
    }

    public void setLine(String line) {
        String prefix;
        String suffix = "";
        if (line.length() > 16) {
            prefix = line.substring(0, 16);
            String lastColors = ChatColor.getLastColors(prefix);
            suffix = lastColors + line.substring(16, Math.min(32 - lastColors.length(), line.length()));
        } else {
            prefix = line;
        }

        this.prefix = prefix;
        this.suffix = suffix;
    }

    @Override
    public Collection<String> getPlayerNameSet() {
        return this.nameSet;
    }
}
