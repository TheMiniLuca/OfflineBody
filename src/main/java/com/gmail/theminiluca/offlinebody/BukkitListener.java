package com.gmail.theminiluca.offlinebody;

import com.gmail.theminiluca.offlinebody.event.OfflinePlayerDeathEvent;
import com.gmail.theminiluca.offlinebody.utils.OfflineBody;
import com.gmail.theminiluca.offlinebody.utils.OfflineEntity;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BukkitListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        Map<UUID, OfflineBody> offlineBodies = OfflineBodyMain.getInstance().offlineBodies;
        if (!offlineBodies.containsKey(player.getUniqueId())) return;
        OfflineBody offlineBody = offlineBodies.get(player.getUniqueId());
        offlineBody.setExists(connectInstance(player.getLocation().getChunk()));
        if (offlineBody.hasInstance()) {
            offlineBody.removeBody();
        } else {
            if (!offlineBody.isExists()) {
                player.setHealth(0);
                offlineBodies.remove(player.getUniqueId());
            }
        }

    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        OfflineBody.spawnOfflineBody(player);
    }


    public boolean connectInstance(Chunk chunk) {
        for (Entity entity : chunk.getEntities()) {
            if (entity instanceof Zombie zombie) {
                if (zombie.getCustomName() == null) continue;
                OfflineBody offlineUser = OfflineBodyMain.getOfflineBody(OfflineBodyMain.getNameToUUID(zombie.getCustomName()));
                if (offlineUser == null) {
                    zombie.remove();
                    continue;
                }
                if (!offlineUser.isExists()) {
                    zombie.remove();
                    zombie = null;
                }
                if (zombie != null)
                    OfflineEntity.clearAI(zombie);
                offlineUser.setZombieInstance(zombie);
                return true;
            }
        }
        return false;
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        Chunk chunk = event.getChunk();
        if (connectInstance(chunk)) {

        }
//            Bukkit.getScheduler().scheduleSyncDelayedTask(OfflinePlayerMain.getInstance(), () -> {

//            });

    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        LivingEntity livingEntity = event.getEntity();
        if (!(livingEntity instanceof Zombie zombie)) return;
        if (zombie.getCustomName() == null) return;
        event.getDrops().clear();
        UUID uniqueId = OfflineBodyMain.getNameToUUID(zombie.getCustomName());
        Player offlinePlayer = OfflineBodyMain.loadPlayer(Bukkit.getOfflinePlayer(uniqueId));
        if (offlinePlayer == null) {
            return;
        }
        List<ItemStack> drops = new ArrayList<>();
        for (ItemStack is : offlinePlayer.getInventory().getArmorContents()) {
            if (is == null || is.getType().equals(Material.AIR)) continue;
            drops.add(is);
        }
        for (ItemStack is : offlinePlayer.getInventory().getContents()) {
            if (is == null || is.getType().equals(Material.AIR)) continue;
            drops.add(is);
        }
        offlinePlayer.getInventory().clear();
        offlinePlayer.getInventory().setArmorContents(new ItemStack[]{new ItemStack(Material.AIR), new ItemStack(Material.AIR),
                new ItemStack(Material.AIR), new ItemStack(Material.AIR)});
        OfflinePlayerDeathEvent playerDeathEvent = new OfflinePlayerDeathEvent(offlinePlayer, drops, ((CraftPlayer) offlinePlayer).getHandle().getExpReward());
        Bukkit.getServer().getPluginManager().callEvent(playerDeathEvent);
        event.setDroppedExp(playerDeathEvent.getDroppedExp());

        for (ItemStack is : drops) {
            zombie.getWorld().dropItemNaturally(zombie.getLocation(), is);
        }
        offlinePlayer.saveData();
    }
}
