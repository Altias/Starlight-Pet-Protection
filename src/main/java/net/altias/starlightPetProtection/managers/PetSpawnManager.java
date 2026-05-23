package net.altias.starlightPetProtection.managers;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PetSpawnManager {

    private final Plugin plugin;
    private final File file;
    private YamlConfiguration config;

    private final Map<UUID, Location> cache = new HashMap<>();

    public PetSpawnManager(Plugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "petspawns.yml");

        if (!file.exists()) {
            file.getParentFile().mkdirs();
            try {
                file.createNewFile();
            } catch (IOException ignored) {}
        }

        this.config = YamlConfiguration.loadConfiguration(file);
        load();
    }

    public void setSpawn(UUID uuid, Location loc) {

        cache.put(uuid, loc);

        String base = uuid.toString();

        config.set(base + ".world", loc.getWorld().getName());
        config.set(base + ".x", loc.getX());
        config.set(base + ".y", loc.getY());
        config.set(base + ".z", loc.getZ());
        config.set(base + ".yaw", loc.getYaw());
        config.set(base + ".pitch", loc.getPitch());

        save();
    }

    public Location getSpawn(UUID uuid) {
        return cache.getOrDefault(uuid, null);
    }

    public boolean hasSpawn(UUID uuid) {
        return cache.containsKey(uuid) && cache.get(uuid) != null;
    }

    private void load() {
        for (String key : config.getKeys(false)) {

            String worldName = config.getString(key + ".world");
            if (worldName == null) continue;

            World world = Bukkit.getWorld(worldName);
            if (world == null) continue;

            if (!config.contains(key + ".x")) continue;

            Location loc = new Location(
                    world,
                    config.getDouble(key + ".x"),
                    config.getDouble(key + ".y"),
                    config.getDouble(key + ".z"),
                    (float) config.getDouble(key + ".yaw"),
                    (float) config.getDouble(key + ".pitch")
            );

            cache.put(UUID.fromString(key), loc);
        }
    }

    private void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
