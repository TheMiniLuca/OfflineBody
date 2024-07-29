package com.gmail.theminiluca.offlinebody.event;

import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class OfflinePlayerStartEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;
    private @NotNull Result result;
    private final Set<UUID> mustExists;

    public OfflinePlayerStartEvent(Set<UUID> mustExists) {
        this.mustExists = mustExists;
        this.result = Result.NO_CALCULATION;
    }

    public Set<UUID> getMustExists() {
        return mustExists;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    public @NotNull Result getResult() {
        return result;
    }

    public void setResult(@NotNull Result result) {
        this.result = result;
    }

    @Override
    public void setCancelled(boolean b) {
        cancelled = b;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public static enum Result {
        NO_CALCULATION,
        CLEAR_MAP;

        private Result() {
        }
    }
}
