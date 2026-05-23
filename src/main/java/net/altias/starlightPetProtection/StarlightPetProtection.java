package net.altias.starlightPetProtection;

import net.altias.starlightPetProtection.commands.SetPetSpawnCommand;
import net.altias.starlightPetProtection.managers.PetReviveManager;
import net.altias.starlightPetProtection.managers.PetSpawnManager;
import net.altias.starlightPetProtection.listeners.TameDeathListener;
import org.bukkit.plugin.java.JavaPlugin;

public final class StarlightPetProtection extends JavaPlugin {

    private PetSpawnManager petSpawnManager;
    private PetReviveManager petReviveManager;

    @Override
    public void onEnable() {

        this.petSpawnManager = new PetSpawnManager(this);
        this.petReviveManager = new PetReviveManager(this, petSpawnManager);

        getServer().getPluginManager().registerEvents(
                new TameDeathListener(this, petSpawnManager, petReviveManager),
                this
        );

        getCommand("setpetspawn").setExecutor(
                new SetPetSpawnCommand(petSpawnManager)
        );

        getLogger().info("StarlightPetProtection enabled!");
    }

    public PetSpawnManager getPetSpawnManager() {
        return petSpawnManager;
    }

    @Override
    public void onDisable() {
        getLogger().info("StarlightPetProtection disabled!");
    }
}
