package com.example.spawnplugin;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class RandomSpawn extends JavaPlugin {

    private SpawnLocationManager locationManager;

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

        getLogger().info("RandomSpawn enabled with " + locationManager.getLocationCount() + " spawn point(s).");
    }

    public SpawnLocationManager getLocationManager() {
        return locationManager;
    }
}
