package net.ceyzel.parkour;

import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import java.util.UUID;

public class ParkourSession {
    private UUID playerId;
    private long startTime;
    private Block lastCheckpoint;
    private String mapName;
    private int ticks; // Добавляем счетчик тиков

    public ParkourSession(UUID playerId, String mapName, Block start) {
        this.playerId = playerId;
        this.startTime = System.currentTimeMillis();
        this.mapName = mapName;
        this.lastCheckpoint = start;
        this.ticks = 0; // Инициализируем счетчик тиков
    }

    public @NotNull Block getLastCheckpoint() {
        return lastCheckpoint;
    }

    public @NotNull UUID getPlayerId() {
        return playerId;
    }

    public void setPlayerId(UUID playerId) {
        this.playerId = playerId;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public String getMapName() {
        return mapName;
    }

    public void setLastCheckpoint(Block lastCheckpoint) {
        this.lastCheckpoint = lastCheckpoint;
    }

    public void setMapName(String mapName) {
        this.mapName = mapName;
    }

    public int getTicks() {
        return ticks;
    }

    public void incrementTicks() {
        this.ticks++;
    }
}