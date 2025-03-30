package net.ceyzel.parkour;

import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import java.util.UUID;

public class ParkourSession {
    private final UUID playerId;
    private final long startTime;
    private Block lastCheckpoint;
    private final String mapName;

    public ParkourSession(UUID playerId, String mapName, Block start) {
        this.playerId = playerId;
        this.startTime = System.currentTimeMillis();
        this.mapName = mapName;
        this.lastCheckpoint = start;
    }

    public @NotNull Block getLastCheckpoint() {
        return lastCheckpoint;
    }

    public @NotNull UUID getPlayerId() {
        return playerId;
    }

    public long getStartTime() {
        return startTime;
    }

    public String getMapName() {
        return mapName;
    }

    public void setLastCheckpoint(Block lastCheckpoint) {
        this.lastCheckpoint = lastCheckpoint;
    }
}