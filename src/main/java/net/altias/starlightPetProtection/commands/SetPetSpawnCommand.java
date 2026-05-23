package net.altias.starlightPetProtection.commands;

import net.altias.starlightPetProtection.managers.PetSpawnManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetPetSpawnCommand implements CommandExecutor {

    private final PetSpawnManager petSpawnManager;

    public SetPetSpawnCommand(PetSpawnManager petSpawnManager) {
        this.petSpawnManager = petSpawnManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage("Players only.");
            return true;
        }

        petSpawnManager.setSpawn(player.getUniqueId(), player.getLocation());

        player.sendMessage("§aPet spawn set to your current location.");
        return true;
    }
}
