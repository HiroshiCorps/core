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
import fr.redxil.api.common.PluginEnabler;
import fr.redxil.api.common.message.TextComponentBuilder;
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.api.common.server.Server;
import fr.redxil.api.velocity.Velocity;
import fr.redxil.core.common.CoreAPI;
import fr.redxil.core.velocity.commands.NickCmd;
import fr.redxil.core.velocity.commands.ShutdownCmd;
import fr.redxil.core.velocity.commands.friend.BlackListCmd;
import fr.redxil.core.velocity.commands.friend.FriendCmd;
import fr.redxil.core.velocity.commands.mod.CibleCmd;
import fr.redxil.core.velocity.commands.mod.InfoCmd;
import fr.redxil.core.velocity.commands.mod.NickCheckCmd;
import fr.redxil.core.velocity.commands.mod.action.cancel.UnBanCmd;
import fr.redxil.core.velocity.commands.mod.action.cancel.UnMuteCmd;
import fr.redxil.core.velocity.commands.mod.action.punish.BanCmd;
import fr.redxil.core.velocity.commands.mod.action.punish.KickCmd;
import fr.redxil.core.velocity.commands.mod.action.punish.MuteCmd;
import fr.redxil.core.velocity.commands.mod.action.punish.WarnCmd;
import fr.redxil.core.velocity.commands.mod.highstaff.SetRankCmd;
import fr.redxil.core.velocity.commands.mod.highstaff.StaffCmd;
import fr.redxil.core.velocity.commands.msg.MsgCmd;
import fr.redxil.core.velocity.commands.msg.RCmd;
import fr.redxil.core.velocity.commands.party.PartyCmd;
import fr.redxil.core.velocity.listener.JoinListener;
import fr.redxil.core.velocity.listener.PlayerListener;
import fr.redxil.core.velocity.pmsListener.PlayerSwitchListener;
import fr.redxil.core.velocity.pmsListener.UpdaterReceiver;
import net.kyori.adventure.text.Component;

import java.io.File;
import java.util.Collection;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.logging.Level;

public class CoreVelocity extends Velocity implements PluginEnabler {

    private static CoreVelocity instance;
    JoinListener joinListener;
    IpInfo ipInfo;

    public CoreVelocity() {
        super();
        instance = this;
        String[] ipString = getProxyServer().getBoundAddress().toString().split(":");
        this.ipInfo = new IpInfo(ipString[0], Integer.valueOf(ipString[1]));
        new CoreAPI(this, CoreAPI.ServerAccessEnum.PRENIUM);
        if (CoreAPI.get().isEnabled()) {
            registerEvents();
            registerCommands();
            checkCrash();
        }
    }

    static void enableCore() {
        new CoreVelocity();
    }

    public void checkCrash() {

        System.out.println("Checking for Crashed APIPlayer");
        for (Player player : getProxyServer().getAllPlayers())
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

                    apiPlayer.unloadPlayer();

                }

        }

    }

    public static CoreVelocity getInstance() {
        return instance;
    }

    @Override
    public ProxyServer getProxyServer() {
        return VelocityEnabler.getInstance().getProxyServer();
    }

    public void registerEvents() {
        this.joinListener = new JoinListener();

        getProxyServer().getEventManager().register(this, this.joinListener);
        getProxyServer().getEventManager().register(this, new PlayerListener());
        new PlayerSwitchListener();
        new UpdaterReceiver();
    }

    @Override
    public void registerCommands() {
        if (CoreAPI.get().isEnabled()) {

            CommandManager cm = VelocityEnabler.getInstance().getCommandManager();

            cm.register(new PartyCmd().getBrigadierCommand());

            cm.register(new BanCmd().getBrigadierCommand());
            cm.register(new WarnCmd().getBrigadierCommand());
            cm.register(new KickCmd().getBrigadierCommand());
            cm.register(new MuteCmd().getBrigadierCommand());

            cm.register(new UnBanCmd().getBrigadierCommand());
            cm.register(new UnMuteCmd().getBrigadierCommand());

            cm.register(new StaffCmd().getBrigadierCommand());
            cm.register(new CibleCmd().getBrigadierCommand());
            cm.register(new NickCheckCmd().getBrigadierCommand());
            cm.register(new InfoCmd().getBrigadierCommand());
            cm.register(new SetRankCmd().getBrigadierCommand());

            cm.register(new NickCmd().getBrigadierCommand());
            cm.register(new FriendCmd().getBrigadierCommand());
            cm.register(new BlackListCmd().getBrigadierCommand());

            cm.register(new MsgCmd().getBrigadierCommand());
            cm.register(new RCmd().getBrigadierCommand());

            cm.register(new ShutdownCmd().getBrigadierCommand());

        }
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
        return VelocityEnabler.getInstance().getPathFile();
    }

    @Override
    public String getServerVersion() {
        return getProxyServer().getVersion().getVersion();
    }

    @Override
    public void printLog(Level level, String msg) {
        VelocityEnabler.getInstance().getLogger().log(level, msg);
    }

}
