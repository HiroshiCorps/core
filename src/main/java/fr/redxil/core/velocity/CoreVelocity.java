/*
 *
 * Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 *
 */

package fr.redxil.core.velocity;

import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import fr.redline.pms.utils.IpInfo;
import fr.redxil.api.common.API;
import fr.redxil.api.common.APIEnabler;
import fr.redxil.api.common.APILoadError;
import fr.redxil.api.common.APIPhaseInit;
import fr.redxil.api.common.event.CoreEnabledEvent;
import fr.redxil.api.common.server.creator.ServerInfo;
import fr.redxil.api.common.server.creator.VelocityServerInfo;
import fr.redxil.api.common.server.type.ServerStatus;
import fr.redxil.core.common.CoreAPI;
import fr.redxil.core.velocity.commands.NickCmd;
import fr.redxil.core.velocity.commands.PartyCMD;
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
import fr.redxil.core.velocity.listener.JoinListener;
import fr.redxil.core.velocity.listener.LeaveListener;
import fr.redxil.core.velocity.listener.PlayerListener;
import fr.redxil.core.velocity.listener.ServerListener;
import fr.redxil.core.velocity.receiver.MessageListener;
import fr.redxil.core.velocity.receiver.PlayerSwitchListener;
import net.kyori.adventure.text.Component;

import java.io.File;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;

public class CoreVelocity implements APIEnabler {

    static CoreVelocity instance;
    final ProxyServer proxyServer;
    final Logger logger;
    final File folder;
    final CommandManager commandManager;
    final IpInfo ipInfo;

    public CoreVelocity(ProxyServer server, CommandManager commandManager, Logger logger, File folder) {

        this.proxyServer = server;
        this.commandManager = commandManager;
        this.logger = logger;
        this.folder = folder;

        String[] ipString = getProxyServer().getBoundAddress().toString().split(":");
        this.ipInfo = new IpInfo(ipString[0], Integer.valueOf(ipString[1]));

        new CoreAPI().initPhase(APIPhaseInit.PART_1, this);

    }

    public static CoreVelocity getInstance() {
        return instance;
    }

    @Override
    public void onAPIEnabled() {
        registerCommands();
        registerEvents();
        assert getProxyServer() != null;
        getProxyServer().getEventManager().fire(new CoreEnabledEvent(this));
    }

    @Override
    public void onAPIDisabled() {
        proxyServer.getEventManager().unregisterListeners(VelocityEnabler.getInstance());
        CommandManager cm = commandManager;

        cm.unregister(new BanCmd().getName());
        cm.unregister(new WarnCmd().getName());
        cm.unregister(new KickCmd().getName());
        cm.unregister(new MuteCmd().getName());

        cm.unregister(new UnBanCmd().getName());
        cm.unregister(new UnMuteCmd().getName());

        cm.unregister(new StaffCmd().getName());
        cm.unregister(new CibleCmd().getName());
        cm.unregister(new NickCheckCmd().getName());
        cm.unregister(new SetRankCmd().getName());
        cm.unregister(new InfoCmd().getName());

        cm.unregister(new BlackListCmd().getName());
        cm.unregister(new FriendCmd().getName());
        cm.unregister(new NickCmd().getName());

        cm.unregister(new ShutdownCmd().getName());
        cm.unregister(new RCmd().getName());
        cm.unregister(new MsgCmd().getName());
    }

    ServerInfo serverInfo = null;

    @Override
    public void onAPILoadFail(APIPhaseInit apiPhaseInit, APILoadError apiLoadError) {

    }

    public void registerEvents() {
        proxyServer.getEventManager().register(VelocityEnabler.getInstance(), new JoinListener());
        proxyServer.getEventManager().register(VelocityEnabler.getInstance(), new LeaveListener());
        proxyServer.getEventManager().register(VelocityEnabler.getInstance(), new PlayerListener());
        proxyServer.getEventManager().register(VelocityEnabler.getInstance(), new ServerListener());
        proxyServer.getEventManager().register(VelocityEnabler.getInstance(), this);
        new PlayerSwitchListener();
        new MessageListener();
    }

    public void registerCommands() {

        CommandManager cm = commandManager;

        cm.register(new BrigadierCommand(new BanCmd().build()));
        cm.register(new BrigadierCommand(new WarnCmd().build()));
        cm.register(new BrigadierCommand(new KickCmd().build()));
        cm.register(new BrigadierCommand(new MuteCmd().build()));

        cm.register(new BrigadierCommand(new UnBanCmd().build()));
        cm.register(new BrigadierCommand(new UnMuteCmd().build()));

        cm.register(new BrigadierCommand(new StaffCmd().build()));
        cm.register(new BrigadierCommand(new CibleCmd().build()));
        cm.register(new BrigadierCommand(new NickCheckCmd().build()));
        cm.register(new BrigadierCommand(new SetRankCmd().build()));
        cm.register(new BrigadierCommand(new InfoCmd().build()));

        cm.register(new BrigadierCommand(new BlackListCmd().build()));
        cm.register(new BrigadierCommand(new FriendCmd().build()));
        cm.register(new BrigadierCommand(new NickCmd().build()));

        cm.register(new BrigadierCommand(new ShutdownCmd().build()));
        cm.register(new BrigadierCommand(new RCmd().build()));
        cm.register(new BrigadierCommand(new MsgCmd().build()));

        cm.register(new BrigadierCommand(new PartyCMD().build()));

    }

    public ProxyServer getProxyServer() {
        return proxyServer;
    }

    @Subscribe
    public void proxyShutdown(ProxyShutdownEvent event) {
        CoreAPI.getInstance().shutdown();
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
    public Logger getLogger() {
        return this.logger;
    }

    @Override
    public boolean sendMessage(String s, String s1) {
        Optional<Player> player = this.proxyServer.getPlayer(s);
        if (player.isPresent()) {
            player.get().sendMessage(Component.text(s1));
            return true;
        }
        return false;
    }

    @Override
    public boolean sendMessage(UUID uuid, String s) {
        Optional<Player> player = this.proxyServer.getPlayer(uuid);
        if (player.isPresent()) {
            player.get().sendMessage(Component.text(s));
            return true;
        }
        return false;
    }

    @Override
    public void onAPIInitPhaseEnded(APIPhaseInit apiPhaseInit) {
        if (apiPhaseInit == APIPhaseInit.PART_1) {
            serverInfo = new VelocityServerInfo(API.getInstance().getServerName(), ipInfo, ServerStatus.ONLINE, false, "Lobby", 20);
            API.getInstance().initPhase(APIPhaseInit.PART_2, this);
        }
    }

    @Override
    public ServerInfo getServerInfo() {
        return serverInfo;
    }

    @Subscribe
    public void playerQuit(DisconnectEvent de) {
        CoreAPI.getInstance().getPartyManager().getPlayerParty(de.getPlayer().getUniqueId()).ifPresent(party -> party.quitParty(de.getPlayer().getUniqueId()));
    }

    @Override
    public boolean isVelocity() {
        return true;
    }

}
