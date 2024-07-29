package com.gmail.theminiluca.offlinebody.utils;

import net.minecraft.server.v1_8_R3.PathfinderGoalSelector;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftZombie;
import org.bukkit.craftbukkit.v1_8_R3.util.UnsafeList;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;

import java.lang.reflect.Field;

public class OfflineEntity {

    private final Zombie zombie;

    public OfflineEntity(Location loc, Player player) {
        CraftPlayer craftPlayer = (CraftPlayer) player;

        CraftZombie zombie = ((CraftZombie) loc.getWorld().spawnEntity(loc, EntityType.ZOMBIE));
        zombie.setBaby(false);
        zombie.setVillager(false);
        zombie.getLocation().setPitch(player.getLocation().getPitch());
        zombie.getLocation().setYaw(player.getLocation().getYaw());
        zombie.setCustomName(player.getName());
        zombie.setMaxHealth(player.getMaxHealth());
        zombie.setHealth(player.getHealth());
        zombie.setCustomNameVisible(true);
        //
        zombie.getEquipment().setBoots(new ItemStack(Material.AIR));
        zombie.getEquipment().setLeggings(new ItemStack(Material.AIR));
        zombie.getEquipment().setChestplate(new ItemStack(Material.AIR));
        zombie.getEquipment().setHelmet(new ItemStack(Material.AIR));
        //
        zombie.getEquipment().setHelmet(getMyHead(player));
        zombie.getEquipment().setChestplate(player.getEquipment().getChestplate());
        zombie.getEquipment().setLeggings(player.getEquipment().getLeggings());
        zombie.getEquipment().setBoots(player.getEquipment().getBoots());
        zombie.getEquipment().setItemInHand(player.getEquipment().getItemInHand());
        zombie.setFallDistance(player.getFallDistance());
        zombie.setCanPickupItems(false);
        zombie.setFireTicks(player.getFireTicks());
        zombie.setNoDamageTicks(player.getNoDamageTicks());
        zombie.setMaximumNoDamageTicks(player.getMaximumNoDamageTicks());
        zombie.setRemoveWhenFarAway(false);
        zombie.setMaximumAir(player.getMaximumAir());

        for (PotionEffect potionEffect : player.getActivePotionEffects()) {
            zombie.addPotionEffect(potionEffect);
        }
        zombie.getHandle().setAbsorptionHearts(craftPlayer.getHandle().getAbsorptionHearts());
        clearAI(zombie);
        this.zombie = zombie;
    }

    public static void clearAI(Zombie zombie) {
        CraftZombie craftZombie = (CraftZombie) zombie;
        PathfinderGoalSelector targetSelector = craftZombie.getHandle().targetSelector;
        clear(targetSelector, "b");
        clear(targetSelector, "c");
        PathfinderGoalSelector goalSelector = craftZombie.getHandle().goalSelector;
        clear(goalSelector, "b");
        clear(goalSelector, "c");
    }

    public Zombie getZombie() {
        return zombie;
    }



    public static void clear(Object instance, String fieldName) {
        try {
            Field field = instance.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(instance, new UnsafeList<>());
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    private ItemStack getMyHead(Player player) {
        ItemStack is = new ItemStack(Material.SKULL_ITEM, 1, (byte) 3);
        SkullMeta im = (SkullMeta) is.getItemMeta();
        im.setOwner(player.getName());
        is.setItemMeta(im);
        return is;
    }
}
