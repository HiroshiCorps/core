package fr.redxil.core.paper.utils;

import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.api.common.utils.JavaUtils;
import net.minecraft.server.level.EntityPlayer;
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_17_R1.util.CraftChatMessage;
import org.bukkit.entity.Player;

public class Nick {

    public static void applyNick(Player p, APIPlayer apiPlayer) {

        EntityPlayer craftPlayerP = ((CraftPlayer) p).getHandle();

        /*TagsManager tagsManager = Paper.getInstance().getTagsManager();

        tagsManager.removePlayer(p);
           */
        JavaUtils.setDeclaredField(craftPlayerP, "listName", CraftChatMessage.fromString(apiPlayer.getTabString())[0]);
        JavaUtils.setDeclaredField(craftPlayerP.getProfile(), "name", apiPlayer.getName());
/*
        tagsManager.addPlayer(p);
        tagsManager.updatePlayer(p);
*/
    }

}
