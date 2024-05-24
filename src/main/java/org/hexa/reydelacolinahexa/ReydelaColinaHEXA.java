package org.hexa.reydelacolinahexa;

import org.bukkit.plugin.java.JavaPlugin;
import org.hexa.reydelacolinahexa.Commands.KOTHCommand;
import org.hexa.reydelacolinahexa.Listeners.KothListener;

public final class ReydelaColinaHEXA extends JavaPlugin {

    private KothListener kothListener;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        this.kothListener = new KothListener(this);
        this.getCommand("koth").setExecutor(new KOTHCommand(this));
        getServer().getPluginManager().registerEvents(kothListener, this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public KothListener getKothListener() {
        return kothListener;
    }
}
