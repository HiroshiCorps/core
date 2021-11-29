/* Copyright (C) Hiroshi - Ibrahim - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Ibrahim for Hiroshi, braimsou@gmail.com - contact@hiroshimc.net - 2021
 */

package fr.redxil.core.paper.tags.player;

import fr.redxil.api.paper.tags.TagPlayer;
import fr.redxil.api.paper.tags.TagProvider;
import fr.redxil.api.paper.tags.utils.TagPosition;
import fr.redxil.core.paper.utils.PacketUtils;
import net.minecraft.server.v1_12_R1.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_12_R1.PacketPlayOutScoreboardTeam;
import net.minecraft.server.v1_12_R1.Scoreboard;
import net.minecraft.server.v1_12_R1.ScoreboardTeam;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;

public class GroupedTagPlayer extends ScoreboardTeam implements TagPlayer {

    private final Player player;
    private int position;
    private String listName;
    private String name;

    public GroupedTagPlayer(Scoreboard scoreboard, Player player, String name) {
        super(scoreboard, name);

        this.name = this.getName();
        this.player = player;
        this.getPlayerNameSet().add(player.getName());
    }

    @Override
    public void update(TagProvider provider, TagPlayer target) {
        PacketPlayOutScoreboardTeam team = new PacketPlayOutScoreboardTeam(this, 0);
        PacketPlayOutPlayerInfo info = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.UPDATE_DISPLAY_NAME, ((CraftPlayer) target.getPlayer()).getHandle());
        PacketUtils.sendPacket(this.player, team);
        PacketUtils.sendPacket(this.player, info);
    }

    @Override
    public Player getPlayer() {
        return this.player;
    }

    @Override
    public String getListName() {
        return this.listName;
    }

    @Override
    public void setListName(String listName) {
        this.listName = listName;
        this.player.setPlayerListName(listName);
    }

    @Override
    public int getPosition() {
        return this.position;
    }

    @Override
    public void setPosition(int position) {
        this.position = position;
        this.name = TagPosition.getPositionAt(this.player, position);
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getDisplayName() {
        return this.name;
    }
}
