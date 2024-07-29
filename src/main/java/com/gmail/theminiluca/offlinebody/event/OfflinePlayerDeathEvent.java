package com.gmail.theminiluca.offlinebody.event;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class OfflinePlayerDeathEvent extends PlayerEvent {
    private static final HandlerList handlers = new HandlerList();
    private final List<ItemStack> drops;
    private int dropExp;

    public OfflinePlayerDeathEvent(@NotNull Player player, @NotNull List<ItemStack> drops, int dropExp) {
        super(player);
        this.drops = drops;
        this.dropExp = dropExp;
    }

    public @NotNull List<ItemStack> getDrops() {
        return drops;
    }

    public int getDroppedExp() {
        return dropExp;
    }

    public void setDroppedExp(int dropExp) {
        this.dropExp = dropExp;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
