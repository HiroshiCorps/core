/* Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 */

package fr.redxil.core.paper.team;

import fr.redline.pms.connect.linker.pm.PMManager;
import fr.redline.pms.connect.linker.pm.PMReceiver;
import fr.redxil.api.common.API;
import fr.redxil.api.common.game.GameState;
import fr.redxil.api.common.game.team.Team;
import fr.redxil.api.spigot.minigame.GameBuilder;
import fr.redxil.api.spigot.utils.Reflection;
import fr.redxil.core.common.game.team.CTeam;
import net.minecraft.server.v1_12_R1.ChatComponentText;
import net.minecraft.server.v1_12_R1.PacketPlayOutScoreboardTeam;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class TeamListener implements PMReceiver, Listener {

    public TeamListener() {
        Bukkit.getPluginManager().registerEvents(this, (JavaPlugin) API.getInstance().getPluginEnabler());
        PMManager.addRedissonPMListener(API.getInstance().getRedisManager().getRedissonClient(), "teamON", Long.class, this);
        PMManager.addRedissonPMListener(API.getInstance().getRedisManager().getRedissonClient(), "teamOFF", Long.class, this);
        PMManager.addRedissonPMListener(API.getInstance().getRedisManager().getRedissonClient(), "opChange", Long.class, this);
        PMManager.addRedissonPMListener(API.getInstance().getRedisManager().getRedissonClient(), "addp", String.class, this);
        PMManager.addRedissonPMListener(API.getInstance().getRedisManager().getRedissonClient(), "rmp", String.class, this);
    }

    public static void sendPacketToAll(PacketPlayOutScoreboardTeam ppost) {

        if (ppost == null) return;
        Bukkit.getOnlinePlayers().forEach((player) -> Reflection.sendPacket(player, ppost));

    }

    @Override
    public void pluginMessageReceived(String s, Object o) {

        switch (s) {

            case "teamON": {
                Team team = API.getInstance().getTeamManager().getTeam((Long) o);
                sendPacketToAll(makePacket(team, team.getListPlayerName(true), 0));
                break;
            }

            case "teamOFF": {
                Team team = API.getInstance().getTeamManager().getTeam((Long) o);
                sendPacketToAll(makePacket(team, null, 1));
                break;
            }

            case "opChange": {
                Team team = API.getInstance().getTeamManager().getTeam((Long) o);
                sendPacketToAll(makePacket(team, null, 2));
                break;
            }

            case "addp": {
                String[] split = ((String) o).split(CTeam.teamBalise);
                Team team = API.getInstance().getTeamManager().getTeam(Long.parseLong(split[0]));
                sendPacketToAll(makePacket(team, Collections.singletonList(Bukkit.getPlayer(UUID.fromString(split[1])).getName()), 3));
                break;
            }

            case "rmp": {
                String[] split = ((String) o).split(CTeam.teamBalise);
                Team team = API.getInstance().getTeamManager().getTeam(Long.parseLong(split[0]));
                sendPacketToAll(makePacket(team, Collections.singletonList(Bukkit.getPlayer(UUID.fromString(split[1])).getName()), 4));
                break;
            }

        }

    }

    protected PacketPlayOutScoreboardTeam makePacket(Team team, List<String> playerList, int n) {
        //0: Création de team
        //1: Suppression de team
        //2: Changements infos de la team
        //3: Ajout d'un joueur
        //4: Suppression d'un joueur

        if (!team.hisClientSideAvailable()) return null;

        try {
            PacketPlayOutScoreboardTeam packet = new PacketPlayOutScoreboardTeam();

            Reflection.setValue(packet, "a", team.getName()); // Team display name
            Reflection.setValue(packet, "b", new ChatComponentText(team.getDisplayName())); // Team display name
            Reflection.setValue(packet, "c", new ChatComponentText(team.getPrefix())); // Team prefix
            Reflection.setValue(packet, "d", new ChatComponentText(team.getSuffix())); // Team suffix
            Reflection.setValue(packet, "e", team.getHideToOtherTeams() ? "always" : "never"); // Name tag visible
            Reflection.setValue(packet, "f", "never"); // Collision rule
            // Reflection.setValue(packet, "g", news.size()); // APIPlayer count
            Reflection.setValue(packet, "h", playerList != null ? playerList : new ArrayList<>()); // Players
            // Reflection.setValue(packet, "i", n); // Action id
            // Reflection.setValue(packet, "j", 0); // Friendly fire
            Reflection.setValue(packet, "i", n); // Action id
            Reflection.setValue(packet, "j", team.getFriendlyFire() ? 1 : 0); // Friendly fire

            return packet;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @EventHandler
    public void playerJoin(PlayerJoinEvent joinEvent) {

        Player player = joinEvent.getPlayer();
        for (Long teamID : API.getInstance().getServer().getTeamLinked()) {
            Team team = API.getInstance().getTeamManager().getTeam(teamID);
            PacketPlayOutScoreboardTeam packet = makePacket(team, team.getListPlayerName(true), 0);
            if (packet == null) continue;
            Reflection.sendPacket(player, packet);
        }

    }

    @EventHandler
    public void entityDamage(EntityDamageEvent event) {
        if (!GameBuilder.getGameBuilder().getGame().isGameState(GameState.OCCUPIED))
            event.setCancelled(true);
    }

}
