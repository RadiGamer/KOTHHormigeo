package org.hexa.reydelacolinahexa.Listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.Transformation;
import org.hexa.reydelacolinahexa.ReydelaColinaHEXA;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class KothListener implements Listener {
    private final ReydelaColinaHEXA plugin;
    private final Map<UUID, Integer> playerPoints = new HashMap<>();
    private boolean isRound2Active = false;
    private int currentZone = 1;
    private int activeRound = 0;
    private Player zonePlayer;
    private int zoneRadius;
    private ItemDisplay currentDisplay;
    private Interaction currentInteraction;

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

        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this::checkPlayersInZone, 0L, 20L); //ESTO CHECA A LOS JUGADORES
    }

    public void startRound3(Player player, int radius) {
        if (activeRound != 0) {
            plugin.getLogger().warning("¡Ya hay una ronda activa!");
            return;
        }

        activeRound = 3;
        this.zonePlayer = player;
        this.zoneRadius = radius;

        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this::checkPlayersInZone, 0L, 20L);
    }

    public void stopRound() {
        activeRound = 0;
        isRound2Active = false;
        currentZone = 1;
        zonePlayer = null;
        Bukkit.getScheduler().cancelTasks(plugin);
        despawnCurrentDisplay();
        despawnCurrentInteraction();
    }

    private void checkPlayersInZone() {
        if (activeRound == 0) return;

        if (activeRound == 1) {
            checkPlayersInRound1();
        } else if (activeRound == 2) {
            checkPlayersInRound2();
        } else if (activeRound == 3) {
            checkPlayersInRound3();
        }
    }

    private void checkPlayersInRound1() {
        ConfigurationSection round1 = plugin.getConfig().getConfigurationSection("Round1.1");

        if (round1 != null) {
            int centerX = round1.getInt("Center.X");
            int centerY = round1.getInt("Center.Y");
            int centerZ = round1.getInt("Center.Z");

            int x1 = round1.getInt("Corner1.X");
            int y1 = round1.getInt("Corner1.Y");
            int z1 = round1.getInt("Corner1.Z");

            int sizeX = Math.abs(centerX - x1) * 2 + 1;
            int sizeY = Math.abs(centerY - y1) * 2 + 1;
            int sizeZ = Math.abs(centerZ - z1) * 2 + 1;

            updateZoneDisplay(centerX, centerY, centerZ, sizeX, sizeY, sizeZ, true);

            for (Player player : Bukkit.getOnlinePlayers()) {
                Location loc = player.getLocation();
                if (isInsideZone(loc, centerX, centerZ, sizeX, sizeZ) || isTouchingInteraction(player)) {
                    playerPoints.put(player.getUniqueId(), playerPoints.getOrDefault(player.getUniqueId(), 0) + 1);
                    plugin.GivePoints(player);
                }
            }
        }
    }

    private void startRound2() {
        isRound2Active = true;
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            currentZone = (currentZone == 1) ? 2 : 1;
            plugin.getLogger().info("Ronda 2: Cambiando a la zona " + currentZone);
        }, 0L, 400L);
    }

    private void checkPlayersInRound2() {
        if (!isRound2Active) return;

        ConfigurationSection round2 = plugin.getConfig().getConfigurationSection("Round2." + currentZone);
        if (round2 != null) {
            int centerX = round2.getInt("Center.X");
            int centerY = round2.getInt("Center.Y");
            int centerZ = round2.getInt("Center.Z");

            int x1 = round2.getInt("Corner1.X");
            int y1 = round2.getInt("Corner1.Y");
            int z1 = round2.getInt("Corner1.Z");

            int sizeX = Math.abs(centerX - x1) * 2 + 1;
            int sizeY = Math.abs(centerY - y1) * 2 + 1;
            int sizeZ = Math.abs(centerZ - z1) * 2 + 1;

            updateZoneDisplay(centerX, centerY, centerZ, sizeX, sizeY, sizeZ, false);

            for (Player player : Bukkit.getOnlinePlayers()) {
                Location loc = player.getLocation();
                if (isInsideZone(loc, centerX, centerZ, sizeX, sizeZ) || isTouchingInteraction(player)) {
                    playerPoints.put(player.getUniqueId(), playerPoints.getOrDefault(player.getUniqueId(), 0) + 1);
                    plugin.GivePoints(player);
                }
            }
        }
    }

    private void checkPlayersInRound3() {
        if (zonePlayer == null) return;

        Location zoneCenter = zonePlayer.getLocation();
        int centerX = zoneCenter.getBlockX();
        int centerZ = zoneCenter.getBlockZ();
        int centerY = zoneCenter.getBlockY();

        int sizeX = zoneRadius * 2 + 1;
        int sizeY = 10;
        int sizeZ = zoneRadius * 2 + 1;

        updateZoneDisplay(centerX, centerY, centerZ, sizeX, sizeY, sizeZ, false);

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.equals(zonePlayer)) continue;

            Location loc = player.getLocation();
            if (isInsideZone(loc, centerX, centerZ, sizeX, sizeZ) || isTouchingInteraction(player)) {
                playerPoints.put(player.getUniqueId(), playerPoints.getOrDefault(player.getUniqueId(), 0) + 1);
                plugin.GivePoints(player);
            }
        }
    }

    private void updateZoneDisplay(int centerX, int centerY, int centerZ, int sizeX, int sizeY, int sizeZ, boolean isStatic) {
        World world = Bukkit.getWorlds().get(0);
        Location center = new Location(world, centerX, centerY, centerZ);

        if (!isStatic) {
            despawnCurrentDisplay();
            despawnCurrentInteraction();
        } else if (currentDisplay != null) {
            return;
        }

        // Spawn ItemDisplay
        currentDisplay = (ItemDisplay) world.spawnEntity(center, EntityType.ITEM_DISPLAY);

        ItemStack stick = new ItemStack(Material.STICK);
        ItemMeta meta = stick.getItemMeta();
        if (meta != null) {
            meta.setCustomModelData(12);
            stick.setItemMeta(meta);
        }

        currentDisplay.setItemStack(stick);
        currentDisplay.addScoreboardTag("koth_zone");


        int scaleX = sizeX;
        int scaleY = sizeY;
        int scaleZ = sizeZ;

        Transformation transformation = new Transformation(new Vector3f(0.5f, 0f, 0.5f), new AxisAngle4f(0, 0, 0, 1), new Vector3f(scaleX, scaleY, scaleZ), new AxisAngle4f(0, 0, 0, 1));
        currentDisplay.setTransformation(transformation);
        currentDisplay.setGlowing(true);

//        currentInteraction = (Interaction) world.spawnEntity(center, EntityType.INTERACTION);
//        currentInteraction.addScoreboardTag("koth_zone");
//
//        currentInteraction.setInteractionWidth(sizeX);
//        currentInteraction.setInteractionHeight(sizeY*3);
    }


    private boolean isInsideZone(Location loc, int centerX, int centerZ, int sizeX, int sizeZ) {
        int halfSizeX = sizeX / 2;
        int halfSizeZ = sizeZ / 2;

        return loc.getBlockX() >= centerX - halfSizeX && loc.getBlockX() <= centerX + halfSizeX &&
                loc.getBlockZ() >= centerZ - halfSizeZ && loc.getBlockZ() <= centerZ + halfSizeZ;
    }

    private boolean isTouchingInteraction(Player player) {
        return player.getNearbyEntities(0.5, 0.5, 0.5).stream()
                .anyMatch(entity -> entity instanceof Interaction && entity.getScoreboardTags().contains("koth_zone"));
    }

    private void despawnCurrentDisplay() {
        if (currentDisplay != null) {
            currentDisplay.remove();
            currentDisplay = null;
        }
    }

    private void despawnCurrentInteraction() {
        if (currentInteraction != null) {
            currentInteraction.remove();
            currentInteraction = null;
        }
    }
}
