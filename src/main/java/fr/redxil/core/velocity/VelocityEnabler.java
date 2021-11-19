package fr.redxil.core.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;

import java.io.File;
import java.nio.file.Path;
import java.util.logging.Logger;

@Plugin(id = "core", name = "Core", version = "1.12", description = "", authors = {"RedLine", "XiliTra"})
public class VelocityEnabler {

    private static VelocityEnabler instance;

    @Inject
    public VelocityEnabler(ProxyServer server, CommandManager commandManager, Logger logger, @DataDirectory Path folder) {
        instance = this;
        this.proxyServer = server;
        this.logger = logger;
        this.pathFile = folder.toFile();
        this.cm = commandManager;
        if (!pathFile.exists()) {
            if (!pathFile.mkdirs()) {
                server.shutdown();
                return;
            }
        }
        logger.info("Hello there, it's a test plugin I made!");
    }

    private final ProxyServer proxyServer;
    private final Logger logger;
    private final File pathFile;
    private final CommandManager cm;

    public static VelocityEnabler getInstance() {
        return instance;
    }

    @Subscribe
    public void onEnable(ProxyInitializeEvent pie) {
        new CoreVelocity(proxyServer, cm, logger, pathFile);
    }

}
