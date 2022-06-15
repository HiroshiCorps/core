package fr.redxil.core.paper;

import fr.redxil.api.common.API;
import fr.redxil.api.common.game.Game;
import org.bukkit.plugin.java.JavaPlugin;

public class JavaStarter extends JavaPlugin {
    @Override
    public void onEnable() {
        new CorePlugin(this);
    }

    @Override
    public void onDisable() {
        if(API.isAPIEnabled())
            API.getInstance().getGame().ifPresent(Game::clearData);
    }

}
