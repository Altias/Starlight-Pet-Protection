package net.altias.starlightPetProtection.managers;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.altias.starlightPetProtection.data.PetData;
import org.bukkit.*;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.*;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.attribute.Attribute;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class PetReviveManager {

    private final Plugin plugin;
    private final PetSpawnManager spawnManager;

    private final File file;
    private final YamlConfiguration config;

    public PetReviveManager(Plugin plugin, PetSpawnManager spawnManager) {
        this.plugin = plugin;
        this.spawnManager = spawnManager;

        this.file = new File(plugin.getDataFolder(), "petrevives.yml");
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            try { file.createNewFile(); } catch (IOException ignored) {}
        }

        this.config = YamlConfiguration.loadConfiguration(file);

        startTickChecker();
    }

    public void scheduleRevive(UUID owner, PetData data, long delayTicks) {

        data.reviveAt = System.currentTimeMillis() + (delayTicks * 50L);

        String base = owner.toString() + ".pets";

        List<Map<String, Object>> pets = (List<Map<String, Object>>) config.getList(base);
        if (pets == null) pets = new ArrayList<>();

        Map<String, Object> map = serialize(data);
        pets.add(map);

        config.set(base, pets);
        save();
    }

    private void startTickChecker() {

        new BukkitRunnable() {
            @Override
            public void run() {

                for (String key : config.getKeys(false)) {

                    List<Map<?, ?>> pets = config.getMapList(key + ".pets");
                    if (pets == null || pets.isEmpty()) continue;

                    List<Map<?, ?>> remaining = new ArrayList<>();

                    UUID owner = UUID.fromString(key);
                    Player player = Bukkit.getPlayer(owner);

                    for (Map<?, ?> map : pets) {

                        PetData data = deserialize(map);

                        if (System.currentTimeMillis() < data.reviveAt) {
                            remaining.add(map);
                            continue;
                        }

                        if (player == null) {
                            remaining.add(map);
                            continue;
                        }

                        Location spawn;
                        if (spawnManager.hasSpawn(owner)) {
                            spawn = spawnManager.getSpawn(owner);
                        } else if (player.getRespawnLocation() != null) {
                            spawn = player.getRespawnLocation();
                        } else {
                            spawn = player.getWorld().getSpawnLocation();
                        }

                        LivingEntity entity = (LivingEntity)
                                player.getWorld().spawnEntity(spawn, data.type);

                        applyData(entity, data, player);

                        if (entity instanceof Wolf wolf) {
                            wolf.setSitting(true);
                        }

                        if (entity instanceof Cat cat) {
                            cat.setSitting(true);
                        }

                        if (entity instanceof Parrot parrot) {
                            parrot.setSitting(true);
                        }

                        player.sendMessage("§aOne of your pets has returned!");
                    }

                    config.set(key + ".pets", remaining);
                }

                save();
            }
        }.runTaskTimer(plugin, 20L * 10, 20L * 10);
    }

    private Map<String, Object> serialize(PetData data) {

        Map<String, Object> map = new HashMap<>();

        map.put("type", data.type.name());
        map.put("name", data.name);
        map.put("reviveAt", data.reviveAt);

        if (data.collarColor != null)
            map.put("collarColor", data.collarColor.name());

        if (data.wolfVariant != null)
            map.put("wolfVariant", data.wolfVariant.getKey().toString());

        if (data.catType != null)
            map.put("catType", data.catType.name());

        if (data.parrotVariant != null)
            map.put("parrotVariant", data.parrotVariant.name());

        if (data.horseColor != null)
            map.put("horseColor", data.horseColor.name());

        if (data.horseStyle != null)
            map.put("horseStyle", data.horseStyle.name());

        map.put("speed", data.speed);
        map.put("jumpStrength", data.jumpStrength);
        map.put("maxHealth", data.maxHealth);

        return map;
    }

    private PetData deserialize(Map<?, ?> map) {

        PetData data = new PetData();

        data.type = EntityType.valueOf((String) map.get("type"));
        data.name = (String) map.get("name");
        data.reviveAt = ((Number) map.get("reviveAt")).longValue();

        if (map.containsKey("collarColor"))
            data.collarColor = DyeColor.valueOf((String) map.get("collarColor"));

        Object raw = map.get("wolfVariant");

        //This one kept breaking so now it's the weird looking one
        if (raw instanceof String s && !s.isBlank()) {

            data.wolfVariant = RegistryAccess.registryAccess()
                    .getRegistry(RegistryKey.WOLF_VARIANT)
                    .get(NamespacedKey.fromString(s));
        }

        if (map.containsKey("catType"))
            data.catType = Cat.Type.valueOf((String) map.get("catType"));

        if (map.containsKey("parrotVariant"))
            data.parrotVariant = Parrot.Variant.valueOf((String) map.get("parrotVariant"));

        if (map.containsKey("horseColor"))
            data.horseColor = Horse.Color.valueOf((String) map.get("horseColor"));

        if (map.containsKey("horseStyle"))
            data.horseStyle = Horse.Style.valueOf((String) map.get("horseStyle"));

        data.speed = map.get("speed") == null ? 0 : ((Number) map.get("speed")).doubleValue();
        data.jumpStrength = map.get("jumpStrength") == null ? 0 : ((Number) map.get("jumpStrength")).doubleValue();
        data.maxHealth = map.get("maxHealth") == null ? 0 : ((Number) map.get("maxHealth")).doubleValue();

        return data;
    }

    private void applyData(LivingEntity entity, PetData data, Player owner) {

        if (entity instanceof Tameable tameable) {
            tameable.setTamed(true);
            tameable.setOwner(owner);
        }

        if (data.name != null && !data.name.isBlank()) {
            entity.setCustomName(data.name);
            entity.setCustomNameVisible(true);
        } else {
            entity.setCustomName(null);
            entity.setCustomNameVisible(false);
        }

        if (entity instanceof Wolf wolf) {

            wolf.setTamed(true);
            wolf.setOwner(owner);

            if (data.wolfVariant != null)
                wolf.setVariant(data.wolfVariant);

            if (data.collarColor != null)
                wolf.setCollarColor(data.collarColor);
        }

        if (entity instanceof Cat cat)
        {
            if (data.catType != null)
                cat.setCatType(data.catType);

            if (data.collarColor != null)
                cat.setCollarColor(data.collarColor);
        }

        if (data.parrotVariant != null && entity instanceof Parrot parrot)
            parrot.setVariant(data.parrotVariant);

        if (entity instanceof Horse horse) {

            if (data.horseColor != null)
                horse.setColor(data.horseColor);

            if (data.horseStyle != null)
                horse.setStyle(data.horseStyle);

            if (data.speed > 0)
                horse.getAttribute(Attribute.MOVEMENT_SPEED)
                        .setBaseValue(data.speed);

            if (data.jumpStrength > 0)
                horse.getAttribute(Attribute.JUMP_STRENGTH)
                        .setBaseValue(data.jumpStrength);

            if (data.maxHealth > 0) {
                horse.getAttribute(Attribute.MAX_HEALTH)
                        .setBaseValue(data.maxHealth);
                horse.setHealth(data.maxHealth);
            }
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