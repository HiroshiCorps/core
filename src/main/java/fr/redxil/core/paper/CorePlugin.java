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
import fr.redline.pms.utils.IpInfo;
import fr.redline.pms.utils.SystemColor;
import fr.redxil.api.common.API;
import fr.redxil.api.common.player.APIPlayer;
import fr.redxil.api.common.server.Server;
import fr.redxil.api.paper.Paper;
import fr.redxil.api.paper.holograms.HologramsManager;
import fr.redxil.api.paper.scoreboard.BoardManager;
import fr.redxil.api.paper.tags.TagsManager;
import fr.redxil.core.common.CoreAPI;
import fr.redxil.core.paper.cmd.FlyCmd;
import fr.redxil.core.paper.cmd.FreezeCmd;
import fr.redxil.core.paper.cmd.ModCmd;
import fr.redxil.core.paper.cmd.VanishCmd;
import fr.redxil.core.paper.event.ConnectionListener;
import fr.redxil.core.paper.event.PlayerInteractEvent;
import fr.redxil.core.paper.freeze.FreezeMessageGestion;
import fr.redxil.core.paper.moderatormode.ModeratorMain;
import fr.redxil.core.paper.receiver.Receiver;
import fr.redxil.core.paper.vanish.VanishGestion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.PluginManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;

public class CorePlugin extends Paper {

    private static CorePlugin instance;

    private VanishGestion vanish;
    private ModeratorMain moderatorMain;
    private FreezeMessageGestion freezeGestion;

    public static CorePlugin getInstance() {
        return instance;
    }

    @Override
    public TagsManager getTagsManager() {
        return null;
    }

    @Override
    public BoardManager getBoardManager() {
        return null;
    }

    @Override
    public HologramsManager getHologramManager() {
        return null;
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
        checkCrash();

        this.vanish = new VanishGestion(this);
        this.freezeGestion = new FreezeMessageGestion(this);

        EventInventory.enableEvent(this);

        /*

        this.coreTagsManager = new CoreTagsManager();
        this.coreTagsManager.setTagProvider(new TagProvider() {
            @Override
            public void update(TagPlayer tagPlayer, TagPlayer tagPlayer1) {

            }

            @Override
            public TagType type() {
                return TagType.INDIVIDUAL;
            }
        });

        this.coreBoardManager = new CoreBoardManager();

        */

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

        printLog(Level.INFO, SystemColor.GREEN + "Command registered" + SystemColor.RESET);

        this.setEnabled(true);
    }

    @Override
    public void onAPIDisabled() {

        if (API.getInstance().isGameServer())
            API.getInstance().getGame().clearData();

        HandlerList.unregisterAll(this);

        Objects.requireNonNull(this.getCommand("mod")).setExecutor(new ModCmd());
        Objects.requireNonNull(this.getCommand("freeze")).setExecutor(new FreezeCmd());
        Objects.requireNonNull(this.getCommand("vanish")).setExecutor(new VanishCmd());
        Objects.requireNonNull(this.getCommand("fly")).setExecutor(new FlyCmd());

    }

    public void checkCrash() {

        printLog(Level.FINE, SystemColor.YELLOW + "Checking for Crashed APIPlayer" + SystemColor.RESET);
        for (Player player : Bukkit.getOnlinePlayers())
            player.kickPlayer("Error");

        Server server = API.getInstance().getServer();
        String serverName = server.getServerName();

        Collection<UUID> playerUUIDList = new ArrayList<>(server.getPlayerUUIDList());
        if (playerUUIDList.isEmpty()) return;

        printLog(Level.SEVERE, SystemColor.RED + "Founded " + playerUUIDList.size() + " crashed player data" + SystemColor.RESET);
        for (UUID playerUUID : playerUUIDList) {
            server.removePlayerInServer(playerUUID);
            APIPlayer apiPlayer = API.getInstance().getPlayerManager().getPlayer(playerUUID);
            if (apiPlayer != null)
                if (apiPlayer.getServer().getServerName().equals(serverName)) {
                    printLog(Level.SEVERE, SystemColor.GREEN + "Saving player: " + apiPlayer.getName() + SystemColor.RESET);
                    apiPlayer.unloadPlayer();
                }
        }

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

}