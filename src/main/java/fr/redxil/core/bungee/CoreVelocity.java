/* Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 */

package fr.redxil.core.bungee;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import fr.redline.pms.utils.IpInfo;
import fr.redline.pms.utils.SystemColor;
import fr.redxil.api.common.PluginEnabler;
import fr.redxil.api.common.message.TextComponentBuilder;
import fr.redxil.api.common.message.TextComponentBuilderVelocity;
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.api.common.rank.RankList;
import fr.redxil.api.common.server.Server;
import fr.redxil.api.common.utils.ServerAccessEnum;
import fr.redxil.api.velocity.Velocity;
import fr.redxil.core.bungee.commands.mod.action.punish.BanCmd;
import fr.redxil.core.bungee.listener.PlayerListener;
import fr.redxil.core.bungee.pmsListener.UpdaterReceiver;
import fr.redxil.core.common.CoreAPI;
import fr.redxil.core.bungee.commands.NickCmd;
import fr.redxil.core.bungee.commands.ProxyCmd;
import fr.redxil.core.bungee.commands.ShutdownCmd;
import fr.redxil.core.bungee.commands.friend.BlackListCmd;
import fr.redxil.core.bungee.commands.friend.FriendCmd;
import fr.redxil.core.bungee.commands.mod.CibleCmd;
import fr.redxil.core.bungee.commands.mod.InfoCmd;
import fr.redxil.core.bungee.commands.mod.NickCheckCmd;
import fr.redxil.core.bungee.commands.mod.action.cancel.UnBanCmd;
import fr.redxil.core.bungee.commands.mod.action.cancel.UnMuteCmd;
import fr.redxil.core.bungee.commands.mod.action.punish.KickCmd;
import fr.redxil.core.bungee.commands.mod.action.punish.MuteCmd;
import fr.redxil.core.bungee.commands.mod.action.punish.WarnCmd;
import fr.redxil.core.bungee.commands.mod.highstaff.SetRankCmd;
import fr.redxil.core.bungee.commands.mod.highstaff.StaffCmd;
import fr.redxil.core.bungee.commands.msg.MsgCmd;
import fr.redxil.core.bungee.commands.msg.RCmd;
import fr.redxil.core.bungee.commands.party.PartyCmd;
import fr.redxil.core.bungee.listener.JoinListener;
import fr.redxil.core.bungee.pmsListener.PlayerSwitchListener;
import net.kyori.adventure.text.Component;

import java.io.File;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

@Plugin(id = "myfirstplugin", name = "My First Plugin", version = "1.0-SNAPSHOT",
        description = "I did it!", authors = {"Me"})
public class CoreVelocity extends Velocity implements PluginEnabler {

    private static CoreVelocity instance;
    private final ProxyServer proxyServer;
    private final Logger logger;
    private final File pathFile;
    private final CommandManager cm;
    private IpInfo ipInfo;
    private JoinListener joinListener;

    @Inject
    public CoreVelocity(ProxyServer server, CommandManager commandManager, Logger logger, @DataDirectory Path folder) {
        super();
        instance = this;
        this.proxyServer = server;
        this.logger = logger;
        this.pathFile = folder.toFile();
        this.cm = commandManager;
        if (!pathFile.exists())
            pathFile.mkdirs();
        server.getEventManager().register(this, this);
        logger.info("Hello there, it's a test plugin I made!");
    }

    public static CoreVelocity getInstance() {
        return instance;
    }

    @Subscribe
    public void onEnable(ProxyInitializeEvent pie) {
        String[] ipString = proxyServer.getBoundAddress().toString().split(":");
        this.ipInfo = new IpInfo(ipString[0], Integer.valueOf(ipString[1]));
        new CoreAPI(this, ServerAccessEnum.PRENIUM);
        if (CoreAPI.get().isEnabled()) {
            checkCrash();

            this.joinListener = new JoinListener();

            proxyServer.getEventManager().register(this, this.joinListener);
            proxyServer.getEventManager().register(this, new PlayerListener());

            cm.register("party", new PartyCmd());

            cm.register("ban", new BanCmd());
            cm.register("warn", new WarnCmd());
            cm.register("kick", new KickCmd());
            cm.register("mute", new MuteCmd());

            cm.register("unban", new UnBanCmd());
            cm.register("unmute", new UnMuteCmd());

            cm.register("staff", new StaffCmd(), "s");
            cm.register("cible", new CibleCmd());
            cm.register("nickcheck", new NickCheckCmd());
            cm.register("info", new InfoCmd());
            cm.register("setrank", new SetRankCmd());

            cm.register("nick", new NickCmd());
            cm.register("proxy", new ProxyCmd());
            cm.register("friend", new FriendCmd());
            cm.register("blacklist", new BlackListCmd(), "bl");

            cm.register("msg", new MsgCmd());
            cm.register("r", new RCmd());

            cm.register("shutdown", new ShutdownCmd());

            new PlayerSwitchListener();
            new UpdaterReceiver();

            RankList.enableCloudPerms();

            System.out.println("Velocity is started");
        }

    }

    public void checkCrash() {

        System.out.println("Checking for Crashed APIPlayer");
        for (Player player : proxyServer.getAllPlayers())
            player.disconnect((Component) TextComponentBuilder.createTextComponent("Error"));

        Server server = CoreAPI.get().getServer();
        String serverName = server.getServerName();

        Collection<UUID> playerUUIDList = server.getPlayerUUIDList();
        if (playerUUIDList.isEmpty()) return;

        System.out.println("Founded " + playerUUIDList.size() + " crashed player data");
        for (UUID playerUUID : playerUUIDList) {

            server.removePlayerInServer(playerUUID);
            APIPlayer apiPlayer = CoreAPI.get().getPlayerManager().getPlayer(playerUUID);

            if (apiPlayer != null)

                if (apiPlayer.getBungeeServer().getServerName().equals(serverName)) {

                    System.out.println("Saving player: " + playerUUID.toString());
                    Server lastSpigotServer = apiPlayer.getServer();
                    if (lastSpigotServer != null)
                        lastSpigotServer.removePlayerInServer(playerUUID);

                    apiPlayer.disconnectPlayer();

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
        CoreAPI.get().shutdown();
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
        return proxyServer.getVersion().getVersion();
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
        this.joinListener.acceptConnection = false;
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
        return pathFile;
    }

    @Override
    public String getServerVersion() {
        return proxyServer.getVersion().getVersion();
    }

    @Override
    public void printLog(Level level, String msg) {
        logger.log(level, msg);
    }

}
