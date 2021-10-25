/* Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 */

package fr.redxil.core.spigot;

import fr.redline.pms.utils.IpInfo;
import fr.redline.pms.utils.SystemColor;
import fr.redxil.api.common.PluginEnabler;
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.api.common.server.Server;
import fr.redxil.core.common.CoreAPI;
import fr.redxil.core.spigot.cmd.*;
import fr.redxil.core.spigot.event.EventListener;
import fr.redxil.core.spigot.event.INVEventListener;
import fr.redxil.core.spigot.freeze.FreezeMessageGestion;
import fr.redxil.core.spigot.minigame.PlayerListener;
import fr.redxil.core.spigot.moderatormode.ModeratorMain;
import fr.redxil.core.spigot.moderatormode.UUIDCheckCmd;
import fr.redxil.core.spigot.receiver.Receiver;
import fr.redxil.core.spigot.receiver.UpdaterReceiver;
import fr.redxil.core.spigot.team.TeamListener;
import fr.redxil.core.spigot.vanish.VanishGestion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;
import java.util.logging.Level;

public class CorePlugin extends JavaPlugin implements PluginEnabler {

    private static CorePlugin instance;

    private String serverName;
    private VanishGestion vanish;
    private ModeratorMain moderatorMain;
    private FreezeMessageGestion freezeGestion;
    private fr.redxil.core.spigot.event.EventListener eventListener;

    public static void log(String message) {
        instance.getLogger().info(message);
    }

    public static void log(Level level, String message) {
        instance.getLogger().log(level, message);
    }

    public static CorePlugin getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        log(
                SystemColor.WHITE + "#==========[WELCOME TO SERVER API]===========#\n"
                        + SystemColor.YELLOW + "# SERVERAPI is now loading. Please read      #\n"
                        + "# carefully all outputs coming from it.        #\n"
                        + SystemColor.WHITE + "#==============================================#" + SystemColor.RESET
        );


        log(SystemColor.YELLOW + "Initialize plugin config" + SystemColor.RESET);
        this.saveDefaultConfig();
        this.getConfig();
        log(SystemColor.GREEN + "Plugin config initialized" + SystemColor.RESET);

        log(SystemColor.YELLOW + "Searching servername" + SystemColor.RESET);
        this.serverName = Bukkit.getServer().getServerName();
        if (this.serverName == null || this.serverName.isEmpty()) {
            log(SystemColor.RED + "Cannot get ServerName, Shuttingdown" + SystemColor.RESET);
            log(Level.SEVERE, "Plugin cannot load : ServerName is empty.");
            this.setEnabled(false);
            this.shutdownServer("Plugin cannot load : ServerName is empty.");
            return;
        }

        log(SystemColor.GREEN + "Servername found: " + serverName + SystemColor.RESET);

        log(SystemColor.YELLOW + "Starting API ..." + SystemColor.RESET);
        new CoreAPI(this, CoreAPI.ServerAccessEnum.PRENIUM);

        if (!CoreAPI.get().isEnabled()) {
            log(SystemColor.RED + "Error while loading API, please check error code below" + SystemColor.RESET);
            return;
        }

        log(SystemColor.GREEN + "API Started" + SystemColor.RESET);
        checkCrash();

        this.vanish = new VanishGestion(this);
        this.freezeGestion = new FreezeMessageGestion(this);

        this.moderatorMain = new ModeratorMain();

        log(SystemColor.GREEN + "Mod started" + SystemColor.RESET);

        new Receiver();
        new UpdaterReceiver();
        new ServerCommand(this);
        new TeamListener();

        PluginManager p = this.getServer().getPluginManager();
        p.registerEvents(new INVEventListener(), this);

        this.eventListener = new fr.redxil.core.spigot.event.EventListener(this);
        p.registerEvents(eventListener, this);
        p.registerEvents(new PlayerListener(), this);

        log(SystemColor.GREEN + "EventListener Started" + SystemColor.RESET);

        this.getCommand("mod").setExecutor(new ModCmd());
        this.getCommand("freeze").setExecutor(new FreezeCmd());
        this.getCommand("vanish").setExecutor(new VanishCmd());
        this.getCommand("fly").setExecutor(new FlyCmd());
        this.getCommand("uuid").setExecutor(new UUIDCheckCmd());

        log(SystemColor.GREEN + "Command registered" + SystemColor.RESET);

        new HostCmd(this);
        new ApiCmd(this);
        this.setEnabled(true);

    }

    public void checkCrash() {

        log(Level.FINE, SystemColor.YELLOW + "Checking for Crashed APIPlayer" + SystemColor.RESET);
        for (Player player : Bukkit.getOnlinePlayers())
            player.kickPlayer("Error");

        Server server = CoreAPI.get().getServer();
        String serverName = server.getServerName();

        Collection<UUID> playerUUIDList = new ArrayList<>(server.getPlayerUUIDList());
        if (playerUUIDList.isEmpty()) return;

        log(Level.SEVERE, SystemColor.RED + "Founded " + playerUUIDList.size() + " crashed player data" + SystemColor.RESET);
        for (UUID playerUUID : playerUUIDList) {
            server.removePlayerInServer(playerUUID);
            APIPlayer apiPlayer = CoreAPI.get().getPlayerManager().getPlayer(playerUUID);
            if (apiPlayer != null)
                if (apiPlayer.getServer().getServerName().equals(serverName)) {
                    log(Level.SEVERE, SystemColor.GREEN + "Saving player: " + apiPlayer.getName() + SystemColor.RESET);
                    apiPlayer.unloadPlayer();
                }
        }

    }

    @Override
    public void onDisable() {
        if (CoreAPI.get().isHostServer()) {
            CoreAPI.get().getHost().stop();
            CoreAPI.get().getSQLConnection().execute("DELETE FROM members_hosts WHERE host_server=?", getServerName());
        }

        CoreAPI.get().shutdown();
        this.setEnabled(false);
    }

    public VanishGestion getVanish() {
        return vanish;
    }

    public FreezeMessageGestion getFreezeGestion() {
        return freezeGestion;
    }

    public ModeratorMain getModeratorMain() {
        return moderatorMain;
    }

    @Override
    public String getServerName() {
        return serverName;
    }

    @Override
    public String getPluginVersion() {
        return getDescription().getVersion();
    }

    @Override
    public IpInfo getServerIp() {
        return new IpInfo(Bukkit.getIp(), Bukkit.getPort());
    }

    @Override
    public boolean isBungee() {
        return false;
    }

    @Override
    public File getPluginDataFolder() {
        return super.getDataFolder();
    }

    public int getMaxPlayer() {
        return Bukkit.getServer().getMaxPlayers();
    }

    @Override
    public void shutdownServer(String s) {

        System.out.println(SystemColor.RED + "Shutting down server: " + s + SystemColor.RESET);
        this.getEventListener().acceptConnection = false;
        Bukkit.getOnlinePlayers().forEach(player -> player.kickPlayer(s));
        Bukkit.getServer().shutdown();
        new Timer().schedule(

                new TimerTask() {
                    @Override
                    public void run() {
                        Bukkit.getServer().shutdown();
                    }
                }

                , 5);

    }

    @Override
    public void shutdownPlugin(String s) {

        System.out.println(SystemColor.RED + "Shutting down plugin: " + s + SystemColor.RESET);
        Bukkit.getPluginManager().disablePlugin(this);

    }

    @Override
    public String getServerVersion() {
        try {
            return Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
        } catch (ArrayIndexOutOfBoundsException whatVersionAreYouUsingException) {
            whatVersionAreYouUsingException.printStackTrace();
            return null;
        }
    }

    @Override
    public void printLog(Level level, String msg) {
        log(level, msg);
    }

    public EventListener getEventListener() {
        return this.eventListener;
    }

}