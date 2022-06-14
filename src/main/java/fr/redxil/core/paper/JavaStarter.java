package fr.redxil.core.paper;

import org.bukkit.plugin.java.JavaPlugin;

public class JavaStarter extends JavaPlugin {
    @Override
    public void onEnable() {
        new CorePlugin(this);
    }
}
