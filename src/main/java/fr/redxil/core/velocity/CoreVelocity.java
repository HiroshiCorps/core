/* Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 */

package fr.redxil.core.velocity;

import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import fr.redline.pms.utils.IpInfo;
import fr.redline.pms.utils.SystemColor;
import fr.redxil.api.common.API;
import fr.redxil.api.common.PluginEnabler;
import fr.redxil.api.common.message.TextComponentBuilder;
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.api.common.server.Server;
import fr.redxil.api.velocity.Velocity;
import fr.redxil.core.common.CoreAPI;
import net.kyori.adventure.text.Component;

import java.io.File;
import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CoreVelocity extends Velocity implements PluginEnabler {

    final ProxyServer proxyServer;
    final Logger logger;
    final File folder;
    final CommandManager commandManager;
    final IpInfo ipInfo;

    public CoreVelocity(ProxyServer server, CommandManager commandManager, Logger logger, File folder) {
        super();

        this.proxyServer = server;
        this.commandManager = commandManager;
        this.logger = logger;
        this.folder = folder;

        String[] ipString = getProxyServer().getBoundAddress().toString().split(":");
        this.ipInfo = new IpInfo(ipString[0], Integer.valueOf(ipString[1]));
        new CoreAPI(this, CoreAPI.ServerAccessEnum.PRENIUM);
        if (API.getInstance().isEnabled()) {
            checkCrash();
        }
    }

    public void checkCrash() {

        System.out.println("Checking for Crashed APIPlayer");
        for (Player player : getProxyServer().getAllPlayers())
            player.disconnect((Component) TextComponentBuilder.createTextComponent("Error"));

        Server server = API.getInstance().getServer();
        String serverName = server.getServerName();

        Collection<UUID> playerUUIDList = server.getPlayerUUIDList();
        if (playerUUIDList.isEmpty()) return;

        System.out.println("Founded " + playerUUIDList.size() + " crashed player data");
        for (UUID playerUUID : playerUUIDList) {

            server.removePlayerInServer(playerUUID);
            APIPlayer apiPlayer = API.getInstance().getPlayerManager().getPlayer(playerUUID);

            if (apiPlayer != null)

                if (apiPlayer.getBungeeServer().getServerName().equals(serverName)) {

                    System.out.println("Saving player: " + playerUUID.toString());
                    Server lastSpigotServer = apiPlayer.getServer();
                    if (lastSpigotServer != null)
                        lastSpigotServer.removePlayerInServer(playerUUID);

                    apiPlayer.unloadPlayer();

                }

        }

    }

    @Override
    public ProxyServer getProxyServer() {
        return proxyServer;
    }

    @Subscribe
    public void proxyShutdown(ProxyShutdownEvent event) {
        onDisable();
    }

    public void onDisable() {
        API.getInstance().shutdown();
    }

    @Override
    public IpInfo getServerIp() {
        return ipInfo;
    }

    @Override
    public boolean isBungee() {
        return true;
    }

    @Override
    public String getServerName() {
        return getProxyServer().getVersion().getName();
    }

    @Override
    public String getPluginVersion() {
        return getProxyServer().getVersion().getVersion();
    }

    @Override
    public int getMaxPlayer() {
        return 0;
    }

    @Override
    public void shutdownPlugin(String s) {

        System.out.println(SystemColor.RED + "Shutting down server: " + s + SystemColor.RESET);
        getProxyServer().getEventManager().unregisterListeners(this);
        onDisable();

    }

    @Override
    public void shutdownServer(String s) {

        System.out.println(SystemColor.RED + "Shutting down server: " + s + SystemColor.RESET);
        getProxyServer().getAllPlayers().forEach(proxPlayer -> proxPlayer.disconnect((Component) TextComponentBuilder.createTextComponent(s)));
        new Timer().schedule(

                new TimerTask() {
                    @Override
                    public void run() {
                        getProxyServer().shutdown();
                    }
                }

                , 5);

    }

    @Override
    public File getPluginDataFolder() {
        return folder;
    }

    @Override
    public String getServerVersion() {
        return getProxyServer().getVersion().getVersion();
    }

    @Override
    public void printLog(Level level, String msg) {
        logger.log(level, msg);
    }

}
