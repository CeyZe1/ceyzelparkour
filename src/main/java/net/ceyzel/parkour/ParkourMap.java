package net.ceyzel.parkour;

import org.bukkit.Location;
import org.bukkit.block.Block;

import java.util.HashSet;
import java.util.Set;

public class ParkourMap {
    private final String name;
    private Block start;
    private Block finish;
    private double score;
    private Set<Block> checkpoints;

    public ParkourMap(String name) {
        this.name = name;
        this.checkpoints = new HashSet<>();
    }

    public ParkourMap(String name, Block start, Block finish, double score, Set<Block> checkpoints) {
        this.name = name;
        this.start = start;
        this.finish = finish;
        this.score = score;
        this.checkpoints = checkpoints;
    }

    public String getName() {
        return name;
    }

    public Block getStart() {
        return start;
    }

    public void setStart(Block start) {
        this.start = start;
    }

    public Block getFinish() {
        return finish;
    }

    public void setFinish(Block finish) {
        this.finish = finish;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public Set<Block> getCheckpoints() {
        return checkpoints;
    }

    public void setCheckpoints(Set<Block> checkpoints) {
        this.checkpoints = checkpoints;
    }

    public void addCheckpoint(Block block) {
        checkpoints.add(block);
    }
}