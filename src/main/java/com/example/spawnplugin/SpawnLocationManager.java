package com.example.spawnplugin;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;

/**
 * Loads the predefined spawn point list from config.yml and hands out a
 * random one on request.
 */
public class SpawnLocationManager {

    private final RandomSpawn plugin;
    private final Random random = new Random();
    private final List<Location> locations = new ArrayList<>();

    private boolean teleportOnJoin;
    private boolean firstJoinOnly;
    private boolean teleportOnRespawn;
    private String teleportMessage = "";
    private boolean debug;

    public SpawnLocationManager(RandomSpawn plugin) {
        this.plugin = plugin;
    }

    /** Re-reads config.yml from disk and rebuilds the location list. */
    public void reload() {
        plugin.reloadConfig();
        locations.clear();

        List<?> rawList = plugin.getConfig().getList("locations");
        if (rawList != null) {
            for (Object entry : rawList) {
                if (entry instanceof Map<?, ?> map) {
                    Location loc = parseLocation(map);
                    if (loc != null) {
                        locations.add(loc);
                    }
                } else {
                    plugin.getLogger().warning("Skipping malformed entry under 'locations' in config.yml.");
                }
            }
        }

        ConfigurationSection settings = plugin.getConfig().getConfigurationSection("settings");
        teleportOnJoin = settings == null || settings.getBoolean("teleport-on-join", true);
        firstJoinOnly = settings != null && settings.getBoolean("first-join-only", false);
        teleportOnRespawn = settings == null || settings.getBoolean("teleport-on-respawn", true);
        teleportMessage = settings != null ? settings.getString("teleport-message", "") : "";
        debug = settings != null && settings.getBoolean("debug", false);

        if (locations.isEmpty()) {
            plugin.getLogger().warning("No valid spawn locations configured under 'locations' in config.yml - "
                    + "/spawn and any automatic teleports will do nothing until you add some.");
        }

        plugin.getLogger().info("Loaded " + locations.size() + " spawn point(s). teleport-on-join=" + teleportOnJoin
                + ", first-join-only=" + firstJoinOnly + ", teleport-on-respawn=" + teleportOnRespawn
                + ", debug=" + debug);
    }

    private Location parseLocation(Map<?, ?> map) {
        Object worldNameObj = map.get("world");
        if (worldNameObj == null) {
            plugin.getLogger().warning("A location entry in config.yml is missing 'world' - skipping.");
            return null;
        }

        String worldName = worldNameObj.toString();
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            plugin.getLogger().log(Level.WARNING,
                    "Unknown world '" + worldName + "' referenced in config.yml locations list - skipping.");
            return null;
        }

        double x = toDouble(map.get("x"));
        double y = toDouble(map.get("y"));
        double z = toDouble(map.get("z"));
        float yaw = (float) toDouble(map.containsKey("yaw") ? map.get("yaw") : 0.0);
        float pitch = (float) toDouble(map.containsKey("pitch") ? map.get("pitch") : 0.0);

        return new Location(world, x, y, z, yaw, pitch);
    }

    private double toDouble(Object o) {
        if (o instanceof Number number) {
            return number.doubleValue();
        }
        try {
            return Double.parseDouble(String.valueOf(o));
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    /** Returns a random spawn point, or null if none are configured. */
    public Location getRandomLocation() {
        if (locations.isEmpty()) {
            return null;
        }
        return locations.get(random.nextInt(locations.size())).clone();
    }

    public int getLocationCount() {
        return locations.size();
    }

    public boolean isTeleportOnJoin() {
        return teleportOnJoin;
    }

    public boolean isFirstJoinOnly() {
        return firstJoinOnly;
    }

    public boolean isTeleportOnRespawn() {
        return teleportOnRespawn;
    }

    public String getTeleportMessage() {
        return teleportMessage;
    }

    public boolean isDebug() {
        return debug;
    }
}
