package fr.redxil.core.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
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

import java.io.File;
import java.nio.file.Path;
import java.util.logging.Logger;

@Plugin(id = "core", name = "Core", version = "1.12", description = "", authors = {"RedLine", "XiliTra"})
public class VelocityEnabler {

    private final ProxyServer proxyServer;
    private final Logger logger;
    private final File pathFile;
    private final CommandManager cm;

    @Inject
    public VelocityEnabler(ProxyServer server, CommandManager commandManager, Logger logger, @DataDirectory Path folder) {
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
        registerCommands();
        registerEvents();
        logger.info("Hello there, it's a test plugin I made!");
    }

    @Subscribe
    public void onEnable(ProxyInitializeEvent pie) {
        new CoreVelocity(proxyServer, cm, logger, pathFile);
    }

    public void registerEvents() {
        proxyServer.getEventManager().register(this, new JoinListener());
        proxyServer.getEventManager().register(this, new PlayerListener());
        new PlayerSwitchListener();
        new UpdaterReceiver();
    }

    public void registerCommands() {

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
