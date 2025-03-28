package net.ceyzel.parkour;

import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

public class ParkourMap {
    private final String name;
    private Location start;
    private Location finish;
    private double score;
    private List<Location> checkpoints;

    public ParkourMap(String name) {
        this.name = name;
        this.checkpoints = new ArrayList<>();
    }

    public ParkourMap(String name, Location start, Location finish, double score, List<Location> checkpoints) {
        this.name = name;
        this.start = start;
        this.finish = finish;
        this.score = score;
        this.checkpoints = checkpoints;
    }

    public String getName() {
        return name;
    }

    public Location getStart() {
        return start;
    }

    public void setStart(Location start) {
        this.start = start;
    }

    public Location getFinish() {
        return finish;
    }

    public void setFinish(Location finish) {
        this.finish = finish;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }

    public List<Location> getCheckpoints() {
        return checkpoints;
    }

    public void setCheckpoints(List<Location> checkpoints) {
        this.checkpoints = checkpoints;
    }

    public void addCheckpoint(Location blockLocation) {
        if (checkpoints == null) {
            checkpoints = new ArrayList<>();
        }
        checkpoints.add(blockLocation);
    }
}
