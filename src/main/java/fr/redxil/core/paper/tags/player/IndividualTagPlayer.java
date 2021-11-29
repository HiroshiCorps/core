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
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_12_R1.util.CraftChatMessage;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public class IndividualTagPlayer extends ScoreboardTeam implements TagPlayer {

    private final Player player;

    private final Set<String> nameSet;
    private String name;
    private String prefix = "";
    private String suffix = "";
    private String listName;
    private int position;

    public IndividualTagPlayer(Scoreboard scoreboard, Player player, String name) {
        super(scoreboard, name);

        this.nameSet = Collections.singleton(player.getName());

        this.name = name;
        this.player = player;
    }

    @Override
    public void update(TagProvider provider, TagPlayer target) {
        provider.update(this, target);
        ((IndividualTagPlayer) target).updateName();
        PacketPlayOutScoreboardTeam team = new PacketPlayOutScoreboardTeam((IndividualTagPlayer) target, 0);
        PacketUtils.sendPacket(this.player, team);
        String listName = target.getListName();
        EntityPlayer targetEP = ((CraftPlayer) target.getPlayer()).getHandle();
        if (listName != null) {
            targetEP.listName = CraftChatMessage.fromString(listName)[0];
            PacketPlayOutPlayerInfo info = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.UPDATE_DISPLAY_NAME, targetEP);
            PacketUtils.sendPacket(this.player, info);
        }
        target.setPrefix("");
        target.setSuffix("");
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
    }

    @Override
    public String getPrefix() {
        return this.prefix;
    }

    @Override
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public String getSuffix() {
        return this.suffix;
    }

    @Override
    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    @Override
    public int getPosition() {
        return this.position;
    }

    @Override
    public void setPosition(int position) {
        this.position = position;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getDisplayName() {
        return this.name;
    }

    @Override
    public Collection<String> getPlayerNameSet() {
        return this.nameSet;
    }

    public void updateName() {
        this.name = TagPosition.getPositionAt(this.player, this.position);
    }
}
