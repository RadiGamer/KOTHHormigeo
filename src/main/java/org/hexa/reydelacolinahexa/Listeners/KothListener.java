package org.hexa.reydelacolinahexa.Listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;
import org.hexa.reydelacolinahexa.ReydelaColinaHEXA;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class KothListener implements Listener {
    private final ReydelaColinaHEXA plugin;
    private final Map<UUID, Integer> playerPoints = new HashMap<>();
    private boolean isRound2Active = false;
    private int currentZone = 1;
    private int activeRound = 0;

    public KothListener(ReydelaColinaHEXA plugin) {
        this.plugin = plugin;
    }

    public void startRound(int round) {
        if (activeRound != 0) {
            plugin.getLogger().warning("¡Ya hay una ronda activa!");
            return;
        }

        activeRound = round;
        if (round == 2) {
            startRound2();
        }

        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this::checkPlayersInZone, 0L, 20L); // Every second
    }

    public void stopRound() {
        activeRound = 0;
        isRound2Active = false;
        currentZone = 1;
        Bukkit.getScheduler().cancelTasks(plugin);
    }

    private void checkPlayersInZone() {
        if (activeRound == 0) return;

        if (activeRound == 1) {
            checkPlayersInRound1();
        } else if (activeRound == 2) {
            checkPlayersInRound2();
        }
    }

    private void checkPlayersInRound1() {
        ConfigurationSection round1 = plugin.getConfig().getConfigurationSection("Round1.1");

        if (round1 != null) {
            int x1 = round1.getInt("Corner1.X");
            int z1 = round1.getInt("Corner1.Z");
            int x2 = round1.getInt("Corner2.X");
            int z2 = round1.getInt("Corner2.Z");

            int minX = Math.min(x1, x2);
            int maxX = Math.max(x1, x2);
            int minZ = Math.min(z1, z2);
            int maxZ = Math.max(z1, z2);

            for (Player player : Bukkit.getOnlinePlayers()) {
                Location loc = player.getLocation();
                if (loc.getBlockX() >= minX && loc.getBlockX() <= maxX && loc.getBlockZ() >= minZ && loc.getBlockZ() <= maxZ) {
                    playerPoints.put(player.getUniqueId(), playerPoints.getOrDefault(player.getUniqueId(), 0) + 1);
                    player.sendMessage(ChatColor.AQUA + "Debug: ¡Estás en la zona!"); // Debug message
                }
            }
        }
    }

    private void startRound2() {
        isRound2Active = true;
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            currentZone = (currentZone == 1) ? 2 : 1;
            plugin.getLogger().info("Ronda 2: Cambiando a la zona " + currentZone);
        }, 0L, 400L); // 20 seconds (20 ticks per second * 20)
    }

    private void checkPlayersInRound2() {
        if (!isRound2Active) return;

        ConfigurationSection round2 = plugin.getConfig().getConfigurationSection("Round2." + currentZone);
        if (round2 != null) {
            int x1 = round2.getInt("Corner1.X");
            int z1 = round2.getInt("Corner1.Z");
            int x2 = round2.getInt("Corner2.X");
            int z2 = round2.getInt("Corner2.Z");

            int minX = Math.min(x1, x2);
            int maxX = Math.max(x1, x2);
            int minZ = Math.min(z1, z2);
            int maxZ = Math.max(z1, z2);

            for (Player player : Bukkit.getOnlinePlayers()) {
                Location loc = player.getLocation();
                if (loc.getBlockX() >= minX && loc.getBlockX() <= maxX && loc.getBlockZ() >= minZ && loc.getBlockZ() <= maxZ) {
                    playerPoints.put(player.getUniqueId(), playerPoints.getOrDefault(player.getUniqueId(), 0) + 1);
                    player.sendMessage(ChatColor.AQUA + "Debug: ¡Estás en la zona para la Ronda 2 Zona " + currentZone + "!"); // Debug message
                }
            }
        }
    }
}