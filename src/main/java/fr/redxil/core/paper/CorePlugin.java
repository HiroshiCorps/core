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
import fr.redline.pms.utils.SystemColor;
import fr.redxil.api.common.API;
import fr.redxil.api.common.PluginEnabler;
import fr.redxil.core.paper.cmd.*;
import fr.redxil.core.paper.event.ConnectionListener;
import fr.redxil.core.paper.event.PlayerInteractEvent;
import fr.redxil.core.paper.freeze.FreezeMessageGestion;
import fr.redxil.core.paper.moderatormode.ModeratorMain;
import fr.redxil.core.paper.receiver.AskSwitchListener;
import fr.redxil.core.paper.receiver.Receiver;
import fr.redxil.core.paper.vanish.VanishGestion;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.logging.Level;

public class CorePlugin {

    private static CorePlugin instance;
    private final PluginEnabler pluginEnabler;
    private final JavaPlugin javaPlugin;

    private VanishGestion vanish;
    private ModeratorMain moderatorMain;
    private FreezeMessageGestion freezeGestion;

    public static CorePlugin getInstance() {
        return instance;
    }

    public CorePlugin(PluginEnabler pluginEnabler, JavaPlugin javaPlugin) {
        instance = this;
        this.pluginEnabler = pluginEnabler;
        this.javaPlugin = javaPlugin;
        this.pluginEnabler.printLog(Level.FINE,
                SystemColor.WHITE + "#==========[WELCOME TO SERVER CORE]===========#\n"
                        + SystemColor.YELLOW + "# SERVERCORE is now loading. Please read      #\n"
                        + "# carefully all outputs coming from it.        #\n"
                        + SystemColor.WHITE + "#==============================================#" + SystemColor.RESET
        );

        onLoad();
    }

    public void onLoad() {
        this.pluginEnabler.printLog(Level.FINE, SystemColor.GREEN + "API Started" + SystemColor.RESET);

        this.vanish = new VanishGestion(this);
        this.freezeGestion = new FreezeMessageGestion(this);

        EventInventory.enableEvent(this.javaPlugin);
        new AskSwitchListener();

        this.moderatorMain = new ModeratorMain();

        new Receiver();

        PluginManager p = this.javaPlugin.getServer().getPluginManager();

        p.registerEvents(new ConnectionListener(this), this.javaPlugin);
        p.registerEvents(new PlayerInteractEvent(), this.javaPlugin);

        this.pluginEnabler.printLog(Level.INFO, SystemColor.GREEN + "EventListener Started" + SystemColor.RESET);

        Objects.requireNonNull(this.javaPlugin.getCommand("mod")).setExecutor(new ModCmd());
        Objects.requireNonNull(this.javaPlugin.getCommand("freeze")).setExecutor(new FreezeCmd());
        Objects.requireNonNull(this.javaPlugin.getCommand("vanish")).setExecutor(new VanishCmd());
        Objects.requireNonNull(this.javaPlugin.getCommand("fly")).setExecutor(new FlyCmd());
        Objects.requireNonNull(this.javaPlugin.getCommand("speed")).setExecutor(new SpeedCmd());
        Objects.requireNonNull(this.javaPlugin.getCommand("flyspeed")).setExecutor(new SpeedCmd());

        this.pluginEnabler.printLog(Level.INFO, SystemColor.GREEN + "Command registered" + SystemColor.RESET);

        API.getInstance().getRedisManager().ifPresent(redis ->
                RedisPMManager.sendRedissonPluginMessage(redis.getRedissonClient(), "onAPIEnabled", API.getInstance().getServerID()));
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

}