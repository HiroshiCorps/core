package fr.redxil.core.paper.tags.score;

import fr.redxil.api.paper.Paper;
import fr.redxil.api.paper.tags.TagScore;
import fr.redxil.core.paper.utils.PacketUtils;
import net.minecraft.server.v1_12_R1.PacketPlayOutScoreboardScore;
import net.minecraft.server.v1_12_R1.Scoreboard;
import net.minecraft.server.v1_12_R1.ScoreboardObjective;
import net.minecraft.server.v1_12_R1.ScoreboardScore;
import org.bukkit.entity.Player;

public class ScoreTagPlayer extends ScoreboardScore implements TagScore {

    private final Player player;
    private int score;

    public ScoreTagPlayer(Scoreboard scoreboard, ScoreboardObjective objective, Player player) {
        super(scoreboard, objective, player.getName());
        this.player = player;
    }

    @Override
    public void addScore(int score) {
        this.score += score;
        this.update();
    }

    @Override
    public void removeScore(int score) {
        this.score -= score;
        this.update();
    }

    @Override
    public int getScore() {
        return this.score;
    }

    @Override
    public void setScore(int score) {
        this.score = score;
        this.update();
    }

    @Override
    public Player getPlayer() {
        return this.player;
    }

    @Override
    public String getPlayerName() {
        return this.player.getName();
    }

    public void update() {
        PacketPlayOutScoreboardScore packet = new PacketPlayOutScoreboardScore(this);
        for (Player player : Paper.getInstance().getTagsManager().getScorePlayers()) {
            PacketUtils.sendPacket(player, packet);
        }
    }
}
