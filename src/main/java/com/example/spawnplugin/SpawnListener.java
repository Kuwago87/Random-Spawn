package com.example.spawnplugin;

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Handles automatic random teleports on join and on respawn. */
public class SpawnListener implements Listener {

    private final RandomSpawn plugin;
    private final SpawnLocationManager locationManager;

    // Tracks the location we asked each player to respawn at, so
    // PlayerPostRespawnEvent can double-check it actually stuck (see onPostRespawn).
    private final Map<UUID, Location> pendingRespawns = new ConcurrentHashMap<>();

    public SpawnListener(RandomSpawn plugin, SpawnLocationManager locationManager) {
        this.plugin = plugin;
        this.locationManager = locationManager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        if (!locationManager.isTeleportOnJoin()) {
            debug("teleport-on-join is disabled in config.yml, skipping.");
            return;
        }

        Player player = event.getPlayer();
        boolean isFirstJoin = !player.hasPlayedBefore();

        if (locationManager.isFirstJoinOnly() && !isFirstJoin) {
            debug(player.getName() + " is a returning player and first-join-only is true, skipping.");
            return;
        }

        Location target = locationManager.getRandomLocation();
        if (target == null) {
            debug("No spawn locations configured - cannot teleport " + player.getName() + " on join.");
            return;
        }

        debug("Teleporting " + player.getName() + " to " + describe(target) + " on join.");

        // Defer a tick so this runs after the vanilla join teleport/placement.
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            player.teleportAsync(target);
            sendMessage(player);
        });
    }

    // HIGHEST = last chance for a plugin to change the destination before the
    // server commits to it. If another plugin (e.g. an essentials-style spawn
    // plugin) also touches PlayerRespawnEvent at a lower priority, this still wins.
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onRespawn(PlayerRespawnEvent event) {
        if (!locationManager.isTeleportOnRespawn()) {
            debug("teleport-on-respawn is disabled in config.yml, skipping.");
            return;
        }

        Location target = locationManager.getRandomLocation();
        if (target == null) {
            debug("No spawn locations configured - cannot set a random respawn point.");
            return;
        }

        debug("Setting respawn location for " + event.getPlayer().getName() + " to " + describe(target)
                + " (reason=" + event.getRespawnReason() + ", bed=" + event.isBedSpawn()
                + ", anchor=" + event.isAnchorSpawn() + ")");

        event.setRespawnLocation(target);
        pendingRespawns.put(event.getPlayer().getUniqueId(), target);
    }

    // Safety net: some servers have other plugins/behaviors that re-decide the
    // respawn point after PlayerRespawnEvent has already been handled. If the
    // player didn't actually land where we asked, force it here.
    @EventHandler
    public void onPostRespawn(PlayerPostRespawnEvent event) {
        Player player = event.getPlayer();
        Location expected = pendingRespawns.remove(player.getUniqueId());
        if (expected == null) {
            return;
        }

        Location actual = event.getRespawnedLocation();
        if (!isSameSpot(expected, actual)) {
            debug(player.getName() + " ended up at " + describe(actual) + " instead of " + describe(expected)
                    + " - forcing a teleport. If this keeps happening, another plugin is likely also "
                    + "handling respawns; check load order / disable its spawn handling.");
            player.teleportAsync(expected);
        }

        sendMessage(player);
    }

    private boolean isSameSpot(Location a, Location b) {
        if (a == null || b == null) {
            return false;
        }
        if (a.getWorld() == null || !a.getWorld().equals(b.getWorld())) {
            return false;
        }
        return a.distanceSquared(b) < 1.0; // within ~1 block, accounts for centering/rounding
    }

    private String describe(Location loc) {
        if (loc == null || loc.getWorld() == null) {
            return "null";
        }
        return loc.getWorld().getName() + " " + loc.getX() + "," + loc.getY() + "," + loc.getZ();
    }

    private void debug(String message) {
        if (locationManager.isDebug()) {
            plugin.getLogger().info("[debug] " + message);
        }
    }

    private void sendMessage(Player player) {
        String message = locationManager.getTeleportMessage();
        if (message != null && !message.isEmpty()) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
        }
    }
}
