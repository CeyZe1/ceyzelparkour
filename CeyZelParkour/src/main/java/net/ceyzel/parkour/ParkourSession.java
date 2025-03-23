package net.ceyzel.parkour;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import java.util.UUID;

public class ParkourSession {
    private UUID playerId;
    private long startTime;
    private Location lastCheckpoint;
    private String mapName;

    public ParkourSession(UUID playerId, String mapName, Location start) {
        this.playerId = playerId;
        this.startTime = System.currentTimeMillis();
        this.mapName = mapName;
        this.lastCheckpoint = start;
    }

    public @NotNull Location getLastCheckpoint() {
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

    public void setMapName(String mapName) {
        this.mapName = mapName;
    }
}