package net.ceyzel.parkour;

import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.List;

public class ParkourMap {
    private final String name;
    private Block start;
    private Block finish;
    private double score;
    private List<Block> checkpoints;

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

    public List<Block> getCheckpoints() {
        return checkpoints;
    }

    public void setCheckpoints(List<Block> checkpoints) {
        this.checkpoints = checkpoints;
    }

    public void addCheckpoint(Block block) {
        checkpoints.add(block);
    }
}