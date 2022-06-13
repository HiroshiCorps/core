/*
 *
 * Copyright (C) GIMENEZ Nino and PHILIPPE Nelson - All Rights Reserved
 * Unauthorized copying or modification of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by GIMENEZ Nino and PHILIPPE Nelson, ninogmz33@gmail.com | philippenelson59@gmail.com - 2021
 *
 */

package fr.redxil.core.paper;

import fr.redline.invinteract.event.EventInventory;
import fr.redline.pms.pm.RedisPMManager;
import fr.redline.pms.utils.IpInfo;
import fr.redline.pms.utils.SystemColor;
import fr.redxil.api.common.API;
import fr.redxil.api.common.PluginEnabler;
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.core.common.CoreAPI;
import fr.redxil.core.paper.cmd.*;
import fr.redxil.core.paper.event.ConnectionListener;
import fr.redxil.core.paper.event.PlayerInteractEvent;
import fr.redxil.core.paper.freeze.FreezeMessageGestion;
import fr.redxil.core.paper.moderatormode.ModeratorMain;
import fr.redxil.core.paper.receiver.AskSwitchListener;
import fr.redxil.core.paper.receiver.Receiver;
import fr.redxil.core.paper.vanish.VanishGestion;
import fr.xilitra.hiroshisav.enums.ServerType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;

public class CorePlugin extends JavaPlugin implements PluginEnabler {

    private static CorePlugin instance;
    boolean enabled = false;

    private VanishGestion vanish;
    private ModeratorMain moderatorMain;
    private FreezeMessageGestion freezeGestion;

    public static CorePlugin getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;

        printLog(Level.FINE,
                SystemColor.WHITE + "#==========[WELCOME TO SERVER API]===========#\n"
                        + SystemColor.YELLOW + "# SERVERAPI is now loading. Please read      #\n"
                        + "# carefully all outputs coming from it.        #\n"
                        + SystemColor.WHITE + "#==============================================#" + SystemColor.RESET
        );

        printLog(Level.FINE, SystemColor.YELLOW + "Starting API ..." + SystemColor.RESET);
        new CoreAPI(this);

    }

    @Override
    public void onAPIEnabled() {
        printLog(Level.FINE, SystemColor.GREEN + "API Started" + SystemColor.RESET);

        this.vanish = new VanishGestion(this);
        this.freezeGestion = new FreezeMessageGestion(this);

        EventInventory.enableEvent(this);
        new AskSwitchListener();

        this.moderatorMain = new ModeratorMain();

        new Receiver();

        PluginManager p = this.getServer().getPluginManager();

        p.registerEvents(new ConnectionListener(this), this);
        p.registerEvents(new PlayerInteractEvent(), this);

        printLog(Level.INFO, SystemColor.GREEN + "EventListener Started" + SystemColor.RESET);

        Objects.requireNonNull(this.getCommand("mod")).setExecutor(new ModCmd());
        Objects.requireNonNull(this.getCommand("freeze")).setExecutor(new FreezeCmd());
        Objects.requireNonNull(this.getCommand("vanish")).setExecutor(new VanishCmd());
        Objects.requireNonNull(this.getCommand("fly")).setExecutor(new FlyCmd());
        Objects.requireNonNull(this.getCommand("speed")).setExecutor(new SpeedCmd());
        Objects.requireNonNull(this.getCommand("flyspeed")).setExecutor(new SpeedCmd());

        printLog(Level.INFO, SystemColor.GREEN + "Command registered" + SystemColor.RESET);

        this.setEnabled(true);

        API.getInstance().getRedisManager().ifPresent(redis ->
                RedisPMManager.sendRedissonPluginMessage(redis.getRedissonClient(), "onAPIEnabled", API.getInstance().getServerID()));
    }

    @Override
    public void onAPIDisabled() {

        HandlerList.unregisterAll(this);

        Objects.requireNonNull(this.getCommand("mod")).setExecutor(new ModCmd());
        Objects.requireNonNull(this.getCommand("freeze")).setExecutor(new FreezeCmd());
        Objects.requireNonNull(this.getCommand("vanish")).setExecutor(new VanishCmd());
        Objects.requireNonNull(this.getCommand("fly")).setExecutor(new FlyCmd());

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


    @Override
    public void onDisable() {
        API.getInstance().shutdown();
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
    public String getPluginVersion() {
        return getDescription().getVersion();
    }

    @Override
    public IpInfo getServerIp() {
        return new IpInfo(Bukkit.getIp(), Bukkit.getPort());
    }

    @Override
    public boolean isVelocity() {
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
        System.out.println("[" + level.getName() + "] " + msg);
    }

    @Override
    public void sendMessage(APIPlayer apiPlayer, String s) {
        Player player = Bukkit.getPlayer(apiPlayer.getUUID());
        if (player != null)
            player.sendMessage(s);
        else apiPlayer.sendMessage(s);
    }

    @Override
    public void sendMessage(String s, String s1) {
        Player player = Bukkit.getPlayer(s);
        if (player != null)
            player.sendMessage(s1);
        else {
            API.getInstance().getPlayerManager().getPlayer(s).ifPresent(player2 -> player2.sendMessage(s1));
        }
    }

    @Override
    public void sendMessage(UUID uuid, String s1) {
        Player player = Bukkit.getPlayer(uuid);
        if (player != null)
            player.sendMessage(s1);
        else {
            API.getInstance().getPlayerManager().getPlayer(uuid).ifPresent(player2 -> player2.sendMessage(s1));
        }
    }

    @Override
    public ServerType getServerType() {
        return ServerType.HUB;
    }

}