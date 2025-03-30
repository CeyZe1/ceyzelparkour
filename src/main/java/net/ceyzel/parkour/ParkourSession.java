package net.ceyzel.parkour;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import java.util.UUID;

public class ParkourSession {
    private final UUID playerId;
    private final long startTime;
    private Block lastCheckpoint;
    private final String mapName;
    private Location lastCheckpointLocation; // Новое поле для хранения координат и угла

    public ParkourSession(UUID playerId, String mapName, Block start) {
        this.playerId = playerId;
        this.startTime = System.currentTimeMillis();
        this.mapName = mapName;
        this.lastCheckpoint = start;
        this.lastCheckpointLocation = start.getLocation(); // Инициализация координат и угла
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
        this.lastCheckpointLocation = lastCheckpoint.getLocation(); // Обновление координат и угла
    }

    public Location getLastCheckpointLocation() {
        return lastCheckpointLocation;
    }
}