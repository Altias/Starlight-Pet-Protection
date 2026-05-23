package net.altias.starlightPetProtection.listeners;

import net.altias.starlightPetProtection.data.PetData;
import net.altias.starlightPetProtection.managers.PetReviveManager;
import net.altias.starlightPetProtection.managers.PetSpawnManager;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.Plugin;

public class TameDeathListener implements Listener {

    private final Plugin plugin;
    private final PetSpawnManager petSpawnManager;
    private final PetReviveManager petReviveManager;

    public TameDeathListener(Plugin plugin, PetSpawnManager petSpawnManager, PetReviveManager petReviveManager) {
        this.plugin = plugin;
        this.petSpawnManager = petSpawnManager;
        this.petReviveManager = petReviveManager;
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {

        if (!(event.getEntity() instanceof LivingEntity entity)) return;
        if (!(entity instanceof Tameable tameable)) return;
        if (!tameable.isTamed()) return;

        AnimalTamer owner = tameable.getOwner();
        if (!(owner instanceof Player player)) return;

        PetData data = new PetData();

        data.type = entity.getType();
        data.name = entity.getCustomName();

        data.reviveAt = System.currentTimeMillis() + 600L * 50L;

        if (entity instanceof Wolf wolf) {
            data.collarColor = wolf.getCollarColor();
            data.wolfVariant = wolf.getVariant();
        }

        if (entity instanceof Cat cat) {
            data.collarColor = cat.getCollarColor();
            data.catType = cat.getCatType();
        }

        if (entity instanceof Parrot parrot) {
            data.parrotVariant = parrot.getVariant();
        }

        if (entity instanceof Horse horse) {
            data.horseColor = horse.getColor();
            data.horseStyle = horse.getStyle();

            AttributeInstance speed = horse.getAttribute(org.bukkit.attribute.Attribute.MOVEMENT_SPEED);
            if (speed != null) data.speed = speed.getBaseValue();

            AttributeInstance jump = horse.getAttribute(org.bukkit.attribute.Attribute.JUMP_STRENGTH);
            if (jump != null) data.jumpStrength = jump.getBaseValue();

            AttributeInstance health = horse.getAttribute(org.bukkit.attribute.Attribute.MAX_HEALTH);
            if (health != null) data.maxHealth = health.getBaseValue();
        }

        player.sendMessage("§6Your pet has been knocked out! §eIt will respawn in one in-game day.");

        petReviveManager.scheduleRevive(
                player.getUniqueId(),
                data,
                24000L
        );
    }
}