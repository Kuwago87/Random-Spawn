package com.example.spawnplugin;

import org.bstats.bukkit.Metrics;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class RandomSpawn extends JavaPlugin {

    // bStats plugin ID for RandomSpawn - see https://bstats.org/plugin/bukkit/RandomSpawn/34217
    private static final int BSTATS_PLUGIN_ID = 34217;

    private SpawnLocationManager locationManager;
    private Metrics metrics;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        this.locationManager = new SpawnLocationManager(this);
        this.locationManager.reload();

        SpawnCommand spawnCommand = new SpawnCommand(locationManager);
        PluginCommand command = getCommand("spawn");
        if (command != null) {
            command.setExecutor(spawnCommand);
            command.setTabCompleter(spawnCommand);
        } else {
            getLogger().warning("Could not register /spawn - is it declared in plugin.yml?");
        }

        getServer().getPluginManager().registerEvents(new SpawnListener(this, locationManager), this);

        this.metrics = new Metrics(this, BSTATS_PLUGIN_ID);

        getLogger().info("RandomSpawn enabled with " + locationManager.getLocationCount() + " spawn point(s).");
    }

    public SpawnLocationManager getLocationManager() {
        return locationManager;
    }
}
