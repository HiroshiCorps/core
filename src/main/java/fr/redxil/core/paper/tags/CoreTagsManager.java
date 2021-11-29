/* Copyright (C) Hiroshi - Ibrahim - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Ibrahim for Hiroshi, braimsou@gmail.com - contact@hiroshimc.net - 2021
 */

package fr.redxil.core.paper.tags;

import fr.redxil.api.paper.tags.*;
import fr.redxil.api.paper.tags.utils.TagPosition;
import fr.redxil.core.paper.tags.player.GroupedTagPlayer;
import fr.redxil.core.paper.tags.player.IndividualTagPlayer;
import fr.redxil.core.paper.tags.score.ScoreTagPlayer;
import fr.redxil.core.paper.utils.PacketUtils;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.minecraft.server.v1_12_R1.*;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_12_R1.CraftServer;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class CoreTagsManager implements TagsManager {

    private final Scoreboard globalScoreboard;
    private final ScoreboardObjective dummyObjective, healthObjective;
    private final Map<Player, TagPlayer> tagMap = new HashMap<>();
    private final Map<Player, ScoreTagPlayer> scoreMap = new HashMap<>();

    private TagProvider tagProvider;
    private Function<Player, TagPlayer> toTagFunction;
    private boolean displayHealth;

    public CoreTagsManager() {
        this.globalScoreboard = new ScoreboardServer(((CraftServer) Bukkit.getServer()).getServer());
        this.dummyObjective = new ScoreboardObjective(this.globalScoreboard, "HiroshiTags", IScoreboardCriteria.b);
        this.healthObjective = new ScoreboardObjective(this.globalScoreboard, "HiroshiTags", IScoreboardCriteria.g);
    }

    @Override
    public TagProvider getTagProvider() {
        return this.tagProvider;
    }

    @Override
    public void setTagProvider(TagProvider provider) {
        this.tagProvider = provider;
        TagType type = provider.type();
        this.toTagFunction = player -> {
            String teamName = TagPosition.getPositionAt(player, 0);
            return type == TagType.GROUPED ? new GroupedTagPlayer(this.globalScoreboard, player, teamName) : new IndividualTagPlayer(this.globalScoreboard, player, teamName);
        };
    }

    @Override
    public void addPlayer(Player player) {
        this.tagMap.put(player, this.toTagFunction.apply(player));
        this.setupPlayer(player);
    }

    @Override
    public void removePlayer(Player player) {
        this.tagMap.remove(player);
        this.scoreMap.remove(player);
    }

    @Override
    public void updatePlayer(Player player) {
        Validate.notNull(this.tagProvider, "Tag provider cannot be null");
        TagPlayer tagPlayer = this.tagMap.get(player);
        if (tagPlayer == null) return;
        if (this.tagProvider.type() == TagType.GROUPED) {
            this.tagProvider.update(tagPlayer, tagPlayer);
        }
        for (TagPlayer targetTag : this.tagMap.values()) {
            targetTag.update(this.tagProvider, tagPlayer);
        }
    }

    @Override
    public void updateOnline(Player player) {
        this.updateOnline(this.tagMap.get(player));
    }

    @Override
    public void updateHeaderFooter(Player player, String header, String footer) {
        PacketPlayOutPlayerListHeaderFooter headerFooter = new PacketPlayOutPlayerListHeaderFooter();
        headerFooter.header = this.toComponents(header);
        headerFooter.footer = this.toComponents(footer);
        PacketUtils.sendPacket(player, headerFooter);
    }

    @Override
    public boolean isDisplayHealth() {
        return this.displayHealth;
    }

    @Override
    public void setDisplayHealth(boolean displayHealth) {
        ScoreboardObjective oldObjective = this.getGlobalObjective();
        boolean old = this.displayHealth;
        this.displayHealth = displayHealth;
        if (this.displayHealth != old) {
            PacketPlayOutScoreboardObjective remove = new PacketPlayOutScoreboardObjective(oldObjective, 1);
            this.tagMap.forEach((player, tag) -> {
                PacketUtils.sendPacket(player, remove);
                this.setupPlayer(player);
                this.updateOnline(tag);
            });
        }
    }

    @Override
    public TagScore getScore(Player player) {
        return this.scoreMap.get(player);
    }

    @Override
    public Collection<? extends Player> getTagPlayers() {
        return this.tagMap.keySet();
    }

    @Override
    public Collection<? extends Player> getScorePlayers() {
        return this.scoreMap.keySet();
    }

    private BaseComponent[] toComponents(String message) {
        return new ComponentBuilder(message).create();
    }

    private void updateOnline(TagPlayer tagPlayer) {
        Validate.notNull(this.tagProvider, "Tag provider cannot be null");
        if (tagPlayer == null) return;
        if (this.tagProvider.type() == TagType.GROUPED) {
            this.tagProvider.update(tagPlayer, tagPlayer);
        }
        for (TagPlayer targetTag : this.tagMap.values()) {
            targetTag.update(this.tagProvider, tagPlayer);
            tagPlayer.update(this.tagProvider, targetTag);
        }
    }

    private void setupPlayer(Player player) {
        PacketPlayOutScoreboardObjective packet = new PacketPlayOutScoreboardObjective(this.getGlobalObjective(), 0);
        PacketUtils.sendPacket(player, packet);
        if (this.displayHealth) {
            PacketUtils.sendPacket(player, new PacketPlayOutScoreboardDisplayObjective(0, this.getGlobalObjective()));
            ScoreTagPlayer scoreTag = new ScoreTagPlayer(this.globalScoreboard, this.getGlobalObjective(), player);
            scoreTag.setScore((int) player.getHealth());
            scoreTag.update();
            this.scoreMap.put(player, scoreTag);
        }
    }

    public ScoreboardObjective getGlobalObjective() {
        return this.displayHealth ? this.healthObjective : this.dummyObjective;
    }
}
