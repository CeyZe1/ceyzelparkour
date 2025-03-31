package net.ceyzel.parkour;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.block.Block;

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
    private double score;
    private final List<Block> checkpoints;

    public ParkourMap(String name) {
        this.name = name;
        this.checkpoints = new ArrayList<>();
    }

    public ParkourMap(String name, Block start, Block finish, double score, List<Block> checkpoints) {
        this.name = name;
        this.start = start;
        this.finish = finish;
        this.score = score;
        this.checkpoints = checkpoints;
    }

    public void addCheckpoint(Block block) {
        checkpoints.add(block);
    }
}