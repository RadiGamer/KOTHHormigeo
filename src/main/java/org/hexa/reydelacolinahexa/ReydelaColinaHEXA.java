package org.hexa.reydelacolinahexa;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.hexa.reydelacolinahexa.Commands.KOTHCommand;
import org.hexa.reydelacolinahexa.Listeners.KothListener;

public final class ReydelaColinaHEXA extends JavaPlugin {

    private KothListener kothListener;

    private static Economy econ = null;
    private static Permission perms = null;
    private static Chat chat = null;

    @Override
    public void onEnable() {

        setupPermissions();
        setupChat();
        setupEconomy();
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

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    private boolean setupChat() {
        RegisteredServiceProvider<Chat> rsp = getServer().getServicesManager().getRegistration(Chat.class);
        chat = rsp.getProvider();
        return chat != null;
    }

    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
        perms = rsp.getProvider();
        return perms != null;
    }
    public void GivePoints(Player player){
        econ.depositPlayer(player.getName(), 1);
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "playglow " + player.getName() + " GREEN 0.4 0.3 0.3" );
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_XYLOPHONE, SoundCategory.MASTER, 1, 1);
        player.sendActionBar(ChatColor.YELLOW + "Estas dentro de la zona");
    }
}
