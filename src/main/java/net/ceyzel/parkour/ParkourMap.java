package net.ceyzel.parkour;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.block.Block;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

@Getter
public class ParkourMap {
    private final String name;
    @Setter
    private Block start;
    @Setter
    private Block finish;
    @Setter
    private Difficulty difficulty;
    private final List<Block> checkpoints;
    private Location startLocation; // Убрано final
    private List<Location> checkpointLocations = new ArrayList<>(); // Убрано final

    public ParkourMap(String name) {
        this.name = name;
        this.checkpoints = new ArrayList<>();
        this.difficulty = Difficulty.EASY;
    }

    public ParkourMap(String name, Block start, Block finish, Difficulty difficulty, List<Block> checkpoints) {
        this.name = name;
        this.start = start;
        this.finish = finish;
        this.difficulty = difficulty;
        this.checkpoints = checkpoints;
    }

    public void addCheckpoint(Block block) {
        checkpoints.add(block);
    }

    public void addCheckpointLocation(Location location) {
        checkpointLocations.add(location);
    }

    public void setStartLocation(Location location) {
        this.startLocation = location;
    }

    public Location getStartLocation() {
        return startLocation;
    }

    public List<Location> getCheckpointLocations() {
        return checkpointLocations;
    }
}