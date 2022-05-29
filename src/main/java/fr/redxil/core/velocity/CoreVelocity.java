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
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import fr.redline.pms.pm.RedisPMManager;
import fr.redline.pms.utils.IpInfo;
import fr.redxil.api.common.API;
import fr.redxil.api.common.PluginEnabler;
import fr.redxil.api.common.event.CoreEnabledEvent;
import fr.redxil.api.common.player.APIPlayer;
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
import java.util.logging.Level;
import java.util.logging.Logger;

public class CoreVelocity implements PluginEnabler {

    static CoreVelocity instance;
    final ProxyServer proxyServer;
    final Logger logger;
    final File folder;
    final CommandManager commandManager;
    final IpInfo ipInfo;
    boolean enabled = false;

    public CoreVelocity(ProxyServer server, CommandManager commandManager, Logger logger, File folder) {

        this.proxyServer = server;
        this.commandManager = commandManager;
        this.logger = logger;
        this.folder = folder;

        String[] ipString = getProxyServer().getBoundAddress().toString().split(":");
        this.ipInfo = new IpInfo(ipString[0], Integer.valueOf(ipString[1]));

        new CoreAPI(this);

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
        API.getInstance().getRedisManager().ifPresent(redis ->
                RedisPMManager.sendRedissonPluginMessage(redis.getRedissonClient(), "onAPIEnabled", API.getInstance().getServerID()));
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

        API.getInstance().getRedisManager().ifPresent(redis ->
                RedisPMManager.sendRedissonPluginMessage(redis.getRedissonClient(), "onAPIDisabled", API.getInstance().getServerID()));
    }

    @Override
    public void onAPILoadFail() {

    }

    @Override
    public boolean isPluginEnabled() {
        return enabled;
    }

    @Override
    public void setPluginEnable(boolean b) {
        enabled = b;
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

        cm.register(new BrigadierCommand(new BanCmd().buildCommands()));
        cm.register(new BrigadierCommand(new WarnCmd().buildCommands()));
        cm.register(new BrigadierCommand(new KickCmd().buildCommands()));
        cm.register(new BrigadierCommand(new MuteCmd().buildCommands()));

        cm.register(new BrigadierCommand(new UnBanCmd().buildCommands()));
        cm.register(new BrigadierCommand(new UnMuteCmd().buildCommands()));

        cm.register(new BrigadierCommand(new StaffCmd().buildCommands()));
        cm.register(new BrigadierCommand(new CibleCmd().buildCommands()));
        cm.register(new BrigadierCommand(new NickCheckCmd().buildCommands()));
        cm.register(new BrigadierCommand(new SetRankCmd().buildCommands()));
        cm.register(new BrigadierCommand(new InfoCmd().buildCommands()));

        cm.register(new BrigadierCommand(new BlackListCmd().buildCommands()));
        cm.register(new BrigadierCommand(new FriendCmd().buildCommands()));
        cm.register(new BrigadierCommand(new NickCmd().buildCommands()));

        cm.register(new BrigadierCommand(new ShutdownCmd().buildCommands()));
        cm.register(new BrigadierCommand(new RCmd().buildCommands()));
        cm.register(new BrigadierCommand(new MsgCmd().buildCommands()));

    }

    public ProxyServer getProxyServer() {
        return proxyServer;
    }

    @Subscribe
    public void proxyShutdown(ProxyShutdownEvent event) {
        API.getInstance().shutdown();
    }

    @Override
    public IpInfo getServerIp() {
        return ipInfo;
    }

    @Override
    public boolean isVelocity() {
        return true;
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
    public File getPluginDataFolder() {
        return folder;
    }

    @Override
    public String getServerVersion() {
        return getProxyServer().getVersion().getVersion();
    }

    @Override
    public void printLog(Level level, String msg) {
        this.logger.log(level, msg);
        //System.out.println("[" + level.getName() + "] " + msg);
    }

    @Override
    public void sendMessage(APIPlayer apiPlayer, String s) {
        Optional<Player> optionalPlayer = this.getProxyServer().getPlayer(apiPlayer.getUUID());
        optionalPlayer.ifPresentOrElse(
                player -> player.sendMessage(Component.text(s)),
                () -> apiPlayer.sendMessage(s)
        );
    }

    @Override
    public void sendMessage(String s, String s1) {
        this.getProxyServer().getPlayer(s).ifPresentOrElse(
                player -> player.sendMessage(Component.text(s)),
                () -> API.getInstance().getPlayerManager().getPlayer(s).ifPresent(player -> player.sendMessage(s1))
        );
    }

    @Override
    public void sendMessage(UUID uuid, String s) {
        this.getProxyServer().getPlayer(uuid).ifPresentOrElse(
                player -> player.sendMessage(Component.text(s)),
                () -> API.getInstance().getPlayerManager().getPlayer(uuid).ifPresent(player -> player.sendMessage(s))
        );
    }

}
