package com.gmail.theminiluca.offlinebody;

import com.gmail.theminiluca.offlinebody.event.OfflinePlayerStartEvent;
import com.gmail.theminiluca.offlinebody.utils.OfflineBody;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.MinecraftServer;
import net.minecraft.server.v1_8_R3.PlayerInteractManager;
import net.minecraft.server.v1_8_R3.WorldServer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;
import sun.misc.Unsafe;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class OfflineBodyMain extends JavaPlugin {

    public final Map<UUID, OfflineBody> offlineBodies = new HashMap<>() {
        @Override
        public OfflineBody put(UUID key, OfflineBody value) {
            saveOfflineUser(value);
            return super.put(key, value);
        }
    };

    public static OfflineBody getOfflineBody(UUID playerUUID) {
        return getInstance().offlineBodies.get(playerUUID);
    }

    public final Map<String, UUID> nameToUUID = new HashMap<>();

    private static OfflineBodyMain instance;

    public static OfflineBodyMain getInstance() {
        return instance;
    }

    public static UUID getNameToUUID(String name) {
        return getInstance().nameToUUID.getOrDefault(name, null);
    }

    public static void registerNameUUID(String name, UUID uniqueId) {
        getInstance().nameToUUID.put(name, uniqueId);
        getInstance().saveNameToUUID(name, uniqueId);

    }

    public void saveOfflineUser(OfflineBody offlineBody) {
        configuration.set("offlineBodies." + offlineBody.getPlayerUUID(), offlineBody);
        try {
            configuration.save(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void saveNameToUUID(String name, UUID uniqueId) {
        configuration.set("maps." + name, uniqueId.toString());
        try {
            configuration.save(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public File file;
    public YamlConfiguration configuration;

    @Override
    public void onEnable() {
        ConfigurationSerialization.registerClass(OfflineBody.class);
        OfflineBodyMain.instance = this;
        this.getServer().getPluginManager().registerEvents(new BukkitListener(), this);
        if (!getDataFolder().exists()) getDataFolder().mkdir();
        this.file = new File(getDataFolder(), "offlineBodies.yml");
        this.configuration = YamlConfiguration.loadConfiguration(file);
        for (String path : configuration.getKeys(true)) {
            String[] splits = path.split("\\.");
            if (splits.length == 2 && splits[0].equals("maps")) {
                nameToUUID.put(splits[1], UUID.fromString(configuration.getString(path)));
            }
            if (splits.length == 2 && splits[0].equals("offlineBodies")) {
                offlineBodies.put(UUID.fromString(splits[1]), (OfflineBody) configuration.get(path));
            }
        }
        //인스턴스 연결
        Set<UUID> mustExists = new HashSet<>(offlineBodies.keySet());
        OfflinePlayerStartEvent offlinePlayerStartEvent = new OfflinePlayerStartEvent(mustExists);
        this.getServer().getPluginManager().callEvent(offlinePlayerStartEvent);
        if (offlinePlayerStartEvent.isCancelled()) {
            if (offlinePlayerStartEvent.getResult().equals(OfflinePlayerStartEvent.Result.CLEAR_MAP)) {
                offlineBodies.clear();
                nameToUUID.clear();
                if (file.delete()) {
                    this.getLogger().warning("file cannot delete");
                }
                configuration = new YamlConfiguration();
                try {
                    configuration.save(file);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            return;
        }
        for (World world : Bukkit.getWorlds()) {
            for (Zombie zombie : world.getEntitiesByClass(Zombie.class)) {
                if (zombie.getCustomName() == null) return;
                UUID uniqueId = OfflineBodyMain.getNameToUUID(zombie.getCustomName());
                if (offlineBodies.containsKey(uniqueId)) {
                    OfflineBody offlineBody = offlineBodies.get(uniqueId);
                    offlineBody.setZombieInstance(zombie);
                    this.getLogger().info("instance connect offlineBody! \nCustomName = " + zombie.getCustomName()
                            + "\nZombieUUID=" + zombie.getUniqueId() + "\nZombieLocation" + zombie.getLocation());
                    mustExists.remove(uniqueId);
                } else {
                    this.getLogger().warning("wrong offlineBody \nCustomName = " + zombie.getCustomName()
                            + "\nZombieUUID=" + zombie.getUniqueId() + "\nZombieLocation" + zombie.getLocation());
                    zombie.remove();
                }
            }
        }
        if (!mustExists.isEmpty()) {
            for (UUID playerUUID : mustExists) {
                @Nullable Player offlinePlayer = OfflineBodyMain.loadPlayer(Bukkit.getOfflinePlayer(playerUUID));
                if (offlinePlayer == null) {
                    this.getLogger().warning("wrong must spawn offlineBody \nplayerUUID=" + playerUUID);
                    continue;
                }
                if (OfflineBody.spawnOfflineBody(offlinePlayer)) {
                    this.getLogger().info("spawn offlineBody! \nplayerUUID=" + playerUUID);
                } else {
                    this.getLogger().warning("spawn cancelled offlineBody! \nplayerUUID=" + playerUUID);
                }
            }
        }

//        getCommand("debug").setExecutor((commandSender, _, _, args) -> {
//            commandSender.sendMessage("check");
//            commandSender.sendMessage(String.valueOf(OfflineBodyMain.getOfflineBody(OfflineBodyMain.getNameToUUID(args[0])).getZombieInstance()));
//            return false;
//        });
    }


    @Override
    public void onDisable() {

    }

    public static Player loadPlayer(final OfflinePlayer offline) {
        // Ensure player has data
        if (!offline.hasPlayedBefore()) {
            return null;
        }
        if (offline.getPlayer() != null) {
            return offline.getPlayer();
        }

        // Create a profile and entity to load the player data
        // See net.minecraft.server.PlayerList#attemptLogin
        GameProfile profile = new GameProfile(offline.getUniqueId(),
                offline.getName() != null ? offline.getName() : offline.getUniqueId().toString());
        MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
        WorldServer worldServer = server.getWorldServer(0);

        if (worldServer == null) {
            return null;
        }

        EntityPlayer entity = new EntityPlayer(server, worldServer, profile, new PlayerInteractManager(worldServer));

        // Get the bukkit entity
        Player target = entity.getBukkitEntity();
        if (target != null) {
            // Load data
            target.loadData();
        }
        // Return the entity
        return target;
    }

    private static Unsafe getUnsafe() throws Exception {
        Field f = Unsafe.class.getDeclaredField("theUnsafe");
        f.setAccessible(true);
        return (Unsafe) f.get(null);
    }

    private static long getObjectAddress(Unsafe unsafe, Object obj) {
        Object[] array = new Object[]{obj};
        long baseOffset = unsafe.arrayBaseOffset(Object[].class);
        return unsafe.getLong(array, baseOffset);
    }

    private static Object getObjectFromAddress(Unsafe unsafe, long address) {
        Object[] array = new Object[1];
        long baseOffset = unsafe.arrayBaseOffset(Object[].class);
        unsafe.putLong(array, baseOffset, address);
        return array[0];
    }
}