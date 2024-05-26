package org.hexa.reydelacolinahexa.Commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.hexa.reydelacolinahexa.Listeners.KothListener;
import org.hexa.reydelacolinahexa.ReydelaColinaHEXA;

public class KOTHCommand implements CommandExecutor {
    private final ReydelaColinaHEXA plugin;

    public KOTHCommand(ReydelaColinaHEXA plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Este comando solo puede ser usado por jugadores.");
            return true;
        }

        Player player = (Player) sender;
        if (args.length < 1) {
            player.sendMessage(ChatColor.YELLOW + "Uso: /koth <setcorner|setcenter|start|stop>");
            return true;
        }

        String subCommand = args[0];
        switch (subCommand.toLowerCase()) {
            case "setcorner":
                if (args.length < 4 && args[1].equals("2")) {
                    player.sendMessage(ChatColor.YELLOW + "Uso: /koth setcorner 2 <corner1|corner2> <identifier>");
                    return true;
                } else if (args.length < 3) {
                    player.sendMessage(ChatColor.YELLOW + "Uso: /koth setcorner <round> <corner1|corner2>");
                    return true;
                }

                String round = args[1];
                String corner = args[2];
                Location loc = player.getLocation();
                String path;

                if (round.equals("2")) {
                    String identifier = args[3];
                    path = "Round" + round + "." + identifier + "." + (corner.equalsIgnoreCase("corner1") ? "Corner1" : "Corner2");
                } else {
                    path = "Round" + round + ".1." + (corner.equalsIgnoreCase("corner1") ? "Corner1" : "Corner2");
                }

                plugin.getConfig().set(path + ".X", loc.getBlockX());
                plugin.getConfig().set(path + ".Y", loc.getBlockY());
                plugin.getConfig().set(path + ".Z", loc.getBlockZ());
                plugin.saveConfig();

                player.sendMessage(ChatColor.GREEN + "Esquina " + corner + " establecida para la Ronda " + round + " en " + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ());
                break;

            case "setcenter":
                if (args.length < 3 && args[1].equals("2")) {
                    player.sendMessage(ChatColor.YELLOW + "Uso: /koth setcenter 2 <identifier>");
                    return true;
                } else if (args.length < 2) {
                    player.sendMessage(ChatColor.YELLOW + "Uso: /koth setcenter <round>");
                    return true;
                }

                round = args[1];
                loc = player.getLocation();
                if (round.equals("2")) {
                    String identifier = args[2];
                    path = "Round" + round + "." + identifier + ".Center";
                } else {
                    path = "Round" + round + ".1.Center";
                }

                plugin.getConfig().set(path + ".X", loc.getBlockX());
                plugin.getConfig().set(path + ".Y", loc.getBlockY());
                plugin.getConfig().set(path + ".Z", loc.getBlockZ());
                plugin.saveConfig();

                player.sendMessage(ChatColor.GREEN + "Centro establecido para la Ronda " + round + " en " + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ());
                break;

            case "start":
                if (args.length < 2) {
                    player.sendMessage(ChatColor.YELLOW + "Uso: /koth start <round>");
                    return true;
                }

                int roundToStart;
                try {
                    roundToStart = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    player.sendMessage(ChatColor.RED + "Número de ronda inválido.");
                    return true;
                }

                KothListener kothListener = plugin.getKothListener();

                if (roundToStart == 3) {
                    if (args.length < 3) {
                        player.sendMessage(ChatColor.YELLOW + "Uso: /koth start 3 <Radius>");
                        return true;
                    }
                    int radius;
                    try {
                        radius = Integer.parseInt(args[2]);
                    } catch (NumberFormatException e) {
                        player.sendMessage(ChatColor.RED + "Radio inválido.");
                        return true;
                    }
                    kothListener.startRound3(player, radius);
                    player.sendMessage(ChatColor.GREEN + "¡Ronda 3 de KOTH iniciada con radio " + radius + "!");
                } else {
                    kothListener.startRound(roundToStart);
                    player.sendMessage(ChatColor.GREEN + "¡Ronda " + roundToStart + " de KOTH iniciada!");
                }
                break;

            case "stop":
                KothListener listener = plugin.getKothListener();
                listener.stopRound();
                player.sendMessage(ChatColor.GREEN + "¡Ronda de KOTH detenida!");
                break;

            default:
                player.sendMessage(ChatColor.YELLOW + "Subcomando desconocido. Uso: /koth <setcorner|setcenter|start|stop>");
                break;
        }

        return true;
    }
}