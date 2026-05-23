package net.altias.starlightPetProtection.data;

import org.bukkit.DyeColor;
import org.bukkit.entity.*;

public class PetData {

    public EntityType type;
    public String name;
    public long reviveAt;

    // Shared cosmetic
    public DyeColor collarColor;

    // Cat / Parrot / Wolf variants
    public Cat.Type catType;
    public Parrot.Variant parrotVariant;
    public Wolf.Variant wolfVariant;

    // Horse stats
    public Horse.Color horseColor;
    public Horse.Style horseStyle;

    public double speed;
    public double jumpStrength;
    public double maxHealth;
}
