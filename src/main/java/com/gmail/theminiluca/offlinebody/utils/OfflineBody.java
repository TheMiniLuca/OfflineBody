package com.gmail.theminiluca.offlinebody.utils;

import com.gmail.theminiluca.offlinebody.OfflineBodyMain;
import com.gmail.theminiluca.offlinebody.event.OfflinePlayerSpawnEvent;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class OfflineBody implements ConfigurationSerializable {

    private Zombie zombieInstance;
    private boolean exists;
    private final @NotNull UUID playerUUID;

    public OfflineBody(@NotNull UUID playerUUID) {
        this.exists = true;
        this.playerUUID = playerUUID;
    }

    public static boolean spawnOfflineBody(@NotNull Player player) {
        Map<UUID, OfflineBody> offlineBodies = OfflineBodyMain.getInstance().offlineBodies;
        OfflinePlayerSpawnEvent spawnEvent = new OfflinePlayerSpawnEvent(player);
        Bukkit.getServer().getPluginManager().callEvent(spawnEvent);
        if (spawnEvent.isCancelled()) return false;
        if (!offlineBodies.containsKey(player.getUniqueId())) {
            OfflineBody offlineBody = new OfflineBody(player.getUniqueId());
            offlineBodies.put(player.getUniqueId(), offlineBody);
        }
        OfflineBody offlineBody = offlineBodies.get(player.getUniqueId());
        offlineBody.createBody();
        return true;
    }


    public boolean isExists() {
        return exists;
    }

    public void setExists(boolean exists) {
        this.exists = exists;
    }

    public void createBody() {
        Player player = OfflineBodyMain.loadPlayer(Bukkit.getOfflinePlayer(playerUUID));
        Validate.notNull(player, "player cannot be null");
        OfflineEntity offlineEntity = new OfflineEntity(player.getLocation(), player);
        setZombieInstance(offlineEntity.getZombie());
        OfflineBodyMain.registerNameUUID(zombieInstance.getCustomName(), playerUUID);
    }

    public boolean hasInstance() {
        return zombieInstance != null;
    }

    public void removeBody() {
        Validate.notNull(getZombieInstance(), "zombieInstance can not be null");
        Player player = Bukkit.getPlayer(playerUUID);
        player.setMaxHealth(getZombieInstance().getMaxHealth());
        player.setHealth(getZombieInstance().getHealth());
        player.teleport(getZombieInstance().getLocation());
        player.setFallDistance(getZombieInstance().getFallDistance());
        player.setMaximumAir(getZombieInstance().getMaximumAir());
        player.getActivePotionEffects().clear();
        for (PotionEffect potionEffect : getZombieInstance().getActivePotionEffects()) {
            player.addPotionEffect(potionEffect);
        }
        ((CraftPlayer) player).getHandle().setAbsorptionHearts(((CraftPlayer) player).getHandle().getAbsorptionHearts());
        this.getZombieInstance().remove();
        this.setZombieInstance(null);
    }

    public Zombie getZombieInstance() {
        return zombieInstance;
    }

    //onEnable 메서드에서만 사용하여야합니다.
    public void setZombieInstance(@Nullable Zombie zombieInstance) {
        setExists(zombieInstance != null);
        this.zombieInstance = zombieInstance;
    }

    public @NotNull UUID getPlayerUUID() {
        return playerUUID;
    }

    @Override
    public Map<String, Object> serialize() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("playerUUID", playerUUID.toString());
        return map;
    }

    @SuppressWarnings("unchecked")
    @NotNull
    public static OfflineBody deserialize(Map<String, Object> map) {
        return new OfflineBody(UUID.fromString((String) map.get("playerUUID")));
    }
}
