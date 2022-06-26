package fr.redxil.core.paper;

import fr.redxil.api.common.game.Game;
import fr.redxil.core.common.CoreAPI;
import org.bukkit.plugin.java.JavaPlugin;

public class JavaStarter extends JavaPlugin {
    @Override
    public void onEnable() {
        new CorePlugin(this);
    }

    @Override
    public void onDisable() {
        if (CoreAPI.isAPIEnabled()) {
            CoreAPI.getInstance().getGameManager().getGameByServerID(CoreAPI.getInstance().getServerID()).ifPresent(Game::clearData);
            CoreAPI.getInstance().shutdown();
        }
    }

}
