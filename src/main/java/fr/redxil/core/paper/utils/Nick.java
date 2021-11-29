package fr.redxil.core.paper.utils;

import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.api.common.utils.JavaUtils;
import net.minecraft.server.v1_12_R1.*;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_12_R1.util.CraftChatMessage;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class Nick {

    public static void applyNick(Player p, APIPlayer apiPlayer, boolean connection) {

        EntityPlayer craftPlayerP = ((CraftPlayer) p).getHandle();
        JavaUtils.setDeclaredField(craftPlayerP, "listName", CraftChatMessage.fromString(apiPlayer.getTabString())[0]);
        JavaUtils.setDeclaredField(craftPlayerP.getProfile(), "name", apiPlayer.getName(true));

        craftPlayerP.playerConnection.sendPacket(new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.UPDATE_DISPLAY_NAME, craftPlayerP));

        if (connection) return;

        PacketPlayOutPlayerInfo removePacket = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER, craftPlayerP);
        PacketPlayOutEntityDestroy entityDestroy = new PacketPlayOutEntityDestroy(craftPlayerP.getId());
        PacketPlayOutPlayerInfo joinPacket = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER, craftPlayerP);
        PacketPlayOutNamedEntitySpawn entitySpawn = new PacketPlayOutNamedEntitySpawn(craftPlayerP);

        List<Player> playerList = new ArrayList<>(Bukkit.getOnlinePlayers());
        playerList.remove(p);

        playerList.forEach((player) -> {
            EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
            if (player.canSee(p)) {

                PlayerConnection playerConnection = entityPlayer.playerConnection;

                playerConnection.sendPacket(entityDestroy);
                playerConnection.sendPacket(removePacket);
                playerConnection.sendPacket(joinPacket);
                playerConnection.sendPacket(entitySpawn);

            }
        });

    }

}
