package net.ceyzel.parkour;

import lombok.Setter;
import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.UUID;

import lombok.Getter;

@Getter
public class ParkourSession {
    private final UUID playerId;
    private final long startTime;
    private Block lastCheckpoint;
    private final String mapName;
    @Setter
    private Location lastCheckpointLocation;
    private final CeyZelParkour plugin;

    public ParkourSession(UUID playerId, String mapName, Block start, CeyZelParkour plugin) {
        this.playerId = playerId;
        this.startTime = System.currentTimeMillis();
        this.mapName = mapName;
        this.lastCheckpoint = start;
        this.lastCheckpointLocation = start.getLocation();
        this.plugin = plugin;
        plugin.getParkourTimer().startTimer(playerId);
    }

    public void setLastCheckpoint(Block lastCheckpoint) {
        this.lastCheckpoint = lastCheckpoint;
        this.lastCheckpointLocation = lastCheckpoint.getLocation();
        this.lastCheckpointLocation.setYaw(lastCheckpointLocation.getYaw());
        this.lastCheckpointLocation.setPitch(lastCheckpointLocation.getPitch());
    }


    public void endSession() {
        plugin.getParkourTimer().stopTimer(playerId);
    }
}