package com.example.spawnplugin;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/** Handles "/spawn" and "/spawn reload". */
public class SpawnCommand implements CommandExecutor, TabCompleter {

    private final SpawnLocationManager locationManager;

    public SpawnCommand(SpawnLocationManager locationManager) {
        this.locationManager = locationManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                              @NotNull String label, @NotNull String[] args) {

        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("randomspawn.reload")) {
                sender.sendMessage(ChatColor.RED + "You don't have permission to do that.");
                return true;
            }
            locationManager.reload();
            sender.sendMessage(ChatColor.GREEN + "RandomSpawn reloaded ("
                    + locationManager.getLocationCount() + " location(s)).");
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use /spawn.");
            return true;
        }

        if (!player.hasPermission("randomspawn.use")) {
            player.sendMessage(ChatColor.RED + "You don't have permission to do that.");
            return true;
        }

        Location target = locationManager.getRandomLocation();
        if (target == null) {
            player.sendMessage(ChatColor.RED + "No spawn locations are configured. Ask an admin to add some to config.yml.");
            return true;
        }

        player.teleportAsync(target);

        String message = locationManager.getTeleportMessage();
        if (message != null && !message.isEmpty()) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                       @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1 && sender.hasPermission("randomspawn.reload")) {
            return Collections.singletonList("reload");
        }
        return Collections.emptyList();
    }
}
